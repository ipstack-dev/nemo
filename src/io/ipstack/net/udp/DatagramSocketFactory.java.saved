package it.unipr.netsec.ipstack.udp;


import java.net.SocketException;


public class DatagramSocketFactory extends it.unipr.netsec.ipstack.socket.DatagramSocketFactory {
	
	/** UDP layer */
	UdpLayer udp_layer;
	
	/** Creates a new DatagramSocketFactory. */
	public DatagramSocketFactory(UdpLayer udp_layer) {
		this.udp_layer=udp_layer;
	}
	
	@Override
	public it.unipr.netsec.ipstack.socket.DatagramSocket createInstance() throws SocketException {
		return new DatagramSocket(udp_layer);
	}

	@Override
	public it.unipr.netsec.ipstack.socket.DatagramSocket createInstance(int port) throws SocketException {
		return port>0? new DatagramSocket(udp_layer,port) : new DatagramSocket(udp_layer);
	}
	
}
