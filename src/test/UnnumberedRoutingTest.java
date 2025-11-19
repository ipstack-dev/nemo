/*
 * Copyright 2018 NetSec Lab - University of Parma
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Author(s):
 * Luca Veltri (luca.veltri@unipr.it)
 */

package test;

import it.unipr.netsec.nemo.ip.Ip4Router;
import it.unipr.netsec.nemo.ip.IpLink;
import it.unipr.netsec.nemo.link.DataLink;
import it.unipr.netsec.nemo.link.DataLinkInterface;

import org.zoolu.util.Flags;
import org.zoolu.util.log.DefaultLogger;
import org.zoolu.util.log.LoggerLevel;
import org.zoolu.util.log.WriterLogger;

import io.ipstack.net.icmp4.PingClient;
import io.ipstack.net.ip4.Ip4Address;
import io.ipstack.net.ip4.Ip4AddressPrefix;
import io.ipstack.net.ip4.Ip4EthInterface;
import io.ipstack.net.ip4.Ip4Layer;
import io.ipstack.net.ip4.Ip4Packet;
import io.ipstack.net.ip4.Ip4Prefix;
import io.ipstack.net.ip4.IpPrefix;
import io.ipstack.net.packet.NetInterface;
import io.ipstack.net.packet.Node;
import io.ipstack.net.packet.Route;
import io.ipstack.net.packet.RoutingTable;
import io.ipstack.net.udp.UdpLayer;
import io.ipstack.net.util.IpAddressUtils;


/** Routing in IPv4 network with linear topology and unnumbered router interfaces.
 * Core routers are interconnected through point-to-point links and no IP address is assigned to those link interfaces.
 */
public class UnnumberedRoutingTest {

	/** DataLink bit rate */
	//static long LINK_BIT_RATE=1000000L;
	static long LINK_BIT_RATE=50000L;
	/** IPv4 prefix used for configuring router loopback addresses */
	static IpPrefix LOOPBACK_PREFIX4=new Ip4Prefix("172.31.0.0",16);

	
	public static void testLinearNetwork(int n) {
		System.out.println("Linear network topology with "+(n+1)+" links and "+n+" routers");
		System.out.println("DataLink bit-rate: "+(LINK_BIT_RATE>=1000000? ""+(LINK_BIT_RATE/1000000D)+" Mb/s" : ""+(LINK_BIT_RATE/1000D)+" kb/s")+"\n");
		try {
			// create all links
			IpLink<Ip4Address,Ip4Packet>[] links=new IpLink[n+1];
			// solution A: all links have different prefixes
			//for (int i=0; i<n+1; i++) links[i]=new IpLink(LINK_BIT_RATE,new Ip4Prefix("10."+i+".0.0/16"));
			// solution B: core links have the same prefix
			// core links:
			for (int i=1; i<n; i++) links[i]=new IpLink(LINK_BIT_RATE,new Ip4Prefix("192.168.3.252/30"));
			// access links:
			links[0]=new IpLink(LINK_BIT_RATE,new Ip4Prefix("10.0.0.0/16"));
			links[n]=new IpLink(LINK_BIT_RATE,new Ip4Prefix("10."+n+".0.0/16"));
			
			// create all routers
			Ip4Router[] routers=new Ip4Router[n];	
			for (int i=0; i<n; i++) {
				Ip4Address loopback_addr=(Ip4Address)IpAddressUtils.addressPrefix(LOOPBACK_PREFIX4,i+1);
				routers[i]=new Ip4Router(loopback_addr,links[i],links[i+1]);
				// add loopback address to all interfaces
				for (NetInterface ni: routers[i].getNetInterfaces()) ni.addAddress(loopback_addr);
			}
			// configure routing tables
			for (int i=0; i<n; i++) {
				System.out.println("Router #"+i+": "+routers[i]+", ni="+routers[i].getNetInterfaces().toString()+", RT:");
				RoutingTable rt=routers[i].getRoutingTable();
				// solution 1: nexthop router is addressed through the address associated to the link interface
				rt.add(new Route(links[0].getPrefix(),i>0? routers[i-1].getNetInterfaces().get(1).getAddress() : null,routers[i].getNetInterfaces().get(0)));
				rt.add(new Route(links[n].getPrefix(),i<(n-1)? routers[i+1].getNetInterfaces().get(0).getAddress() : null,routers[i].getNetInterfaces().get(1)));
				// solution 2: nexthop router is addressed through the unique loopback address associated to it
				//rt.add(new Route(links[0].getPrefix(),i>0? routers[i-1].getLoopbackAddress() : null,routers[i].getNetInterfaces()[0]));
				//rt.add(new Route(links[n].getPrefix(),i<(n-1)? routers[i+1].getLoopbackAddress() : null,routers[i].getNetInterfaces()[1]));
				System.out.println(rt.toString());
			}	
			
			// create host H2
			Ip4AddressPrefix h2=(Ip4AddressPrefix)links[n].nextAddressPrefix();
			NetInterface h2_eth0=new DataLinkInterface(links[n],h2);
			final Ip4Layer host2=new Ip4Layer(new NetInterface[]{h2_eth0});		
			host2.getRoutingTable().add(new Route(Ip4Prefix.ANY,new Ip4Address("10."+n+".0.1"),h2_eth0));
			System.out.println("H2 running at "+h2);

			// create host H1
			Ip4AddressPrefix h1=(Ip4AddressPrefix)links[0].nextAddressPrefix();
			NetInterface h1_eth0=new DataLinkInterface(links[0],h1);
			Ip4Layer host1=new Ip4Layer(new NetInterface[]{h1_eth0});
			host1.getRoutingTable().add(new Route(Ip4Prefix.ANY,new Ip4Address("10.0.0.1"),h1_eth0));
			PingClient ping_client=new PingClient(host1,System.out);
			ping_client.ping(0,"0123456789".getBytes(),h2,1,1000);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	/** Main method. */
	public static void main(String[] args) {
		Flags flags=new Flags(args);
		boolean help=flags.getBoolean("-h","prints this message");
		boolean verbose=flags.getBoolean("-v","verbode mode");
		int n=flags.getInteger("-n",3,"<num>","nxn is the number of nodes in case of Manhattan topology");
		LINK_BIT_RATE=flags.getLong("-b",LINK_BIT_RATE,"<bit-rate>","link bit rate [b/s]");
		
		if (help) {
			System.out.println(flags.toUsageString(UnnumberedRoutingTest.class));
			return;
		}
		verbose=true;

		if (verbose) {
			DefaultLogger.setLogger(new WriterLogger(System.out,LoggerLevel.DEBUG));
			DataLink.DEBUG=true;
			//DataLinkInterface.DEBUG=true;
			//Ip4Link.DEBUG=true;
			Ip4EthInterface.DEBUG=true;
			Node.DEBUG=true;
			Ip4Layer.DEBUG=true;
			UdpLayer.DEBUG=true;			
		}
		//Clock.setDefaultClock(new VirtualClock());
		
		testLinearNetwork(n);
	}

}
