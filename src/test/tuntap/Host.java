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

package test.tuntap;


import it.unipr.netsec.ipstack.ip4.Ip4Address;
import it.unipr.netsec.ipstack.ip4.Ip4AddressPrefix;
import it.unipr.netsec.ipstack.ip4.Ip4Layer;
import it.unipr.netsec.ipstack.ip4.Ip4Packet;
import it.unipr.netsec.ipstack.ip4.Ip4Prefix;
import it.unipr.netsec.ipstack.net.NetInterface;
import it.unipr.netsec.tuntap.Ip4TunInterface;
import it.unipr.netsec.tuntap.Ip4TuntapInterface;
import it.unipr.netsec.tuntap.TapInterface;

import java.io.IOException;

import org.zoolu.util.Flags;
import org.zoolu.util.LoggerLevel;
import org.zoolu.util.LoggerWriter;
import org.zoolu.util.SystemUtils;


/** Simple IPv4 node attached to a TUN interface.
 * */
public abstract class Host {

	/** The main method. */
	public static void main(String[] args) throws IOException {
		Flags flags=new Flags(args);
		boolean verbose=flags.getBoolean("-v","verbose mode");
		boolean help=flags.getBoolean("-h","prints this message");
		String tuntap_interface=flags.getString(null,null,"<tuntap>","TUN/TAP interface (e.g. 'tun0')");
		String ipaddr_prefix=flags.getString(null,null,"<ipaddr/prefix>","IPv4 address and prefix length (e.g. '10.1.1.3/24')");
		String default_router=flags.getString(null,null,"<router>","IPv4 address of the default router");
		
		if (help /*|| tun_interface==null || ipaddr_prefix==null */|| default_router==null) {
			System.out.println(flags.toUsageString(Host.class.getSimpleName()));
			System.exit(0);					
		}
		if (verbose) {
			SystemUtils.setDefaultLogger(new LoggerWriter(System.out,LoggerLevel.DEBUG));
			Ip4TunInterface.DEBUG=true;
			TapInterface.DEBUG=true;
		}
		NetInterface<Ip4Address,Ip4Packet> ni=new Ip4TuntapInterface(tuntap_interface,new Ip4AddressPrefix(ipaddr_prefix));	
		Ip4Layer ip4_layer=new Ip4Layer(ni);
		ip4_layer.getRoutingTable().set(Ip4Prefix.ANY,new Ip4Address(default_router));
	}
}
