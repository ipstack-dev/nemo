package io.ipstack.net.socket;


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.SocketException;


/** Implementation of {@link DatagramSocket} based on {@link java.net.DatagramSocket}.
 */
public class JavanetDatagramSocket implements DatagramSocket {
	
	/** Java.net datagram socket */
	java.net.DatagramSocket socket=null;

	
	/** Creates a new socket. */
	public JavanetDatagramSocket() throws SocketException {
		this(new java.net.DatagramSocket());
	}
	
	/** Creates a new socket. */
	public JavanetDatagramSocket(int port) throws SocketException {
		this(port>0? new java.net.DatagramSocket(port) : new java.net.DatagramSocket());
	}
	
	/** Creates a new socket. */
	public JavanetDatagramSocket(java.net.DatagramSocket socket) throws SocketException {
		this.socket=socket;
	}
	
	@Override
	public InetAddress getInetAddress() {
		return socket.getInetAddress();
	}

	@Override
	public InetAddress getLocalAddress() {
		return socket.getLocalAddress();
	}

	@Override
	public int getPort() {
		return socket.getPort();
	}

	@Override
	public int getLocalPort() {
		return socket.getLocalPort();
	}

	@Override
	public SocketAddress getRemoteSocketAddress() {
		return socket.getRemoteSocketAddress();
	}

	@Override
	public SocketAddress getLocalSocketAddress() {
		return socket.getLocalSocketAddress();
	}
	
	@Override
	public void send(DatagramPacket p) throws IOException {
		socket.send(p);
	}

	@Override
	public void receive(DatagramPacket p) throws IOException {
		socket.receive(p);
	}

	@Override
	public void close() {
		socket.close();
	}
	
	@Override
	public boolean isClosed() {
		return socket.isClosed();
	}
	
	@Override
	public void setSoTimeout(int timeout) throws SocketException {
		socket.setSoTimeout(timeout);
	}
	
	@Override
	public int getSoTimeout() throws SocketException {
		return socket.getSoTimeout();
	}

}
