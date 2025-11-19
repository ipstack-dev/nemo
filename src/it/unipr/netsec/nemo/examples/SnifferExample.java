package it.unipr.netsec.nemo.examples;


import io.ipstack.net.analyzer.ProtocolAnalyzer;
import io.ipstack.net.ip4.Ip4Address;
import io.ipstack.net.ip4.Ip4Packet;
import io.ipstack.net.ip4.Ip4Prefix;
import io.ipstack.net.link.MultipleNetInterface;
import io.ipstack.net.packet.NetInterface;
import io.ipstack.net.packet.NetInterfaceListener;
import it.unipr.netsec.nemo.ip.Ip4Host;
import it.unipr.netsec.nemo.ip.Ip4Router;
import it.unipr.netsec.nemo.ip.IpLink;


public class SnifferExample {

	public static void main(String[] args) {
		long bit_rate=1000000; // 1Mb/s
		IpLink<Ip4Address,Ip4Packet> link1=new IpLink<>(bit_rate,new Ip4Prefix("10.1.1.0/24"));
		IpLink<Ip4Address,Ip4Packet> link2=new IpLink<>(bit_rate,new Ip4Prefix("10.2.2.0/24"));
		
		Ip4Router r1=new Ip4Router(link1,link2);
		Ip4Host host1=new Ip4Host(link1);				
		Ip4Host host2=new Ip4Host(link2);
		
		// capture all packets sent through link1
		new MultipleNetInterface<Ip4Address,Ip4Packet>(new NetInterfaceListener<Ip4Address,Ip4Packet>(){
			@Override
			public void onIncomingPacket(NetInterface<Ip4Address,Ip4Packet> ni, Ip4Packet pkt) {
				System.out.println("Captured packet: "+ProtocolAnalyzer.exploreInner(pkt));
			}			
		}).addLink(link1);

		host1.ping((Ip4Address)host2.getAddress(),3,System.out);	
	}

}
