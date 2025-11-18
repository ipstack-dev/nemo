package it.unipr.netsec.ipstack.link;


import java.util.ArrayList;
import java.util.List;

import it.unipr.netsec.ipstack.link.Link;
import it.unipr.netsec.ipstack.link.LinkInterface;
import it.unipr.netsec.ipstack.net.Address;
import it.unipr.netsec.ipstack.net.NetInterface;
import it.unipr.netsec.ipstack.net.Packet;


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
