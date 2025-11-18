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

package it.unipr.netsec.ipstack.ethernet;


import it.unipr.netsec.ipstack.ip4.Ip4Address;
import it.unipr.netsec.ipstack.ip4.SocketAddress;
import it.unipr.netsec.ipstack.net.NetInterface;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;

import org.zoolu.util.LoggerLevel;
import org.zoolu.util.SystemUtils;


/** Hub that interconnects a set of Ethernet over UDP tunnels.
 */
public class EthTunnelHub {

	/** Debug mode */
	public static boolean DEBUG=false;

	/** Prints a debug message. */
	private void debug(String str) {
		SystemUtils.log(LoggerLevel.DEBUG,toString()+": "+str);
	}

	/** Prints an output message. */
	private void println(String str) {
		System.out.println(getClass().getSimpleName()+": "+str);
	}


	/** Default port */
	public static int DEFAULT_PORT=7002;

	/** Default maximum number of endpoints */
	public static int DEFAULT_SWITCH_SIZE=32;

	/** Receiver buffer size */
	private static final int BUFFER_SIZE=8000;

	/** UDP port */
	int port;
	
	/** UDP socket */
	DatagramSocket sock;
	
	/** Maximum number of connected endpoints */
	int max_endpoints;
	
	/** Active endpoints attached to this hub */
	ArrayList<SocketAddress> endpoints=new ArrayList<SocketAddress>();
	
	/** Whether it is running */
	boolean running=true;

	
	/** Creates a new hub. 
	 * @throws SocketException */
	public EthTunnelHub() throws SocketException {
		this(DEFAULT_PORT,DEFAULT_SWITCH_SIZE);
	}
	
	
	/** Creates a new hub.
	 * @param port the local UDP port 
	 * @throws SocketException */
	public EthTunnelHub(int port) throws SocketException {
		this(port,DEFAULT_SWITCH_SIZE);
	}
	
	
	/** Creates a new hub. 
	 * @param port the local UDP port 
	 * @param max_endpoints maximum number of connected endpoints
	 * @throws SocketException */
	public EthTunnelHub(int port, int max_endpoints) throws SocketException {
		println("started on UDP port "+port+", number of Etherent ports: "+max_endpoints);
		this.port=port;
		this.sock=new DatagramSocket(port);
		this.max_endpoints=max_endpoints;		
		run();
	}
	
	
	/** Runs the hub. */
	private void run() {
		final int max_endpoints1=max_endpoints;
		new Thread() {
			public void run() {
				DatagramPacket datagram=new DatagramPacket(new byte[BUFFER_SIZE],BUFFER_SIZE);
				int index=0; // virtual index of the next end-point
				try {
					while (running) {
						sock.receive(datagram);
						EthPacket eth_pkt=EthPacket.parseEthPacket(datagram.getData(),datagram.getOffset(),datagram.getLength());
						if (DEBUG) debug("packet received: "+eth_pkt);
						int proto=eth_pkt.getType();
						SocketAddress src_soaddr=new SocketAddress(new Ip4Address(datagram.getAddress()),datagram.getPort());
						if (!endpoints.contains(src_soaddr)) {
							println("new endpoint ["+index+"]: "+src_soaddr);							
							if (max_endpoints1>0 && max_endpoints1==endpoints.size()) {
								//if (DEBUG) debug("too much endpoints already connected ("+max_endpoints1+"): packet discarded");
								//continue;
								println("there are already "+max_endpoints1+" end-points connected: disconnecting "+endpoints.get(0));
								endpoints.remove(0);
							}
							// else
							endpoints.add(src_soaddr);
							index=(index+1)%max_endpoints;
						}
						if (proto==EthTunnelInterface.PING_TYPE) {
							if (DEBUG) debug("it's a ping");
							continue;
						}
						// else, process the Ethernet-over-UDP packet
						processIncomingPacket(src_soaddr,datagram,eth_pkt.getSourceAddress(),eth_pkt.getDestAddress());
					}
					println("stopped");							
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		}.start();
	}

	
	/** Processes an incoming Ethernet-over-UDP packet.
	 * @param src_soaddr address of the endpoint where the packet come from
	 * @param udp_pkt incoming packet 
	 * @throws IOException */
	protected void processIncomingPacket(SocketAddress src_soaddr, DatagramPacket udp_pkt, EthAddress src_ethaddr, EthAddress dst_ethaddr) throws IOException {
		// broadcast to all endpoints
		for (SocketAddress dst_soaddr : endpoints) {
			if (!dst_soaddr.equals(src_soaddr)) {
				if (DEBUG) debug("packet sent to "+dst_soaddr);
				udp_pkt.setAddress(dst_soaddr.getIpAddress().toInetAddress());
				udp_pkt.setPort(dst_soaddr.getPort());
				sock.send(udp_pkt);
			}
		}		
	}

	
	/** Stops running. */
	public void halt() {
		running=false;
		if (sock!=null) {
			sock.close();
			sock=null;
		}
	}

	
	@Override
	public String toString() {
		return getClass().getSimpleName()+"["+port+"]";
	}

}
