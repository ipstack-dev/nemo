package io.ipstack.net.ethernet;


import java.util.List;

import io.ipstack.net.link.Link;
import io.ipstack.net.link.Repeater;
import io.ipstack.net.packet.NetInterface;


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
