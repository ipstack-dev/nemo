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

package test.ipstack;

import java.io.IOException;
import java.util.ArrayList;

import org.zoolu.util.Flags;
import org.zoolu.util.log.DefaultLogger;
import org.zoolu.util.log.LoggerLevel;
import org.zoolu.util.log.WriterLogger;

import io.ipstack.net.analyzer.LibpcapHeader;
import io.ipstack.net.analyzer.LibpcapSniffer;
import io.ipstack.net.ip4.Ip4Address;
import io.ipstack.net.ip4.Ip4AddressPrefix;
import io.ipstack.net.ip4.Ip4Layer;
import io.ipstack.net.ip4.Ip4Node;
import io.ipstack.net.ip4.Ip4Packet;
import io.ipstack.net.nat.SDestNAT;
import io.ipstack.net.packet.NetInterface;
import io.ipstack.net.packet.Node;
import io.ipstack.net.tuntap.Ip4TunInterface;
import io.ipstack.net.tuntap.Ip4TuntapInterface;


/** S-D-NAT node, attached to TUN/TAP interfaces.
 * <p>
 * See {@link io.ipstack.net.nat.SDestNAT} for a description of how it works.
 * <p>
 * The NAT table entries must be explicitly set through command-line option <code>-a in-daddr out-saddr out-daddr</code>.
 */
public abstract class TunNAT {

	
	/** The main method. */
	public static void main(String[] args) throws IOException {
		Flags flags= new Flags(args);
		boolean DEBUG= flags.getBoolean("-debug","debug mode");
		boolean VERBOSE= flags.getBoolean("-v","verbose mode");
		boolean help= flags.getBoolean("-h","prints this message");
		//final double err= flags.getDouble("-e","<PER>",0.0,"adds packet error rate, just for testing purpose");
		String pcapFile= flags.getString("-pcap",null,"file","captures packets on the monitor port and writes them to a pcap file");
		LibpcapSniffer sniffer= null;
		ArrayList<NetInterface<Ip4Address,Ip4Packet>> tuntap= new ArrayList<>();
		String[] tuntap_param= flags.getStringTuple("-i",2,null,"<tun> <ipaddr/prefix>","TUN/TAP interface and IPv4 address/prefix length (e.g. '-i tun0 10.1.1.3/24')") ;
		while (tuntap_param!=null) {
			Ip4TuntapInterface ni= new Ip4TuntapInterface(tuntap_param[0],new Ip4AddressPrefix(tuntap_param[1]));
			tuntap.add(ni);
			if (pcapFile!=null && sniffer==null) sniffer= new LibpcapSniffer(ni,LibpcapHeader.LINKTYPE_IPV4,pcapFile);
			tuntap_param= flags.getStringTuple("-i",2,null,null,null);
		}
		if (tuntap.size()==0) {
			System.out.println(TunNAT.class.getSimpleName()+": At least one TUN/TAP interface has to be configured");
			help=true;
		}
		ArrayList<Ip4Address[]> nat_table= new ArrayList<>();
		String[] addr_tuple= flags.getStringTuple("-a",3,null,"<in-daddr> <out-saddr> <out-daddr>","adds a new mapping formed by in-daddr, out-saddr, and out-daddr") ;
		while (addr_tuple!=null) {
			Ip4Address[] ipaddr_tuple= new Ip4Address[3];
			for (int i=0; i<3; i++) ipaddr_tuple[i]= new Ip4Address(addr_tuple[i]);
			nat_table.add(ipaddr_tuple);
			addr_tuple= flags.getStringTuple("-a",3,null,null,null);
		}
		
		if (help) {
			System.out.println(flags.toUsageString(TunNAT.class.getSimpleName()));
			System.exit(0);					
		}	
		if (DEBUG) {
			DefaultLogger.setLogger(new WriterLogger(System.out,LoggerLevel.DEBUG));
			Ip4TunInterface.DEBUG= true;
			Node.DEBUG= true;
			Ip4Node.DEBUG= true;
			Ip4Layer.DEBUG= true;							
			SDestNAT.DEBUG= true;
		}
		if (VERBOSE) {
			DefaultLogger.setLogger(new WriterLogger(System.out,LoggerLevel.DEBUG));
			SDestNAT.DEBUG= true;
		}
		
//		final LibpcapWriter writer= pcapFile!=null? new LibpcapWriter(LibpcapHeader.LINKTYPE_IPV4,pcapFile) : null;
//		SDestNAT nat= new SDestNAT(tuntap) {
//			@Override
//			protected void processReceivedPacket(NetInterface<Ip4Address,Ip4Packet> ni, Ip4Packet ip_pkt) {
//				if (writer!=null) writer.write(ip_pkt);
//				super.processReceivedPacket(ni,ip_pkt);
//			}
//		};
		SDestNAT nat= new SDestNAT(tuntap);
		for (Ip4Address[] ipaddr_tuple : nat_table) {
			nat.add(ipaddr_tuple[0],ipaddr_tuple[1],ipaddr_tuple[2]);
		}
	}
	
}
