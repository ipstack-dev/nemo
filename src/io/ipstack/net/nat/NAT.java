package io.ipstack.net.nat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.zoolu.util.log.DefaultLogger;
import org.zoolu.util.log.LoggerLevel;

import io.ipstack.net.icmp4.IcmpMessage;
import io.ipstack.net.icmp4.message.IcmpEchoReplyMessage;
import io.ipstack.net.icmp4.message.IcmpEchoRequestMessage;
import io.ipstack.net.ip4.Ip4Address;
import io.ipstack.net.ip4.Ip4Node;
import io.ipstack.net.ip4.Ip4Packet;
import io.ipstack.net.ip4.Ip4Prefix;
import io.ipstack.net.ip4.SocketAddress;
import io.ipstack.net.ip4.TransportPacket;
import io.ipstack.net.packet.NetInterface;
import io.ipstack.net.tcp.TcpPacket;
import io.ipstack.net.udp.UdpPacket;
import io.ipstack.net.util.PacketUtils;


/** NAT router.
 * It implements both dynamic NATting (S-NAT) and static NATting (D-NAT).
 */
public class NAT extends Ip4Node {

	/** Debug mode */
	public static boolean DEBUG=false;
	
	/** Prints a debug message. */
	private void debug(String str) {
		DefaultLogger.log(LoggerLevel.DEBUG,this.getClass().getSimpleName()+"["+getAddress()+"]: "+str);
	}

	
	public static int DEFAULT_FIRST_PORT=6200;
	public static int DEFAULT_LAST_PORT=6299;
	
	private NetInterface<Ip4Address,Ip4Packet> int_ni, ext_ni;
	private Ip4Address ext_addr;
	private ArrayList<Integer> free_udp_ports=new ArrayList<>();
	private ArrayList<Integer> free_tcp_ports=new ArrayList<>();
	private int icmp_id=0;

	private HashSet<Ip4Prefix> int_prefixes=new HashSet<>();

	private NatTable udp_table=new NatTable();
	private NatTable tcp_table=new NatTable();
	private NatTable icmp_table=new NatTable();

	private HashMap<SocketAddress,SocketAddress> udp_static_table=new HashMap<>();
	private HashMap<SocketAddress,SocketAddress> tcp_static_table=new HashMap<>();
	
	
	/** Creates a new node.
	 * @param int_ni internal network interface
	 * @param ext_ni external network interface */
	public NAT(NetInterface<Ip4Address,Ip4Packet> int_ni, NetInterface<Ip4Address,Ip4Packet> ext_ni) {
		this(int_ni,ext_ni,ext_ni.getAddress(),DEFAULT_FIRST_PORT,DEFAULT_LAST_PORT);
	}

	
	/** Creates a new node.
	 * @param int_ni internal network interface
	 * @param ext_ni external network interface
	 * @param ext_addr external NAT IP address
	 * @param ext_first_port external first NAT port
	 * @param ext_last_port  external last NAT port */
	public NAT(NetInterface<Ip4Address,Ip4Packet> int_ni, NetInterface<Ip4Address,Ip4Packet> ext_ni, Ip4Address ext_addr, int ext_first_port, int ext_last_port) {
		super(int_ni,ext_ni);
		this.int_ni=int_ni;
		this.ext_ni=ext_ni;
		this.ext_addr=ext_addr;
		for (int i=ext_first_port; i<=ext_last_port; i++) {
			free_udp_ports.add(i);
			free_tcp_ports.add(i);
		}
		ArrayList<Ip4Address> int_addrs=int_ni.getAddresses();
		for (Ip4Address addr : int_addrs) if (addr instanceof Ip4Prefix) int_prefixes.add((Ip4Prefix)addr);
		setForwarding(true);
	}

	
	/** Adds a static mapping.
	 * @param proto transport protocol (e.g. UDP, TCP)
	 * @param ext_port local transport port
	 * @param int_soaddr internal socket address */
	public synchronized void addStatic(int proto, int ext_port, SocketAddress int_soaddr) {
		addStatic(proto,new SocketAddress(ext_addr,ext_port),int_soaddr);
	}

	
	/** Adds a static mapping.
	 * @param proto transport protocol (e.g. UDP, TCP)
	 * @param ext_soaddr local socket address
	 * @param int_soaddr internal socket address */
	public synchronized void addStatic(int proto, SocketAddress ext_soaddr, SocketAddress int_soaddr) {
		if (proto==Ip4Packet.IPPROTO_UDP) {
			udp_static_table.put(ext_soaddr,int_soaddr);
		}
		else
		if (proto==Ip4Packet.IPPROTO_TCP) {
			tcp_static_table.put(ext_soaddr,int_soaddr);
		}
		else {
			if (DEBUG) debug("addStatic(): unsupported protocol ("+proto+")");
		}
	}

	
	/** Removes a static mapping).
	 * @param proto transport protocol (e.g. UDP, TCP)
	 * @param ext_soaddr local socket address */
	public synchronized void removeStatic(int proto, SocketAddress ext_soaddr) {
		if (proto==Ip4Packet.IPPROTO_UDP) {
			udp_static_table.remove(ext_soaddr);
		}
		else
		if (proto==Ip4Packet.IPPROTO_TCP) {
			tcp_static_table.remove(ext_soaddr);
		}
	}

	
	@Override
	protected void processReceivedPacket(NetInterface<Ip4Address,Ip4Packet> ni, Ip4Packet ip_pkt) {
		//if (DEBUG) debug("processReceivedPacket(): "+ProtocolAnalyzer.exploreInner(ip_pkt));
		if (ni==int_ni) {
			if (DEBUG) debug("processReceivedPacket(): internal: received packet: "+PacketUtils.toString(ip_pkt));
			Ip4Address dst_addr=ip_pkt.getDestAddress();
			if (isInternal(dst_addr)) {
				super.processReceivedPacket(ni,ip_pkt);
			}
			else {
				switch (ip_pkt.getProto()) {
				case Ip4Packet.IPPROTO_UDP :
					UdpPacket udp_pkt=UdpPacket.parseUdpPacket(ip_pkt);
					processReceivedTransportPacket(ni,udp_pkt,udp_table,udp_static_table,free_udp_ports);
					break;
				case Ip4Packet.IPPROTO_TCP :
					TcpPacket tcp_pkt=TcpPacket.parseTcpPacket(ip_pkt);
					processReceivedTransportPacket(ni,tcp_pkt,tcp_table,tcp_static_table,free_tcp_ports);
					break;
				case Ip4Packet.IPPROTO_ICMP :
					IcmpMessage icmp_pkt=new IcmpMessage(ip_pkt);
					processReceivedIcmpPacket(ni,icmp_pkt);
					break;
				default :
					if (DEBUG) debug("processReceivedPacket(): unsupported protocol ("+ip_pkt.getProto()+"): packet discarded");
				}			
			}
		}
		else if (ni==ext_ni) {
			Ip4Address dst_addr=ip_pkt.getDestAddress();
			if (!isInternal(dst_addr)) {
				super.processReceivedPacket(ni,ip_pkt);
			}
			else {
				if (DEBUG) debug("processReceivedPacket(): external: received packet: "+PacketUtils.toString(ip_pkt));
				switch (ip_pkt.getProto()) {
				case Ip4Packet.IPPROTO_UDP :
					UdpPacket udp_pkt=UdpPacket.parseUdpPacket(ip_pkt);
					processReceivedTransportPacket(ni,udp_pkt,udp_table,udp_static_table,free_udp_ports);
					break;
				case Ip4Packet.IPPROTO_TCP :
					TcpPacket tcp_pkt=TcpPacket.parseTcpPacket(ip_pkt);
					processReceivedTransportPacket(ni,tcp_pkt,tcp_table,tcp_static_table,free_tcp_ports);
					break;
				case Ip4Packet.IPPROTO_ICMP :
					IcmpMessage icmp_pkt=new IcmpMessage(ip_pkt);
					processReceivedIcmpPacket(ni,icmp_pkt);
					break;
				default:
					if (DEBUG) debug("processReceivedPacket(): unsupported protocol ("+ip_pkt.getProto()+"): packet discarded");
				}
			}
		}
		else {
			throw new RuntimeException("Unknown input interface: "+ni);
		}
	}

	
	/** Processes a transport packet like UDP and TCP. */
	private void processReceivedTransportPacket(NetInterface<Ip4Address,Ip4Packet> ni, TransportPacket tpkt, NatTable table, HashMap<SocketAddress,SocketAddress> static_table, ArrayList<Integer> free_ports) {
		if (DEBUG) debug("processReceivedTransportPacket(): received: "+tpkt);
		if (ni==int_ni) {
			if (DEBUG) debug("processReceivedTransportPacket(): received: "+tpkt);
			SocketAddress src_soaddr=tpkt.getSourceSocketAddress();
			SocketAddress dst_soaddr=tpkt.getDestSocketAddress();
			NatTableEntry entry=table.getFromInternalAddresses(src_soaddr,dst_soaddr);
			if (entry==null) {
				if (free_ports.size()==0) {
					if (DEBUG) debug("processReceivedTransportPacket(): no free port: packet discarded");
					return;
				}
				// else
				int ext_port=free_ports.remove(0);
				entry=new NatTableEntry(src_soaddr,new SocketAddress(ext_addr,ext_port),dst_soaddr);
				table.add(entry);
			}
			tpkt.setSourceSocketAddress(entry.getExtSocketAddress());
			tpkt.setDestSocketAddress(entry.getExtRemoteSocketAddress());
			if (DEBUG) debug("processReceivedTransportPacket(): sending: "+tpkt);
			super.processReceivedPacket(ni,tpkt.toIp4Packet());
		}
		else
		if (ni==ext_ni) {
			SocketAddress src_soaddr=tpkt.getSourceSocketAddress();
			SocketAddress dst_soaddr=tpkt.getDestSocketAddress();
			NatTableEntry entry=table.getFromExternalAddresses(src_soaddr,dst_soaddr);
			if (entry==null) {
				SocketAddress int_soaddr=static_table.get(dst_soaddr);
				if (int_soaddr==null) {
					if (DEBUG) debug("processReceivedTransportPacket(): no NAT entry found: packet discarded");
					return;						
				}					
				// else
				entry=new NatTableEntry(int_soaddr,dst_soaddr,src_soaddr);
				table.add(entry);
			}
			tpkt.setDestSocketAddress(entry.getIntSocketAddress());
			if (DEBUG) debug("processReceivedTransportPacket(): sending: "+tpkt);
			super.processReceivedPacket(ni,tpkt.toIp4Packet());
		}
	}

	
	/** Processes a ICMP packet. */
	private void processReceivedIcmpPacket(NetInterface<Ip4Address,Ip4Packet> ni, IcmpMessage icmp_pkt) {		
		if (DEBUG) debug("processReceivedIcmpPacket(): received: "+icmp_pkt);
		if (ni==int_ni) {
			if (icmp_pkt.getType()==IcmpMessage.TYPE_Echo_Request) {
				IcmpEchoRequestMessage echo_req=new IcmpEchoRequestMessage(icmp_pkt);
				Ip4Address src_addr=icmp_pkt.getSourceAddress();
				Ip4Address dst_addr=icmp_pkt.getDestAddress();
				int id=echo_req.getIdentifier();
				SocketAddress src_soaddr=new SocketAddress(src_addr,id);
				SocketAddress dst_soaddr=new SocketAddress(dst_addr,0);
				NatTableEntry entry=icmp_table.getFromInternalAddresses(src_soaddr,dst_soaddr);
				if (entry==null) {
					int ext_id=(++icmp_id)%0x7fffffff;
					entry=new NatTableEntry(src_soaddr,new SocketAddress(ext_addr,ext_id),dst_soaddr);
					icmp_table.add(entry);
				}
				echo_req=new IcmpEchoRequestMessage((Ip4Address)entry.getExtSocketAddress().getIpAddress(),(Ip4Address)entry.getExtRemoteSocketAddress().getIpAddress(),entry.getExtSocketAddress().getPort(),echo_req.getSequenceNumber(),echo_req.getEchoData());
				if (DEBUG) debug("processReceivedIcmpPacket(): sending: "+echo_req);
				super.processReceivedPacket(ni,echo_req.toIp4Packet());
			}
			else {
				if (DEBUG) debug("processReceivedIcmpPacket(): ICMP type not supported by this NAT: "+icmp_pkt.getType());					
			}
		}
		else if (ni==ext_ni) {
			if (icmp_pkt.getType()==IcmpMessage.TYPE_Echo_Reply) {
				IcmpEchoReplyMessage echo_resp=new IcmpEchoReplyMessage(icmp_pkt);
				Ip4Address src_addr=icmp_pkt.getSourceAddress();
				Ip4Address dst_addr=icmp_pkt.getDestAddress();
				int id=echo_resp.getIdentifier();
				SocketAddress src_soaddr=new SocketAddress(src_addr,0);
				SocketAddress dst_soaddr=new SocketAddress(dst_addr,id);
				NatTableEntry entry=icmp_table.getFromExternalAddresses(src_soaddr,dst_soaddr);
				if (entry==null) {
					if (DEBUG) debug("processReceivedIcmpPacket(): no NAT entry found: packet discarded");
					return;
				}
				// else
				echo_resp=new IcmpEchoReplyMessage((Ip4Address)entry.getExtRemoteSocketAddress().getIpAddress(),(Ip4Address)entry.getIntSocketAddress().getIpAddress(),entry.getIntSocketAddress().getPort(),echo_resp.getSequenceNumber(),echo_resp.getEchoData());
				if (DEBUG) debug("processReceivedIcmpPacket(): sending: "+echo_resp);
				super.processReceivedPacket(ni,echo_resp.toIp4Packet());
			}
			else {
				if (DEBUG) debug("processReceivedIcmpPacket(): ICMP type not supported by this NAT: "+icmp_pkt.getType());					
			}
		}
	}

	
	private boolean isInternal(Ip4Address addr) {
		if (hasAddress(addr)) return true;
		for (Ip4Prefix prefix : int_prefixes) {
			if (prefix.contains(addr)) return true;
		}
		return false;
	}

}
