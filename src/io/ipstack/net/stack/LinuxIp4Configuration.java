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

package io.ipstack.net.stack;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.zoolu.util.log.DefaultLogger;
import org.zoolu.util.log.LoggerLevel;

import io.ipstack.net.ethernet.EthAddress;
import io.ipstack.net.ethernet.EthHub;
import io.ipstack.net.ethernet.EthPacket;
import io.ipstack.net.ethernet.EthTunnelHub;
import io.ipstack.net.ethernet.EthTunnelInterface;
import io.ipstack.net.ip4.Ip4Address;
import io.ipstack.net.ip4.Ip4AddressPrefix;
import io.ipstack.net.ip4.Ip4EthInterface;
import io.ipstack.net.ip4.Ip4Packet;
import io.ipstack.net.ip4.Ip4Prefix;
import io.ipstack.net.ip4.SocketAddress;
import io.ipstack.net.link.Link;
import io.ipstack.net.link.LinkInterface;
import io.ipstack.net.packet.NetInterface;
import io.ipstack.net.packet.Route;
import io.ipstack.net.tuntap.Ip4TunInterface;
import io.ipstack.net.tuntap.TapInterface;


/** IP configuration based on Linux '{@code ip addr add}' and '{@code ip route add}' commands.
 */
public class LinuxIp4Configuration implements NetConfiguration<Ip4Address,Ip4Packet> {

	/** Debug mode */
	public static boolean VERBOSE=true;
	
	/** Prints a debug message. */
	private void debug(String str) {
		DefaultLogger.log(LoggerLevel.INFO,this.getClass(),str);
	}

	
	private static Link<EthAddress,EthPacket> TAP_LINK=null;

	private static HashMap<String,Link<Ip4Address,Ip4Packet>> VIRTUAL_LINKS=new HashMap<>();

	public static SocketAddress DEFAULT_UDPTUNNEL_SOADDR=new SocketAddress("127.0.0.1:"+EthTunnelHub.DEFAULT_PORT);

	
	private HashMap<String,NetInterface<Ip4Address,Ip4Packet>> net_interfaces=new HashMap<>();

	private ArrayList<Route<Ip4Address,Ip4Packet>> routes=new ArrayList<>();

	
	/** Creates a new configuration. */
	public LinuxIp4Configuration() {
	}

	/** Creates a new configuration.
	 * @param file_name name of the configuration file 
	 * @throws IOException */
	public LinuxIp4Configuration(String file_name) throws IOException {
		BufferedReader config=new BufferedReader(new FileReader(file_name));
		String line;
		while ((line=config.readLine())!=null) {
			line=line.trim();
			if (line.length()>0 && !line.startsWith("#")) add(line);
		}
		config.close();
	}

	@Override
	public NetConfiguration<Ip4Address,Ip4Packet> add(String name, NetInterface<Ip4Address,Ip4Packet> ni) {
		if (VERBOSE) debug("add address: "+name+" "+ni);
		net_interfaces.put(name,ni);			
		return this;
	}

	@Override
	public NetConfiguration<Ip4Address,Ip4Packet> add(Route<Ip4Address,Ip4Packet> r) {
		if (VERBOSE) debug("add route: "+r);
		routes.add(r);
		return this;
	}
	
	@Override
	public LinuxIp4Configuration add(String command) throws IOException {
		String[] tokens=command.split("\\s");
		if (tokens.length==6 && tokens[0].equals("ip") && tokens[1].startsWith("addr") && tokens[2].equals("add") && tokens[4].equals("dev")) {
			// ip addr add <ipaddr/prefixlen> dev <interface>
			Ip4AddressPrefix addr_prefix=new Ip4AddressPrefix(tokens[3]);
			String dev=tokens[5];
			int index=0;
			while (index<dev.length() && (dev.charAt(index)<'0' || dev.charAt(index)>'9')) index++;
			String type=index==dev.length()?dev:dev.substring(0,index);
			if (type.equals("eth")) {
				String network=addr_prefix.getPrefix().toString();
				Link<Ip4Address,Ip4Packet> link;
				if (VIRTUAL_LINKS.containsKey(network)) link=VIRTUAL_LINKS.get(network);
				else VIRTUAL_LINKS.put(network,link=new Link<Ip4Address,Ip4Packet>());
				NetInterface<Ip4Address,Ip4Packet> eth=new LinkInterface<Ip4Address,Ip4Packet>(link,addr_prefix);
				add(dev,eth);
			}
			else
			if (type.equals("tun") || type.equals("utun")) {
				Ip4TunInterface tun=new Ip4TunInterface(dev,addr_prefix);
				add(dev,tun);
			}
			else
			if (type.equals("tap")) {
				//Ip4TapInterface tap=new Ip4TapInterface(dev,addr_prefix);
				if (TAP_LINK==null) {
					TAP_LINK=new Link<EthAddress,EthPacket>();
					new EthHub(new TapInterface(dev,null),TAP_LINK.createLinkInterface());	
				}
				LinkInterface<EthAddress,EthPacket> tap_ni=new LinkInterface<>(TAP_LINK,EthAddress.generateAddress(addr_prefix));
				Ip4EthInterface ip4_ni=new Ip4EthInterface(tap_ni,addr_prefix);
				add(dev,ip4_ni);
			}
			else
			if (type.equals("udptunnel")) {
				SocketAddress hub_soaddr=DEFAULT_UDPTUNNEL_SOADDR;
				index=dev.indexOf('/');
				if (index>0) {
					hub_soaddr=new SocketAddress(dev.substring(index+1));
					dev=dev.substring(0,index);
				}
				Ip4EthInterface udptunnel=new Ip4EthInterface(new EthTunnelInterface(hub_soaddr,EthAddress.generateAddress(addr_prefix)),addr_prefix);
				add(dev,udptunnel);
			}
			// else
		}
		else
		if (tokens.length==8 && tokens[0].equals("ip") && tokens[1].startsWith("route") && tokens[2].equals("add") && tokens[4].equals("via") && tokens[6].equals("dev")) {
			// ip route add <netaddr/prefixlen> via <router> dev <interface>
			Ip4Prefix dest=new Ip4Prefix(tokens[3]);
			Ip4Address router=new Ip4Address(tokens[5]);					
			String dev=tokens[7];
			Route<Ip4Address,Ip4Packet> route=new Route<>(dest,router,net_interfaces.get(dev));
			add(route);
		}
		else {
			throw new IOException("Unsupported configuration command: "+command);
		}
		return this;
	}
	
	@Override
	public ArrayList<NetInterface<Ip4Address,Ip4Packet>> getNetInterfaces() {
		return new ArrayList<NetInterface<Ip4Address,Ip4Packet>>(net_interfaces.values());
	}

	@Override
	public ArrayList<Route<Ip4Address,Ip4Packet>> getRoutes() {
		return new ArrayList<Route<Ip4Address,Ip4Packet>>(routes);
	}

}
