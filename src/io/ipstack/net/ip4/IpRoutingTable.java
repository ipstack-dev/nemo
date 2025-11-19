package io.ipstack.net.ip4;


import io.ipstack.net.packet.Packet;
import io.ipstack.net.packet.Route;
import io.ipstack.net.packet.RoutingTable;


/** IP routing-table with longest-prefix matching.
 */
public class IpRoutingTable<A extends IpAddress, P extends Packet<A>> extends RoutingTable<A,P> {

	
	/** Creates a new routing table. */
	public IpRoutingTable() {
	}
	
	
	@Override
	public Route<A,P> getRoute(A dest_addr) {
		Route<A,P> result=null;
		for (Route<A,P> route : rt) {
			A route_dest_addr=route.getDestNetAddress();
			if (route_dest_addr instanceof IpPrefix) {
				IpPrefix route_dest_prefix=(IpPrefix)route_dest_addr;
				if (route_dest_prefix.contains(dest_addr)) {
					if (result==null || ((IpPrefix)result.getDestNetAddress()).getPrefixLength()<route_dest_prefix.getPrefixLength()) result=route;
				}
			}
			else {
				if (route_dest_addr.equals(dest_addr)) return route;
			}
		}
		return result;
	}

}
