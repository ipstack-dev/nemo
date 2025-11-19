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

package io.ipstack.net.stack;

import java.util.ArrayList;
import java.util.Random;

import org.zoolu.util.Clock;
import org.zoolu.util.TimerListener;
import org.zoolu.util.log.DefaultLogger;
import org.zoolu.util.log.LoggerLevel;

import io.ipstack.net.ip4.Ip4Packet;
import io.ipstack.net.ip4.IpAddress;
import io.ipstack.net.ip6.Ip6Packet;
import io.ipstack.net.packet.NetInterface;
import io.ipstack.net.packet.NetInterfaceListener;
import io.ipstack.net.packet.Packet;


/** Lossy and slow interface.
 * <p>
 * It adds a delay and packet loss probability to the sent/received packets.
 * The two directions (send and receive) can be configured separately.
 */
public class LossyIpInterface<A extends IpAddress, P extends Packet<A>> extends NetInterface<A,P> {

	/** Debug mode */
	public static boolean DEBUG=false;

	/** Prints a debug message. */
	private void debug(String str) {
		DefaultLogger.log(LoggerLevel.DEBUG,getClass(),str);
	}


	/** Inner interface */
	NetInterface<A,P> net_interface;
	
	/** Interface listeners */
	ArrayList<NetInterfaceListener<A,P>> lossy_listeners=new ArrayList<>();

	/** Loss probability for outgoing packets */
	double send_loss;
	 
	/** Loss probability for incoming packets */
	double recv_loss;
	
	/** Mean delay for outgoing packets in nanoseconds */
	long send_delay;
	
	/** Mean delay for incoming packets in nanoseconds */
	long recv_delay;
	
	Random random=new Random();

	
	/** Creates a new interface.
	 * @param net_interface the network interface
	 * @param delay mean delay in nanoseconds
	 * @param loss loss probability */
	public LossyIpInterface(NetInterface<A,P> net_interface, long delay, double loss) {
		this(net_interface,delay,loss,delay,loss);
	}

	/** Creates a new interface.
	 * @param net_interface the network interface
	 * @param send_delay mean delay for outgoing packets in nanoseconds
	 * @param send_loss loss probability for outgoing packets
	 * @param recv_delay mean delay for incoming packets in nanoseconds
	 * @param recv_loss loss probability for incoming packets */
	public LossyIpInterface(NetInterface<A,P> net_interface, long send_delay, double send_loss, long recv_delay, double recv_loss) {
		super(net_interface.getAddresses());
		this.net_interface=net_interface;
		this.send_delay=send_delay;
		this.send_loss=send_loss;
		this.recv_delay=recv_delay;
		this.recv_loss=recv_loss;
		net_interface.addListener(new NetInterfaceListener<A,P>(){
			@Override
			public void onIncomingPacket(NetInterface<A,P> ni, P pkt) {
				processIncomingPacket(pkt);
			}
		});
		if (DEBUG) debug("LossyIp4TunInterface(): send-loss="+send_loss+", recv-loss="+recv_loss);
	}
	
	private long uniformRandom(long mean_value) {
		if (mean_value==0) return 0;
		// else
		long r=random.nextLong();
		r%=(mean_value*2);
		if (r<0) r+=mean_value*2;
		return r;
	}
		
	private void processIncomingPacket(P pkt) {
		//if (DEBUG) debug("processIncomingPacket(): "+pkt);
		if (random.nextDouble()>recv_loss) {
			long delay=uniformRandom(recv_delay);
			if (delay==0) {
				deliverPacket(pkt);
			}
			else {
				final P pkt_copy;
				if (pkt instanceof Ip4Packet) pkt_copy=(P)Ip4Packet.parseIp4Packet(pkt.getBytes());
				else
				if (pkt instanceof Ip6Packet) pkt_copy=(P)Ip6Packet.parseIp6Packet(pkt.getBytes());
				else throw new RuntimeException("Packet format '"+pkt.getClass().getSimpleName()+"' not supported");
					
				if (DEBUG) debug("processIncomingPacket(): packet delay: "+delay+"ms");
				Clock.getDefaultClock().newTimer(delay/1000000,(int)(delay%1000000),new TimerListener(){
					@Override
					public void onTimeout(org.zoolu.util.Timer t) {
						deliverPacket(pkt_copy);
					}
				}).start();			
			}
		}
		else {
			if (DEBUG) debug("processIncomingPacket(): packet discarded");
		}
	}
	
	/** Passes an incoming packet to the proper listeners.
	 * @param pkt the packet to be delivered */
	private void deliverPacket(P pkt) {
		for (NetInterfaceListener<A,P> li : lossy_listeners) {
			try { li.onIncomingPacket(this,pkt); } catch (Exception e) {
				e.printStackTrace();
			}
		}
		// promiscuous mode
		for (NetInterfaceListener<A,P> li : promiscuous_listeners) {
			try { li.onIncomingPacket(this,pkt); } catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void send(final P pkt, final A dest_addr) {
		//if (DEBUG) debug("send(): "+pkt);
		if (random.nextDouble()>send_loss) {
			long delay=uniformRandom(send_delay);
			if (delay==0) {
				net_interface.send(pkt,dest_addr);
			}
			else {
				if (DEBUG) debug("send(): packet delay: "+delay+"ms");
				Clock.getDefaultClock().newTimer(delay/1000000,(int)(delay%1000000),new TimerListener(){
					@Override
					public void onTimeout(org.zoolu.util.Timer t) {
						net_interface.send(pkt,dest_addr);
					}
				}).start();							
			}
			// promiscuous mode
			for (NetInterfaceListener<A,P> li : promiscuous_listeners) {
				try { li.onIncomingPacket(this,pkt); } catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		else {
			if (DEBUG) debug("send(): packet discarded");
		}
	}

	@Override
	public void addListener(NetInterfaceListener<A,P> listener) {
		synchronized (lossy_listeners) {
			lossy_listeners.add(listener);
		}
	}
	
	@Override
	public void removeListener(NetInterfaceListener<A,P> listener) {
		synchronized (lossy_listeners) { 
			for (int i=0; i<lossy_listeners.size(); i++) {
				NetInterfaceListener<A,P> li=lossy_listeners.get(i);
				if (li==listener) {
					lossy_listeners.remove(i);
				}
			}
		}
	}
	

}
