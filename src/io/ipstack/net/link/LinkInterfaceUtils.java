package io.ipstack.net.link;


import java.util.ArrayList;
import java.util.List;

import io.ipstack.net.link.Link;
import io.ipstack.net.link.LinkInterface;
import io.ipstack.net.packet.Address;
import io.ipstack.net.packet.NetInterface;
import io.ipstack.net.packet.Packet;


/** Collects some static methods for dealing with {@link LinkInterface link interfaces}.
 */
public abstract class LinkInterfaceUtils {

	/** Creates a list of network interface from an array of links.
	 * @param links array of links
	 * @return a list of network interfaces */
	public static <A extends Address, P extends Packet<A>> ArrayList<NetInterface<A,P>> createInterfaces(Link<A,P>[] links) {
		ArrayList<NetInterface<A,P>> net_interfaces=new ArrayList<>();
		for (Link<A,P> link : links) net_interfaces.add(link.createLinkInterface());
		return net_interfaces;
	}

	
	/** Creates a list of network interface from a list of links.
	 * @param links list of links
	 * @return a list of network interfaces */
	public static <A extends Address, P extends Packet<A>> ArrayList<NetInterface<A,P>> createInterfaces(List<Link<A,P>> links) {
		ArrayList<NetInterface<A,P>> net_interfaces=new ArrayList<>();
		for (Link<A,P> link : links) net_interfaces.add(link.createLinkInterface());
		return net_interfaces;
	}

}
