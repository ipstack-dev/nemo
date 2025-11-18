package it.unipr.netsec.nemo.examples.p1;


import java.util.ArrayList;
import java.util.List;

import it.unipr.netsec.ipstack.net.Address;
import it.unipr.netsec.ipstack.net.NetInterface;
import it.unipr.netsec.ipstack.routing.Route;
import it.unipr.netsec.ipstack.routing.RoutingFunction;


/** Routing function in a nxm network.
 */
class P1RoutingFunction implements RoutingFunction<P1Address,P1Packet> {
	
	int i;
	int j;
	ArrayList<NetInterface<P1Address,P1Packet>> ni; // north, east, south, west
	
	/** Creates a new routing function.
	 * @param addr the node address
	 * @param ni the network interfaces */
	public P1RoutingFunction(P1Address addr, List<NetInterface<P1Address,P1Packet>> ni) {
		this.i=addr.getI();
		this.j=addr.getJ();
		this.ni=new ArrayList<>(ni);
	}
	
	@Override
	public Route<P1Address,P1Packet> getRoute(P1Address dest_addr) {
		P1Address d=(P1Address)dest_addr;
		int di=d.getI();
		int dj=d.getJ();
		if (i<di) return new Route<P1Address,P1Packet>(null,new P1Address(i+1,j),ni.get(2)); // -> south
			else if (i>di) return new Route<P1Address,P1Packet>(null,new P1Address(i-1,j),ni.get(0)); // -> north
				else if (j<dj) return new Route<P1Address,P1Packet>(null,new P1Address(i,j+1),ni.get(1)); // -> east
					else if (j>dj) return new Route<P1Address,P1Packet>(null,new P1Address(i,j-1),ni.get(3)); // -> west
						else return null;
	}
}

