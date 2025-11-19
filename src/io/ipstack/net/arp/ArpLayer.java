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

import org.zoolu.util.log.DefaultLogger;
import org.zoolu.util.log.LoggerLevel;

import io.ipstack.net.ethernet.EthAddress;
import io.ipstack.net.ethernet.EthLayer;
import io.ipstack.net.ethernet.EthPacket;
import io.ipstack.net.packet.Layer;
import io.ipstack.net.packet.LayerListener;



/** ARP interface for sending and receiving ARP packets through an underling Ethernet interface.
 */
public class ArpLayer extends Layer<EthAddress,ArpPacket> {
	
	/** Debug mode */
	public static boolean DEBUG=false;
	
	/** Prints a debug message. */
	private void debug(String str) {
		DefaultLogger.log(LoggerLevel.DEBUG,getClass(),str);
	}

	
	/** Ethernet layer */
	EthLayer eth_layer;

	/** This Ethernet listener */
	LayerListener<EthAddress,EthPacket> this_eth_listener;

		
	
	/** Creates a new ARP interface.
	 * @param eth_layer the Ethernet interface */
	public ArpLayer(EthLayer eth_layer) {
		this.eth_layer=eth_layer;
		this_eth_listener=new LayerListener<EthAddress,EthPacket>() {
			@Override
			public void onIncomingPacket(Layer<EthAddress,EthPacket> layer, EthPacket pkt) {
				processIncomingPacket(layer,pkt);
			}
		};
		eth_layer.addListener((Object)new Integer(EthPacket.ETH_ARP),this_eth_listener);
	}

	
	@Override
	public EthAddress getAddress() {
		return eth_layer.getAddress();
	}

	@Override
	public void send(ArpPacket arp_pkt) {
		if (DEBUG) debug("send(): "+arp_pkt);
		EthAddress src_addr=arp_pkt.getSourceAddress();
		if (src_addr==null) src_addr=eth_layer.getAddress();
		EthPacket eth_pkt=new EthPacket(src_addr,arp_pkt.getDestAddress(),EthPacket.ETH_ARP,arp_pkt.getBytes());	
		eth_layer.send(eth_pkt);
	}

	
	/** Processes an incoming Ethernet packet. */
	private void processIncomingPacket(Layer<EthAddress,EthPacket> layer, EthPacket eth_pkt) {
		try {
			if (eth_pkt.getType()==EthPacket.ETH_ARP) {
				ArpPacket arp_pkt=ArpPacket.parseArpPacket(eth_pkt);
				if (DEBUG) debug("processIncomingPacket(): "+arp_pkt);
				Integer operation=arp_pkt.getOperation();
				LayerListener<EthAddress,ArpPacket> listener=listeners.get(operation);
				if (DEBUG) debug("processIncomingPacket(): operation="+operation+", listener="+listener);
				if (listener!=null) {
					try { listener.onIncomingPacket(this,arp_pkt); } catch (Exception e) {
						e.printStackTrace();
					}
				}
				else {
					if (DEBUG) debug("processIncomingPacket(): no listener found");
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	@Override
	public void close() {
		eth_layer.removeListener(this_eth_listener);
		super.close();
	}	

}
