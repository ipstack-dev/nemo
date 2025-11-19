/*
 * Copyright 2018 NetSec Lab - University of Parma
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Author(s):
 * Luca Veltri (luca.veltri@unipr.it)
 */

package io.ipstack.net.ip6;

import java.util.Arrays;
import java.util.List;

import org.zoolu.util.log.DefaultLogger;
import org.zoolu.util.log.LoggerLevel;

import io.ipstack.net.icmp6.Icmp6Message;
import io.ipstack.net.icmp6.SolicitedNodeMulticastAddress;
import io.ipstack.net.icmp6.message.Icmp6DestinationUnreachableMessage;
import io.ipstack.net.icmp6.message.Icmp6EchoReplyMessage;
import io.ipstack.net.icmp6.message.Icmp6EchoRequestMessage;
import io.ipstack.net.icmp6.message.Icmp6TimeExceededMessage;
import io.ipstack.net.ip4.IpAddress;
import io.ipstack.net.ip4.IpRoutingTable;
import io.ipstack.net.ip6.exthdr.ExtensionHeader;
import io.ipstack.net.ip6.exthdr.RoutingHeader;
import io.ipstack.net.ip6.exthdr.SegmentRoutingHeader;
import io.ipstack.net.packet.LoopbackInterface;
import io.ipstack.net.packet.NetInterface;
import io.ipstack.net.packet.Node;
import io.ipstack.net.packet.Route;
import io.ipstack.net.packet.RoutingTable;


/** IPv6 node.
 * It includes ICMP support and IP routing function.
 * <p>
 * A routing table is automatically created based on the directly connected links and corresponding IP prefixes.
 * Use method {@link #getRoutingTable()} and method {@link io.ipstack.net.packet.RoutingTable#add(Route)} to add more routing entries.
 * <p>
 * Ip6Node can act as either a router or host, depending whether <i>IP forwarding</i> is enabled or not.
 * Use method {@link #setForwarding(boolean)} to enable IP forwarding function.
 */
public class Ip6Node extends Node<Ip6Address,Ip6Packet> {

	/** Debug mode */
	public static boolean DEBUG=false;
	
	/** Prints a debug message. */
	private void debug(String str) {
		//DefaultLogger.log(LoggerLevel.DEBUG,toString()+": "+str);
		DefaultLogger.log(LoggerLevel.DEBUG,Ip6Node.class.getSimpleName()+"["+getAddress()+"]: "+str);
	}


	/** Loopback interface */
	LoopbackInterface<Ip6Address,Ip6Packet> loopback;
	
	/** Listener for incoming packets */
	Ip6NodeListener listener;

	
	/** Creates a new IP node.
	 * @param ip_interfaces set of IP network interfaces */
	public Ip6Node(List<NetInterface<Ip6Address,Ip6Packet>> ip_interfaces) {
		super(ip_interfaces,new IpRoutingTable<Ip6Address,Ip6Packet>(),false);
		loopback=new LoopbackInterface<Ip6Address,Ip6Packet>(Ip6Address.ADDR_LOCALHOST);
		loopback.setName("lo");
	}
	
	/** Creates a new IP node.
	 * @param ip_interfaces IP network interfaces */
	@SafeVarargs
	public Ip6Node(NetInterface<Ip6Address,Ip6Packet>... ip_interfaces) {
		this(Arrays.asList(ip_interfaces));
	}

	/** Sets incoming packet receiver.
	 * @param listener listener for incoming packets */
	public void setListener(Ip6NodeListener listener) {
		this.listener=listener;
	}

	@Override
	public void addNetInterface(NetInterface<Ip6Address,Ip6Packet> ni) {
		super.addNetInterface(ni);
		for (Ip6Address addr : ni.getAddresses()) {
			/*if (addr instanceof Ip6AddressPrefix) {
				Ip6AddressPrefix ip_addr=(Ip6AddressPrefix)addr;
				Ip6Address sn_m_addr=new SolicitedNodeMulticastAddress(ip_addr);
				ni.addAddress(sn_m_addr);
				Ip6Prefix prefix=ip_addr.getPrefix();
				getRoutingTable().add(new Route<Ip6Address,Ip6Packet>(prefix,null,ni));					
			}*/
			removeAddress(ni,addr);
			addAddress(ni,addr);
		}
		ni.removeAddress(Ip6Address.ADDR_ALL_HOSTS_INTERFACE_MULTICAST);
		ni.removeAddress(Ip6Address.ADDR_ALL_HOSTS_LINK_MULTICAST);		
		ni.addAddress(Ip6Address.ADDR_ALL_HOSTS_INTERFACE_MULTICAST);
		ni.addAddress(Ip6Address.ADDR_ALL_HOSTS_LINK_MULTICAST);		
	}
	
	@Override
	public void removeNetInterface(NetInterface<Ip6Address,Ip6Packet> ni) {
		RoutingTable<Ip6Address,Ip6Packet> rt=getRoutingTable();
		/*for (Ip6Address addr : ni.getAddresses()) {
			if (addr instanceof Ip6AddressPrefix) rt.remove(((Ip6AddressPrefix)addr).getPrefix());					
		}*/
		for (int i=rt.size()-1; i>=0; i--) {
			Route<Ip6Address,Ip6Packet> route=rt.get(i);
			if (route.getOutputInterface()==ni) rt.remove(i);
		}
		super.removeNetInterface(ni);
	}
	
	/** Adds a new IP address to a the network interface.
	 * @param ni the network interface
	 * @param addr the address */
	public void addAddress(NetInterface<Ip6Address,Ip6Packet> ni, Ip6Address addr) {
		// remove and re-add at the end all-host multicast addresses
		ni.removeAddress(Ip6Address.ADDR_ALL_HOSTS_INTERFACE_MULTICAST);
		ni.removeAddress(Ip6Address.ADDR_ALL_HOSTS_LINK_MULTICAST);
		ni.addAddress(addr);
		if (addr instanceof Ip6AddressPrefix) {
			Ip6AddressPrefix ip_addr=(Ip6AddressPrefix)addr;
			Ip6Address sn_m_addr=new SolicitedNodeMulticastAddress(ip_addr);
			ni.addAddress(sn_m_addr);
			Ip6Prefix prefix=ip_addr.getPrefix();
			getRoutingTable().add(new Route<Ip6Address,Ip6Packet>(prefix,null,ni));
		}
		ni.addAddress(Ip6Address.ADDR_ALL_HOSTS_INTERFACE_MULTICAST);
		ni.addAddress(Ip6Address.ADDR_ALL_HOSTS_LINK_MULTICAST);
	}

	/** Removes an IP address from a the network interface.
	 * @param ni the network interface
	 * @param addr the address */
	public void removeAddress(NetInterface<Ip6Address,Ip6Packet> ni, Ip6Address addr) {
		ni.removeAddress(addr);
		if (addr instanceof Ip6AddressPrefix) {
			Ip6AddressPrefix ip_addr=(Ip6AddressPrefix)addr;
			Ip6Address sn_m_addr=new SolicitedNodeMulticastAddress(ip_addr);
			ni.removeAddress(sn_m_addr);
			Ip6Prefix prefix=ip_addr.getPrefix();
			RoutingTable<Ip6Address,Ip6Packet> rt=getRoutingTable();
			for (int i=0; i<rt.size(); i++) {
				Route<Ip6Address,Ip6Packet> r=rt.get(i);
				if (r.getOutputInterface()==ni && r.getDestNetAddress().equals(prefix)) {
					rt.remove(i);
					break;
				}
			}
		}
	}
	
	/** Gets the loopback interface.
	 * @return the interface */
	public NetInterface<Ip6Address,Ip6Packet> getLoopbackInterface() {
		return loopback;
	}
	/** Gets the routing table.
	 * @return routing table */
	public RoutingTable<Ip6Address,Ip6Packet> getRoutingTable() {
		return (RoutingTable<Ip6Address,Ip6Packet>)getRoutingFunction();
	}
	
	/** Gets a local IP address for sending datagrams to a target node.
	 * @param dst_addr address of the target node
	 * @return the IP address */
	public Ip6Address getSourceAddress(Ip6Address dst_addr) {
		if (dst_addr.isLoopback()) return Ip6Address.ADDR_LOCALHOST;
		if (dst_addr.isMulticast()) return getAddress();
		Route<Ip6Address,Ip6Packet> route=getRoutingTable().getRoute(dst_addr);
		if (route!=null) {
			Ip6Address next_hop=route.getNextHop();
			if (next_hop==null) next_hop=dst_addr;
			for (Ip6Address addr : route.getOutputInterface().getAddresses()) {
				if (addr instanceof Ip6AddressPrefix && ((Ip6AddressPrefix)addr).getPrefix().contains(next_hop)) return addr;
			}
		}
		return null;
	}
	
	@Override
	public boolean hasAddress(Ip6Address addr) {
		if (addr!=null && addr.isLoopback()) return true;
		return super.hasAddress(addr);
	}
	
	@Override
	protected void processReceivedPacket(NetInterface<Ip6Address,Ip6Packet> ni, Ip6Packet ip_pkt) {
		if (DEBUG) debug("processReceivedPacket(): "+ip_pkt);
		// process IPv6 extension headers
		// TODO
		Ip6Address dest_addr=ip_pkt.getDestAddress();
		if (hasAddress(dest_addr)) {
			// process routing header
			if (forwarding && ip_pkt.hasExtHdr(ExtensionHeader.ROUTING_HDR)) {
				//debug(local_addrs[0]+": packet has RH");
				RoutingHeader rh=new RoutingHeader(ip_pkt.getExtHdr(ExtensionHeader.ROUTING_HDR));
				if (rh.getRoutingType()==RoutingHeader.TYPE_SRH) {
					debug("packet has SRH");
					SegmentRoutingHeader srh=new SegmentRoutingHeader(rh);
					int segment_left=srh.getSegmentLeft();
					if (segment_left>0) {
						debug("there are more segments");
						srh.setSegmentLeft(--segment_left);
						dest_addr=srh.getSegmentAt(segment_left);
						ip_pkt.setDestAddress(dest_addr);
						if (segment_left==0) {
							// IF Clean-up bit is set THEN remove the SRH
							debug("last segment");
							if (srh.getCleanupFlag()) {
								debug("clean-up");
								ip_pkt.removeExtHdr(ExtensionHeader.ROUTING_HDR);
							}
						}
						// forward the packet
						processForwardingPacket(ip_pkt);
					}
					else {
						// give the packet to the next PID (application)
						debug("end of segments");
					}
				}
				return;
			}
			
			// process other extension headers
			// TODO
			
			// process payload
			Integer proto=Integer.valueOf(ip_pkt.getPayloadType());
			if (proto.intValue()==Ip6Packet.IPPROTO_ICMP6) {
				Icmp6Message icmp_msg=new Icmp6Message(ip_pkt);
				if (DEBUG) debug("processReceivedPacket(): ICMP message: "+icmp_msg);
				if (icmp_msg.getType()==Icmp6Message.TYPE_Echo_Request) {
					Icmp6EchoRequestMessage icmp_echo_request=new Icmp6EchoRequestMessage(icmp_msg);
					if (DEBUG) debug("processReceivedPacket(): ICMPv6 Echo request from "+icmp_echo_request.getSourceAddress());
					Icmp6EchoReplyMessage icmp_echo_reply=new Icmp6EchoReplyMessage((Ip6Address)icmp_echo_request.getDestAddress(),(Ip6Address)icmp_echo_request.getSourceAddress(),icmp_echo_request.getIdentifier(),icmp_echo_request.getSequenceNumber(),icmp_echo_request.getEchoData());
					sendPacket(icmp_echo_reply.toIp6Packet());
				}
				else {
					// process other ICMP messages
					if (listener!=null) listener.onIncomingPacket(this,ip_pkt);
				}
			}
			else {
				// process non-ICMP packets
				if (listener!=null) listener.onIncomingPacket(this,ip_pkt);
				else {
					// packet discarded
					// sends Destination (protocol) Unreachable ICMP message
					sendPacket(new Icmp6DestinationUnreachableMessage((Ip6Address)ip_pkt.getDestAddress(),(Ip6Address)ip_pkt.getSourceAddress(),Icmp6DestinationUnreachableMessage.CODE_Address_unreachable,ip_pkt).toIp6Packet());
				}
			}
		}
		else {
			// packet forwarding
			if (forwarding) {
				processForwardingPacket(ip_pkt);
			}
		}
	}
	
	@Override
	protected void processForwardingPacket(Ip6Packet ip_pkt) {
		if (DEBUG) debug("processForwardingPacket(): "+ip_pkt);
		//don't forward multicast packets
		if (((IpAddress)ip_pkt.getDestAddress()).isMulticast()) {
			if (DEBUG) debug("processForwardingPacket(): multicast packets are not forwarded");
			return;			
		}
		// else
		// decrement hop_limit
		int hop_limit=ip_pkt.getHopLimit();
		if (hop_limit<=1) {
			if (DEBUG) debug("processForwardingPacket(): hop_limit<1, packet discarded");
			// send ICMP Time Exceeded
			Ip6Address dst_addr=(Ip6Address)ip_pkt.getSourceAddress();
			Ip6Address src_addr=getSourceAddress(dst_addr);
			sendPacket(new Icmp6TimeExceededMessage(src_addr,dst_addr,Icmp6TimeExceededMessage.CODE_time_to_live_exceeded_in_transit,ip_pkt).toIp6Packet());
			return;						
		}
		// else
		ip_pkt.setHopLimit(hop_limit-1);
		// process IPv6 Hop-By-Hop Options header
		// TODO
		sendPacket(ip_pkt);
	}

	@Override
	public void sendPacket(Ip6Packet ip_pkt) {
		if (DEBUG) debug("sendPacket(): "+ip_pkt);
		Ip6Address dest_addr=ip_pkt.getDestAddress();
		if (hasAddress(dest_addr)) {
			processReceivedPacket(loopback,ip_pkt);
		}
		else
		if (dest_addr.isMulticast()) {
			for (NetInterface<Ip6Address,Ip6Packet> ni: net_interfaces) {
				if (DEBUG) debug("sendPacket(): forwarding packet through interface "+ni+" to "+dest_addr);
				ni.send(ip_pkt,dest_addr);	
			}			
		}
		else super.sendPacket(ip_pkt);
	}

}
