package test;


import java.io.IOException;
import java.util.ArrayList;

import org.zoolu.util.Bytes;
import org.zoolu.util.Flags;

import it.unipr.netsec.ipstack.ethernet.EthAddress;
import it.unipr.netsec.ipstack.ethernet.EthHub;
import it.unipr.netsec.ipstack.ethernet.EthPacket;
import it.unipr.netsec.ipstack.ethernet.EthSwitch;
import it.unipr.netsec.ipstack.ethernet.EthTunnelHub;
import it.unipr.netsec.ipstack.ethernet.EthTunnelInterface;
import it.unipr.netsec.ipstack.ethernet.EthTunnelSwitch;
import it.unipr.netsec.ipstack.ip4.Ip4Address;
import it.unipr.netsec.ipstack.ip4.Ip4AddressPrefix;
import it.unipr.netsec.ipstack.ip4.Ip4EthInterface;
import it.unipr.netsec.ipstack.ip4.SocketAddress;
import it.unipr.netsec.ipstack.link.Link;
import it.unipr.netsec.ipstack.link.LinkInterface;
import it.unipr.netsec.ipstack.net.NetInterface;
import it.unipr.netsec.nemo.ip.Ip4Host;
import it.unipr.netsec.tuntap.TapInterface;


/** Ethernet LAN that connects IPv4 hosts and attached to TAP interface.
 * <p>
 * The IPv4 hosts can be interconnected by means of either:
 * <ul>
 * 	<li>a shared Ethernet link (default);</li>
 * 	<li>an Ethernet switch;</li>
 * 	<li>an Ethernet-over-UDP tunneled virtual hub.</li>
 * </ul>
 * <p>
 * Optionally each host can run a HTTP server and/or a TELNET server (user/pwd: nemo/nemo).
 */
public abstract class LanTest {

	
	public static void main(String[] args) throws IOException {
		Flags flags= new Flags(args);
		int n= flags.getInteger("-n",1,"num","number of attached hosts");
		Ip4AddressPrefix addrPrefix= new Ip4AddressPrefix(flags.getString("-prefix","172.18.0.2/16","address/len","IPv4 address/len of the first host"));
		String dev= flags.getString("-i","tap0","dev","attaches the LAN to the given TAP device");
		boolean virtualHub= flags.getBoolean("-vhub","connects the hosts by means of a virtual hub");
		boolean virtuaSwitch= flags.getBoolean("-vsw","connects the hosts by means of a virtual switch");
		int virtualHubSize= flags.getInteger("-vsize",256,"size","number of external ports of the virtual hub/switch");
		boolean ethSwitch= flags.getBoolean("-sw","connects the hosts by means of an Ethernet switch");
		boolean http= flags.getBoolean("-http","runs a HTTP server on each host");
		boolean telnet= flags.getBoolean("-telnet","runs TELNET server on each host");
		String defaulRouter= flags.getString("-gw",null,"addr","default router");
		boolean help= flags.getBoolean("-h","prints this message");
		
		if (help) {
			System.out.println(flags.toUsageString(LanTest.class));
			return;
		}
		
		System.out.println("running "+n+" hosts connected to an Ethernet network attached to TAP interface");

		NetInterface<EthAddress,EthPacket> tap= new TapInterface(dev,null);
		
		Link<EthAddress,EthPacket> sharedLink= null;
		ArrayList<Link<EthAddress,EthPacket>> hostLinks= null;
		SocketAddress vhubAddress= null;
		
		// LAN
		if (virtuaSwitch) {
			vhubAddress= new SocketAddress("127.0.0.1:"+EthTunnelSwitch.DEFAULT_PORT);
			new EthTunnelSwitch(EthTunnelSwitch.DEFAULT_PORT,virtualHubSize);
			new EthHub(tap,new EthTunnelInterface(vhubAddress,null));
		}
		else
		if (virtualHub) {
			vhubAddress= new SocketAddress("127.0.0.1:"+EthTunnelHub.DEFAULT_PORT);
			new EthTunnelHub(EthTunnelHub.DEFAULT_PORT,virtualHubSize);
			new EthHub(tap,new EthTunnelInterface(vhubAddress,null));
		}
		else
		if (ethSwitch) {
			hostLinks= new ArrayList<>();
			ArrayList<NetInterface<EthAddress,EthPacket>> swPorts= new ArrayList<>();
			swPorts.add(tap);
			for (int i=0; i<n; i++) {
				Link<EthAddress,EthPacket> link= new Link<>();
				hostLinks.add(link);
				swPorts.add(link.createLinkInterface());
			}
			System.out.println("creating an Ethernet switch with "+(n+1)+" ports");			
			new EthSwitch(swPorts);
		}
		else {
			sharedLink= new Link<>();
			new EthHub(tap,sharedLink.createLinkInterface());
		}
		
		// hosts
		long addr= Bytes.toInt32(addrPrefix.getBytes());
		for (int i= 0; i<n; i++) {
			Ip4AddressPrefix hostAddress= new Ip4AddressPrefix(new Ip4Address(Bytes.fromInt32(addr+i)),addrPrefix.getPrefixLength());
			if (i==0) System.out.println("first host: "+hostAddress);
			if (i==n-1) System.out.println("last host: "+hostAddress);
			
			NetInterface<EthAddress,EthPacket> eth0= (virtualHub||virtuaSwitch)? new EthTunnelInterface(vhubAddress,null) : ethSwitch? new LinkInterface<EthAddress,EthPacket>(hostLinks.get(i)) : new LinkInterface<EthAddress,EthPacket>(sharedLink);

			Ip4Host host=new Ip4Host(new Ip4EthInterface(eth0,hostAddress),defaulRouter!=null? new Ip4Address(defaulRouter) : null);
			if (http) host.startHttpServer();		
			if (telnet) host.startTelnetServer(null);
		}
	}

}
