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

package io.ipstack.net.ethernet;

import org.zoolu.util.log.DefaultLogger;
import org.zoolu.util.log.LoggerLevel;

import io.ipstack.net.packet.Layer;
import io.ipstack.net.packet.LayerListener;
import io.ipstack.net.packet.NetInterface;
import io.ipstack.net.packet.NetInterfaceListener;


/** Ethernet layer for sending or receiving Ethernet packets through an Ethernet interface.
 */
public class EthLayer extends Layer<EthAddress,EthPacket> {

	/** Debug mode */
	public static boolean DEBUG=false;
	
	/** Prints a debug message. */
	private void debug(String str) {
		//DefaultLogger.log(LoggerLevel.DEBUG,getClass(),str);
		DefaultLogger.log(LoggerLevel.DEBUG,getClass().getSimpleName()+"["+getAddress()+"]: "+str);
	}
	
	/** Ethernet address */
	//EthAddress eth_addr;

	/** Ethernet interface */
	NetInterface<EthAddress,EthPacket> eth_ni;

	/** This physical interface listener */
	NetInterfaceListener<EthAddress,EthPacket> this_eth_listener;

	
	
	/** Creates a new Ethernet interface.
	 * @param eth_ni Ethernet interface */
	public EthLayer(NetInterface<EthAddress,EthPacket> eth_ni) {
		this.eth_ni=eth_ni;
		if (eth_ni.getAddress()==null) eth_ni.addAddress(EthAddress.generateAddress());

		eth_ni.addAddress(EthAddress.BROADCAST_ADDRESS);
		this_eth_listener=new NetInterfaceListener<EthAddress,EthPacket>() {
			@Override
			public void onIncomingPacket(NetInterface<EthAddress,EthPacket> ni, EthPacket pkt) {
				processIncomingPacket(pkt);
			}
		};
		eth_ni.addListener(this_eth_listener);
	}

	
	/** Gets the Ethernet interface
	 * @return the interface */
	public NetInterface<EthAddress,EthPacket> getEthInterface() {
		return eth_ni;
	}

	@Override
	public EthAddress getAddress() {
		return eth_ni.getAddress();
	}

	@Override
	public void send(EthPacket eth_pkt) {
		if (eth_pkt.getSourceAddress()==null) eth_pkt.setSourceAddress(eth_ni.getAddress());
		if (DEBUG) debug("send(): Ethernet packet: "+eth_pkt);
		eth_ni.send(eth_pkt,null);
	}

	
	/** Processes an incoming Ethernet packet. */
	private void processIncomingPacket(EthPacket pkt) {
		EthPacket eth_pkt=EthPacket.parseEthPacket(pkt.getBytes());
		EthAddress dest_addr=(EthAddress)eth_pkt.getDestAddress();
		if (eth_ni.hasAddress(dest_addr)) {
			if (DEBUG) debug("processIncomingPacket(): Ethernet packet: "+eth_pkt);
			Integer type=eth_pkt.getType();
			LayerListener<EthAddress,EthPacket> listener=listeners.get(type);
			if (listener!=null) {
				try { listener.onIncomingPacket(this,eth_pkt); } catch (Exception e) {
					e.printStackTrace();
				}				
			}
			else {
				if (DEBUG) debug("processIncomingPacket(): no listener for proto 0x"+Integer.toHexString(type));
			}
		}
	}	

	
	@Override
	public void close() {
		if (DEBUG) debug("close()");
		eth_ni.removeListener(this_eth_listener);
		eth_ni.close();
		super.close();
	}	

}
