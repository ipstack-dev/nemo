package it.unipr.netsec.nemo.program.tcpdump;


import java.util.ArrayList;

import io.ipstack.net.packet.Packet;
import io.ipstack.net.tcp.TcpPacket;
import io.ipstack.net.udp.UdpPacket;


public class PortMatch implements Match {
	
	public enum Type {
		SRC, DST, ANY
	}

	int port;
	Type type;


	public PortMatch(int port, Type type) {
		this.port=port;
		this.type=type;
	}

	@Override
	public boolean getValue(ArrayList<Packet<?>> pp) {
		int src=-1;
		int dst=-1;
		for (Packet<?> pkt : pp) {
			if (pkt instanceof TcpPacket) {
				TcpPacket tcp_pkt=(TcpPacket)pkt;
				src=tcp_pkt.getSourcePort();
				dst=tcp_pkt.getDestPort();
				break;
			}
			if (pkt instanceof UdpPacket) {
				UdpPacket udp_pkt=(UdpPacket)pkt;
				src=udp_pkt.getSourcePort();
				dst=udp_pkt.getDestPort();
				break;
			}
		}
		if (src>=0 || dst>=0) {
			switch(type) {
				case SRC : if (src==port) return true; else return false;
				case DST : if (dst==port) return true; else return false;
				case ANY : if (src==port || dst==port) return true; else return false;
			}
		}
		return false;
	}

}
