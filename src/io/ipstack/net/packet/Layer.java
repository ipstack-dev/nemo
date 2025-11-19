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

package io.ipstack.net.packet;

import java.util.HashMap;

import org.zoolu.util.log.DefaultLogger;
import org.zoolu.util.log.LoggerLevel;


/** Service Access Point (SAP) of a given communication protocol.
 * It is the interface that a layer (service provider), implementing the communication protocol, offers to the upper layer (user).
 * Ii provides sending and receiving primitives.
 * <p>
 * The receiving service is provided through listeners. Incoming packets are passed to the listener addressed by the packet.
 * The way that packets are addressed to the proper listener depends on the type of protocol.
 * For example, UDP and TCP use destination port numbers, IP uses the protocol field, Ethernet uses the protocol type field, etc.
 * @param <A> the address type
 * @param <P> the packet type
 */
public abstract class Layer<A extends Address, P extends Packet<A>> {

	/** Debug mode */
	public static boolean DEBUG=false;
	
	/** Prints a debug message. */
	private void debug(String str) {
		DefaultLogger.log(LoggerLevel.DEBUG,getClass(),str);
	}

	
	/** Selective receivers */
	protected HashMap<Object,LayerListener<A,P>> listeners=new HashMap<>();
	
	/** Promiscuous receivers */
	//protected ArrayList<LayerListener<A,P>> promiscuos_listeners=new ArrayList<>();

	
	/** Adds a listener for receiving all packets.
	 * @param listener the listener */
	/*public void addListener(LayerListener<A,P> listener) {
		if (DEBUG) debug("addListener(): listener for "+id);
		synchronized (promiscuos_listeners) {
			promiscuos_listeners.add(listener);
		}
	}*/
	
	/** Adds a listener.
	 * @param id identifies the packets that the listener has to be associated to
	 * @param listener the listener */
	public void addListener(Object id, LayerListener<A,P> listener) {
		if (DEBUG) debug("addListener(): listener for "+id);
		synchronized (listeners) {
			listeners.put(id,listener);
		}
	}
	
	/** Removes a listener.
	 * @param id identifies the packets that the listener was associated to */
	public void removeListener(Object id) {
		if (DEBUG) debug("removeListener(): listener for "+id);
		synchronized (listeners) {
			listeners.remove(id);
		}
	}
	
	/** Removes a listener.
	 * @param listener the listener to be removed */
	public void removeListener(LayerListener<A,P> listener) {
		for (Object id : listeners.keySet()) {
			if (listeners.get(id)==listener) {
				if (DEBUG) debug("removeListener(): listener for "+id);
				listeners.remove(id);
				break;
			}
		}
	}
	
	/** Gets address.
	 * @return the address */
	public abstract A getAddress();
	
	/** Sends a packet.
	 * @param pkt the packet to be sent */
	public abstract void send(P pkt);	

		
	/** Closes the layer. */
	public void close() {
		listeners.clear();
	}	
	
}
