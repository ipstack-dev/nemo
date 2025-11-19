package io.ipstack.net.stack;


import java.util.ArrayList;
import java.util.HashMap;

import io.ipstack.net.ip4.Ip4Address;
import io.ipstack.net.ip4.Ip4Packet;
import io.ipstack.net.link.Link;


/** Keeps the mapping from link names to links.
 */
public class Links {

	private static HashMap<String,Link<Ip4Address,Ip4Packet>> LINKS=new HashMap<>();

	//private static HashMap<Integer,EthTunnelHub> TUNNEL_HUBS=new HashMap<>();
	
	
	public static synchronized void putLink(String name, Link<Ip4Address,Ip4Packet> link) {
		if (!LINKS.containsKey(name)) LINKS.put(name,link);
	}

	public static synchronized Link<Ip4Address,Ip4Packet> getLink(String name) {
		Link<Ip4Address,Ip4Packet> link=LINKS.get(name);
		if (link==null) {
			link=new Link<>();
			LINKS.put(name,link);
		}
		return link;
	}

	public static synchronized String getLinkName(Link<Ip4Address,Ip4Packet> link) {
		for (String name : LINKS.keySet()) {
			if (LINKS.get(name)==link) return name;
		}
		// else
		return null;
	}

	public static synchronized String[] getNames() {
		return LINKS.keySet().toArray(new String[0]);
	}
	
	/** Removes unused links.*/
	public static synchronized void prune() {
		ArrayList<String> toPurge=new ArrayList<>();
		for (String name : LINKS.keySet()) if (LINKS.get(name).numberOfInterfaces()==0) toPurge.add(name); 
		for (String name : toPurge) LINKS.remove(name);
	}

}
