package io.ipstack.net.analyzer;


import org.zoolu.util.DateFormat;

import io.ipstack.net.packet.Packet;


/** A packet with its timestamp.
 */
public class PacketDump<P extends Packet<?>> {

	/** Timestamp */
	long time;
	
	/** Packet */
	P pkt;
	
	
	/** Creates a new packet dump.
	 * @param time the packet timestamp, in milliseconds
	 * @param pkt the packet */
	public PacketDump(long time, P pkt) {
		this.time=time;
		this.pkt=pkt;
	}
	
	/** Gets timestamp.
	 * @return the packet timestamp, in milliseconds */
	public long getTimestamp() {
		return time;
	}

	/** Gets packet.
	 * @return the packet */
	public P getPacket() {
		return pkt;
	}

	@Override
	public String toString() {
		return DateFormat.formatHHmmssSSS(time)+" "+ProtocolAnalyzer.exploreInner(pkt).toString();
	}
	
}
