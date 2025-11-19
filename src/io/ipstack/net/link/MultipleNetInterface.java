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

package io.ipstack.net.link;


import java.util.ArrayList;
import java.util.Collection;

import io.ipstack.net.packet.Address;
import io.ipstack.net.packet.NetInterface;
import io.ipstack.net.packet.NetInterfaceListener;
import io.ipstack.net.packet.Packet;


/** It listens to different network interfaces and/or links.
 * <p>
 * For each received packet the method {@link NetInterfaceListener#onIncomingPacket(NetInterface, Packet)} is called reporting the actual interface where the packet has been received.
 */
public class MultipleNetInterface<A extends Address, P extends Packet<A>> {
	
	/** Network interfaces */
	ArrayList<NetInterface<A,P>> net_interfaces=new ArrayList<>();
	
	/** Multiple network interface listener */
	NetInterfaceListener<A,P> listener;
	
	/** This network interface listener */
	//NetInterfaceListener<A,P> this_ni_listener = (ni,pkt) -> listener.onIncomingPacket(ni,pkt);
	NetInterfaceListener<A,P> this_ni_listener=new NetInterfaceListener<A,P>() {
		@Override
		public void onIncomingPacket(NetInterface<A,P> ni, P pkt) {
			MultipleNetInterface.this.listener.onIncomingPacket(ni,pkt);
		}
	};

	
	/** Creates a new interface.
	 * @param listener the sniffer listener */
	public MultipleNetInterface(NetInterfaceListener<A,P> listener) {
		this.listener=listener;
	}
	
	/** Adds a link.
	 * @param link the link to attach the sniffer to */
	public void addLink(Link<A,P> link) {
		addInterface(new PromiscuousLinkInterface<A,P>(link));
	}
	
	/** Adds a set of links.
	 * @param links the link to attach the sniffer to */
	public void addLinks(Collection<Link<A,P>> links) {
		for (Link<A,P> link : links) addLink(link);
	}
	
	/** Adds an network interfaces.
	 * @param ni the interface to attach the sniffer to */
	public synchronized void addInterface(NetInterface<A,P> ni) {
		this.net_interfaces.add(ni);
		ni.addListener(this_ni_listener);
	}
	
	/** Adds a set of network interfaces.
	 * @param net_interfaces the interfaces to attach the sniffer to */
	public void addInterfaces(Collection<NetInterface<A,P>> net_interfaces) {
		for (NetInterface<A,P> ni : net_interfaces) addInterface(ni);
	}
	
	/** Removes all interfaces. */
	public synchronized void RemoveAllInterfaces() {
		for (NetInterface<A,P> ni : net_interfaces) {
			ni.removePromiscuousListener(this_ni_listener);
		}
	}
	
}
