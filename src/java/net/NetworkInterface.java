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

package java.net;


import java.util.ArrayList;
import java.util.Enumeration;
import java.util.NoSuchElementException;

import org.zoolu.util.log.DefaultLogger;
import org.zoolu.util.log.LoggerLevel;

import io.ipstack.net.ip4.Ip4Address;
import io.ipstack.net.ip4.Ip4AddressPrefix;
import io.ipstack.net.ip4.Ip4Packet;
import io.ipstack.net.ip4.IpAddress;
import io.ipstack.net.ip6.Ip6Address;
import io.ipstack.net.ip6.Ip6AddressPrefix;
import io.ipstack.net.ip6.Ip6Packet;
import io.ipstack.net.packet.Address;
import io.ipstack.net.packet.LoopbackInterface;
import io.ipstack.net.packet.NetInterface;
import io.ipstack.net.stack.IpStack;


public final class NetworkInterface {

	/** Debug mode */
	public static boolean DEBUG=true;
	
	/** Prints a debug message. */
	private static void debug(String str) {
		DefaultLogger.log(LoggerLevel.DEBUG,NetworkInterface.class,str);
	}
	
	
	static int MTU=460;

	private NetInterface net_interface;

	
	/** Creates a new network interface.
	 * @param net_interface the interface */
	NetworkInterface(NetInterface net_interface) {
		debug("NetworkInterface(): "+net_interface);
		this.net_interface=net_interface;
	}

	public String getName() {
		return net_interface.getName();
	}
	
	static int DEBUG_COUNT=0;

	public Enumeration<InetAddress> getInetAddresses() {
		debug("getInetAddresses()");
		DEBUG_COUNT++;
		ArrayList<Address> addrs=net_interface.getAddresses();
		final ArrayList<InetAddress> ipaddrs=new ArrayList<InetAddress>();
		for (Address a: addrs) if (a instanceof IpAddress) ipaddrs.add(((IpAddress)a).toInetAddress());
		debug("getInetAddresses(): "+ipaddrs);
		return new Enumeration<InetAddress>(){
			int index=0;
			@Override
			public boolean hasMoreElements() {
				return (index<ipaddrs.size());
			}			
			@Override
			public InetAddress nextElement() {
				if (index<ipaddrs.size()) {
					debug("getInetAddresses(): nextElement(): "+index+", "+ipaddrs.get(index));
					if (DEBUG_COUNT>=3) try { throw new RuntimeException("Debug"); } catch (Exception e) { e.printStackTrace(); System.exit(0); }
					return ipaddrs.get(index++);
				}
				else {
					throw new NoSuchElementException();
				}
			}
		};
	}

	public java.util.List<InterfaceAddress> getInterfaceAddresses() {
		debug("getInetAddresses()");
		ArrayList<Address> addrs=net_interface.getAddresses();
		ArrayList<InterfaceAddress> iaddrs=new ArrayList<InterfaceAddress>();
		for (Address a : addrs) {
			if (a instanceof Ip4AddressPrefix) iaddrs.add(new Ip4InterfaceAddressImpl((Ip4AddressPrefix)a));
			else
			if (a instanceof Ip6AddressPrefix) iaddrs.add(new Ip6InterfaceAddressImpl((Ip6AddressPrefix)a));
		}
		debug("getInetAddresses(): "+iaddrs.toString());
		return iaddrs;
	}

	/*public Enumeration<NetworkInterface> getSubInterfaces() {
		return null;
	}*/

	/*public NetworkInterface getParent() {
		return null;
	}*/

	public int getIndex() {
		ArrayList<NetInterface<Ip4Address,Ip4Packet>> ip4_interfaces=IpStack.getDefaultInstance().getIp4Layer().getNetInterfaces();
		for (int i=0; i<ip4_interfaces.size(); i++) {
			if (ip4_interfaces.get(i)==net_interface) return i;
		}			
		ArrayList<NetInterface<Ip6Address,Ip6Packet>> ip6_interfaces=IpStack.getDefaultInstance().getIp6Layer().getNetInterfaces();
		for (int i=0; i<ip6_interfaces.size(); i++) {
			if (ip6_interfaces.get(i)==net_interface) return i;
		}			
		return -1;
	}

	public String getDisplayName() {
		return getName();
	}

	public static NetworkInterface getByName(String name) throws SocketException {
		if (name==null) throw new NullPointerException();
		// else
		NetInterface ni=null;
		ni=IpStack.getDefaultInstance().getIp4Layer().getNetInterface(name);
		if (ni!=null) return new NetworkInterface(ni);
		ni=IpStack.getDefaultInstance().getIp6Layer().getNetInterface(name);
		if (ni!=null) return new NetworkInterface(ni);
		// else
		return null;
	}

	public boolean isUp() throws SocketException {
		return true;
	}

	public boolean isLoopback() throws SocketException {
		return net_interface instanceof LoopbackInterface;
	}

	public boolean isPointToPoint() throws SocketException {
		// TODO
		return false;
	}

	public boolean supportsMulticast() throws SocketException {
		// TODO
		return true;
	}

	public byte[] getHardwareAddress() throws SocketException {
		// TODO
		return null;
	}

	public int getMTU() throws SocketException {
		// TODO
		return MTU;
	}

	public boolean isVirtual() {
		// TODO
		return false;
	}

	public boolean equals(Object obj) {
		if (obj==null|| !(obj instanceof NetworkInterface)) return false;
		// else
		NetworkInterface ni=(NetworkInterface)obj;
		return net_interface.equals(ni.net_interface);
	}

	public int hashCode() {
		//return name==null? 0: name.hashCode();
		// TODO
		return net_interface.hashCode();
	}

	public String toString() {
		return getName();
	}

	// STATIC METHODS:
	
	public static NetworkInterface getByIndex(int index) throws SocketException {
		debug("getByIndex()");
		if (index<0) throw new IllegalArgumentException("Interface index can't be negative");
		return new NetworkInterface(IpStack.getDefaultInstance().getIp4Layer().getNetInterfaces().get(index));
	}

	public static NetworkInterface getByInetAddress(InetAddress addr) throws SocketException {
		debug("getByInetAddress()");
		if (addr==null) throw new NullPointerException();
		if (!(addr instanceof Inet4Address || addr instanceof Inet6Address)) throw new IllegalArgumentException ("invalid address type");

		if (addr instanceof Inet4Address) {
			Ip4Address ipaddr=new Ip4Address((Inet4Address)addr);
			for (NetInterface<Ip4Address, Ip4Packet> ni : IpStack.getDefaultInstance().getIp4Layer().getNetInterfaces()) {
				if (ni.hasAddress(ipaddr))return new NetworkInterface(ni);
			}
		}
		else
		if (addr instanceof Inet6Address) {
			Ip6Address ipaddr=new Ip6Address((Inet6Address)addr);
			for (NetInterface<Ip6Address,Ip6Packet> ni : IpStack.getDefaultInstance().getIp6Layer().getNetInterfaces()) {
				if (ni.hasAddress(ipaddr)) return new NetworkInterface(ni);
			}
		}
		return null;
	}

	public static Enumeration<NetworkInterface> getNetworkInterfaces() throws SocketException {
		debug("getNetworkInterfaces()");
		final ArrayList<NetInterface<Ip4Address,Ip4Packet>> net_interfaces=IpStack.getDefaultInstance().getIp4Layer().getNetInterfaces();
		debug("getNetworkInterfaces(): "+net_interfaces.toString());
		return new Enumeration<NetworkInterface>(){
			int index=0;
			@Override
			public boolean hasMoreElements() {
				return index<net_interfaces.size();
			}
			@Override
			public NetworkInterface nextElement() {
				NetInterface<Ip4Address,Ip4Packet> ni=net_interfaces.get(index++);
				return new NetworkInterface(ni);
			}
		};
	}

	static NetworkInterface getDefault() {
		debug("getDefault()");
		ArrayList<NetInterface<Ip4Address,Ip4Packet>> net_interfaces=IpStack.getDefaultInstance().getIp4Layer().getNetInterfaces();
		if (net_interfaces==null || net_interfaces.size()==0) return null;
		// else
		return new NetworkInterface(net_interfaces.get(0));
	}
}
