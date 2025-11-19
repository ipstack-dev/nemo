package test.nemo.mngnetwork.info;


import io.ipstack.net.ip4.Ip4Address;
import io.ipstack.net.ip4.Ip4Packet;
import io.ipstack.net.packet.NetInterface;
import io.ipstack.net.stack.IpInterfaceUtils;


public class ConfigInterfaceInfo {	

	public String name;
	public String type;
	public String[] addresses;
	
	
	public ConfigInterfaceInfo() {
	}

	public ConfigInterfaceInfo(String name, String link, String[] addresses) {
		this.name=name;
		this.type=link;
		this.addresses=addresses;
	}

	public ConfigInterfaceInfo(NetInterface<Ip4Address,Ip4Packet> ni) {
		this.name=ni.getName();
		this.type=IpInterfaceUtils.getType(ni);
		this.addresses=IpInterfaceUtils.getUnicastAddresses(ni);
	}
	
}
