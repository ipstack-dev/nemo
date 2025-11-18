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

package it.unipr.netsec.nemo.link;


import java.util.ArrayList;
import java.util.List;

import org.zoolu.util.Clock;
import org.zoolu.util.LoggerLevel;
import org.zoolu.util.SystemUtils;
import org.zoolu.util.Timer;
import org.zoolu.util.TimerListener;

import it.unipr.netsec.ipstack.ethernet.EthAddress;
import it.unipr.netsec.ipstack.ethernet.EthPacket;
import it.unipr.netsec.ipstack.link.Link;
import it.unipr.netsec.ipstack.link.LinkInterface;
import it.unipr.netsec.ipstack.net.Address;
import it.unipr.netsec.ipstack.net.NetInterface;
import it.unipr.netsec.ipstack.net.NetInterfaceListener;
import it.unipr.netsec.ipstack.net.Packet;


/** Generic {@link it.unipr.netsec.ipstack.link.LinkInterface link interface} attached to a {@link DataLink link} with a finite bit-rate.
 */
public class DataLinkInterface<A extends Address, P extends Packet<A>> extends LinkInterface<A,P> {

	/** Prints a debug message. */
	private void debug(String str) {
		SystemUtils.log(LoggerLevel.DEBUG,DataLinkInterface.class.getSimpleName()+"["+getName()+"]: "+str);
	}

	
	/** Sender buffer */
	ArrayList<LinkPacket<A,P>> buffer=new ArrayList<>();

	/** Whether the interface is transmitting a packet */
	boolean transmitting=false;
	
	
	/** Creates a new interface.
	 * @param link the link to be attached to */
	public DataLinkInterface(DataLink<A,P> link) {
		super(link);
	}
	
	/** Creates a new interface.
	 * @param link the link to be attached to
	 * @param addr the interface address */
	public DataLinkInterface(DataLink<A,P> link, A addr) {
		super(link,addr);
	}
	
	/** Creates a new interface.
	 * @param link the link to be attached to
	 * @param addresses the interface addresses */
	public DataLinkInterface(Link<A,P> link, List<A> addresses) {
		super(link,addresses);
	}
	
	@Override
	public void send(P pkt, A dest_addr) {
		if (DEBUG) debug("send(): sending "+pkt.getPacketLength()+" bytes to "+dest_addr);
		if (((DataLink<A,P>)link).getBitRate()<=0) {
			link.transmit(pkt,this,dest_addr);
		}
		else {
			synchronized (buffer) {
				buffer.add(new LinkPacket<A,P>((P)pkt.clone(),dest_addr));
				if (DEBUG) debug("send(): queued packet "+buffer.size());
				if (!transmitting) {
					if (buffer.size()>1) new RuntimeException("Bug found: link with a queued-packet is not in 'transmit' state");
					transmitHOL();
				}
			}
		}
		// promiscuous mode
		for (NetInterfaceListener<A,P> li : promiscuous_listeners) li.onIncomingPacket(this,pkt);
	}
	
	/** Transmits the packet head of line of the output buffer.
	 * It waits the time for transmitting the entire packet (TX time = (packet_length * 8 bit) / bit_rate)
	 * and passes it to the link for being delivered to the destination interfaces. */
	private void transmitHOL() {
		long transmit_nanosecs=Math.round(buffer.get(0).getPacket().getPacketLength()*8*1000000000.0D/((DataLink<A,P>)link).getBitRate());
		transmitting=true;
		if (DEBUG) debug("transmitHOL(): transmit_time: "+transmit_nanosecs);
		TimerListener timer_listener=new TimerListener() {
			@Override
			public void onTimeout(Timer t) {
				if (DEBUG) debug("onTimeout(): transmission completed");
				synchronized (buffer) {
					LinkPacket<A,P> link_pkt=buffer.get(0);
					buffer.remove(0);
					link.transmit(link_pkt.getPacket(),DataLinkInterface.this,link_pkt.getDestAddress());
					if (buffer.size()>0) transmitHOL();
					else transmitting=false;
				}				
			}
		};
		Timer timer=Clock.getDefaultClock().newTimer(transmit_nanosecs/1000000,(int)(transmit_nanosecs%1000000),timer_listener);
		timer.start();		
	}
	
	/** Processes an incoming packet.
	 * @param link the input link
	 * @param pkt the packet */
	public void processIncomingPacket(DataLink<A,P> link, P pkt) {
		System.out.println("DEBUGGGRRRR: processIncomingPacket(): "+pkt.getPacketLength());
		if (!running) return;
		// else
		if (DEBUG) debug("processIncomingPacket(): received "+pkt.getPacketLength()+" bytes");
		// promiscuous mode
		for (NetInterfaceListener<A,P> li : promiscuous_listeners) li.onIncomingPacket(this,pkt);
		// non-promiscuous mode
		for (NetInterfaceListener<A,P> li :listeners)  li.onIncomingPacket(this,pkt);
	}
	
	@Override
	public void close() {
		link.removeLinkInterface(this);
		running=false;
		super.close();
	}
	
	/** Creates a list of link interfaces.
	 * @param links an array of links
	 * @return the new link interfaces */
	/*public static <A extends Address, P extends Packet<A>> ArrayList<NetInterface<A,P>> createLinkInterfaceArray(DataLink<A,P>[] links) {
		return createLinkInterfaceArray(Arrays.asList(links));
	}*/

	/** Creates a list of link interfaces.
	 * @param links a list of links
	 * @return the new link interfaces */
	/*public static <A extends Address, P extends Packet<A>> ArrayList<NetInterface<A,P>> createLinkInterfaceArray(List<DataLink<A,P>> links) {
		ArrayList<NetInterface<A,P>> interfaces=new ArrayList<>();
		for (DataLink<A,P> link : links) interfaces.add(new DataLinkInterface<A,P>(link));
		return interfaces;
	}*/

	
	/** A buffered packet. */
	class LinkPacket<A extends Address, P extends Packet<A>> {
		P pkt;
		A dst_addr;
		
		public LinkPacket(P pkt, A dst_addr) {
			this.pkt=pkt;
			this.dst_addr=dst_addr;
		}
		public P getPacket() {
			return pkt;
		}
		public A getDestAddress() {
			return dst_addr;
		}
	}

}
