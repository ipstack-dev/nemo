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

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.SocketException;
import java.util.HashMap;

import org.zoolu.util.Clock;
import org.zoolu.util.log.DefaultLogger;
import org.zoolu.util.log.LoggerLevel;

import io.ipstack.net.ip4.SocketAddress;


/** Hub that interconnects a set of Ethernet over UDP tunnels.
 */
public class EthTunnelSwitch extends EthTunnelHub {
	
	/** Prints a debug message. */
	private void debug(String str) {
		DefaultLogger.log(LoggerLevel.DEBUG,toString()+": "+str);
	}
	

	/** Expiration time, in milliseconds */
	public static long EXPIRATION_TIME=20000; // 20 secs
	
	/** Switching table. Each entry contains the endpoint socket and last time (in milliseconds) */
	HashMap<EthAddress,Object[]> sw_table=new HashMap<>();


	/** Creates a new switch. 
	 * @throws SocketException */
	public EthTunnelSwitch() throws SocketException {
		super();
	}
	
	
	/** Creates a new switch.
	 * @param port the local UDP port 
	 * @throws SocketException */
	public EthTunnelSwitch(int port) throws SocketException {
		super(port);
	}
	
	
	/** Creates a new switch. 
	 * @param port the local UDP port 
	 * @param max_endpoints maximum number of connected endpoints
	 * @throws SocketException */
	public EthTunnelSwitch(int port, int max_endpoints) throws SocketException {
		super(port,max_endpoints);
	}
	
	
	@Override
	protected void processIncomingPacket(SocketAddress src_soaddr, DatagramPacket udp_pkt, EthAddress src_ethaddr, EthAddress dst_ethaddr) throws IOException {
		if (dst_ethaddr.isMulticast()) {
			super.processIncomingPacket(src_soaddr,udp_pkt,src_ethaddr,dst_ethaddr);
			return;
		}
		// else
		if (DEBUG) debug("processIncomingPacket: "+src_ethaddr+" > "+dst_ethaddr);
		long time=Clock.getDefaultClock().currentTimeMillis();
		// backward learning
		Object[] addr_entry=sw_table.get(src_ethaddr);
		if (addr_entry==null) sw_table.put(src_ethaddr,new Object[]{src_soaddr,new Long(time)});
		else {
			addr_entry[0]=src_soaddr;
			addr_entry[1]=new Long(time);
		}
		// get the output interface
		addr_entry=sw_table.get(dst_ethaddr);
		if (addr_entry!=null) {
			if (time<((long)addr_entry[1]+EXPIRATION_TIME)) {
				SocketAddress dst_soaddr=(SocketAddress)addr_entry[0];
				if (!dst_soaddr.equals(src_soaddr)) {
					if (DEBUG) debug("packet sent to "+dst_soaddr);
					udp_pkt.setAddress(dst_soaddr.getIpAddress().toInetAddress());
					udp_pkt.setPort(dst_soaddr.getPort());
					sock.send(udp_pkt);
				}
			}
			else {
				sw_table.remove(dst_ethaddr);
				if (DEBUG) debug("expired: "+dst_ethaddr);
				super.processIncomingPacket(src_soaddr,udp_pkt,src_ethaddr,dst_ethaddr);
			}
		}
		else {
			if (DEBUG) debug("not found: "+dst_ethaddr);
			super.processIncomingPacket(src_soaddr,udp_pkt,src_ethaddr,dst_ethaddr);
		}
	}

}
