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


import java.net.NetworkInterface;

import org.zoolu.util.Bytes;
import org.zoolu.util.Flags;

import io.ipstack.net.ethernet.EthAddress;
import io.ipstack.net.ethernet.EthPacket;
import io.ipstack.net.ip6.Ip6Address;
import io.ipstack.net.ip6.Ip6Packet;
import io.ipstack.net.ip6.exthdr.SegmentRoutingHeader;
import io.ipstack.net.rawsocket.*;


/** Program for sending SR IPv6 packets.
  */
public abstract class SrSendTo {


	/** The main method. */
	public static void main(String[] args) {
		
		Flags flags=new Flags(args);		
		boolean help=flags.getBoolean("-h","prints this message");
		try {
			String out_intrface=flags.getString(null,null,"<out_interface>","name of the output interface (e.g. eth0)");
			String eth_dst_str=flags.getString(null,null,"<eth_dst>","MAC address of the next-hop node (e.g. the mac address of the default router)");
			String ip6_src_str=flags.getString(null,null,"<ip6_src>","IPv6 source address");
			String ip6_dst_str=flags.getString(null,null,"<ip6_dst>","IPv6 address of the final destination");
			int proto=flags.getInteger(null,-1,"<proto>","protocol number of the IP payload (e.g. TCP=6)");
			String hex_data=flags.getString(null,null,"<hex_data>","IP payload as hexadecimal string");
			String[] segments=flags.getRemainingStrings(true,"<sgm1> <sgm2> .. <sgmN>","list of possible SR intermediate routers (in the same order of the path)");
			
			if (help || hex_data==null) {
				System.out.println(flags.toUsageString(SrSendTo.class.getSimpleName()));
				return;
			}
			
			Socket.setDebug(true);
			EthAddress eth_src=new EthAddress(NetworkInterface.getByName(out_intrface).getHardwareAddress());
			EthAddress eth_dst=new EthAddress(eth_dst_str);
			Ip6Address ip6_src=new Ip6Address(ip6_src_str);
			Ip6Address ip6_dst=new Ip6Address(ip6_dst_str);
			byte[] data=Bytes.fromHex(hex_data);		
			Ip6Packet ip6_pkt;
			
			if (segments.length>0) {
				Ip6Address[] sgms=new Ip6Address[segments.length+1];
				sgms[0]=ip6_dst;
				for (int i=1; i<sgms.length; i++) sgms[i]=new Ip6Address(segments[segments.length-i]);
				ip6_pkt=new Ip6Packet(ip6_src,sgms[sgms.length-1],proto,data);
				ip6_pkt.addExtHdr(new SegmentRoutingHeader(sgms));
			}
			else {
				ip6_pkt=new Ip6Packet(ip6_src,ip6_dst,proto,data);
			}
			
			EthPacket eth_pkt=new EthPacket(eth_src,eth_dst,EthPacket.ETH_IP6,ip6_pkt.getBytes());
			eth_pkt.setOutInterface(out_intrface);
			RawLinkSocket socket=new RawLinkSocket();
			socket.send(eth_pkt);
		}
		catch (Exception e) {
			e.printStackTrace();
			System.out.println(flags.toUsageString(SrSendTo.class.getSimpleName()));
		}
	}
}
