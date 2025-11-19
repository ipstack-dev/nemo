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

import java.util.List;

import org.zoolu.util.log.DefaultLogger;
import org.zoolu.util.log.LoggerLevel;

import io.ipstack.net.packet.Address;
import io.ipstack.net.packet.NetInterface;
import io.ipstack.net.packet.NetInterfaceListener;
import io.ipstack.net.packet.Packet;
import io.ipstack.net.util.PacketUtils;


/** A generic link interface.
 * <p>
 * It allows the sending and receiving of packets through a {@link Link}.
 */
public class LinkInterface<A extends Address, P extends Packet<A>> extends NetInterface<A,P> {

	/** Debug mode */
	public static boolean DEBUG=false;

	/** Prints a debug message. */
	private void debug(String str) {
		DefaultLogger.log(LoggerLevel.DEBUG,LinkInterface.class.getSimpleName()+"["+getName()+"]: "+str);
	}

	
	/** Link */
	protected Link<A,P> link;
	
	/** Whether the interface is running */
	protected boolean running;
	
	
	/** Creates a new interface.
	 * @param link the link to be attached to */
	public LinkInterface(Link<A,P> link) {
		super((A)null);
		init(link);
	}
	
	/** Creates a new interface.
	 * @param link the link to be attached to
	 * @param addr the interface address */
	public LinkInterface(Link<A,P> link, A addr) {
		super(addr);
		init(link);
	}
	
	/** Creates a new interface.
	 * @param link the link to be attached to
	 * @param addresses the interface addresses */
	public LinkInterface(Link<A,P> link, List<A> addresses) {
		super(addresses);
		init(link);
	}
	
	/** Initializes the interface.
	 * @param link the link to be attached to */
	private void init(Link<A,P> link) {
		this.link=link;
		link.addLinkInterface(this);
		running=true;
	}
	
	/** Gets the link.
	 * @return the link */
	public Link<A,P> getLink() {
		return link;
	}
	
	@Override
	public void send(P pkt, A dest_addr) {
		if (DEBUG) debug("send(): to "+dest_addr+": "+PacketUtils.toString(pkt));
		link.transmit(pkt,this,dest_addr);
		// promiscuous mode
		for (NetInterfaceListener<A,P> li : promiscuous_listeners) li.onIncomingPacket(this,pkt);
	}
		
	/** Processes an incoming packet.
	 * @param link the input link
	 * @param pkt the packet */
	public void processIncomingPacket(Link<A,P> link, P pkt) {
		if (!running) return;
		// else
		if (DEBUG) debug("processIncomingPacket(): "+PacketUtils.toString(pkt));
		// promiscuous mode
		for (NetInterfaceListener<A,P> li : promiscuous_listeners) li.onIncomingPacket(this,pkt);
		// non-promiscuous mode
		for (NetInterfaceListener<A,P> li : listeners)  li.onIncomingPacket(this,pkt);
	}
	
	@Override
	public void close() {
		link.removeLinkInterface(this);
		running=false;
		super.close();
	}

}
