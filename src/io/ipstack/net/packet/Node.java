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

import org.zoolu.util.log.DefaultLogger;
import org.zoolu.util.log.LoggerLevel;


/** A generic network node.
 * It may have one or more network interfaces and one routing function. <br>
 * It may act either as terminal node or intermediate relay node,
 * depending on the value of the 'forwarding' attribute.
 * <p>
 * Incoming packets are processed by two different methods:
 * <ul> 
 * <li>{@link #processReceivedPacket(NetInterface, Packet)} - it processes all received packets;</li>
 * <li>{@link #processForwardingPacket(Packet)} - it processes packets that targeted to this node and has to be
 * be forwarded to a remote node.</li>
 * </ul>
 * @param <A> the address type
 * @param <P> the packet type
 */
public class Node<A extends Address, P extends Packet<A>> {

	/** Debug mode */
	public static boolean DEBUG=false;
	
	/** Prints a debug message. */
	private void debug(String str) {
		//DefaultLogger.log(LoggerLevel.DEBUG,toString()+": "+str);
		DefaultLogger.log(LoggerLevel.DEBUG,Node.class.getSimpleName()+"["+getAddress()+"]: "+str);
	}

	
	/** Network interfaces */
	protected ArrayList<NetInterface<A,P>> net_interfaces=new ArrayList<>();
	
	/** Routing function */
	protected RoutingFunction<A,P> routing_function;
	
	/** Packet forwarding */
	protected boolean forwarding;

	/** Network interface listener */
	protected NetInterfaceListener<A,P> this_ni_listener;
	

	
	/** Creates a new node. */
	/*public Node() {
		this(null,null,false);
	}*/
	
	/** Creates a new node.
	 * @param net_interfaces network interfaces
	 * @param routing_function the routing function
	 * @param forwarding whether acting as relay node; <i>true</i> for relay node, <i>false</i> for terminal node. */
	public Node(List<NetInterface<A,P>> net_interfaces, RoutingFunction<A,P> routing_function, boolean forwarding) {
		this.routing_function=routing_function;
		this.forwarding=forwarding;
		this_ni_listener=new NetInterfaceListener<A,P>() {
			public void onIncomingPacket(NetInterface<A,P> ni, P pkt) {
				processReceivedPacket(ni,pkt);
			}
		};
		if (net_interfaces!=null) {
			for (NetInterface<A,P> ni : net_interfaces) addNetInterface(ni);
		}
	}

	/** Gets the name (if any) or the first address of this node.
	 * @return the name or address */
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}
	
	/** Gets an address identifying this node.
	 * @return the address */
	public A getAddress() {
		return net_interfaces.size()==0? null : net_interfaces.get(0).getAddress();
	}

	/** Gets all addresses without repetitions.
	 * @return the addresses */
	public ArrayList<A> getAddresses() {
		ArrayList<A> list=new ArrayList<>();
		for (NetInterface<A,P> ni : net_interfaces) {
			for (A a : ni.getAddresses()) {
				if (!list.contains(a)) list.add(a);
			}
		}
		return list;
	}

	/** Adds a network interface.
	 * @param ni the network interface */
	public void addNetInterface(NetInterface<A,P> ni) {
		synchronized (net_interfaces) {
			ni.addListener(this_ni_listener);
			ni.setName("ni"+net_interfaces.size());
			net_interfaces.add(ni);			
		}
	}
	
	/** Removes a network interface.
	 * @param ni the network interface */
	public void removeNetInterface(NetInterface<A,P> ni) {
		synchronized (net_interfaces) {
			ni.removeListener(this_ni_listener);
			ni.close();
			net_interfaces.remove(ni);
			// rename all interfaces
			for (int i=0; i<net_interfaces.size(); i++) {
				NetInterface<A,P> ni_i=net_interfaces.get(i);
				ni_i.setName("ni"+i);
			}			
		}
	}
	
	/** Gets all network interfaces.
	 * @return the list of network interfaces */
	@SuppressWarnings("unchecked")
	public ArrayList<NetInterface<A,P>> getNetInterfaces() {
		return (ArrayList<NetInterface<A,P>>)net_interfaces.clone();
	}
	
	/** Sets routing function.
	 * @param routing_function the routing function */
	public void setRouting(RoutingFunction<A,P> routing_function) {
		this.routing_function=routing_function;
	}
	
	/** Gets the routing function.
	 * @return the routing function of this node*/
	public RoutingFunction<A,P> getRoutingFunction() {
		return routing_function;
	}
	
	/** Sets forwarding mode.
	 * @param forwarding whether acting as relay node; <i>true</i> for relay node, <i>false</i> for terminal node. */
	public void setForwarding(boolean forwarding) {
		this.forwarding=forwarding;
	}
	
	/** Gets forwarding mode.
	 * @return <i>true</i> for relay node, <i>false</i> for terminal node. */
	public boolean getForwarding() {
		return forwarding;
	}
	
	/** Whether a given address targets this node.
	 * @param addr the address
	 * @return <i>true</i> if the address targets this node; <i>false</i> otherwise */
	public boolean hasAddress(A addr) {
		for (NetInterface<A,P> ni : net_interfaces) if (ni.hasAddress(addr)) return true;
		// else
		return false;
	}
	
	/** Sends a packet.
	 * @param pkt the packet to be sent */
	public void sendPacket(P pkt) {
		//if (DEBUG) debug("sendPacket(): "+Bytes.toHex(pkt.getBytes()));
		if (DEBUG) debug("sendPacket(): "+pkt);
		if (routing_function==null) throw new RuntimeException("No routing function as been set for this node.");
		Route<A,P> route=routing_function.getRoute(pkt.getDestAddress());
		if (route!=null) {
			A next_hop=route.getNextHop();
			if (next_hop==null) next_hop=pkt.getDestAddress();
			NetInterface<A,P> out_interface=route.getOutputInterface();
			/*if (out_interface==null && next_hop!=null)
				for (NetInterface ni : link_interfaces)
					if (ni.getLink().findAddress(next_hop)) { out_interface=ni; break; }*/
			if (DEBUG) debug("sendPacket(): forwarding packet through interface "+out_interface+" to next node "+next_hop);
			//if (out_interface!=null) out_interface.send(pkt,next_hop);
			out_interface.send(pkt,next_hop);
		}
		else {
			if (DEBUG) debug("sendPacket(): WARNING: no route to "+pkt.getDestAddress());
		}
	}
	
	/** Processes incoming packet received by a network interface.
	 * @param ni the input network interface
	 * @param pkt the packet */
	protected void processReceivedPacket(NetInterface<A,P> ni, P pkt) {
		//if (DEBUG) debug("processPacket(): "+Bytes.toHex(pkt.getBytes()));
		if (DEBUG) debug("processIncomingPacket(): "+pkt);
		A dest_addr=pkt.getDestAddress();
		if (!hasAddress(dest_addr)) {
			// packet forwarding
			if (forwarding) {		
				processForwardingPacket(pkt);
			}
		}
	}
	
	/** Processes a packet that has to be forwarded.
	 * @param pkt the packet to be forwarded */
	protected void processForwardingPacket(P pkt) {
		sendPacket(pkt);
	}

	/** Removes all network interfaces and closes the node. */
	public void close() {
		synchronized (net_interfaces) {
			while (net_interfaces.size()>0) {	
				removeNetInterface(net_interfaces.get(net_interfaces.size()-1));
			}		
		}
	}

	@Override
	public String toString() {
		return getClass().getSimpleName()+'['+getAddress()+']';
	}

}
