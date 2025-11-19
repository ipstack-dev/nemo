package io.ipstack.net.socket;


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.SocketException;


/** Implementation-independent UDP socket interface.
 */
public interface DatagramSocket {
	
	/** Returns the address to which the socket is connected. */
	public InetAddress getInetAddress();

	/** Gets the local address to which the socket is bound. */
	public InetAddress getLocalAddress();

	/** Returns the remote port number to which this socket is connected. */
	public int getPort();

	/** Returns the local port number to which this socket is bound. */
	public int getLocalPort();

	/** Returns the address of the endpoint this socket is connected to, or {@code null} if it is unconnected. */
	public SocketAddress getRemoteSocketAddress();

	/** Returns the address of the endpoint this socket is bound to. */
	public SocketAddress getLocalSocketAddress();
	
	/** Sends a datagram packet from this socket.
	 * @param p the packet
	 * @throws IOException */
	public void send(DatagramPacket p) throws IOException;

	/** Receives a datagram packet from this socket. */
	public void receive(DatagramPacket p) throws IOException;

	/** Closes this socket. */
	public void close();
	
	/** Whether this socket is closed. */
	public boolean isClosed();
	
	/** Gets the SO_TIMEOUT timeout, in milliseconds.
	 * @return the timeout value 
	 * @throws SocketException */
	public int getSoTimeout() throws SocketException;
	
	/** Enables/disables SO_TIMEOUT with the specified timeout, in milliseconds.
	 * @param timeout the timeout value
	 * @throws SocketException */
	public void setSoTimeout(int timeout) throws SocketException;
	
}
