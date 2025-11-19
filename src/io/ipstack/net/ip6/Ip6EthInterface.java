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

import org.zoolu.util.log.DefaultLogger;
import org.zoolu.util.log.LoggerLevel;

import io.ipstack.net.ethernet.EthAddress;
import io.ipstack.net.ethernet.EthLayer;
import io.ipstack.net.ethernet.EthMulticastAddress;
import io.ipstack.net.ethernet.EthPacket;
import io.ipstack.net.icmp6.Icmp6Message;
import io.ipstack.net.icmp6.NeighborDiscoveryClient;
import io.ipstack.net.icmp6.NeighborDiscoveryServer;
import io.ipstack.net.icmp6.SolicitedNodeMulticastAddress;
import io.ipstack.net.packet.Layer;
import io.ipstack.net.packet.LayerListener;
import io.ipstack.net.packet.NetInterface;
import io.ipstack.net.packet.NetInterfaceListener;


/** IPv6 interface for sending and receiving IPv6 packets through an Ethernet-like layer.
 * <p>
 * Layer-two address resolution is performed through the ICMPv6 Neighbor Discovery protocol.
 */
public class Ip6EthInterface extends NetInterface<Ip6Address,Ip6Packet> {
	
	/** Debug mode */
	public static boolean DEBUG=false;
	
	/** Prints a debug message. */
	private void debug(String str) {
		//DefaultLogger.log(LoggerLevel.DEBUG,getClass(),str);
		DefaultLogger.log(LoggerLevel.DEBUG,toString()+": "+str);
	}

	/** Prints a warning message. */
	private void warning(String str) {
		DefaultLogger.log(LoggerLevel.WARNING,toString()+": "+str);
	}

	
	/** ARP table timeout */
	public static long ARP_TABLE_TIMEOUT=60000;
	
	/** Ethernet layer */
	EthLayer eth_layer;

	/** Neighbor DiscoveryServer client */
	NeighborDiscoveryClient nd_client=null;

	/** Neighbor DiscoveryServer server */
	NeighborDiscoveryServer nd_server=null;

	/** This Ethernet listener */
	LayerListener<EthAddress,EthPacket> this_eth_listener;

	
	
	/** Creates a new IP interface.
	 * @param eth_layer the Ethernet layer */
	public Ip6EthInterface(EthLayer eth_layer) {
		super();
		this.eth_layer=eth_layer;
		this_eth_listener=new LayerListener<EthAddress,EthPacket>() {
			@Override
			public void onIncomingPacket(Layer<EthAddress,EthPacket> layer, EthPacket pkt) {
				processIncomingPacket(pkt);
			}
		};
		eth_layer.addListener(new Integer(EthPacket.ETH_IP6),this_eth_listener);
		// start Neighbor Discovery service
		EthAddress eth_addr=eth_layer.getAddress();
		nd_server=new NeighborDiscoveryServer(this,eth_addr);
		nd_client=new NeighborDiscoveryClient(this,eth_addr,ARP_TABLE_TIMEOUT);
	}
	
	
	/** Creates a new IP interface.
	 * @param eth_layer the Ethernet layer
	 * @param ip_addr the IP address and prefix length */
	public Ip6EthInterface(EthLayer eth_layer, Ip6AddressPrefix ip_addr) {
		this(eth_layer);
		addAddress(ip_addr);
	}
	
	
	/** Creates a new IP interface.
	 * @param eth_ni an Ethernet-like interface */
	public Ip6EthInterface(NetInterface<EthAddress,EthPacket> eth_ni) {
		this(new EthLayer(eth_ni));
	}

		
	/** Creates a new IP interface.
	 * @param eth_ni an Ethernet-like interface
	 * @param ip_addr the IP address and prefix length */
	public Ip6EthInterface(NetInterface<EthAddress,EthPacket> eth_ni, Ip6AddressPrefix ip_addr) {
		this(new EthLayer(eth_ni),ip_addr);
	}

		
	@Override
	public void addAddress(Ip6Address ip_addr) {
		super.addAddress(ip_addr);
		Ip6Address sn_m_addr=new SolicitedNodeMulticastAddress(ip_addr);
		eth_layer.getEthInterface().addAddress(new EthMulticastAddress(sn_m_addr));
		nd_client.addIpAddress(ip_addr);
		nd_server.addIpAddress(ip_addr);
	}	
			
			
	@Override
	public void removeAddress(Ip6Address ip_addr) {
		super.removeAddress(ip_addr);
		Ip6Address sn_m_addr=new SolicitedNodeMulticastAddress(ip_addr);
		eth_layer.getEthInterface().removeAddress(new EthMulticastAddress(sn_m_addr));
		nd_client.removeIpAddress(ip_addr);
		nd_server.removeIpAddress(ip_addr);
	}	
			
			
	/** Gets the Ethernet address.
	 * @return the address */
	public EthAddress getEthAddress() {
		return (EthAddress)eth_layer.getAddress();
	}

	/*/** Gets addresses of attached networks.
	 * @return the network addresses */
	/*public Ip6Prefix[] getNetAddresses() {
		return net_addresses;
	}*/

	
	@Override
	public void send(final Ip6Packet ip_pkt, final Ip6Address dest_addr) {
		(new Thread() {
			public void run() {
				if (DEBUG) debug("send(): IP packet: "+ip_pkt);
				Ip6Address dest_ip_addr=(Ip6Address)dest_addr;
				EthAddress dst_eth_addr=null;
				if (dest_ip_addr.isMulticast()) dst_eth_addr=new EthMulticastAddress(dest_ip_addr);
				else dst_eth_addr=nd_client.lookup(dest_ip_addr);
				if (dst_eth_addr!=null) {
					EthPacket eth_pkt=new EthPacket(eth_layer.getAddress(),dst_eth_addr,EthPacket.ETH_IP6,ip_pkt.getBytes());
					eth_layer.send(eth_pkt);
					if (DEBUG) debug("send(): IP packet (proto="+ip_pkt.getPayloadType()+" datalen="+ip_pkt.getPayloadLength()+") sent to "+dst_eth_addr);
					// promiscuous mode
					for (NetInterfaceListener<Ip6Address,Ip6Packet> li : promiscuous_listeners) {
						try { li.onIncomingPacket(Ip6EthInterface.this,ip_pkt); } catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
				else {
					if (DEBUG) warning("send(): address resolution of "+dest_ip_addr+" failed: packet discarded");										
				}
			}
		}).start();
	}

	
	/** Processes an incoming Ethernet packet. */
	private void processIncomingPacket(EthPacket eth_pkt) {
		if (eth_pkt.getType()!=EthPacket.ETH_IP6) {
			throw new RuntimeException("It is not an IPv6 packet (type=0x"+Integer.toHexString(eth_pkt.getType())+")");
		}
		Ip6Packet ip_pkt=Ip6Packet.parseIp6Packet(eth_pkt.getPayloadBuffer(),eth_pkt.getPayloadOffset(),eth_pkt.getPayloadLength());
		if (DEBUG) debug("processIncomingPacket(): IP packet: "+ip_pkt);
		// learn the Ethernet address of the source of ICMPv6 Neighbor Solicitation message
		if (ip_pkt.getPayloadType()==Ip6Packet.IPPROTO_ICMP6) {
			Icmp6Message icmp_msg=new Icmp6Message(ip_pkt);
			int icmp_type=icmp_msg.getType();
			if (icmp_type==Icmp6Message.TYPE_Neighbor_Solicitation) {
				EthAddress eth_addr=(EthAddress)eth_pkt.getSourceAddress();
				Ip6Address ip_addr=(Ip6Address)ip_pkt.getSourceAddress();
				nd_client.put(ip_addr,eth_addr);
			}
		}
		// promiscuous mode
		for (NetInterfaceListener<Ip6Address,Ip6Packet> li : promiscuous_listeners) {
			try { li.onIncomingPacket(this,ip_pkt); } catch (Exception e) {
				e.printStackTrace();
			}
		}
		// non-promiscuous mode
		for (NetInterfaceListener<Ip6Address,Ip6Packet> li : listeners) {
			try { li.onIncomingPacket(this,ip_pkt); } catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	
	@Override
	public void close() {
		nd_client.close();
		nd_server.close();
		eth_layer.removeListener(this_eth_listener);
		super.close();
	}

	
	@Override
	public String toString() {
		return getClass().getSimpleName()+'['+getEthAddress()+']';
	}

}
