package it.unipr.netsec.nemo.examples;


import java.io.IOException;

import io.ipstack.net.analyzer.LibpcapHeader;
import io.ipstack.net.analyzer.LibpcapSniffer;
import io.ipstack.net.ip4.Ip4Address;
import io.ipstack.net.ip4.Ip4Packet;
import io.ipstack.net.ip4.Ip4Prefix;
import it.unipr.netsec.nemo.ip.Ip4Host;
import it.unipr.netsec.nemo.ip.Ip4Router;
import it.unipr.netsec.nemo.ip.IpLink;
import it.unipr.netsec.nemo.link.PromiscuousDataLinkInterface;


public class LibpcapSnifferExample {

	public static void main(String[] args) throws IOException {
		long bit_rate=1000000; // 1Mb/s
		IpLink<Ip4Address,Ip4Packet> link1=new IpLink<>(bit_rate,new Ip4Prefix("10.1.0.0/16"));
		IpLink<Ip4Address,Ip4Packet> link2=new IpLink<>(bit_rate,new Ip4Prefix("10.2.0.0/16"));
		
		Ip4Router r1=new Ip4Router(link1,link2);
		Ip4Host host1=new Ip4Host(link1);
		Ip4Host host2=new Ip4Host(link2);
		
		// capture all packets sent through link1
		new LibpcapSniffer(new PromiscuousDataLinkInterface<Ip4Address,Ip4Packet>(link1),LibpcapHeader.LINKTYPE_IPV4,"example-trace.pcap");

		host1.ping((Ip4Address)host2.getAddress(),3,System.out);
	}

}
