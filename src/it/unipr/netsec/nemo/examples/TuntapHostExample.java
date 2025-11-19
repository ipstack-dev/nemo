package it.unipr.netsec.nemo.examples;


import java.io.IOException;

import io.ipstack.net.ip4.Ip4Address;
import io.ipstack.net.ip4.Ip4AddressPrefix;
import io.ipstack.net.ip4.Ip4Packet;
import io.ipstack.net.packet.NetInterface;
import io.ipstack.net.tuntap.Ip4TuntapInterface;
import it.unipr.netsec.nemo.ip.Ip4Host;


/** Simple host attached to a TUN interface.
 *  It can optionally run a HTTP server.
 */
public class TuntapHostExample {

	public static void main(String[] args) throws IOException {
		String tuntap_interface=args[0]; // e.g. tun0
		Ip4AddressPrefix ipaddr_prefix=new Ip4AddressPrefix(args[1]); // e.g. "172.0.18.2/24"
		Ip4Address default_router=new Ip4Address(args[2]); // e.g. "172.0.18.1"
		
		NetInterface<Ip4Address,Ip4Packet> ni=new Ip4TuntapInterface(tuntap_interface,ipaddr_prefix);
		Ip4Host host=new Ip4Host(ni,default_router);
		host.startHttpServer();
	}

}
