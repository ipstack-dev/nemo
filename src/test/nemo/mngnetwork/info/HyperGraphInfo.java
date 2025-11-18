package test.nemo.mngnetwork.info;


import java.util.Collection;

import it.unipr.netsec.nemo.ip.Ip4Node;


public class HyperGraphInfo {

	NodeInfo[] nodes;
	HyperLinkInfo[] links;

	public HyperGraphInfo() {
	}

	public HyperGraphInfo(NodeInfo[] nodes, HyperLinkInfo[] links) {
		this.nodes=nodes;
		this.links=links;
	}

	public HyperGraphInfo(Collection<Ip4Node> ip_nodes, Collection<String> ip_links) {
		nodes=new NodeInfo[ip_nodes.size()];
		int i=0;
		for (Ip4Node ip_node : ip_nodes) nodes[i++]=new NodeInfo(ip_node);
		links=new HyperLinkInfo[ip_links.size()];
		int j=0;
		for (String ip_link : ip_links) links[j++]=new HyperLinkInfo(ip_link);
	}

	/**
	 * @return the nodes */
	public NodeInfo[] getNodes() {
		return nodes;
	}
	
	/**
	 * @return the links */
	public HyperLinkInfo[] getLinks() {
		return links;
	}
	
}
