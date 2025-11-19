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

package io.ipstack.net.arp;

import java.util.HashSet;

import org.zoolu.util.log.DefaultLogger;
import org.zoolu.util.log.LoggerLevel;

import io.ipstack.net.ethernet.EthAddress;
import io.ipstack.net.ip4.Ip4Address;
import io.ipstack.net.packet.Layer;
import io.ipstack.net.packet.LayerListener;


/** ARP server.
 * It responds to ARP requests for mapping an IPv4 address to corresponding Ethernet address.
 */
public class ArpServer {
	
	/** Debug mode */
	public static boolean DEBUG=false;
	
	/** Prints a debug message. */
	private void debug(String str) {
		DefaultLogger.log(LoggerLevel.DEBUG,getClass(),str);
	}

	
	/** ARP layer */
	ArpLayer arp_layer;

	/** Server Ethernet address */
	EthAddress eth_addr;

	/** Server IPv4 addresses */
	HashSet<Ip4Address> ip_addrs=new HashSet<>();

	
	
	/** Creates a new ARP server.
	 * @param arp_layer the ARP layer */
	public ArpServer(ArpLayer arp_layer) {
		this.arp_layer=arp_layer;
		this.eth_addr=(EthAddress)arp_layer.getAddress();
		LayerListener<EthAddress,ArpPacket> this_arp_listener=new LayerListener<EthAddress,ArpPacket>() {
			@Override
			public void onIncomingPacket(Layer<EthAddress,ArpPacket> layer, ArpPacket pkt) {
				processIncomingPacket(pkt);
			}
		};
		arp_layer.addListener(new Integer(ArpPacket.ARP_REQUEST),this_arp_listener);
		if (DEBUG) debug("ArpServer(): "+eth_addr);
	}
	
	
	/** Creates a new ARP server.
	 * @param arp_layer the ARP layer
	 * @param ip_addr the IP address */
	public ArpServer(ArpLayer arp_layer, Ip4Address ip_addr) {
		this(arp_layer);
		addIpAddress(ip_addr);
	}
	
	
	/** Adds an IP address.
	 * @param ip_addr the address */
	public void addIpAddress(Ip4Address ip_addr) {
		if (DEBUG) debug("addIpAddress(): "+ip_addr);
		synchronized (ip_addrs) {
			ip_addrs.add(ip_addr);
		}
	}

	
	/** Removes an IP address.
	 * @param ip_addr the address */
	public void removeIpAddress(Ip4Address ip_addr) {
		if (DEBUG) debug("removeIpAddress(): "+ip_addr);
		synchronized (ip_addrs) {
			ip_addrs.remove(ip_addr);
		}
	}

	
	/** Processes an incoming ARP packet. */
	protected void processIncomingPacket(ArpPacket arp_pkt) {
		if (arp_pkt.getOperation()!=ArpPacket.ARP_REQUEST) {
			throw new RuntimeException("It is not an ARP REQUREST ("+arp_pkt.getOperation()+")");			
		}
		// else
		Ip4Address ip_addr=new Ip4Address(arp_pkt.getTargetProtocolAddress());
		if (ip_addrs.contains(ip_addr)) {
			EthAddress remote_eth_addr=new EthAddress(arp_pkt.getSenderHardwareAddress());
			Ip4Address remote_ip_addr=new Ip4Address(arp_pkt.getSenderProtocolAddress());
			if (DEBUG) debug("processIncomingPacket(): ARP_REQUEST: who-has "+ip_addr+"? tell "+remote_ip_addr);
			ArpPacket arp_reply=new ArpPacket(eth_addr,remote_eth_addr,ArpPacket.ARP_REPLY,eth_addr,ip_addr,remote_eth_addr,remote_ip_addr);
			if (DEBUG) debug("processIncomingPacket(): "+ip_addr+" is-at "+eth_addr);
			arp_layer.send(arp_reply);			
		}			
	}

	
	/** Closes the ARP server. */ 
	public void close() {
		arp_layer.close();
	}	

}
