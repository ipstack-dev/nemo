package test.nemo.mngnetwork.info;


import java.util.ArrayList;
import java.util.Set;

import it.unipr.netsec.ipstack.ip4.Ip4Address;
import it.unipr.netsec.ipstack.ip4.Ip4Layer;
import it.unipr.netsec.ipstack.ip4.Ip4Packet;
import it.unipr.netsec.ipstack.net.NetInterface;
import it.unipr.netsec.ipstack.routing.Route;
import it.unipr.netsec.nemo.ip.Ip4Node;


public class ConfigNodeInfo {

	public String label;
	public ConfigInterfaceInfo[] interfaces;
	public RouteInfo[] rt;
	public boolean forwarding;
	public String[] services;

	
	public ConfigNodeInfo() {
	}

		public ConfigNodeInfo(String label, ConfigInterfaceInfo[] interfaces, RouteInfo[] rt, boolean forwarding) {
		this.label=label;
		this.interfaces=interfaces;
		this.rt=rt;
		this.forwarding=forwarding;
	}

	public ConfigNodeInfo(Ip4Node ip_node) {
		// label
		this.label=ip_node.getName();
		Ip4Layer ip_layer=ip_node.getIpStack().getIp4Layer();
		// interfaces
		ArrayList<ConfigInterfaceInfo> interface_list=new ArrayList<>();		
		for (NetInterface<Ip4Address,Ip4Packet> ni: ip_layer.getNetInterfaces()) interface_list.add(new ConfigInterfaceInfo(ni));
		interfaces=interface_list.toArray(new ConfigInterfaceInfo[0]);
		// rt
		ArrayList<RouteInfo> route_list=new ArrayList<>();
		for (Route<Ip4Address,Ip4Packet> route: ip_layer.getRoutingTable().getAll()) route_list.add(new RouteInfo(route));
		rt=route_list.toArray(new RouteInfo[0]);
		// forwarding
		forwarding=ip_layer.getIpNode().getForwarding();
		// services
		Set<Integer> tcp_ports=ip_node.getIpStack().getTcpLayer().getServerListeners().keySet();
		ArrayList<String> serv_list=new ArrayList<>();
		if (tcp_ports.contains(23)) serv_list.add("telnet");
		if (tcp_ports.contains(80)) serv_list.add("http");
		services=serv_list.toArray(new String[0]);
	}
	
}
