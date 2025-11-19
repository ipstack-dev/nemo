package it.unipr.netsec.nemo.program.tcpdump;


import java.util.ArrayList;

import io.ipstack.net.packet.Address;
import io.ipstack.net.packet.Packet;


public class AddressMatch implements Match {
	
	public static enum Type {
		SRC, DST, ANY
	}

	Address addr;
	Type type;


	public AddressMatch(Address addr, Type type) {
		this.addr=addr;
		this.type=type;
	}

	@Override
	public boolean getValue(ArrayList<Packet<?>> pp) {
		Address src=null;
		Address dst=null;
		for (Packet<?> pkt : pp) {
			src=pkt.getSourceAddress();
			dst=pkt.getDestAddress();
			switch(type) {
				case SRC : if (addr.equals(src)) return true; break;
				case DST : if (addr.equals(dst)) return true; break;
				case ANY : if (addr.equals(src) || addr.equals(dst)) return true; break;
			}
		}
		return false;
	}

}
