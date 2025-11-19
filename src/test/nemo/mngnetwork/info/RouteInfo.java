package test.nemo.mngnetwork.info;


import io.ipstack.net.ip4.Ip4Address;
import io.ipstack.net.ip4.Ip4Packet;
import io.ipstack.net.ip4.Ip4Prefix;
import io.ipstack.net.packet.Route;


public class RouteInfo {

	public String dest;
	public String nexthop;
	public String ni;

	
	public RouteInfo() {
	}

	public RouteInfo(String dest, String nexthop, String ni) {
		this.dest=dest;
		this.nexthop=nexthop;
		this.ni=ni;
	}

	public RouteInfo(Route<Ip4Address,Ip4Packet> r) {
		Ip4Address ip_dest=r.getDestNetAddress();
		//if (ip_dest==null) this.dest=Ip4Prefix.ANY.toStringWithPrefixLength();
		if (ip_dest==null) this.dest="default";
		else this.dest=ip_dest instanceof Ip4Prefix?((Ip4Prefix)ip_dest).toStringWithPrefixLength():ip_dest.toString();
		Ip4Address nexhop=r.getNextHop();
		this.nexthop=nexhop!=null?nexhop.toString():"none";
		this.ni=r.getOutputInterface().getName();
	}
	
}
