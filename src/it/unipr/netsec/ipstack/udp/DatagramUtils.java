package it.unipr.netsec.ipstack.udp;


import java.net.DatagramPacket;
import java.net.SocketAddress;

import org.zoolu.util.Bytes;


public abstract class DatagramUtils {

	public static String toString(DatagramPacket pkt) {
		SocketAddress soaddr=pkt.getSocketAddress();
		return (soaddr!=null?soaddr.toString().substring(1):null)+" data="+Bytes.toHex(pkt.getData(),pkt.getOffset(),pkt.getLength());
	}
}
