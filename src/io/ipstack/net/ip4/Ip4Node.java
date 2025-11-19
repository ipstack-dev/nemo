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

package io.ipstack.net.ip4;

import java.util.Arrays;
import java.util.List;

import org.zoolu.util.log.DefaultLogger;
import org.zoolu.util.log.LoggerLevel;

import io.ipstack.net.icmp4.IcmpMessage;
import io.ipstack.net.icmp4.message.IcmpDestinationUnreachableMessage;
import io.ipstack.net.icmp4.message.IcmpEchoReplyMessage;
import io.ipstack.net.icmp4.message.IcmpEchoRequestMessage;
import io.ipstack.net.icmp4.message.IcmpTimeExceededMessage;
import io.ipstack.net.packet.LoopbackInterface;
import io.ipstack.net.packet.NetInterface;
import io.ipstack.net.packet.Node;
import io.ipstack.net.packet.Route;
import io.ipstack.net.packet.RoutingTable;


/** IPv4 node.
 * It includes ICMP support and IP routing function.
 * <p>
 * A routing table is automatically created based on the directly connected links and corresponding IP prefixes.
 * Use method {@link #getRoutingTable()} and method {@link io.ipstack.net.packet.RoutingTable#add(Route)} to add more routing entries.
 * <p>
 * Ip4Node can act as either a router or host, depending whether <i>IP forwarding</i> is enabled or not.
 * Use method {@link #setForwarding(boolean)} to enable IP forwarding function.
 */
public class Ip4Node extends Node<Ip4Address,Ip4Packet> {

	/** Debug mode */
	public static boolean DEBUG=false;
	
	/** Prints a debug message. */
	private void debug(String str) {
		//DefaultLogger.log(LoggerLevel.DEBUG,toString()+": "+str);
		DefaultLogger.log(LoggerLevel.DEBUG,Ip4Node.class.getSimpleName()+"["+getAddress()+"]: "+str);
	}


	/** Loopback interface */
	LoopbackInterface<Ip4Address,Ip4Packet> loopback;
	
	/** Listener for incoming packets */
	Ip4NodeListener listener;
	
	/** Whether sending ICMP Destination Unreachable messages */
	public boolean SEND_ICMP_DEST_UREACHABLE=false;

	/** Discard incoming ICMP echo requests targetted to a broadcast address */
	public boolean DISCARD_BROADCAST_ECHO_REQUESTS=false;


	/** Creates a new IP node.
	 * @param ip_interfaces list of IP network interfaces */
	public Ip4Node(List<NetInterface<Ip4Address,Ip4Packet>> ip_interfaces) {
		super(ip_interfaces,new IpRoutingTable<Ip4Address,Ip4Packet>(),false);
		loopback=new LoopbackInterface<Ip4Address,Ip4Packet>(new Ip4AddressPrefix("127.0.0.1/8"));
		loopback.setName("lo");
	}

	/** Creates a new IP node.
	 * @param ip_interfaces IP network interfaces */
	@SafeVarargs
	public Ip4Node(NetInterface<Ip4Address,Ip4Packet>... ip_interfaces) {
		this(Arrays.asList(ip_interfaces));
	}
	
	/** Sets incoming packet receiver.
	 * @param listener listener for incoming packets */
	public void setListener(Ip4NodeListener listener) {
		this.listener=listener;
	}
	
	@Override
	public void addNetInterface(NetInterface<Ip4Address,Ip4Packet> ni) {
		super.addNetInterface(ni);
		for (Ip4Address addr : ni.getAddresses()) {
			/*if (addr instanceof Ip4AddressPrefix) {
				Ip4Prefix prefix=((Ip4AddressPrefix)addr).getPrefix();
				// add directed broadcast address
				if (prefix.length()<32) ni.addAddress(prefix.getDirectedBroadcastAddress());
				// add route
				getRoutingTable().add(new Route<Ip4Address,Ip4Packet>(prefix,null,ni));
			}*/
			removeAddress(ni,addr);
			addAddress(ni,addr);
		}
		// add broadcast and all-hosts addresses
		ni.removeAddress(Ip4Address.ADDR_BROADCAST);
		ni.removeAddress(Ip4Address.ADDR_ALL_HOSTS_MULTICAST);
		ni.addAddress(Ip4Address.ADDR_BROADCAST);
		ni.addAddress(Ip4Address.ADDR_ALL_HOSTS_MULTICAST);
	}
	
	@Override
	public void removeNetInterface(NetInterface<Ip4Address,Ip4Packet> ni) {
		RoutingTable<Ip4Address,Ip4Packet> rt=getRoutingTable();
		/*for (Ip4Address addr : ni.getAddresses()) {
			if (addr instanceof Ip4AddressPrefix) rt.remove(((Ip4AddressPrefix)addr).getPrefix());
		}*/
		for (int i=rt.size()-1; i>=0; i--) {
			Route<Ip4Address,Ip4Packet> route=rt.get(i);
			if (route.getOutputInterface()==ni) rt.remove(i);
		}	
		super.removeNetInterface(ni);
	}	
	
	/** Adds a new IP address to a the network interface.
	 * @param ni the network interface
	 * @param addr the address */
	public void addAddress(NetInterface<Ip4Address,Ip4Packet> ni, Ip4Address addr) {
		// remove and re-add at the end broadcast and all-host broadcast addresses
		ni.removeAddress(Ip4Address.ADDR_BROADCAST);
		ni.removeAddress(Ip4Address.ADDR_ALL_HOSTS_MULTICAST);
		ni.addAddress(addr);
		if (addr instanceof Ip4AddressPrefix) {
			Ip4Prefix prefix=((Ip4AddressPrefix)addr).getPrefix();
			// add directed broadcast address
			if (prefix.length()<32) ni.addAddress(prefix.getDirectedBroadcastAddress());
			// add route entry
			getRoutingTable().add(new Route<Ip4Address,Ip4Packet>(prefix,null,ni));
		}
		ni.addAddress(Ip4Address.ADDR_BROADCAST);
		ni.addAddress(Ip4Address.ADDR_ALL_HOSTS_MULTICAST);
	}

	/** Removes an IP address from a the network interface.
	 * @param ni the network interface
	 * @param addr the address */
	public void removeAddress(NetInterface<Ip4Address,Ip4Packet> ni, Ip4Address addr) {
		ni.removeAddress(addr);
		if (addr instanceof Ip4AddressPrefix) {
			Ip4Prefix prefix=((Ip4AddressPrefix)addr).getPrefix();
			// remove directed broadcast address
			if (prefix.length()<32) ni.removeAddress(prefix.getDirectedBroadcastAddress());
			// remove route entry
			RoutingTable<Ip4Address,Ip4Packet> rt=getRoutingTable();
			for (int i=0; i<rt.size(); i++) {
				Route<Ip4Address,Ip4Packet> r=rt.get(i);
				if (r.getOutputInterface()==ni && r.getDestNetAddress().equals(prefix)) {
					rt.remove(i);
					break;
				}
			}
		}
	}

	/** Gets the loopback interface.
	 * @return the interface */
	public NetInterface<Ip4Address,Ip4Packet> getLoopbackInterface() {
		return loopback;
	}

	/** Gets the routing table.
	 * @return routing table */
	public RoutingTable<Ip4Address,Ip4Packet> getRoutingTable() {
		return (RoutingTable<Ip4Address,Ip4Packet>)getRoutingFunction();
	}
	
	/** Gets a local IP address for sending datagrams to a target node.
	 * @param dst_addr address of the target node
	 * @return the IP address */
	public Ip4Address getSourceAddress(Ip4Address dst_addr) {
		if (dst_addr.isLoopback()) return Ip4Address.ADDR_LOCALHOST;
		if (dst_addr.isMulticast()) return getAddress();
		Route<Ip4Address,Ip4Packet> route=getRoutingTable().getRoute(dst_addr);
		if (route!=null) {
			Ip4Address next_hop=route.getNextHop();
			if (next_hop==null) next_hop=dst_addr;
			for (Ip4Address addr : route.getOutputInterface().getAddresses()) {
				if (addr instanceof Ip4AddressPrefix && ((Ip4AddressPrefix)addr).getPrefix().contains(next_hop)) return addr;
			}
		}
		return null;
	}
	
	@Override
	public boolean hasAddress(Ip4Address addr) {
		if (addr!=null && addr.isLoopback()) return true;
		return super.hasAddress(addr);
	}
	
	@Override
	protected void processReceivedPacket(NetInterface<Ip4Address,Ip4Packet> ni, Ip4Packet ip_pkt) {
		if (DEBUG) debug("processReceivedPacket(): "+ip_pkt);
		if (hasAddress(ip_pkt.getDestAddress())) {
			Integer proto=Integer.valueOf(ip_pkt.getProto());
			// process ICMP messages
			if (proto.intValue()==Ip4Packet.IPPROTO_ICMP) {
				IcmpMessage icmp_msg=new IcmpMessage(ip_pkt.getSourceAddress(),ip_pkt.getDestAddress(),ip_pkt.getPayloadBuffer(),ip_pkt.getPayloadOffset(),ip_pkt.getPayloadLength());
				if (DEBUG) debug("processReceivedPacket(): ICMP message: "+icmp_msg);
				if (icmp_msg.getType()==IcmpMessage.TYPE_Echo_Request) {
					IcmpEchoRequestMessage icmp_echo_request=new IcmpEchoRequestMessage(icmp_msg);
					if (DEBUG) debug("processReceivedPacket(): ICMP Echo request from "+icmp_echo_request.getSourceAddress());
					Ip4Address src_addr=icmp_echo_request.getDestAddress();
					if (!hasUnicastAddress(src_addr)) {
						if (DISCARD_BROADCAST_ECHO_REQUESTS) {
							src_addr=null;
							if (DEBUG) debug("processReceivedPacket(): broadcast ICMP Echo request: silently discarded");
						}
						else {
							src_addr=getFirstUnicastAddress(ni);
							if (src_addr==null) {
								if (DEBUG) debug("processReceivedPacket(): broadcast ICMP Echo request: no unicast address for the reply: silently discarded");								
							}
						}
					}
					if (src_addr!=null) {
						IcmpEchoReplyMessage icmp_echo_reply=new IcmpEchoReplyMessage(src_addr,icmp_echo_request.getSourceAddress(),icmp_echo_request.getIdentifier(),icmp_echo_request.getSequenceNumber(),icmp_echo_request.getEchoData());
						sendPacket(icmp_echo_reply.toIp4Packet());
					}
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
					if (SEND_ICMP_DEST_UREACHABLE) sendPacket(new IcmpDestinationUnreachableMessage(ip_pkt.getDestAddress(),ip_pkt.getSourceAddress(),IcmpDestinationUnreachableMessage.CODE_protocol_unreachable,ip_pkt).toIp4Packet());
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
	protected void processForwardingPacket(Ip4Packet ip_pkt) {
		if (DEBUG) debug("processForwardingPacket(): "+ip_pkt);
		Ip4Address dest_addr=(Ip4Address)ip_pkt.getDestAddress();
		//don't forward multicast packets
		if (dest_addr.isMulticast()) {
			if (DEBUG) debug("processForwardingPacket(): multicast packets are not forwarded");
			return;			
		}
		// else
		// decrement TTL and update checksum
		int ttl=ip_pkt.getTTL();
		if (ttl<=1) {
			if (DEBUG) debug("processForwardingPacket(): TTL<1, packet discarded");
			// send ICMP Time Exceeded
			Ip4Address dst_addr=ip_pkt.getSourceAddress();
			Ip4Address src_addr=getSourceAddress(dst_addr);
			sendPacket(new IcmpTimeExceededMessage(src_addr,dst_addr,IcmpTimeExceededMessage.CODE_time_to_live_exceeded_in_transit,ip_pkt).toIp4Packet());
			return;
		}
		// else	
		ip_pkt.setTTL(ttl-1);
		sendPacket(ip_pkt);
	}
	
	@Override
	public void sendPacket(Ip4Packet ip_pkt) {
		if (DEBUG) debug("sendPacket(): "+ip_pkt);
		Ip4Address dest_addr=ip_pkt.getDestAddress();
		if (hasUnicastAddress(dest_addr)) {
			processReceivedPacket(loopback,ip_pkt);
		}
		else
		if (dest_addr.isMulticast()) {
			for (NetInterface<Ip4Address,Ip4Packet> ni: net_interfaces) {
				if (DEBUG) debug("sendPacket(): forwarding packet through interface "+ni+" to "+dest_addr);
				ni.send(ip_pkt,dest_addr);	
			}			
		}
		else super.sendPacket(ip_pkt);
	}
	
	
	/** Checks whether a given address is present and unicast.
	 * @param addr the address
	 * @return <i>true</i> in case the address is present and unicast */
	private boolean hasUnicastAddress(Ip4Address addr) {
		if (addr==null) return false;
		if (addr.isLoopback()) return true;
		for (Ip4Address a : getAddresses()) {
			if (a.equals(addr)) {
				if (a.isMulticast()) return false;
				if (a instanceof Ip4AddressPrefix && ((Ip4AddressPrefix)a).isDirectedBradcastAddress()) return false;
				return true;
			}
		}
		return false;
	}

	
	/** Gets the first unicast address of an interface.
	 * @param ni the interface
	 * @return the unicast addresss or <i>null</i> */
	private static Ip4Address getFirstUnicastAddress(NetInterface<Ip4Address,Ip4Packet> ni) {
		for (Ip4Address a : ni.getAddresses()) {
			if (a.isMulticast()) continue;
			if (a instanceof Ip4AddressPrefix && ((Ip4AddressPrefix)a).isDirectedBradcastAddress()) continue;
			return a; 
		}
		return null;
	}

}
