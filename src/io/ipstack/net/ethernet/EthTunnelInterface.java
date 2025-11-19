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
import java.lang.reflect.Field;
import java.net.DatagramPacket;
import java.net.SocketException;

import org.zoolu.util.log.DefaultLogger;
import org.zoolu.util.log.LoggerLevel;

import io.ipstack.net.ip4.Ip4Address;
import io.ipstack.net.ip4.SocketAddress;
import io.ipstack.net.packet.NetInterface;
import io.ipstack.net.packet.NetInterfaceListener;
import io.ipstack.net.socket.DatagramSocket;
import io.ipstack.net.socket.JavanetDatagramSocket;


/** Ethernet over UDP tunnel toward a selected end-point.
 */
public class EthTunnelInterface extends NetInterface<EthAddress,EthPacket> {

	/** Debug mode */
	public static boolean DEBUG=false;

	/** Prints a debug message. */
	private void debug(String str) {
		DefaultLogger.log(LoggerLevel.DEBUG,toString()+": "+str);
	}


	/** Ping protocol type */
	public static final int PING_TYPE=0;

	/** Ping packet */
	public static final byte[] PING=new EthPacket(EthAddress.BROADCAST_ADDRESS,EthAddress.BROADCAST_ADDRESS,PING_TYPE,null,0,0).getBytes();

	/** Receiver buffer size */
	private static final int BUFFER_SIZE=8000;
	
	/** UDP socket */
	DatagramSocket socket=null;

	/** Remote UDP end-point */
	SocketAddress remote_soaddr;

	/** Reverse UDP */
	//boolean reverse_udp=false;
	
	/** Whether no Ethernet address has been configured, like a PH port */
	boolean no_eth_address;


	
	/** Creates a new Ethernet over UDP tunnel interface.
	 * @param remote_soaddr remote tunnel end-point 
	 * @param addr Ethernet address
	 * @throws SocketException */
	public EthTunnelInterface(SocketAddress remote_soaddr, EthAddress addr) throws SocketException {
		this(-1,remote_soaddr,addr);
	}

	
	/** Creates a new Ethernet over UDP tunnel interface.
	 * @param local_port UDP port for the local tunnel end-point
	 * @param remote_soaddr remote tunnel end-point 
	 * @param addr Ethernet address
	 * @throws SocketException */
	public EthTunnelInterface(int local_port, SocketAddress remote_soaddr, EthAddress addr) throws SocketException {
		super(addr);
		try {
			Field provider=java.net.DatagramSocket.class.getField("PROVIDER"); // throws an exception in case of standard DatagramSocket	
			socket=new io.ipstack.net.rawsocket.udp.DatagramSocket(local_port);
			if (DEBUG) debug("DatagramSocket implementation: "+provider.get(null).toString());
		}
		catch (Exception e) {
			socket=new JavanetDatagramSocket(local_port);
			if (DEBUG) debug("DatagramSocket implementation: standard");
		}
		this.remote_soaddr=remote_soaddr;
		no_eth_address=addr==null;
		start();
	}
	
	
	/** Gets the remote UDP socket address.  
	 * @return the address */
	public SocketAddress getRemoteSocketAddress() {
		return remote_soaddr;
	}

	
	/** Starts the receiver. */
	private void start() {
		new Thread() {
			public void run() {
				DatagramPacket datagram=new DatagramPacket(new byte[BUFFER_SIZE],BUFFER_SIZE);
				try {
					while (true) {
						socket.receive(datagram);
						EthPacket eth_pkt=EthPacket.parseEthPacket(datagram.getData(),datagram.getOffset(),datagram.getLength());
						if (DEBUG) debug("run(): packet received: "+eth_pkt);
						if (remote_soaddr==null) {
							remote_soaddr=new SocketAddress(new Ip4Address(datagram.getAddress()),datagram.getPort());
							if (DEBUG) debug("run(): remote-soaddr="+remote_soaddr);
						}
						if (eth_pkt.getType()==PING_TYPE) {
							if (DEBUG) debug("run(): ping received");
							continue;
						}
						// promiscuous mode
						for (NetInterfaceListener<EthAddress,EthPacket> li : promiscuous_listeners) {
							try { li.onIncomingPacket(EthTunnelInterface.this,eth_pkt); } catch (Exception e) {
								e.printStackTrace();
							}
						}
						// non-promiscuous mode
						EthAddress dest_addr=eth_pkt.getDestAddress();
						if (no_eth_address || hasAddress(dest_addr)) {
							for (NetInterfaceListener<EthAddress,EthPacket> li : listeners) {
								try { li.onIncomingPacket(EthTunnelInterface.this,eth_pkt); } catch (Exception e) {
									e.printStackTrace();
								}
							}									
						}
					}
				}
				catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}.start();
		// PING
		if (remote_soaddr!=null) ping();
	}

	
	/** Sends a ping packet to a remote end-point (typically for create an association). */
	public void ping() {
		if (DEBUG) debug("ping(): "+remote_soaddr);
		DatagramPacket datagram=new DatagramPacket(PING,PING.length,remote_soaddr.getIpAddress().toInetAddress(),remote_soaddr.getPort());
		try {
			socket.send(datagram);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	@Override
	public void send(EthPacket eth_pkt, EthAddress dest_addr) {
		if (DEBUG) debug("send(): "+eth_pkt);
		if (remote_soaddr==null){
			if (DEBUG) debug("send(): no remote end-point address: packet discarded");
			return;
		}
		byte[] data=eth_pkt.getBytes();
		DatagramPacket datagram=new DatagramPacket(data,data.length,remote_soaddr.getIpAddress().toInetAddress(),remote_soaddr.getPort());
		try {
			socket.send(datagram);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		// promiscuous mode
		for (NetInterfaceListener<EthAddress,EthPacket> li : promiscuous_listeners) {
			try { li.onIncomingPacket(this,eth_pkt); } catch (Exception e) {
				e.printStackTrace();
			}
		}

	}
	
	
	/** Closes the interface. */
	public void close() {
		super.close();
		if (socket!=null) socket.close();
	}
	

	@Override
	public String toString() {
		return getClass().getSimpleName()+"["+socket.getLocalPort()+","+remote_soaddr+"]";
	}
	
}
