package io.ipstack.net.udp;


import java.net.DatagramPacket;
import java.net.SocketAddress;

import org.zoolu.util.Bytes;


public final class DatagramUtils {
	private DatagramUtils() {}

	public static String toString(DatagramPacket pkt) {
		SocketAddress soaddr=pkt.getSocketAddress();
		return (soaddr!=null?soaddr.toString().substring(1):null)+" data="+Bytes.toHex(pkt.getData(),pkt.getOffset(),pkt.getLength());
	}
}
