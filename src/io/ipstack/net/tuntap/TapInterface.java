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

package io.ipstack.net.tuntap;

import java.io.IOException;

import org.zoolu.util.log.DefaultLogger;
import org.zoolu.util.log.LoggerLevel;

import io.ipstack.net.ethernet.EthAddress;
import io.ipstack.net.ethernet.EthPacket;
import io.ipstack.net.packet.NetInterface;
import io.ipstack.net.packet.NetInterfaceListener;
import io.ipstack.net.util.IpAddressUtils;
import io.ipstack.net.util.PacketUtils;


/** Network interface for sending or receiving Ethernet packets through a TAP interface.
 */
public class TapInterface extends NetInterface<EthAddress,EthPacket> {
	
	/** Debug mode */
	public static boolean DEBUG=false;

	/** Prints a debug message. */
	private void debug(String str) {
		DefaultLogger.log(LoggerLevel.DEBUG,getClass(),str);
	}

	
	/** Default device file prefix for macvtap interface */
	static String DEFAULT_MACVTAP_DEV_FILE="/dev/tap";

	/** Default Ethernet address, used in case of no other address is provided neither obtained from the system */
	static EthAddress DEFAULT_ADDR=new EthAddress("11:22:33:44:55:66");
	
	/** TAP header length */
	private static int MACVTAP_HDR_LEN=10;
	
	/** Buffer size */
	private static int BUFFER_SIZE=MACVTAP_HDR_LEN+EthPacket.MAXIMUM_PAYLOAD_SIZE+18; // TAP-HDR +  MTU + 14B ETH-HDR + 4B IEEE802.1Q-TAG
	
	/** Sender buffer */
	private byte[] send_buffer=new byte[BUFFER_SIZE];

	/** Receiver buffer */
	private byte[] recv_buffer=new byte[BUFFER_SIZE];

	/** TAP interface */
	TuntapSocket tap;
	
	/** TAP header length */
	int tap_hdr_len=0;
	
	/** Whether it is running */
	boolean is_running=true;	

	
	/** Creates a new TAP interface.
	 * @param name name of the TAP interface (e.g. "tap0"); if <i>null</i>, a new interface is added
	 * @param eth_addr Ethernet address
	 * @throws IOException */
	public TapInterface(String name, EthAddress eth_addr) throws IOException {
		this(name,null,eth_addr);
	}

	
	/** Creates a new TAP interface.
	 * @param name name of the TAP interface (e.g. "tap0"); if <i>null</i>, a new interface is added
	 * @param dev_file device file (if any) or <i>null</i>
	 * @param eth_addr Ethernet address
	 * @throws IOException */
	public TapInterface(String name, String dev_file, EthAddress eth_addr) throws IOException {
		super(eth_addr!=null?eth_addr:(eth_addr=getNonNullHardwareAddress(name)));
		if (name.startsWith("macvtap")) {
			tap_hdr_len=10;
			if (dev_file==null) dev_file=getMacvtapDevFile(name);
		}
		if (DEBUG) debug("name="+name+" file="+dev_file+" addr="+eth_addr);
		tap=new TuntapSocket(TuntapSocket.Type.TAP,name,dev_file);
		new Thread() {
			public void run() {
				receiver();
			}
		}.start();
	}

	
	@Override
	public void send(EthPacket pkt, EthAddress dest_addr) {
		if (DEBUG) debug("send(): packet: "+PacketUtils.toString(pkt));
		synchronized (send_buffer) {
			int len=pkt.getBytes(send_buffer,tap_hdr_len);
			try {
				tap.send(send_buffer,0,tap_hdr_len+len);
				// promiscuous mode
				for (NetInterfaceListener<EthAddress,EthPacket> li : promiscuous_listeners) {
					try { li.onIncomingPacket(this,pkt); } catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			catch (IOException e) {
				if (DEBUG) debug(e.toString());
			}
		}
	}
	
	
	/** Receives packets. */
	private void receiver() {
		synchronized (recv_buffer) {
			while (is_running) {
				try {				
					int len=tap.receive(recv_buffer,0);
					if (is_running) {
						EthPacket eth_pkt=EthPacket.parseEthPacket(recv_buffer,tap_hdr_len,len-tap_hdr_len);
						if (DEBUG) debug("receiver(): packet: "+PacketUtils.toString(eth_pkt));
						// promiscuous mode
						for (NetInterfaceListener<EthAddress,EthPacket> li : promiscuous_listeners) {
							try { li.onIncomingPacket(this,eth_pkt); } catch (Exception e) {
								e.printStackTrace();
							}
						}
						// non-promiscuous mode
						for (NetInterfaceListener<EthAddress,EthPacket> li : listeners) {
							try { li.onIncomingPacket(this,eth_pkt); } catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				}
				catch (IOException e) {
					if (DEBUG) debug(e.toString());
				}
			}
			tap.close();				
			if (DEBUG) debug("closed");
		}
	}

	
	@Override
	public void close() {
		if (DEBUG) debug("close()");
		is_running=false;
		super.close();
	}
		
	
	/** Gets Ethernet address of a (physical) network interface.
	 * @param name name of the network interface
	 * @return the address if exists, otherwise a default value */
	private static EthAddress getNonNullHardwareAddress(String name) {
		EthAddress addr=IpAddressUtils.getHardwareAddress(name);
		if (addr!=null) return addr;
		else return DEFAULT_ADDR;
	}

	
	/** Gets device file of a macvtap network interface.
	 * @param name name of the network interface
	 * @return the address */
	private static String getMacvtapDevFile(String name) {
		int index=IpAddressUtils.getNetworkInterfaceIndex(name);
		if (index>=0) return DEFAULT_MACVTAP_DEV_FILE+index;
		return null;
	}


}
