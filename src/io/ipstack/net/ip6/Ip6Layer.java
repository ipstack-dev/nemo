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

package io.ipstack.net.ip6;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import org.zoolu.util.ArrayUtils;
import org.zoolu.util.log.DefaultLogger;
import org.zoolu.util.log.LoggerLevel;

import io.ipstack.net.icmp6.Icmp6Layer;
import io.ipstack.net.icmp6.message.Icmp6DestinationUnreachableMessage;
import io.ipstack.net.packet.NetInterface;
import io.ipstack.net.packet.RoutingTable;


/** IPv6 layer provides standard IPv6 service to upper layers.
 * <p>
 * It includes basic ICMPv6 support.
 */
public class Ip6Layer {

	/** Debug mode */
	public static boolean DEBUG=false;
	
	/** Prints a debug message. */
	private void debug(String str) {
		DefaultLogger.log(LoggerLevel.DEBUG,toString()+": "+str);
	}

	/** Receivers */
	Hashtable<Integer,Ip6LayerListener> listeners=new Hashtable<Integer,Ip6LayerListener>();

	/** IP node  */
	Ip6Node ip_node;

	/** Network interface names */
	Hashtable<String,NetInterface<Ip6Address,Ip6Packet>> ni_names=new Hashtable<>();	

	/** ICMPv6 layer  */
	Icmp6Layer icmp_layer;
	
	/** Whether it has been closed  */
	boolean closed=false;


	
	/** Creates a new IP layer.
	 * @param ip_interfaces list of IP network interfaces */
	public Ip6Layer(List<NetInterface<Ip6Address,Ip6Packet>> ip_interfaces) {
		ip_node=new Ip6Node(ip_interfaces);
		ip_node.setListener(new Ip6NodeListener(){
			@Override
			public void onIncomingPacket(Ip6Node ip_node, Ip6Packet ip_pkt) {
				processIncomingPacket(ip_pkt);
			}
		});
		ip_node.setForwarding(false);
		icmp_layer=new Icmp6Layer(this);
		for (int i=0; i<ip_interfaces.size(); i++) ni_names.put("eth"+i,ip_interfaces.get(i));
	}
	
	/** Creates a new IP layer.
	 * @param ip_interfaces IP network interfaces */
	@SafeVarargs
	public Ip6Layer(NetInterface<Ip6Address,Ip6Packet>... ip_interfaces) {
		this(Arrays.asList(ip_interfaces));
	}

	/** Creates a new IP layer.
	 * @param ip_interface IP network interface */
	public Ip6Layer(NetInterface<Ip6Address,Ip6Packet> ip_interface) {
		this(ArrayUtils.arraylist(ip_interface));
	}
	
	/** Creates a new IP layer.
	 * @param ip_node IP node */
	public Ip6Layer(Ip6Node ip_node) {
		this.ip_node=ip_node;
		ip_node.setListener(new Ip6NodeListener() {
			@Override
			public void onIncomingPacket(Ip6Node ip_node, Ip6Packet ip_pkt) {
				processIncomingPacket(ip_pkt);
			}
		});
		icmp_layer=new Icmp6Layer(this);
		for (int i=0; i<getNetInterfaces().size(); i++) ni_names.put("eth"+i,getNetInterfaces().get(i));
	}

	/** Sets the listener for a given protocol number.
	 * @param proto the protocol number
	 * @param listener the new listener for the given protocol number */
	public void setListener(int proto, Ip6LayerListener listener) {
		synchronized (listeners) {
			Integer key=Integer.valueOf(proto);
			if (listeners.containsKey(key)) listeners.remove(key);
			listeners.put(key,listener);
		}
	}
	
	/** Removes the listener for a given protocol number.
	 * @param proto the protocol number */
	public void removeListener(int proto) {
		synchronized (listeners) {
			Integer key=Integer.valueOf(proto);
			listeners.remove(key);
		}
	}
	
	/** Removes a listener.
	 * @param listener the listener to be removed */
	public void removeListener(Ip6LayerListener listener) {
		for (Integer key : listeners.keySet()) {
			if (listeners.get(key)==listener) {
				listeners.remove(key);
				break;
			}
		}
	}
	
	/** Gets a given interface.
	 * @param name the name of the interface
	 * @return the interface */
	public NetInterface<Ip6Address,Ip6Packet> getNetInterface(String name) {
		for (NetInterface<Ip6Address,Ip6Packet> ni : getNetInterfaces()) if (ni.getName().equals(name)) return ni;
		return null;
	}

	/** Gets the names of the network interfaces.
	 * @return the set of names */
	public ArrayList<String> getNetInterfaceNames() {
		ArrayList<String> names=new ArrayList<>();
		for (NetInterface<Ip6Address,Ip6Packet> ni : getNetInterfaces()) names.add(ni.getName());
		return names;
	}


	/** Gets the network interfaces.
	 * @return network interfaces */
	public ArrayList<NetInterface<Ip6Address,Ip6Packet>> getNetInterfaces() {
		return ip_node.getNetInterfaces();
	}
	
	/** Gets all interfaces, including loobback.
	 * @return routing table */
	public ArrayList<NetInterface<Ip6Address,Ip6Packet>> getAllInterfaces() {
		ArrayList<NetInterface<Ip6Address,Ip6Packet>> all=new ArrayList<>(ip_node.getNetInterfaces());
		all.add(0,ip_node.getLoopbackInterface());
		return all;
	}
	
	/** Gets the first non-loopback address (if any).
	 * @return the address */
	public Ip6Address getAddress() {
		return ip_node.getAddress();
	}
	
	/** Gets the routing table.
	 * @return routing table */
	public RoutingTable<Ip6Address,Ip6Packet> getRoutingTable() {
		return (RoutingTable<Ip6Address,Ip6Packet>)ip_node.getRoutingFunction();
	}
	
	/** Gets the IP node.
	 * @return ip_node */
	public Ip6Node getIpNode() {
		return ip_node;
	}

	/** Gets the ICMP6 layer.
	 * @return the ICMP6 layer used by this IPv6 layer */
	public Icmp6Layer getIcmp6Layer() {
		return icmp_layer;
	}

	/** Sets forwarding mode.
	 * @param forwarding <i>true</i> for acting as relay node, <i>false</i> for acting as terminal node. */
	public void setForwarding(boolean forwarding) {
		ip_node.setForwarding(forwarding);
	}
	
	/** Gets a local IP address for sending datagrams to a target node.
	 * @param dst_addr address of the target node
	 * @return the IP address */
	public Ip6Address getSourceAddress(Ip6Address dst_addr) {
		return ip_node.getSourceAddress(dst_addr);
	}
	
	/** Sends an IP packet.
	 * @param pkt the packet to be sent */
	public void send(Ip6Packet pkt) {
		if (DEBUG) debug("send(): "+pkt);
		ip_node.sendPacket(pkt);
	}
	
	/** Processes an incoming packet.
	 * @param ip_pkt the packet */
	protected void processIncomingPacket(Ip6Packet ip_pkt) {
		// process IPv6 extension headers
		// TODO
		Integer proto=Integer.valueOf(ip_pkt.getPayloadType());
		if (listeners.containsKey(proto)) {
			if (DEBUG) debug("processIncomingPacket(): "+ip_pkt);
			listeners.get(proto).onReceivedPacket(this,ip_pkt);
		}
		else {
			if (proto.intValue()==Ip6Packet.IPPROTO_ICMP6) {
				// re-connect the default ICMP implementation
				icmp_layer.close();
				icmp_layer=new Icmp6Layer(this);
				listeners.get(proto).onReceivedPacket(this,ip_pkt);
			}
			else {
				// packet discarded
				// sends Destination (protocol) Unreachable ICMP message
				icmp_layer.send(new Icmp6DestinationUnreachableMessage((Ip6Address)ip_pkt.getDestAddress(),(Ip6Address)ip_pkt.getSourceAddress(),Icmp6DestinationUnreachableMessage.CODE_Address_unreachable,ip_pkt));
			}
		}
	}
	
	/** Closes the layer. */
	public void close() {
		if (!closed) {
			closed=true;
			listeners.clear();
			icmp_layer.close();
			ip_node.close();
		}
	}	
	
	@Override
	public String toString() {
		return getClass().getSimpleName()+'['+getAddress()+']';
	}

}
