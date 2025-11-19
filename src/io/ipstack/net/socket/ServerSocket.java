package io.ipstack.net.socket;


import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketAddress;


/** Server socket API.
 */
public interface ServerSocket {

	/** Listens for a connection to be made to this socket and accepts it.
	 * @return the new socket
	 * @throws IOException */
	public Socket accept() throws IOException;

	/** Gets the local address of this server socket.
	 * @return the address */
	public InetAddress getInetAddress();

	/** Gets the port number on which this socket is listening.
	 * @return the port number */
	public int getLocalPort();

	/** Returns the address of the endpoint this socket is bound to. */
	public SocketAddress getLocalSocketAddress();

	/** Closes this socket. */
	public void close() throws IOException;
	
	/** Gets the TCP layer.
	 * @return the TCP layer, in case of {@link io.ipstack.net.tcp.ServerSocket ipstack server socket}, or <i>null</i>. */
	public io.ipstack.net.tcp.TcpLayer getTcpLayer();

}
