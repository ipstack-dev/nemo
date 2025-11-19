package io.ipstack.net.stack;


import java.util.ArrayList;
import java.util.HashSet;

import io.ipstack.net.ethernet.EthAddress;
import io.ipstack.net.ethernet.EthPacket;
import io.ipstack.net.ethernet.EthTunnelInterface;
import io.ipstack.net.ip4.Ip4Address;
import io.ipstack.net.ip4.Ip4AddressPrefix;
import io.ipstack.net.ip4.Ip4EthInterface;
import io.ipstack.net.ip4.Ip4Packet;
import io.ipstack.net.ip4.Ip4Prefix;
import io.ipstack.net.ip4.SocketAddress;
import io.ipstack.net.link.LinkInterface;
import io.ipstack.net.packet.NetInterface;
import io.ipstack.net.tuntap.Ip4TuntapInterface;


public abstract class IpInterfaceUtils {

	
	public static String getLinkName(NetInterface<Ip4Address,Ip4Packet> ni) {
		if (ni instanceof LinkInterface) return Links.getLinkName(((LinkInterface<Ip4Address,Ip4Packet>)ni).getLink());
		return null;
	}

	
	public static String getType(NetInterface<Ip4Address,Ip4Packet> ni) {
		String ni_info=ni.getClass().getSimpleName();
		if (ni instanceof LinkInterface) {
			String name=Links.getLinkName(((LinkInterface<Ip4Address,Ip4Packet>)ni).getLink());
			if (name!=null) ni_info+="/"+name;
		}
		else
		if (ni instanceof Ip4TuntapInterface) {
			ni_info+="/"+((Ip4TuntapInterface)ni).getParameters();
		}
		else
		if (ni instanceof Ip4EthInterface) {
			NetInterface<EthAddress,EthPacket> eth=((Ip4EthInterface)ni).getEthInterface();
			if (eth instanceof EthTunnelInterface) {
				SocketAddress soaddr=((EthTunnelInterface)eth).getRemoteSocketAddress();
				ni_info+="/"+soaddr;						
			}
		}
		return ni_info;
	}
	
	
	public static String getPhAddress(NetInterface<Ip4Address,Ip4Packet> ni) {
		return ni instanceof Ip4EthInterface? ((Ip4EthInterface)ni).getEthInterface().getAddress().toString() : null;
	}
	
	
	public static String[] getAddresses(NetInterface<Ip4Address,Ip4Packet> ni) {
		ArrayList<String> addr_list=new ArrayList<String>();
		// list all directed-broadcast addresses in order to not display them
		/*HashSet<Ip4Address> directed_broadcast_addrs=new HashSet<>();
		for (Ip4Address addr : ni.getAddresses()) if (addr instanceof Ip4AddressPrefix) {
			Ip4Prefix prefix=((Ip4AddressPrefix)addr).getPrefix();
			if (prefix.getPrefixLength()<32) directed_broadcast_addrs.add(prefix.getDirectedBroadcastAddress());
		}*/
		for (Ip4Address addr : ni.getAddresses()) {
			//if (addr.isMulticast()) continue; // skip multicast address
			//if (directed_broadcast_addrs.contains(addr)) continue; // skip directed-broadcast address
			String str=addr instanceof Ip4AddressPrefix? ((Ip4AddressPrefix)addr).toStringWithPrefixLength() : addr.toString();
			addr_list.add(str);
		}
		return addr_list.toArray(new String[0]);
	}

	
	public static String[] getUnicastAddresses(NetInterface<Ip4Address,Ip4Packet> ni) {
		ArrayList<String> addr_list=new ArrayList<String>();
		// list all directed-broadcast addresses in order to not display them
		HashSet<Ip4Address> directed_broadcast_addrs=new HashSet<>();
		for (Ip4Address addr : ni.getAddresses()) if (addr instanceof Ip4AddressPrefix) {
			Ip4Prefix prefix=((Ip4AddressPrefix)addr).getPrefix();
			if (prefix.getPrefixLength()<32) directed_broadcast_addrs.add(prefix.getDirectedBroadcastAddress());
		}
		for (Ip4Address addr : ni.getAddresses()) {
			if (addr.isMulticast()) continue; // skip multicast address
			if (directed_broadcast_addrs.contains(addr)) continue; // skip directed-broadcast address
			String str=addr instanceof Ip4AddressPrefix? ((Ip4AddressPrefix)addr).toStringWithPrefixLength() : addr.toString();
			addr_list.add(str);
		}
		return addr_list.toArray(new String[0]);
	}

}
