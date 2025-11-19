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


import java.io.IOException;

import org.zoolu.util.Flags;

import io.ipstack.net.analyzer.LibpcapHeader;
import io.ipstack.net.analyzer.LibpcapWriter;
import io.ipstack.net.analyzer.ProtocolAnalyzer;
import io.ipstack.net.ethernet.EthPacket;
import io.ipstack.net.rawsocket.RawLinkSocket;


/** It analyzes all packets captured at data-link level.
 * <p> 
 * It uses {@link io.ipstack.net.rawsocket.RawLinkSocket}, that in turn uses a PF_PACKET SOCK_RAW socket.
 * Since PF_PACKET SOCK_RAW sockets are not supported neither in Windows OS neither nor in Mac OS,
 * TcpDump can be run only on Linux OS.
 */
public abstract class TcpDump {
	
	/** Maximum receiver buffer size */
	public static int RECV_BUFF_SIZE=65535;
	
	
	/** The main method. 
	 * @throws IOException */
	public static void main(String[] args) throws IOException {
		
		Flags flags=new Flags(args);
		boolean help=flags.getBoolean("-h","prints this message");
		//boolean verbose=flags.getBoolean("-v","runs in verbose mode");
		int count=flags.getInteger("-c",-1,"<num>","captures the given number of packets and exits");
		boolean no_ssh=flags.getBoolean("-nossh","suppresses output for ssh packets (TCP port 22)");
		String out_file=flags.getString("-out",null,"<file>","writes the trace to the given file");
				
		if (help || flags.size()>0) {
			System.out.println(flags.toUsageString(TcpDump.class.getSimpleName()));
			return;			
		}
		/*if (verbose) {
			System.out.println("Network interfaces:");
			for (Enumeration<NetworkInterface> i=NetworkInterface.getNetworkInterfaces(); i.hasMoreElements(); ) {
				NetworkInterface ni=i.nextElement();
				System.out.println(" - "+ni.getName()+" ("+ni.getDisplayName()+")");
			}			
		}*/
		LibpcapWriter trace=out_file!=null? new LibpcapWriter(LibpcapHeader.LINKTYPE_ETHERNET,out_file) : null; 
		RawLinkSocket raw_socket=new RawLinkSocket();
		byte[] buf=new byte[RECV_BUFF_SIZE];
		while (count!=0) {
			int len=raw_socket.recv(buf,0,0);
			EthPacket pkt=EthPacket.parseEthPacket(buf,0,len);
			String dump=ProtocolAnalyzer.packetDump(pkt);
			if (!no_ssh || dump.indexOf(":22 ")<0) {
				System.out.println(dump);
				if (trace!=null) trace.write(pkt);
				if (count>0) count--;
			}
		}	
	}
}
