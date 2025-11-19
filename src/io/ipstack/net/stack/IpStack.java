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

import java.net.SocketException;

import org.zoolu.util.ArrayUtils;
import org.zoolu.util.log.DefaultLogger;
import org.zoolu.util.log.LoggerLevel;
import org.zoolu.util.log.WriterLogger;

import io.ipstack.net.ethernet.EthTunnelInterface;
import io.ipstack.net.ip4.Ip4EthInterface;
import io.ipstack.net.ip4.Ip4Layer;
import io.ipstack.net.ip4.Ip4Node;
import io.ipstack.net.ip6.Ip6Address;
import io.ipstack.net.ip6.Ip6EthInterface;
import io.ipstack.net.ip6.Ip6Layer;
import io.ipstack.net.ip6.Ip6Node;
import io.ipstack.net.ip6.Ip6Packet;
import io.ipstack.net.packet.LoopbackInterface;
import io.ipstack.net.packet.NetInterface;
import io.ipstack.net.tcp.TcpConnection;
import io.ipstack.net.tcp.TcpLayer;
import io.ipstack.net.tuntap.Ip4TunInterface;
import io.ipstack.net.udp.UdpLayer;


/** TCP/IP stack.
 * <p>
 * It includes a static default stack that is initialized at boot by reading the IP configuration file specified by the 'ipcfg' property.
 * If the 'ipcfg' property is not set, an IP layer with only lo interface is considered.
 */
public class IpStack {

	/** The IP configuration file property */
	//private static final String CONFIG_FILE_PARAM="ipstack.config"; 
	private static final String CONFIG_FILE_PARAM="ipcfg"; 
	
	/** The IP configuration command property */
	private static final String IP_PARAM="ip"; 

	/** The ipstack verbose property */
	private static final String VERBOSE_PARAM="ipstack.verbose";

	/** Default stack */
	private static IpStack DEFAULT_STACK=null;

	
	/** Initializes the static attributes */
	static {
		try {
			String verbose=System.getProperty(VERBOSE_PARAM);
			if (verbose!=null) {
				System.out.println("ipstack: verbose level: "+verbose);
				int level=Integer.parseInt(verbose);
				if (level>=1) {
					DefaultLogger.setLogger(new WriterLogger(System.out,LoggerLevel.DEBUG));
					UdpLayer.DEBUG=true;
					TcpConnection.DEBUG=true;
				}
				if (level>=2) {
					TcpLayer.DEBUG=true;
					Ip4Layer.DEBUG=true;
					Ip6Layer.DEBUG=true;
				}
				if (level>=3) {
					Ip4Node.DEBUG=true;
					Ip6Node.DEBUG=true;
					Ip4EthInterface.DEBUG=true;
					Ip6EthInterface.DEBUG=true;
					Ip4TunInterface.DEBUG=true;
					EthTunnelInterface.DEBUG=true;
				}
				if (level>=4) {
					io.ipstack.net.rawsocket.Socket.setDebug(true);
					io.ipstack.net.rawsocket.udp.DatagramSocket.DEBUG=true;
				}
			}
			String config_file=System.getProperty(CONFIG_FILE_PARAM);
			Ip4Layer ip4_layer=config_file!=null? new LinuxIp4Layer(new LinuxIp4Configuration(config_file)) : new LinuxIp4Layer(new LinuxIp4Configuration());
			Ip6Layer ip6_layer=new Ip6Layer(ArrayUtils.arraylist((NetInterface<Ip6Address,Ip6Packet>)new LoopbackInterface<Ip6Address,Ip6Packet>(new Ip6Address("::1"))));
						
			DEFAULT_STACK=new IpStack(ip4_layer,ip6_layer);
			// start TELNET server
			//new TelnetServer(DEFAULT_STACK);

			String ip_param=System.getProperty(IP_PARAM);
			if (ip_param!=null) {
				IpCommand ip=new IpCommand(DEFAULT_STACK,System.out);
				for (String args: ip_param.split("[,;]")) {
					args=args.trim();
					ip.command(args.split(" "),0);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	/** The IPv4 layer */
	public Ip4Layer ip4_layer=null; 

	/** The IPv6 layer */
	public Ip6Layer ip6_layer=null; 

	/** The UDP layer */
	public UdpLayer udp_layer=null;

	/** The TCP layer */
	public TcpLayer tcp_layer=null;
	
	/** Whether it has been closed  */
	boolean closed=false;


	
	/** Creates a new IP stack. */
	public IpStack() {
	}

	
	/** Creates a new IP stack.
	 * @param ip4_layer IPv4 layer
	 * @param ip6_layer IPv6 layer */
	public IpStack(Ip4Layer ip4_layer, Ip6Layer ip6_layer) {
		this.ip4_layer=ip4_layer;
		this.ip6_layer=ip6_layer;
	}

	
	/** Gets the IPv4 layer.
	 * @return the IP layer */
	public Ip4Layer getIp4Layer() {
		if (closed) return null;
		if (ip4_layer==null) ip4_layer=new Ip4Layer();
		return ip4_layer;
	}

	
	/** Gets the IPv4 layer.
	 * @return the IP layer */
	public Ip6Layer getIp6Layer() {
		if (closed) return null;
		if (ip6_layer==null) ip6_layer=new Ip6Layer();
		return ip6_layer;
	}

	
	/** Gets the TCP layer.
	 * @return the TCP layer */
	public TcpLayer getTcpLayer() {
		if (closed) return null;
		if (tcp_layer==null)
		try {
			tcp_layer=new TcpLayer(getIp4Layer());
		}
		catch (SocketException e) {
			e.printStackTrace();
		}
		return tcp_layer;
	}
	

	/** Gets the UDP layer.
	 * @return the UDP layer */
	public UdpLayer getUdpLayer() {
		if (closed) return null;
		if (udp_layer==null)
		try {
			udp_layer=new UdpLayer(getIp4Layer());
		}
		catch (SocketException e) {
			e.printStackTrace();
		}
		return udp_layer;
	}
	
	
	/** Gets the default stack.
	 * @return the stack */
	public static IpStack getDefaultInstance() {
		//if (DEFAULT_STACK==null) DEFAULT_STACK=new IpStack();
		return DEFAULT_STACK;
	}
	
	
	/** Closes the stack. */
	public void close() {
		if (!closed) {
			closed=true;
			if (udp_layer!=null) udp_layer.close();
			if (tcp_layer!=null) tcp_layer.close();
			if (ip4_layer!=null) ip4_layer.close();
			if (ip6_layer!=null) ip6_layer.close();
			udp_layer=null;
			tcp_layer=null;
			ip4_layer=null;
			ip6_layer=null;
		}
	}
	
}
