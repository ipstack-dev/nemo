package test.nemo.mngnetwork.info;


import java.util.Collection;

import it.unipr.netsec.nemo.ip.Ip4Node;


public class ConfigNetworkInfo {

	public ConfigNodeInfo[] nodes;

	
	public ConfigNetworkInfo() {
	}

	public ConfigNetworkInfo(ConfigNodeInfo[] nodes) {
		this.nodes=nodes;
	}

	public ConfigNetworkInfo(Collection<Ip4Node> ip_nodes) {
		nodes=new ConfigNodeInfo[ip_nodes.size()];
		int i=0;
		for (Ip4Node ip_node : ip_nodes) nodes[i++]=new ConfigNodeInfo(ip_node);
	}
	
}
