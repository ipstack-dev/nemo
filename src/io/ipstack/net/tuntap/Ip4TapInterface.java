package io.ipstack.net.tuntap;


import java.io.IOException;

import io.ipstack.net.ethernet.EthAddress;
import io.ipstack.net.ethernet.EthLayer;
import io.ipstack.net.ip4.Ip4AddressPrefix;
import io.ipstack.net.ip4.Ip4EthInterface;


/** TAP interface for sending or receiving Ethernet packets.
 */
public class Ip4TapInterface extends Ip4EthInterface {

	
	/** Creates a new interface.
	 * @param name name of the interface (e.g. "tap0"); if <i>null</i>, a new interface is added
	 * @param ip_addr_prefix the IP address and prefix length 
	 * @throws IOException */
	public Ip4TapInterface(String name, Ip4AddressPrefix ip_addr_prefix) throws IOException {
		this(name,null,null,ip_addr_prefix);
	}

	/** Creates a new interface.
	 * @param name name of the interface (e.g. "tap0"); if <i>null</i>, a new interface is added
	 * @param dev_file device file (if any) or <i>null</i>
	 * @param ip_addr_prefix the IP address and prefix length 
	 * @throws IOException */
	public Ip4TapInterface(String name, String dev_file, Ip4AddressPrefix ip_addr_prefix) throws IOException {
		this(name,dev_file,null,ip_addr_prefix);
	}

	/** Creates a new interface.
	 * @param name name of the interface (e.g. "tap0"); if <i>null</i>, a new interface is added
	 * @param dev_file device file (if any) or <i>null</i>
	 * @param eth_addr Ethernet address
	 * @param ip_addr_prefix the IP address and prefix length 
	 * @throws IOException */
	public Ip4TapInterface(String name, String dev_file, EthAddress eth_addr, Ip4AddressPrefix ip_addr_prefix) throws IOException {
		super(new EthLayer(new TapInterface(name,dev_file,eth_addr)),ip_addr_prefix);
	}

}
