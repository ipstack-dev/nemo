package it.unipr.netsec.ipstack.socket;


import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;


/** Single server socket API for different server socket implementations.
 * I actually wraps two server socket implementations: {@link it.unipr.netsec.ipstack.tcp.ServerSocket} and {@link java.net.ServerSocket}.
 */
public class ServerSocket {

	/** Ipstack server socket */
	it.unipr.netsec.ipstack.tcp.ServerSocket ipstack_server_socket=null;
	
	/** Java.net server socket */
	java.net.ServerSocket javanet_server_socket=null;

	
	/** Creates a new server socket. */
	public ServerSocket(it.unipr.netsec.ipstack.tcp.ServerSocket ipstack_server_socket) {
		this.ipstack_server_socket=ipstack_server_socket;
	}

	/** Creates a new server socket. */
	public ServerSocket(java.net.ServerSocket javanet_server_socket) {
		this.javanet_server_socket=javanet_server_socket;
	}

	
	/** Gets the TCP layer.
	 * @return the TCP layer, in case of {@link it.unipr.netsec.ipstack.tcp.ServerSocket ipstack server socket}, or <i>null</i>. */
	public it.unipr.netsec.ipstack.tcp.TcpLayer getTcpLayer() {
		return ipstack_server_socket!=null? ipstack_server_socket.getTcpLayer() : null;
	}

	
	/** Listens for a connection to be made to this socket and accepts it.
	 * @return the new socket
	 * @throws IOException */
	public Socket accept() throws IOException {
		return ipstack_server_socket!=null? new Socket(ipstack_server_socket.accept()) : new Socket(javanet_server_socket.accept());
	}

	/** Gets the local address of this server socket.
	 * @return the address */
	public InetAddress getInetAddress() {
		return ipstack_server_socket!=null? ipstack_server_socket.getInetAddress() : javanet_server_socket.getInetAddress();
	}

	/** Gets the port number on which this socket is listening.
	 * @return the port number */
	public int getLocalPort() {
		return ipstack_server_socket!=null? ipstack_server_socket.getLocalPort() : javanet_server_socket.getLocalPort();
	}

	/** Returns the address of the endpoint this socket is bound to. */
	public SocketAddress getLocalSocketAddress() {
		return ipstack_server_socket!=null? ipstack_server_socket.getLocalSocketAddress() : javanet_server_socket.getLocalSocketAddress();
	}

	/** Closes this socket. */
	public void close() throws IOException {
		if (ipstack_server_socket!=null) ipstack_server_socket.close(); else javanet_server_socket.close();
	}

}
