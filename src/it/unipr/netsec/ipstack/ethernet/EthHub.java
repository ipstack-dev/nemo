package it.unipr.netsec.ipstack.ethernet;


import java.util.List;

import it.unipr.netsec.ipstack.link.Link;
import it.unipr.netsec.ipstack.link.Repeater;
import it.unipr.netsec.ipstack.net.NetInterface;


/* Ethernet hub.
 */
public class EthHub extends Repeater<EthAddress, EthPacket> {

	/** Creates a new hub.
	 * @param links connected links */
	@SafeVarargs
	public EthHub(Link<EthAddress,EthPacket>... links) {
		super(links);
	}
	
	
	/** Creates a new hub.
	 * @param net_interfaces the network interfaces */
	@SafeVarargs
	public EthHub(NetInterface<EthAddress,EthPacket>... net_interfaces) {
		super(net_interfaces);
	}
	
	
	/** Creates a new hub.
	 * @param net_interfaces the network interfaces */
	public EthHub(List<NetInterface<EthAddress,EthPacket>> net_interfaces) {
		super(net_interfaces);
	}

}
