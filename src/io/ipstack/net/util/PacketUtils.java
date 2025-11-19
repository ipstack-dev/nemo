package io.ipstack.net.util;


import io.ipstack.net.analyzer.ProtocolAnalyzer;
import io.ipstack.net.packet.Packet;


public abstract class PacketUtils {

	public static String toString(Packet<?> pkt) {
		try {
			return ProtocolAnalyzer.exploreInner(pkt).toString();
		}
		catch (Exception e) {
			return pkt!=null? pkt.toString() : null;
		}
	}

}
