package it.unipr.netsec.ipstack.socket;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.SocketAddress;


/** Single socket API for different TCP socket implementations.
 * I actually wraps two socket implementations: {@link it.unipr.netsec.ipstack.tcp.Socket} and {@link java.net.Socket javanet_socket}.
 */
public class Socket {

	/** Ipstack socket */
	it.unipr.netsec.ipstack.tcp.Socket ipstack_socket=null;
	
	/** Java.net socket */
	java.net.Socket javanet_socket=null;

	
	/** Creates a new socket. */
	public Socket(it.unipr.netsec.ipstack.tcp.Socket ipstack_socket) {
		this.ipstack_socket=ipstack_socket;
	}

	/** Creates a new socket. */
	public Socket(java.net.Socket javanet_socket) {
		this.javanet_socket=javanet_socket;
	}

	
	/** Gets the TCP layer.
	 * @return the TCP layer, in case of {@link it.unipr.netsec.ipstack.tcp.Socket ipstack socket}, or <i>null</i>. */
	public it.unipr.netsec.ipstack.tcp.TcpLayer getTcpLayer() {
		return ipstack_socket!=null? ipstack_socket.getTcpLayer() : null;
	}

	
	/** Returns the address to which the socket is connected. */
	public InetAddress getInetAddress() {
		return ipstack_socket!=null? ipstack_socket.getInetAddress() : javanet_socket.getInetAddress();
	}

	/** Gets the local address to which the socket is bound. */
	public InetAddress getLocalAddress() {
		return ipstack_socket!=null? ipstack_socket.getLocalAddress() : javanet_socket.getLocalAddress();
	}

	/** Returns the remote port number to which this socket is connected. */
	public int getPort() {
		return ipstack_socket!=null? ipstack_socket.getPort() : javanet_socket.getPort();
	}

	/** Returns the local port number to which this socket is bound. */
	public int getLocalPort() {
		return ipstack_socket!=null? ipstack_socket.getLocalPort() : javanet_socket.getLocalPort();
	}

	/** Returns the address of the endpoint this socket is connected to, or {@code null} if it is unconnected. */
	public SocketAddress getRemoteSocketAddress() {
		return ipstack_socket!=null? ipstack_socket.getRemoteSocketAddress() : javanet_socket.getRemoteSocketAddress();
	}

	/** Returns the address of the endpoint this socket is bound to. */
	public SocketAddress getLocalSocketAddress() {
		return ipstack_socket!=null? ipstack_socket.getLocalSocketAddress() : javanet_socket.getLocalSocketAddress();
	}

	/** Returns an input stream for this socket. */
	public InputStream getInputStream() throws IOException {
		return ipstack_socket!=null? ipstack_socket.getInputStream() : javanet_socket.getInputStream();
	}

	/** Returns an output stream for this socket. */
	public OutputStream getOutputStream() throws IOException {
		return ipstack_socket!=null? ipstack_socket.getOutputStream() : javanet_socket.getOutputStream();
	}

	/** Closes this socket. */
	public void close() throws IOException {
		if (ipstack_socket!=null) ipstack_socket.close(); else javanet_socket.close();
	}

	
}
