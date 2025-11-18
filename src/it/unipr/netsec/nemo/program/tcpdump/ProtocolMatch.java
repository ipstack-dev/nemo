package it.unipr.netsec.nemo.program.tcpdump;


import java.util.ArrayList;

import it.unipr.netsec.ipstack.arp.ArpPacket;
import it.unipr.netsec.ipstack.ethernet.EthPacket;
import it.unipr.netsec.ipstack.icmp4.IcmpMessage;
import it.unipr.netsec.ipstack.icmp6.Icmp6Message;
import it.unipr.netsec.ipstack.ip4.Ip4Packet;
import it.unipr.netsec.ipstack.ip6.Ip6Packet;
import it.unipr.netsec.ipstack.net.Packet;
import it.unipr.netsec.ipstack.tcp.TcpPacket;
import it.unipr.netsec.ipstack.udp.UdpPacket;


public class ProtocolMatch implements Match {

	public enum Type {
		ETH, ARP, IP4, IP6, ICMP, ICMP6, TCP, UDP
	}
	
	Type proto;
	
	public ProtocolMatch(Type proto) {
		this.proto=proto;
	}

	public ProtocolMatch(String proto) {
		if (proto.equalsIgnoreCase("eth")) this.proto=Type.ETH;
		else
		if (proto.equalsIgnoreCase("arp")) this.proto=Type.ARP;
		else
		if (proto.equalsIgnoreCase("ip4")) this.proto=Type.IP4;
		else
		if (proto.equalsIgnoreCase("icmp")) this.proto=Type.ICMP;
		else
		if (proto.equalsIgnoreCase("icmp6")) this.proto=Type.ICMP6;
		else
		if (proto.equalsIgnoreCase("tcp")) this.proto=Type.TCP;
		else
		if (proto.equalsIgnoreCase("udp")) this.proto=Type.UDP;
	}			

	@Override
	public boolean getValue(ArrayList<Packet<?>> pp) {
		for (Packet<?> pkt : pp) {
			if (proto==Type.ETH && pkt instanceof EthPacket) return true;
			if (proto==Type.ARP && pkt instanceof ArpPacket) return true;
			if (proto==Type.IP4 && pkt instanceof Ip4Packet) return true;
			if (proto==Type.IP6 && pkt instanceof Ip6Packet) return true;
			if (proto==Type.ICMP && pkt instanceof IcmpMessage) return true;
			if (proto==Type.ICMP6 && pkt instanceof Icmp6Message) return true;
			if (proto==Type.TCP && pkt instanceof TcpPacket) return true;
			if (proto==Type.UDP && pkt instanceof UdpPacket) return true;
		}
		return false;
	}

}
