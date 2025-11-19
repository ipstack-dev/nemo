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

package io.ipstack.net.icmp6;

import java.util.HashSet;

import org.zoolu.util.log.DefaultLogger;
import org.zoolu.util.log.LoggerLevel;

import io.ipstack.net.ethernet.EthAddress;
import io.ipstack.net.icmp6.message.Icmp6NeighborAdvertisementMessage;
import io.ipstack.net.icmp6.message.Icmp6NeighborSolicitationMessage;
import io.ipstack.net.icmp6.message.option.Icmp6Option;
import io.ipstack.net.icmp6.message.option.TargetLinkLayerAddressOption;
import io.ipstack.net.ip6.Ip6Address;
import io.ipstack.net.ip6.Ip6EthInterface;
import io.ipstack.net.ip6.Ip6Packet;
import io.ipstack.net.packet.NetInterface;
import io.ipstack.net.packet.NetInterfaceListener;


/**  Neighbor Discovery server.
 * It responds to Neighbor Discovery requests.
 */
public class NeighborDiscoveryServer {
	
	/** Debug mode */
	public static boolean DEBUG=false;
	
	/** Prints a debug message. */
	private void debug(String str) {
		//DefaultLogger.log(LoggerLevel.DEBUG,getClass(),str);
		DefaultLogger.log(LoggerLevel.DEBUG,getClass().getSimpleName()+"["+eth_addr+"]: "+str);
	}

	
	/** IP interface */
	Ip6EthInterface ip_interface;

	/** Server IPv6 address */
	HashSet<Ip6Address> ip_addrs=new HashSet<>();

	/** Server Ethernet address */
	EthAddress eth_addr;

	/** Listener for incoming IP packets */ 
	NetInterfaceListener<Ip6Address,Ip6Packet> this_ip_listener;
	
	

	/** Creates a new Neighbor Discovery server.
	 * @param ip_interface the IP interface
	 * @param eth_addr the Ethernet address */
	public NeighborDiscoveryServer(Ip6EthInterface ip_interface, EthAddress eth_addr) {
		this.ip_interface=ip_interface;
		this.eth_addr=eth_addr;
		this_ip_listener=new NetInterfaceListener<Ip6Address,Ip6Packet>() {
			@Override
			public void onIncomingPacket(NetInterface<Ip6Address,Ip6Packet> ni, Ip6Packet pkt) {
				processIncomingPacket(ni,pkt);
			}
		};
		ip_interface.addListener(this_ip_listener);
	}
	
	
	/** Creates a new Neighbor Discovery server.
	 * @param ip_interface the IP interface
	 * @param eth_addr the Ethernet address
	 * @param ip_addr the IP address */
	public NeighborDiscoveryServer(Ip6EthInterface ip_interface, EthAddress eth_addr, Ip6Address ip_addr) {
		this(ip_interface,eth_addr);
		addIpAddress(ip_addr);
	}
	
	
	/** Adds a new IP address of the server.
	 * @param ip_addr the address */
	public void addIpAddress(Ip6Address ip_addr) {
		ip_addrs.add(ip_addr);
	}	

	
	/** Removes an IP address of the server.
	 * @param ip_addr the address */
	public void removeIpAddress(Ip6Address ip_addr) {
		ip_addrs.remove(ip_addr);
	}	

			
	/** Processes an incoming IP packet. */
	protected void processIncomingPacket(NetInterface<Ip6Address,Ip6Packet> ni, Ip6Packet ip_pkt) {
		if (ip_pkt.getPayloadType()==Ip6Packet.IPPROTO_ICMP6) {
			//if (DEBUG) debug("processIncomingPacket(): received ICMPv6 packet");
			Icmp6Message icmp_msg=new Icmp6Message(ip_pkt);
			int icmp_type=icmp_msg.getType();
			if (icmp_type==Icmp6Message.TYPE_Neighbor_Solicitation) {
				Icmp6NeighborSolicitationMessage neighbor_solicitation=new Icmp6NeighborSolicitationMessage(icmp_msg);
				Ip6Address ip_addr=neighbor_solicitation.getTargetAddress();
				if (ip_addrs.contains(ip_addr)) {
					Ip6Address remote_ip_addr=(Ip6Address)icmp_msg.getSourceAddress();
					if (DEBUG) debug("processIncomingPacket(): received ICMP6 Neighbor Solicitation: who-has "+ip_addr+"? tell "+remote_ip_addr);
					Icmp6Option[] options=new Icmp6Option[]{new TargetLinkLayerAddressOption(eth_addr)};
				    Icmp6NeighborAdvertisementMessage neighbor_advertisement=new Icmp6NeighborAdvertisementMessage(ip_addr,(Ip6Address)neighbor_solicitation.getSourceAddress(),false,true,true,ip_addr,options);
					if (DEBUG) debug("processIncomingPacket(): sending ICMP6 Neighbor Advertisement: "+ip_addr+" is-at "+eth_addr);
					ip_interface.send(neighbor_advertisement.toIp6Packet(),remote_ip_addr);
				}
			}
		}
	}

	
	/** Closes the server. */ 
	public void close() {
		ip_interface.removeListener(this_ip_listener);
	}	

}
