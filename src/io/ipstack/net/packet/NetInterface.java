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


import java.util.ArrayList;
import java.util.List;

import org.zoolu.util.Random;


/** A network interface for sending and receiving packets using an underlying protocol, a network interface, or directly a link.
 * <p>
 * A network interface may have one or more addresses.
 * <p>
 * Packets are sent out calling the method {@link #send(Packet, Address)}). This method can take as parameter the address
 * of the (next-hop) node that the packet has to be passed to. This address, if present, may need to be translated to the actual address of node according to the underlying protocol.
 * If and how this is achieved depends on the specific protocol and should be implemented by the NetInterface.
 * <p>
 * Packet are received through a {@link NetInterfaceListener} set through the method {@link #addListener(NetInterfaceListener)}. 
 * @param <A> the address type
 * @param <P> the packet type
 */
public abstract class NetInterface<A extends Address, P extends Packet<A>> {

	/** Length of the interface id */
	private static int ID_LEN=4;

	/** Interface name */
	private String name=null;
	
	/** Interface addresses */
	protected ArrayList<A> addresses=new ArrayList<A>();
	
	/** Interface listeners */
	protected ArrayList<NetInterfaceListener<A,P>> listeners=new ArrayList<NetInterfaceListener<A,P>>();

	/** Interface listeners in 'promiscuous' mode */
	protected ArrayList<NetInterfaceListener<A,P>> promiscuous_listeners=new ArrayList<NetInterfaceListener<A,P>>();
	

	
	/** Creates a new interface. */
	protected NetInterface() {
		this((A)null);
	}

	
	/** Creates a new interface.
	 * @param addr interface address */
	protected NetInterface(A addr) {
		name=generateName();
		if (addr!=null) addresses.add(addr);
	}

	
	/** Creates a new interface.
	 * @param addrs interface addresses */
	protected NetInterface(List<A> addrs) {
		name=generateName();
		if (addrs!=null) for (A a : addrs) addresses.add(a);
	}

	
	/** Generates a random name.
	 * @return the name */
	private static String generateName() {
		return Random.nextHexString(ID_LEN);
	}

	
	/** Adds a listener to this interface for receiving incoming packets targeted to this interface.
	 * @param listener interface listener to be added */
	public void addListener(NetInterfaceListener<A,P> listener) {
		synchronized (listeners) {
			listeners.add(listener);
		}
	}
	
	
	/** Removes a listener.
	 * @param listener interface listener to be removed */
	public void removeListener(NetInterfaceListener<A,P> listener) {
		synchronized (listeners) { 
			for (int i=0; i<listeners.size(); i++) {
				NetInterfaceListener<A,P> li=listeners.get(i);
				if (li==listener) {
					listeners.remove(i);
				}
			}
		}
	}

	
	/** Removes all listeners. */
	/*public void removeAllListeners() {
		synchronized (listeners) { 
			listeners.clear();
		}
	}*/

	
	/** Gets all interface listeners.
	 * @return array of listeners */
	/*protected ArrayList<NetInterfaceListener<A,P>> getListeners() {
		synchronized (listeners) { 
			return (ArrayList<NetInterfaceListener<A,P>>)listeners.copy();
		}
	}*/

		
	/** Adds a listener to this interface for capturing all outgoing and incoming packets.
	 * Incoming packets are captured regardless the destination address (i.e. listening in 'promiscuous' mode).
	 * @param listener interface listener to be added */
	public void addPromiscuousListener(NetInterfaceListener<A,P> listener) {
		synchronized (promiscuous_listeners) {
			promiscuous_listeners.add(listener);
		}
	}

	
	/** Removes a listener that had been set in 'promiscuous' mode.
	 * @param listener interface listener to be removed */
	public void removePromiscuousListener(NetInterfaceListener<A,P> listener) {
		synchronized (promiscuous_listeners) {
			for (int i=0; i<promiscuous_listeners.size(); i++) {
				NetInterfaceListener<A,P> li=promiscuous_listeners.get(i);
				if (li==listener) {
					promiscuous_listeners.remove(i);
				}
			}	
		}
	}


	/** Gets all promiscuous listeners.
	 * @return array of listeners */
	/*protected ArrayList<NetInterfaceListener<A,P>> getPromiscuousListeners() {
		synchronized (promiscuous_listeners) { 
			return (ArrayList<NetInterfaceListener<A,P>>)promiscuous_listeners.copy();
		}
	}*/

	
	/** Adds an interface address.
	 * @param addr the address */
	public void addAddress(A addr) {
		synchronized (addresses) {
			addresses.add(addr);
		}
	}

	
	/** Removes an interface address.
	 * @param addr the address */
	public void removeAddress(A addr) {
		synchronized (addresses) {
			for (int i=0; i<addresses.size(); i++) {
				A a=addresses.get(i);
				if (a.equals(addr)) {
					addresses.remove(a);
				}
			}
		}		
	}
	
	/** Gets the first interface address.
	 * @return the address */
	public A getAddress() {
		synchronized (addresses) { 
			if (addresses.size()>0) return addresses.get(0);
			else return null;
		}
	}
	
	/** Gets all interface addresses.
	 * @return a list containing the addresses */
	@SuppressWarnings("unchecked")
	public ArrayList<A> getAddresses() {
		synchronized (addresses) {
			return (ArrayList<A>)addresses.clone();
		}
	}
	
	/** Whether a given address belongs to this interface.
	 * @param addr the address
	 * @return <i>true</i> if the address belongs to this interface */
	public boolean hasAddress(A addr) {
		synchronized (addresses) { 
			for (A a : addresses) {
				if (a.equals(addr)) return true;
			}
		}
		return false;
	}

	
	/** Sends a packet.
	 * @param pkt the packet to be sent
	 * @param dest_addr the address of the next-hop node */
	public abstract void send(P pkt, A dest_addr);	

		
	/** Closes the interface. */
	public void close() {
		listeners.clear();
	}
	
	
	/** Gets the interface name.
	 * @return the name */
	public String getName() {
		return name;
	}

	
	/** Sets the interface name.
	 * @param name the name */
	public void setName(String name) {
		this.name=name;
	}

	
	@Override	
	public String toString() {
		A addr=getAddress();
		return getClass().getSimpleName()+'['+(addr!=null? addr : name)+']';
	}

}
