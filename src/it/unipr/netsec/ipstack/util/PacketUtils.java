package it.unipr.netsec.ipstack.util;


import it.unipr.netsec.ipstack.analyzer.ProtocolAnalyzer;
import it.unipr.netsec.ipstack.net.Packet;


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
