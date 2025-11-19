package io.ipstack.net.socket;


import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketAddress;


/** Server socket API that may wrap different TCP server socket implementations.
 * I actually can wrap two server socket implementations: {@link io.ipstack.net.tcp.ServerSocket} and {@link java.net.ServerSocket}.
 */
public class JavanetServerSocket implements ServerSocket {

	/** Java.net server socket */
	java.net.ServerSocket javanet_server_socket=null;

	
	/** Creates a new server socket based on a java.net.ServerSocket.
	 * @param javanet_server_socket the java.net server socket
	 */
	public JavanetServerSocket(java.net.ServerSocket javanet_server_socket) {
		this.javanet_server_socket=javanet_server_socket;
	}

	
	@Override
	public Socket accept() throws IOException {
		return new JavanetSocket(javanet_server_socket.accept());
	}

	@Override
	public InetAddress getInetAddress() {
		return javanet_server_socket.getInetAddress();
	}

	@Override
	public int getLocalPort() {
		return javanet_server_socket.getLocalPort();
	}

	@Override
	public SocketAddress getLocalSocketAddress() {
		return javanet_server_socket.getLocalSocketAddress();
	}

	@Override
	public void close() throws IOException {
		javanet_server_socket.close();
	}

	@Override
	public io.ipstack.net.tcp.TcpLayer getTcpLayer() {
		return null;
	}
	
}
