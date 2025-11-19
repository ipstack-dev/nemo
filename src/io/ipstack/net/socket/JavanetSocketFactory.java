package io.ipstack.net.socket;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;


public class JavanetSocketFactory implements SocketFactory {
	
	private static JavanetSocketFactory INSTANCE= null;
	
	
	public static JavanetSocketFactory getInstance() {
		if (INSTANCE==null) INSTANCE= new JavanetSocketFactory();
		return INSTANCE;
	}
	
	private JavanetSocketFactory() {}
	
	@Override
	public ServerSocket createServerSocket() throws IOException {
		return new JavanetServerSocket(new java.net.ServerSocket());
	}

	@Override
	public ServerSocket createServerSocket(int port) throws IOException {
		return new JavanetServerSocket(new java.net.ServerSocket(port));
	}

	@Override
	public ServerSocket createServerSocket(int port, int backlog) throws IOException {
		return new JavanetServerSocket(new java.net.ServerSocket(port,backlog));
	}

	@Override
	public ServerSocket createServerSocket(int port, int backlog, InetAddress bindAddr) throws IOException {
		return new JavanetServerSocket(new java.net.ServerSocket(port,backlog,bindAddr));
	}

	@Override
	public Socket createSocket() {
		return new JavanetSocket(new java.net.Socket());
	}

	@Override
	public Socket createSocket(InetAddress address, int port) throws IOException {
		return new JavanetSocket(new java.net.Socket(address,port));
	}

	@Override
	public Socket createSocket(String host, int port) throws UnknownHostException, IOException {
		return new JavanetSocket(new java.net.Socket(host,port));
	}

	@Override
	public Socket createSocket(String host, int port, InetAddress localAddr, int localPort) throws IOException {
		return new JavanetSocket(new java.net.Socket(host,port,localAddr,localPort));
	}

	@Override
	public Socket createSocket(InetAddress address, int port, InetAddress localAddr, int localPort) throws IOException {
		return new JavanetSocket(new java.net.Socket(address,port,localAddr,localPort));
	}

	@Override
	public DatagramSocket createDatagramSocket() throws SocketException {
		return new JavanetDatagramSocket();
	}

	@Override
	public DatagramSocket createDatagramSocket(int port) throws SocketException {
		return new JavanetDatagramSocket(port);
	}

}
