package io.ipstack.net.ethernet;


import java.util.List;

import io.ipstack.net.ethernet.EthAddress;
import io.ipstack.net.ethernet.EthPacket;
import io.ipstack.net.link.Bridge;
import io.ipstack.net.link.Link;
import io.ipstack.net.packet.NetInterface;


/** Backward learning Ethernet switch.
 *  It uses backward learning for maintaining a map between node addresses and network segments attached to the external ports.
 */
public class EthSwitch extends Bridge<EthAddress,EthPacket> {
	
	/** Creates a new switch.
	 * @param links the links the switch is attached to */
	@SafeVarargs
	public EthSwitch(Link<EthAddress,EthPacket>... links) {
		super(links);
	}

	
	/** creates a new switch.
	 * @param net_interfaces the switch network interfaces */
	@SafeVarargs
	public EthSwitch(NetInterface<EthAddress,EthPacket>... net_interfaces) {
		super(net_interfaces);
	}

	
	/** creates a new switch.
	 * @param net_interfaces the switch network interfaces */
	public EthSwitch(List<NetInterface<EthAddress,EthPacket>> net_interfaces) {
		super(net_interfaces);
	}
	
	
	@Override
	protected void processIncomingPacket(NetInterface<EthAddress,EthPacket> ni, EthPacket pkt) {
		if (pkt.getDestAddress().isMulticast()) broadcast(ni,pkt);
		else super.processIncomingPacket(ni,pkt);
	}

}
