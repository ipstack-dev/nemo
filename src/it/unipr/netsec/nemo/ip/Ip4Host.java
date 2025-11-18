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

package it.unipr.netsec.nemo.ip;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;

import org.zoolu.util.ArrayUtils;
import org.zoolu.util.Bytes;
import org.zoolu.util.Random;

import it.unipr.netsec.ipstack.ip4.Ip4Address;
import it.unipr.netsec.ipstack.ip4.Ip4Packet;
import it.unipr.netsec.ipstack.ip4.Ip4Prefix;
import it.unipr.netsec.ipstack.net.NetInterface;
import it.unipr.netsec.ipstack.tcp.ServerSocket;
import it.unipr.netsec.ipstack.tcp.Socket;
import it.unipr.netsec.ipstack.udp.DatagramSocket;
import it.unipr.netsec.nemo.http.HttpRequestHandle;
import it.unipr.netsec.nemo.http.HttpServer;
import it.unipr.netsec.nemo.http.HttpServerListener;
import it.unipr.netsec.nemo.link.DataLink;


/** IPv4 Host.
 * <p>
 * It is an IP node with a web server (port 80), a UDP echo server (port 7), and a PING client.
 */
public class Ip4Host extends Ip4Node {	
	
	/** Size of the receiver buffer of the echo server */
	int ECHO_BUFFER_SIZE=8000;

	/** HTTP server */
	HttpServer http_server=null;
	
	/** UDP echo server */
	DatagramSocket udp_socket=null;
	
	/** TCP echo server */
	ServerSocket tcp_server;

	
	/** Creates a new host. */
	public Ip4Host() {
		this(null,null);
	}

	/** Creates a new host.
	 * @param ni network interface
	 * @param gw default router */
	public Ip4Host(NetInterface<Ip4Address,Ip4Packet> ni, Ip4Address gw) {
		super(ArrayUtils.arraylist(ni));
		if (gw!=null) getRoutingTable().set(Ip4Prefix.ANY,gw);
	}

	/** Creates a new host.
	 * @param link attached IP link
	 * @param addr the IP address
	 * @param gw default router */
	public Ip4Host(DataLink<Ip4Address,Ip4Packet> link, Ip4Address addr, Ip4Address gw) {
		this(new IpLinkInterface<Ip4Address,Ip4Packet>(link,addr),gw);
	}
		
	/** Creates a new host.
	 * The IP address and default router are automatically configured
	 * @param link attached IP link */
	public Ip4Host(IpLink<Ip4Address,Ip4Packet> link) {
		this(new IpLinkInterface<Ip4Address,Ip4Packet>(link),(link.getRouters().length>0?(Ip4Address)link.getRouters()[0]:null));
	}
	
	/** Starts a UDP echo server. */
	public void startUdpEchoServer() {
		try {
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						udp_socket=new DatagramSocket(getIpStack().getUdpLayer(),7);
						DatagramPacket datagram_packet=new DatagramPacket(new byte[ECHO_BUFFER_SIZE],0);
						while (true) {
							udp_socket.receive(datagram_packet);
							if (DEBUG) debug("UDP ECHO: received data: "+Bytes.toHex(datagram_packet.getData(),datagram_packet.getOffset(),datagram_packet.getLength()));
							//datagram_packet.setPort(udp_socket.getPort());
							if (DEBUG) debug("UDP ECHO: reply to: "+datagram_packet.getAddress().getHostAddress().toString());			
							udp_socket.send(datagram_packet);
						}		
					}
					catch (IOException e) {
						e.printStackTrace();
					}
				}			
			}).start();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/** Starts a UDP echo server. */
	public void startTcpEchoServer() {
		try {
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						tcp_server=new ServerSocket(getIpStack().getTcpLayer(),7);
						while (true) {
							final Socket conn=tcp_server.accept();
							new Thread(new Runnable() {
								@Override
								public void run() {
									try {
										InputStream in=conn.getInputStream();
										OutputStream out=conn.getOutputStream();
										byte[] buffer=new byte[ECHO_BUFFER_SIZE];
										while (true) {
											int len=in.read(buffer);
											out.write(buffer,0,len);
										}
									}
									catch (Exception e) {
										if (conn!=null) try { conn.close(); } catch (IOException e1) {}
									}
								}								
							}).start();
						}							
					}
					catch (IOException e) {
						e.printStackTrace();
						if (tcp_server!=null) try { tcp_server.close(); } catch (IOException e1) {}
					}
				}			
			}).start();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/** Starts a HTTP server. */
	public void startHttpServer() {
		try {
			http_server=new HttpServer(getIpStack().getTcpLayer(),80,new HttpServerListener() {
				@Override
				public void onHttpRequest(HttpRequestHandle req_handle) {
					processHttpRequest(req_handle);
				}			
			});
		}
		catch (IOException e) {
			System.err.println("Error when starting a HTTP server on port 80");
			e.printStackTrace();
		}
	}
	
	/** Processes a HTTP request.
	 * @param req_handle the request handle */
	private void processHttpRequest(HttpRequestHandle req_handle) {
		if (req_handle.getMethod().equals("GET")) {
			String resource=req_handle.getRequestURL().getAbsPath();
			if (resource.equals("/") || resource.equals("/index.html")) {
				String resource_value="<html>\r\n" + 
						"<body>\r\n" + 
						"<h1>Hello, World!</h1>\r\n" +
						"<p>Server running on host: "+getAddress()+"</p>\r\n" +
						"<p>Random value: "+Random.nextHexString(8)+"</p>\r\n" +
						"</body>\r\n" + 
						"</html>";
				req_handle.setResponseContentType("text/html");
				req_handle.setResponseBody(resource_value.getBytes());
				req_handle.setResponseCode(200);							
			}
			else req_handle.setResponseCode(404);
		}
		else req_handle.setResponseCode(405);		
	}
	
	@Override
	public void close() {
		if (http_server!=null) http_server.close();
		try {
			if (udp_socket!=null) udp_socket.close();
			if (tcp_server!=null) tcp_server.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		super.close();
	}

}
