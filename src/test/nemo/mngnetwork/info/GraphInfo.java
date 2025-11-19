package test.nemo.mngnetwork.info;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import io.ipstack.net.ethernet.EthAddress;
import io.ipstack.net.ethernet.EthPacket;
import io.ipstack.net.ethernet.EthTunnelInterface;
import io.ipstack.net.ip4.Ip4Address;
import io.ipstack.net.ip4.Ip4EthInterface;
import io.ipstack.net.ip4.Ip4Layer;
import io.ipstack.net.ip4.Ip4Packet;
import io.ipstack.net.link.LinkInterface;
import io.ipstack.net.packet.NetInterface;
import io.ipstack.net.stack.Links;
import it.unipr.netsec.nemo.ip.Ip4Node;
import test.nemo.mngnetwork.UniqueID;


public class GraphInfo {

	private static abstract class NodeType {
		public static final String TERMINAL="host";
		public static final String RELAY="router";
		public static final String LINK="link";	
	}
	
	GraphNodeInfo[] nodes;
	GraphEdgeInfo[] edges;

	public GraphInfo(Collection<Ip4Node> ip_nodes, Collection<String> links) {
		ArrayList<GraphNodeInfo> graph_nodes=new ArrayList<>();
		ArrayList<GraphEdgeInfo> graph_edges=new ArrayList<>();
		HashMap<String,GraphNodeInfo> map=new HashMap<>(); // maps link name to id
		for (Ip4Node ip_node : ip_nodes) {
			Ip4Layer ip_layer=ip_node.getIpStack().getIp4Layer();
			String node_id=UniqueID.getId(ip_node);
			GraphNodeInfo graph_node=new GraphNodeInfo(node_id,ip_layer.getIpNode().getForwarding()?NodeType.RELAY:NodeType.TERMINAL,ip_node.getName());
			graph_nodes.add(graph_node);
			for (NetInterface<Ip4Address,Ip4Packet> ni: ip_layer.getNetInterfaces()) {
				Ip4Address ipaddr=ni.getAddress();
				String link_name=null;
				if (ni instanceof LinkInterface) {
					link_name=Links.getLinkName(((LinkInterface<Ip4Address,Ip4Packet>)ni).getLink());
				}
				else
				if (ni instanceof Ip4EthInterface) {
					NetInterface<EthAddress,EthPacket> eth=((Ip4EthInterface)ni).getEthInterface();
					if (eth instanceof EthTunnelInterface) {
						link_name=((EthTunnelInterface)eth).getRemoteSocketAddress().toString();						
					}
				}
				if (link_name!=null) {
					if (!map.containsKey(link_name)) {
						String link_id=UniqueID.getId(link_name);
						GraphNodeInfo graph_node2=new GraphNodeInfo(link_id,NodeType.LINK,link_name);
						graph_nodes.add(graph_node2);
						map.put(link_name,graph_node2);
					}
					graph_edges.add(new GraphEdgeInfo(graph_node.getId(),map.get(link_name).getId(),ipaddr!=null?ipaddr.toString():null));
				}
			}
		}
		// add also links that are not connected to any node
		for (String li : links) {
			String id=UniqueID.getId(li);
			if (!map.containsKey(li)) graph_nodes.add(new GraphNodeInfo(id,NodeType.LINK,li));
		}
		nodes=graph_nodes.toArray(new GraphNodeInfo[0]);
		edges=graph_edges.toArray(new GraphEdgeInfo[0]);
	}

}
