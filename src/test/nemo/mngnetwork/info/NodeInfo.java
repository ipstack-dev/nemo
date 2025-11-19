package test.nemo.mngnetwork.info;


import java.util.ArrayList;

import io.ipstack.net.ethernet.EthAddress;
import io.ipstack.net.ethernet.EthPacket;
import io.ipstack.net.ethernet.EthTunnelInterface;
import io.ipstack.net.ip4.Ip4Address;
import io.ipstack.net.ip4.Ip4EthInterface;
import io.ipstack.net.ip4.Ip4Packet;
import io.ipstack.net.ip4.SocketAddress;
import io.ipstack.net.link.LinkInterface;
import io.ipstack.net.packet.NetInterface;
import io.ipstack.net.stack.Links;
import it.unipr.netsec.nemo.ip.Ip4Node;
import test.nemo.mngnetwork.UniqueID;


public class NodeInfo {

	String id;
	String label;
	String[] links; // link IDs

	
	public NodeInfo() {
	}

	
	public NodeInfo(String id, String label, String[] links) {
		this.id=id;
		this.label=label;
		this.links=links;
	}

	
	public NodeInfo(Ip4Node ip_node) {
		this.id=UniqueID.getId(ip_node.getIpStack());
		this.label=ip_node.getName();
		ArrayList<String> link_ids=new ArrayList<>();
		for (NetInterface<Ip4Address,Ip4Packet> ni: ip_node.getIpStack().getIp4Layer().getNetInterfaces()) {
			if (ni instanceof LinkInterface) {
				String link_name=Links.getLinkName(((LinkInterface<Ip4Address,Ip4Packet>)ni).getLink());
				if (link_name!=null) link_ids.add(UniqueID.getId(link_name));
			}
			else
			if (ni instanceof Ip4EthInterface) {
				NetInterface<EthAddress,EthPacket> eth=((Ip4EthInterface)ni).getEthInterface();
				if (eth instanceof EthTunnelInterface) {
					SocketAddress soaddr=((EthTunnelInterface)eth).getRemoteSocketAddress();
					link_ids.add(UniqueID.getId(soaddr.toString()));						
				}
			}		
		}
		this.links=link_ids.toArray(new String[0]);
	}

	/**
	 * @return the id */
	public String getId() {
		return id;
	}
	
	/**
	 * @return the label */
	public String getLabel() {
		return label;
	}
	
	/**
	 * @return the links the node is attached to */
	public String[] getLinks() {
		return links;
	}
	
}
