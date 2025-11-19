package io.ipstack.net.socket;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.SocketException;


/** Socket API.
 */
public interface Socket {

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

	/** Returns an input stream for this socket. */
	public InputStream getInputStream() throws IOException;

	/** Returns an output stream for this socket. */
	public OutputStream getOutputStream() throws IOException;

	/** Closes this socket. */
	public void close() throws IOException;
	
	/** Enable/disable TCP_NODELAY TCP_NODELAY. */
	public void setTcpNoDelay(boolean on) throws SocketException;

	/** Tests if TCP_NODELAY TCP_NODELAY is enabled. */
	public boolean getTcpNoDelay() throws SocketException;

	/** Gets the TCP layer.
	 * @return the TCP layer, in case of {@link io.ipstack.net.tcp.Socket ipstack socket}, or <i>null</i>. */
	public io.ipstack.net.tcp.TcpLayer getTcpLayer();

}
