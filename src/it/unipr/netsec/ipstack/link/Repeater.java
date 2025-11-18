package it.unipr.netsec.ipstack.link;


import java.util.Arrays;
import java.util.List;

import it.unipr.netsec.ipstack.net.Address;
import it.unipr.netsec.ipstack.net.NetInterface;
import it.unipr.netsec.ipstack.net.NetInterfaceListener;
import it.unipr.netsec.ipstack.net.Packet;

import static org.zoolu.util.SystemLogger.info;


/** Multiport repeater.
 * Store-and-forward node that relays incoming packets through all attached network interfaces
 * except the interface where the packets is received from.
 */
public class Repeater<A extends Address, P extends Packet<A>> {

	/** Verbose mode */
	public static boolean VERBOSE=true;
	
	/** Network interfaces */
	protected List<NetInterface<A,P>> net_interfaces;
	
	/** Interface listener */
	NetInterfaceListener<A,P> listener=this::processIncomingPacket;

	/** Whether it is running */
	boolean is_running=true;

	
	/** Creates a new repeater.
	 * @param links connected links */
	@SafeVarargs
	public Repeater(Link<A,P>... links) {
		this(LinkInterfaceUtils.<A,P>createInterfaces(links));
	}
	
	
	/** Creates a new repeater.
	 * @param net_interfaces the network interfaces */
	@SafeVarargs
	public Repeater(NetInterface<A,P>... net_interfaces) {
		this(Arrays.asList(net_interfaces));
	}
	
	
	/** Creates a new repeater.
	 * @param net_interfaces the network interfaces */
	public Repeater(List<NetInterface<A,P>> net_interfaces) {
		this.net_interfaces=net_interfaces;
		for (NetInterface<A,P> ni: net_interfaces) ni.addListener(listener);
	}
	
	
	/** Processes an incoming packet.
	 * @param ni input network interface
	 * @param pkt incoming packet */
	protected void processIncomingPacket(NetInterface<A,P> ni, P pkt) {
		if (VERBOSE) info(this.getClass(),"from "+ni+": "+pkt);
		for (NetInterface<A,P> ni2: net_interfaces) if (ni2!=ni) ni2.send(pkt,null);
	}
	
	
	/** Closes this bridge. */
	public synchronized void close() {
		if (is_running) {
			for (NetInterface<A,P> ni: net_interfaces) ni.removeListener(listener);
			is_running=false;
		}
	}
	
}
