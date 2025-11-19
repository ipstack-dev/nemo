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

import java.io.IOException;

import org.zoolu.util.Flags;
import org.zoolu.util.log.DefaultLogger;
import org.zoolu.util.log.LoggerLevel;
import org.zoolu.util.log.WriterLogger;

import io.ipstack.net.ip4.Ip4Address;
import io.ipstack.net.ip4.Ip4AddressPrefix;
import io.ipstack.net.ip4.Ip4Layer;
import io.ipstack.net.ip4.Ip4Packet;
import io.ipstack.net.ip4.Ip4Prefix;
import io.ipstack.net.packet.NetInterface;
import io.ipstack.net.stack.LossyIpInterface;
import io.ipstack.net.tcp.TcpConnection;
import io.ipstack.net.tcp.TcpConnectionListener;
import io.ipstack.net.tcp.TcpLayer;
import io.ipstack.net.tuntap.Ip4TunInterface;
import io.ipstack.net.tuntap.Ip4TuntapInterface;


/** TCP server running on a IPv4 node attached to a TUN interface.
 * */
public abstract class TcpServer {

	/** Prints a message. */
	private static void println(String str) {
		System.out.println("OUT: "+TcpServer.class.getSimpleName()+": "+str);
	}

	/** The main method. */
	public static void main(String[] args) throws IOException {
		Flags flags=new Flags(args);
		boolean verbose=flags.getBoolean("-v","verbose mode");
		boolean help=flags.getBoolean("-h","prints this message");
		long delay=flags.getLong("-t",0,"<mean-delay>","packet delay [time_nanosecs]");
		double loss=flags.getDouble("-e",0,"<error-rate>","packet error rate");
		String tuntap_interface=flags.getString(null,null,"<tuntap>","TUN/TAP interface (e.g. 'tun0')");
		String ipaddr_prefix=flags.getString(null,null,"<ipaddr/prefix>","IPv4 address and prefix length (e.g. '10.1.1.3/24')");
		int local_port=(int)flags.getLong(null,-1,"<port>","TCP server port");
		String default_router=flags.getString(null,null,"<router>","IPv4 address of the default router");
		
		if (help || local_port<0) {
			System.out.println(flags.toUsageString(TcpServer.class.getSimpleName()));
			System.exit(0);					
		}	
		if (verbose) {
			DefaultLogger.setLogger(new WriterLogger(System.out,LoggerLevel.DEBUG));
			Ip4TunInterface.DEBUG=true;
			LossyIpInterface.DEBUG=true;
			//Node.DEBUG=true;
			//Ip4Layer.DEBUG=true;
			//NewTcpLayer.DEBUG=true;
			TcpConnection.DEBUG=true;
		}
		NetInterface<Ip4Address,Ip4Packet> ni=new Ip4TuntapInterface(tuntap_interface,new Ip4AddressPrefix(ipaddr_prefix));	
		Ip4Layer ip4_layer=new Ip4Layer(new LossyIpInterface<Ip4Address,Ip4Packet>(ni,delay,loss,delay,loss));
		ip4_layer.getRoutingTable().set(Ip4Prefix.ANY,new Ip4Address(default_router));
		TcpLayer tcp=new TcpLayer(ip4_layer);
		final TcpConnectionListener this_conn_listener=new TcpConnectionListener() {
			@Override
			public void onConnected(TcpConnection tcp_conn) {
				println("Connected");
			}
			@Override
			public void onReceivedData(TcpConnection tcp_conn, byte[] buf, int off, int len) {
				processReceivedData(tcp_conn,buf,off,len);
			}
			@Override
			public void onClose(TcpConnection tcp_conn) {
				println("Connection remotely closing");
				tcp_conn.close();
			}
			@Override
			public void onReset(TcpConnection tcp_conn) {
				println("Connection reset");
			}
			@Override
			public void onClosed(TcpConnection tcp_conn) {
				println("Connection closed");
				//System.exit(0);
			}
		};
		println("Listening on port "+local_port);
		
		new TcpConnection(tcp,null,local_port,this_conn_listener).listen();
	}
	
	private static void processReceivedData(TcpConnection tcp_conn, byte[] buf, int off, int len) {
		String str=new String(buf,off,len);
		println("received: "+str);
		//try { Thread.sleep(100); } catch (Exception e) {}
		if (str.startsWith("x")) {
			tcp_conn.close();
			println("Connection locally closing");
			try { Thread.sleep(2000); } catch (Exception e) {}
		}
		else {	
			tcp_conn.send(buf,off,len);
		}
	}
}
