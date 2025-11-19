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

package test.rawsocket;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

import org.zoolu.util.Flags;

import io.ipstack.net.arp.ArpClient;
import io.ipstack.net.arp.ArpLayer;
import io.ipstack.net.arp.ArpServer;
import io.ipstack.net.ethernet.EthAddress;
import io.ipstack.net.ethernet.EthLayer;
import io.ipstack.net.ip4.Ip4Address;
import io.ipstack.net.rawsocket.ethernet.RawEthInterface;


/** ARP client and (optionally) ARP server.
 * It is bound to a network card and performs IP address to Ethernet address lookup.
 * <p>
 * It uses {@link io.ipstack.net.rawsocket.ethernet.RawEthInterface} for capturing and/sending
 * raw Ethernet packets.
 */
public abstract class ArpLookup {
	
		
	/** The main method. */
	public static void main(String[] args) {		

		Flags flags=new Flags(args);
		boolean help=flags.getBoolean("-h","prints this messsage");
		String eth_name=flags.getString(null,null,"<eth>","Ethernet interface");
		String addr=flags.getString(null,null,"<local-ipaddr>","Local IP address");
		Ip4Address local_ip_addr=addr!=null? new Ip4Address(addr) : null;
		long arp_table_timeout=flags.getLong(null,20000,"<timeout>","ARP table timeout in milliseconds");	
		if (help || eth_name==null) {
			System.out.println(flags.toUsageString(ArpLookup.class.getSimpleName()));
			System.exit(0);
		}
		boolean server_on=local_ip_addr!=null;
		
		try {
			NetworkInterface network_interface=NetworkInterface.getByName(eth_name);
			EthAddress local_eth_addr=new EthAddress(network_interface.getHardwareAddress());
			InetAddress inet_addr=null;
			for (Enumeration<InetAddress> i=network_interface.getInetAddresses(); i.hasMoreElements(); ) {
				inet_addr=i.nextElement();
				System.out.println("DEBUG: inet_addr: "+inet_addr);
				if (inet_addr instanceof java.net.Inet4Address) break;
			}
			if (local_ip_addr==null) local_ip_addr=new Ip4Address(inet_addr.getAddress());
			RawEthInterface eth_ni=new RawEthInterface(eth_name);
							
			System.out.println("DEBUG: local Eth addr: "+local_eth_addr.toString());
			System.out.println("DEBUG: local IP addr: "+local_ip_addr.toString());
			System.out.println("DEBUG: ARP table timeout: "+arp_table_timeout);

			ArpLayer arp_layer=new ArpLayer(new EthLayer(eth_ni));
			ArpClient arp_clinet=new ArpClient(arp_layer,arp_table_timeout,local_ip_addr);
			ArpServer arp_server=server_on? new ArpServer(arp_layer,local_ip_addr) : null;

			boolean stop=false;
			while(!stop) {
				String line=null;
				try { line=new BufferedReader(new InputStreamReader(System.in)).readLine(); } catch (Exception e) {}
				if (line==null || line.equalsIgnoreCase("exit")) {
					stop=true;
					continue;
				}
				// else
				Ip4Address target_ip_addr=new Ip4Address(line);
				EthAddress target_eth_addr=arp_clinet.lookup(target_ip_addr);
				System.out.println("ARP answer: "+target_ip_addr+" is at "+target_eth_addr);
			}

			arp_clinet.close();
			if (arp_server!=null) arp_server.close();
			eth_ni.close();			
		}
		catch (Exception e) {
			e.printStackTrace();
		}	

	}

}
