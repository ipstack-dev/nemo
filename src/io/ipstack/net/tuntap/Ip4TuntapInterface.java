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
import java.util.ArrayList;

import io.ipstack.net.ethernet.EthAddress;
import io.ipstack.net.ip4.Ip4Address;
import io.ipstack.net.ip4.Ip4AddressPrefix;
import io.ipstack.net.ip4.Ip4Packet;
import io.ipstack.net.packet.NetInterface;
import io.ipstack.net.packet.NetInterfaceListener;


/** IPv4 interface for sending and receiving IPv4 packets through an underling TUN/TAP interface.
 */
public class Ip4TuntapInterface extends NetInterface<Ip4Address,Ip4Packet> {
	
	/** The actual TUN or TAP interface */
	NetInterface<Ip4Address,Ip4Packet> tuntap;
	
	/** Interface parameters */
	String parameters;
	
	
	/** Creates a new interface.
	 * @param name name of the interface (e.g. "tun0" or "tap0"). if <i>null</i>, a new TAP interface is added
	 * @param ip_addr_prefix the IP address and prefix length 
	 * @throws IOException */
	public Ip4TuntapInterface(String name, Ip4AddressPrefix ip_addr_prefix) throws IOException {
		this(getTypeFromName(name),name,null,null,ip_addr_prefix);
	}

	/** Creates a new interface.
	 * @param type the interface type, TAP or TUN
	 * @param name name of the interface (e.g. "tun0" or "tap0"). if <i>null</i>, a new TAP interface is added
	 * @param ip_addr_prefix the IP address and prefix length 
	 * @throws IOException */
	public Ip4TuntapInterface(TuntapSocket.Type type, String name, Ip4AddressPrefix ip_addr_prefix) throws IOException {
		this(type,name,null,null,ip_addr_prefix);
	}
	
	/** Creates a new interface.
	 * @param type the interface type, TAP or TUN
	 * @param name name of the interface (e.g. "tun0" or "tap0"). if <i>null</i>, a new TAP interface is added
	 * @param dev_file device file (if any) or <i>null</i>
	 * @param ip_addr_prefix the IP address and prefix length 
	 * @throws IOException */
	public Ip4TuntapInterface(TuntapSocket.Type type, String name, String dev_file, Ip4AddressPrefix ip_addr_prefix) throws IOException {
		this(type,name,dev_file,null,ip_addr_prefix);
	}

	/** Creates a new interface.
	 * @param type the interface type, TAP or TUN
	 * @param name name of the interface (e.g. "tun0" or "tap0"). if <i>null</i>, a new TAP interface is added
	 * @param dev_file device file (if any) or <i>null</i>
	 * @param eth_addr Ethernet address (in case of TAP) or <i>null</i>
	 * @param ip_addr_prefix the IP address and prefix length
	 * @throws IOException */
	public Ip4TuntapInterface(TuntapSocket.Type type, String name, String dev_file, EthAddress eth_addr, Ip4AddressPrefix ip_addr_prefix) throws IOException {
		super(ip_addr_prefix);
		switch (type) {
			case  TAP : tuntap=new Ip4TapInterface(name,dev_file,eth_addr,ip_addr_prefix); break;
			case TUN : tuntap=new Ip4TunInterface(name,ip_addr_prefix); break;
		}
		parameters=type+","+name+","+dev_file+","+eth_addr;
		
		tuntap.addListener(new NetInterfaceListener<Ip4Address,Ip4Packet>() {
			@Override
			public void onIncomingPacket(NetInterface<Ip4Address,Ip4Packet> ni, Ip4Packet pkt) {
				// promiscuous mode
				for (NetInterfaceListener<Ip4Address,Ip4Packet> li : promiscuous_listeners) {
					try { li.onIncomingPacket(Ip4TuntapInterface.this,pkt); } catch (Exception e) {
						e.printStackTrace();
					}
				}
				// non-promiscuous mode
				for (NetInterfaceListener<Ip4Address,Ip4Packet> li : listeners) {
					try { li.onIncomingPacket(Ip4TuntapInterface.this,pkt); } catch (Exception e) {
						e.printStackTrace();
					}
				}
			}			
		});
	}
	
	/** Guesses the interface type from the name.
	 * @param name the interface name
	 * @return the type 
	 * @throws IOException */
	private static TuntapSocket.Type getTypeFromName(String name) throws IOException {
		if (name==null || name.toLowerCase().startsWith("tap") || name.toLowerCase().startsWith("macvtap")) return TuntapSocket.Type.TAP;
		if (name.toLowerCase().startsWith("tun") || name.toLowerCase().startsWith("utun")) return TuntapSocket.Type.TUN;
		// else
		throw new IOException("Unrecognized TUN/TAP interface type: "+name);
	}
	
	/** Gets interface parameters.
	 * @return comma-separated list of parameters */
	public String getParameters() {
		return parameters;
	}

	@Override
	public boolean hasAddress(Ip4Address addr) {
		return tuntap.hasAddress(addr);
	}

	@Override
	public void addAddress(Ip4Address addr) {
		tuntap.addAddress(addr);
	}

	@Override
	public void removeAddress(Ip4Address addr) {
		tuntap.removeAddress(addr);
	}

	@Override
	public ArrayList<Ip4Address> getAddresses() {
		return tuntap.getAddresses();
	}

	@Override
	public void send(final Ip4Packet pkt, final Ip4Address dest_addr) {
		tuntap.send(pkt,dest_addr);
		// promiscuous mode
		for (NetInterfaceListener<Ip4Address,Ip4Packet> li : promiscuous_listeners) {
			try { li.onIncomingPacket(this,pkt); } catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void close() {
		tuntap.close();
	}
		
}
