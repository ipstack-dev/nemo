package it.unipr.netsec.rawsocket.udp;


import java.net.SocketException;


public class DatagramSocketFactory extends it.unipr.netsec.ipstack.socket.DatagramSocketFactory {
	
	@Override
	public it.unipr.netsec.ipstack.socket.DatagramSocket createInstance() throws SocketException {
		return new DatagramSocket();
	}

	@Override
	public it.unipr.netsec.ipstack.socket.DatagramSocket createInstance(int port) throws SocketException {
		return port>0? new DatagramSocket(port) : new DatagramSocket();
	}
	
}
