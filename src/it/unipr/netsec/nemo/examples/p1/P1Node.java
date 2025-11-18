package it.unipr.netsec.nemo.examples.p1;


import java.util.ArrayList;

import it.unipr.netsec.ipstack.net.NetInterface;
import it.unipr.netsec.ipstack.net.Node;
import it.unipr.netsec.nemo.link.DataLink;
import it.unipr.netsec.nemo.link.DataLinkInterface;


/** Node implementing protocol P1.
 * <p>
 * Nodes are interconnected in nxm network.
 * Hence, each node has 4 network interfaces: norther, easter, souther, and western interface.
 * <p>
 * Routing is performed accordingly to this network topology.
 */
public class P1Node extends Node<P1Address,P1Packet> {

	/** Creates a new node.
	 * @param addr the node address
	 * @param north norther link
	 * @param east easter link
	 * @param south souther link
	 * @param west wester link */
	public P1Node(P1Address addr, DataLink north, DataLink east, DataLink south, DataLink west) {
		super(createNetInterfaces(addr,north,east,south,west),null,true);
		setRouting(new P1RoutingFunction(addr,getNetInterfaces()));
	}
	
	@Override
	protected void processReceivedPacket(NetInterface<P1Address,P1Packet> ni, P1Packet pkt) {
		if (hasAddress(pkt.getDestAddress())) System.out.println("Node "+getAddress()+" received packet: "+pkt);
		else processForwardingPacket(pkt);
	}
	
	private static ArrayList<NetInterface<P1Address,P1Packet>> createNetInterfaces(P1Address addr, DataLink<P1Address,P1Packet> north, DataLink<P1Address,P1Packet> east, DataLink<P1Address,P1Packet> south, DataLink<P1Address,P1Packet> west) {
		ArrayList<NetInterface<P1Address,P1Packet>> ni=new ArrayList<>();
		ni.add(new DataLinkInterface<P1Address,P1Packet>(north,addr));
		ni.add(new DataLinkInterface<P1Address,P1Packet>(east,addr));
		ni.add(new DataLinkInterface<P1Address,P1Packet>(south,addr));
		ni.add(new DataLinkInterface<P1Address,P1Packet>(west,addr));
		return ni;
	}

}
