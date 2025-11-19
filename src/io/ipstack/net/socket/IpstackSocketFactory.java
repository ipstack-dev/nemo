package io.ipstack.net.socket;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import io.ipstack.net.ip4.Ip4Layer;
import io.ipstack.net.tcp.TcpLayer;
import io.ipstack.net.udp.UdpLayer;


public class IpstackSocketFactory implements SocketFactory {
	
	private final TcpLayer tcpLayer;
	private final UdpLayer udpLayer;
	
	
	public static IpstackSocketFactory getInstance(Ip4Layer ip4Layer) throws SocketException {
		return new IpstackSocketFactory(ip4Layer);
	}
		
	private IpstackSocketFactory(Ip4Layer ip4Layer) throws SocketException {
		tcpLayer= new TcpLayer(ip4Layer);
		udpLayer= new UdpLayer(ip4Layer);		
	}
	
	@Override
	public ServerSocket createServerSocket() throws IOException {
		return new io.ipstack.net.tcp.ServerSocket(tcpLayer);
	}

	@Override
	public ServerSocket createServerSocket(int port) throws IOException {
		return new io.ipstack.net.tcp.ServerSocket(tcpLayer,port);
	}

	@Override
	public ServerSocket createServerSocket(int port, int backlog) throws IOException {
		return new io.ipstack.net.tcp.ServerSocket(tcpLayer,port,backlog);
	}

	@Override
	public ServerSocket createServerSocket(int port, int backlog, InetAddress bindAddr) throws IOException {
		return new io.ipstack.net.tcp.ServerSocket(tcpLayer,port,backlog,bindAddr);
	}

	@Override
	public Socket createSocket() {
		return new io.ipstack.net.tcp.Socket(tcpLayer);
	}

	@Override
	public Socket createSocket(InetAddress address, int port) throws IOException {
		return new io.ipstack.net.tcp.Socket(tcpLayer,address,port);
	}

	@Override
	public Socket createSocket(String host, int port) throws UnknownHostException, IOException {
		return new io.ipstack.net.tcp.Socket(tcpLayer,host,port);
	}

	@Override
	public Socket createSocket(String host, int port, InetAddress localAddr, int localPort) throws IOException {
		return new io.ipstack.net.tcp.Socket(tcpLayer,host,port,localAddr,localPort);
	}

	@Override
	public Socket createSocket(InetAddress address, int port, InetAddress localAddr, int localPort) throws IOException {
		return new io.ipstack.net.tcp.Socket(tcpLayer,address,port,localAddr,localPort);
	}

	@Override
	public DatagramSocket createDatagramSocket() throws SocketException {
		return new io.ipstack.net.udp.DatagramSocket(udpLayer);
	}

	@Override
	public DatagramSocket createDatagramSocket(int port) throws SocketException {
		return new io.ipstack.net.udp.DatagramSocket(udpLayer,port);
	}


}
