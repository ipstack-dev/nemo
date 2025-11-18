package test.nemo.mngnetwork.info;


import it.unipr.netsec.ipstack.ip4.Ip4Address;
import it.unipr.netsec.ipstack.ip4.Ip4Packet;
import it.unipr.netsec.ipstack.net.NetInterface;
import it.unipr.netsec.ipstack.stack.IpInterfaceUtils;


public class NetInterfaceInfo {	

	String name;
	String info;
	String status;
	String phaddr;
	String[] addresses;
	
	public NetInterfaceInfo() {
	}

	public NetInterfaceInfo(String name, String info, String status, String phaddr, String[] addresses) {
		this.name=name;
		this.info=info;
		this.status=status;
		this.phaddr=phaddr;
		this.addresses=addresses;
	}

	public NetInterfaceInfo(NetInterface<Ip4Address,Ip4Packet> ni) {
		/*String ni_info=ni.getClass().getSimpleName();
		EthAddress eth_addr=null;
		if (ni instanceof LinkInterface) {
			String name=Links.getLinkName(((LinkInterface<Ip4Address,Ip4Packet>)ni).getLink());
			if (name!=null) ni_info+="/link=\""+name+"\"";
		}
		else
		if (ni instanceof Ip4EthInterface) {
			NetInterface<EthAddress,EthPacket> eth=((Ip4EthInterface)ni).getEthInterface();
			eth_addr=eth.getAddress();
			if (eth instanceof EthTunnelInterface) {
				SocketAddress soaddr=((EthTunnelInterface)eth).getRemoteSocketAddress();
				ni_info+="/vhub="+soaddr;						
			}
		}
		ArrayList<String> addr_list=new ArrayList<String>();
		// list all directed-broadcast addresses in order to not display them
		HashSet<Ip4Address> directed_broadcast_addrs=new HashSet<>();
		for (Ip4Address addr : ni.getAddresses()) if (addr instanceof Ip4AddressPrefix) {
			Ip4Prefix prefix=((Ip4AddressPrefix)addr).getPrefix();
			if (prefix.getPrefixLength()<32) directed_broadcast_addrs.add(prefix.getDirectedBroadcastAddress());
		}
		for (Ip4Address addr : ni.getAddresses()) {
			//if (addr.isMulticast()) continue; // skip multicast address
			//if (directed_broadcast_addrs.contains(addr)) continue; // skip directed-broadcast address
			String str=addr instanceof Ip4AddressPrefix? ((Ip4AddressPrefix)addr).toStringWithPrefixLength() : addr.toString();
			addr_list.add(str);
		}*/
		this.name=ni.getName();
		this.info=IpInterfaceUtils.getType(ni);
		this.status="UP";
		this.phaddr=IpInterfaceUtils.getPhAddress(ni);
		this.addresses=IpInterfaceUtils.getAddresses(ni);
	}
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * @param name the name to set */
	public void setName(String name) {
		this.name=name;
	}
	
	/**
	 * @return the info */
	public String getInfo() {
		return info;
	}
	
	/**
	 * @return the status */
	public String getStatus() {
		return status;
	}
	
	/**
	 * @return the phaddr */
	public String getPhaddr() {
		return phaddr;
	}
	
	/**
	 * @return the addresses */
	public String[] getAddresses() {
		return addresses;
	}
	
}
