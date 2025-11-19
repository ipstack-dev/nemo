package io.ipstack.net.rawsocket.udp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import io.ipstack.net.socket.ServerSocket;
import io.ipstack.net.socket.Socket;
import io.ipstack.net.socket.SocketFactory;


public class RawsocketSocketFactory implements SocketFactory {

	private static RawsocketSocketFactory INSTANCE= null;
	
	
	public static RawsocketSocketFactory getInstance() {
		if (INSTANCE==null) INSTANCE= new RawsocketSocketFactory();
		return INSTANCE;
	}
	
	private RawsocketSocketFactory() {}

	@Override
	public ServerSocket createServerSocket() throws IOException {
		throw new IOException("createServerSocket(): ServerSocket is not supported by RawsocketSocketFactory");
	}

	@Override
	public ServerSocket createServerSocket(int port) throws IOException {
		throw new IOException("createServerSocket(): ServerSocket is not supported by RawsocketSocketFactory");
	}

	@Override
	public ServerSocket createServerSocket(int port, int backlog) throws IOException {
		throw new IOException("createServerSocket(): ServerSocket is not supported by RawsocketSocketFactory");
	}

	@Override
	public ServerSocket createServerSocket(int port, int backlog, InetAddress bindAddr) throws IOException {
		throw new IOException("createServerSocket(): ServerSocket is not supported by RawsocketSocketFactory");
	}

	@Override
	public Socket createSocket() {
		throw new RuntimeException("createSocket(): Socket is not supported by RawsocketSocketFactory");
	}

	@Override
	public Socket createSocket(InetAddress address, int port) throws IOException {
		throw new IOException("createSocket(): Socket is not supported by RawsocketSocketFactory");
	}

	@Override
	public Socket createSocket(String host, int port) throws UnknownHostException, IOException {
		throw new IOException("createSocket(): Socket is not supported by RawsocketSocketFactory");
	}

	@Override
	public Socket createSocket(String host, int port, InetAddress localAddr, int localPort) throws IOException {
		throw new IOException("createSocket(): Socket is not supported by RawsocketSocketFactory");
	}

	@Override
	public Socket createSocket(InetAddress address, int port, InetAddress localAddr, int localPort) throws IOException {
		throw new IOException("createSocket(): Socket is not supported by RawsocketSocketFactory");
	}

	@Override
	public io.ipstack.net.socket.DatagramSocket createDatagramSocket() throws SocketException {
		return new DatagramSocket();
	}

	@Override
	public io.ipstack.net.socket.DatagramSocket createDatagramSocket(int port) throws SocketException {
		return port>0? new DatagramSocket(port) : new DatagramSocket();
	}
	

}
