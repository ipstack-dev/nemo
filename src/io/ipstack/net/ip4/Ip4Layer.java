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

package io.ipstack.net.ip4;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import org.zoolu.util.log.DefaultLogger;
import org.zoolu.util.log.LoggerLevel;

import io.ipstack.net.icmp4.IcmpLayer;
import io.ipstack.net.icmp4.message.IcmpDestinationUnreachableMessage;
import io.ipstack.net.packet.NetInterface;
import io.ipstack.net.packet.RoutingTable;


/** IPv4 layer provides standard IPv4 service to upper layers.
 * <p>
 * It includes ICMP support.
 */
public class Ip4Layer {

	/** Debug mode */
	public static boolean DEBUG=false;
	
	/** Prints a debug message. */
	private void debug(String str) {
		DefaultLogger.log(LoggerLevel.DEBUG,toString()+": "+str);
	}

	/** Whether sending ICMP Destination Unreachable messages */
	boolean SEND_ICMP_DEST_UREACHABLE=false;

	/** Receivers */
	Hashtable<Integer,Ip4LayerListener> listeners=new Hashtable<Integer,Ip4LayerListener>();

	/** IP node  */
	Ip4Node ip_node;
	
	/** ICMP layer  */
	IcmpLayer icmp_layer;
	
	/** Whether it has been closed  */
	boolean closed=false;


	/** Creates a new IP layer.
	 * @param ip_interfaces list of IP network interfaces */
	public Ip4Layer(List<NetInterface<Ip4Address,Ip4Packet>> ip_interfaces) {
		ip_node=new Ip4Node(ip_interfaces);
		ip_node.setListener(new Ip4NodeListener() {
			@Override
			public void onIncomingPacket(Ip4Node ip_node, Ip4Packet ip_pkt) {
				processIncomingPacket(ip_pkt);
			}
		});
		ip_node.setForwarding(false);
		icmp_layer=new IcmpLayer(this);
		for (int i=0; i<ip_interfaces.size(); i++) ip_interfaces.get(i).setName("eth"+i);
	}

	/** Creates a new IP layer.
	 * @param ip_interfaces IP network interfaces */
	@SafeVarargs
	public Ip4Layer(NetInterface<Ip4Address,Ip4Packet>... ip_interfaces) {
		this(Arrays.asList(ip_interfaces));
	}

	/** Creates a new IP layer.
	 * @param ip_node IP node */
	public Ip4Layer(Ip4Node ip_node) {
		this.ip_node=ip_node;
		ip_node.setListener(new Ip4NodeListener() {
			@Override
			public void onIncomingPacket(Ip4Node ip_node, Ip4Packet ip_pkt) {
				processIncomingPacket(ip_pkt);
			}
		});
		icmp_layer=new IcmpLayer(this);
	}

	/** Sets the listener for a given protocol number.
	 * @param proto the protocol number
	 * @param listener the new listener for the given protocol number */
	public void setListener(int proto, Ip4LayerListener listener) {
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
	public void removeListener(Ip4LayerListener listener) {
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
	public NetInterface<Ip4Address,Ip4Packet> getNetInterface(String name) {
		for (NetInterface<Ip4Address,Ip4Packet> ni : getNetInterfaces()) if (ni.getName().equals(name)) return ni;
		return null;
	}

	/** Gets the names of the network interfaces.
	 * @return the set of names */
	public ArrayList<String> getNetInterfaceNames() {
		ArrayList<String> names=new ArrayList<>();
		for (NetInterface<Ip4Address,Ip4Packet> ni : getNetInterfaces()) names.add(ni.getName());
		return names;
	}

	/** Gets the network interfaces.
	 * @return network interfaces */
	public ArrayList<NetInterface<Ip4Address,Ip4Packet>> getNetInterfaces() {
		return ip_node.getNetInterfaces();
	}
	
	/** Gets all network interfaces, including loobback.
	 * @return network interfaces */
	public ArrayList<NetInterface<Ip4Address,Ip4Packet>> getAllInterfaces() {
		ArrayList<NetInterface<Ip4Address,Ip4Packet>> all=new ArrayList<>(ip_node.getNetInterfaces());
		all.add(0,ip_node.getLoopbackInterface());
		return all;
	}
	
	/** Gets the first non-loopback address (if any).
	 * @return the address */
	public Ip4Address getAddress() {
		return ip_node.getAddress();
	}
	
	/** Gets the routing table.
	 * @return routing table */
	public RoutingTable<Ip4Address,Ip4Packet> getRoutingTable() {
		return (RoutingTable<Ip4Address,Ip4Packet>)ip_node.getRoutingFunction();
	}
	
	/** Gets the IP node.
	 * @return ip_node */
	public Ip4Node getIpNode() {
		return ip_node;
	}

	/** Gets the ICMP layer.
	 * @return the ICMP layer used by this IP layer */
	public IcmpLayer getIcmpLayer() {
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
	public Ip4Address getSourceAddress(Ip4Address dst_addr) {
		return ip_node.getSourceAddress(dst_addr);
	}
	
	/** Sends an IP packet.
	 * @param pkt the packet to be sent */
	public void send(Ip4Packet pkt) {
		ip_node.sendPacket(pkt);
	}
	
	/** Processes an incoming packet.
	 * @param pkt the packet */
	private void processIncomingPacket(Ip4Packet ip_pkt) {
		Integer proto=Integer.valueOf(ip_pkt.getProto());
		if (listeners.containsKey(proto)) {
			if (DEBUG) debug("processIncomingPacket(): "+ip_pkt);
			listeners.get(proto).onReceivedPacket(this,ip_pkt);
		}
		else {
			if (proto.intValue()==Ip4Packet.IPPROTO_ICMP) {
				// re-connect the default ICMP implementation
				icmp_layer.close();
				icmp_layer=new IcmpLayer(this);
				listeners.get(proto).onReceivedPacket(this,ip_pkt);
			}
			else {
				// packet discarded
				// sends Destination (protocol) Unreachable ICMP message
				if (SEND_ICMP_DEST_UREACHABLE) icmp_layer.send(new IcmpDestinationUnreachableMessage(ip_pkt.getDestAddress(),ip_pkt.getSourceAddress(),IcmpDestinationUnreachableMessage.CODE_protocol_unreachable,ip_pkt));
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
