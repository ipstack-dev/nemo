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

import org.zoolu.util.ArrayUtils;
import org.zoolu.util.log.DefaultLogger;
import org.zoolu.util.log.LoggerLevel;

import io.ipstack.net.packet.Address;
import io.ipstack.net.packet.NetInterface;
import io.ipstack.net.packet.Packet;

import java.util.ArrayList;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


/** A generic link providing both one-to-one, one-to-many, or one-to-all delivery service.
 * It may connect any number of attached {@link LinkInterface link interfaces}.
 * <p>
 * When sending a packet through the method {@link #transmit(Packet, LinkInterface, Address)},
 * if a target (unicast or multicast) address is provided,
 * the packet is passed only to the interfaces that have the given address.
 * @param <A> the address type
 * @param <P> the packet type
 */
public class Link<A extends Address, P extends Packet<A>> {

	/** Debug mode */
	public static boolean DEBUG=false;

	/** Prints a debug message. */
	private void debug(String str) {
		DefaultLogger.log(LoggerLevel.DEBUG,Link.class,str);
	}

	
	/** Active interfaces attached to this link */
	//Set<LinkInterface<A,P>> link_interfaces=new HashSet<>();
	ArrayList<LinkInterface<A,P>> link_interfaces=new ArrayList<>();

	/** Lock for using interfaces */
	ReadWriteLock link_interfaces_lock=new ReentrantReadWriteLock();

	
	/** Creates a new link. */
	public Link() {
	}
	
	/** Creates a network interface attached to this link.
	 * @return the network interface */
	public NetInterface<A,P> createLinkInterface() {
		return new LinkInterface<>(this);
	}
	
	/** Adds a link interface.
	 * @param ni the interface to be added */
	public void addLinkInterface(LinkInterface<A,P> ni) {
		link_interfaces_lock.writeLock().lock();
		try {
			if (ni instanceof PromiscuousLinkInterface<?,?>) link_interfaces.add(0,ni);
			else link_interfaces.add(ni);
		}
		finally {
			link_interfaces_lock.writeLock().unlock();
		}
	}
	
	/** Removes an interface.
	 * @param ni the interface to be removed */
	public void removeLinkInterface(LinkInterface<A,P> ni) {
		link_interfaces_lock.writeLock().lock();
		try {
			link_interfaces.remove(ni);
		}
		finally {
			link_interfaces_lock.writeLock().unlock();
		}
	}
	
	/** Gets the number of attached interfaces.
	 * @return number of interfaces */
	public int numberOfInterfaces() {
		return link_interfaces.size();
	}
	
	/** Whether a given address is present on this link.
	 * @param addr the target address
	 * @return <i>true</i> if the address is present */
	public boolean findAddress(A addr) {
		link_interfaces_lock.readLock().lock();
		try {
			for (LinkInterface<A,P> ni : link_interfaces) if (ni.hasAddress(addr)) return true;
		}
		finally {
			link_interfaces_lock.readLock().unlock();
		}
		return false;
	}
	
	/** Transmits a packet to a all attached interfaces except the source interface.
	 * @param pkt the packet to be sent
	 * @param src_ni the source interface, used for sending the packet */
	public void transmit(P pkt, final LinkInterface<A,P> src_ni) {
		transmit(pkt,src_ni,null);
	}

		
	/** Transmits a packet to a target interface.
	 * @param pkt the packet to be sent
	 * @param src_ni the source interface, used for sending the packet
	 * @param dst_ni_addr the address of the destination link interface */
	public void transmit(P pkt, final LinkInterface<A,P> src_ni, final A dst_ni_addr) {
		//if (DEBUG) debug("transmit(): attached interfaces: "+link_interfaces.size());
		boolean success=false;
		link_interfaces_lock.readLock().lock();
		try {
			for (LinkInterface<A,P> ni : ArrayUtils.synchronizedList(link_interfaces)) {
				if (ni!=src_ni) {
					if (dst_ni_addr==null || ni.hasAddress(dst_ni_addr)) {
						if (DEBUG) debug("transmit(): packet passed to "+ni);
						ni.processIncomingPacket(this,pkt);
						success=true;
					}
					else {
						if (DEBUG) debug("transmit(): packet NOT passed to "+ni);
					}
				}
			}			
		}
		finally {
			link_interfaces_lock.readLock().unlock();
		}
		if (!success) {
			if (DEBUG) debug("transmit(): no destination interface found");
		}
	}
	
	/*@Override
	public String toString() {
		return id;
	}*/

}
