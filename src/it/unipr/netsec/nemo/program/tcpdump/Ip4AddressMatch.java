package it.unipr.netsec.nemo.program.tcpdump;


import java.util.ArrayList;

import it.unipr.netsec.ipstack.ip4.Ip4Address;
import it.unipr.netsec.ipstack.ip4.Ip4Packet;
import it.unipr.netsec.ipstack.net.Packet;


public class Ip4AddressMatch implements Match {
	
	public enum Type {
		SRC, DST, ANY
	}

	Ip4Address addr;
	Type type;


	public Ip4AddressMatch(Ip4Address addr, Type type) {
		this.addr=addr;
		this.type=type;
	}

	@Override
	public boolean getValue(ArrayList<Packet<?>> pp) {
		Ip4Address src=null;
		Ip4Address dst=null;
		for (Packet<?> pkt : pp) {
			if (pkt instanceof Ip4Packet) {
				Ip4Packet ip_pkt=(Ip4Packet)pkt;
				src=ip_pkt.getSourceAddress();
				dst=ip_pkt.getDestAddress();
				break;
			}
		}
		if (src!=null || dst!=null) {
			switch(type) {
				case SRC : if (addr.equals(src)) return true; else return false;
				case DST : if (addr.equals(dst)) return true; else return false;
				case ANY : if (addr.equals(src) || addr.equals(dst)) return true; else return false;
			}
		}
		return false;
	}

}
