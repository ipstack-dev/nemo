package io.ipstack.net.socket;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;


/** Factory that creates {@link ServerSocekt}, {@link Socket}, and {@link DatagramSocket} instances.
 */
public interface SocketFactory {
	
	/** Gets default factory.
	 * @return the default factory
	 */
	public static SocketFactory getDefaultFactory() {
		return DefaultSocketFactory.getFactory();
	}

	/** Sets default factory.
	 */
	public static void setDefaultFactory(SocketFactory factory) {
		DefaultSocketFactory.setFactory(factory);
	}
	
	
	/** Creates an unbound server socket.
	 * @throws IOException */
	public ServerSocket createServerSocket() throws IOException;

	/** Creates a server socket, bound to the specified port. A port number of {@code 0} means that the port number is automatically allocated.
	 * @param port the server port
	 * @throws IOException */
	public ServerSocket createServerSocket(int port) throws IOException;

	/** Creates a server socket and binds it to the specified local port number, with the specified backlog.
	 * @param port the server port
	 * @param backlog a given backlog
	 * @throws IOException */
	public ServerSocket createServerSocket(int port, int backlog) throws IOException;

	/** Create a server with the specified port, listen backlog, and local IP address to bind to.
	 * @param port the server port
	 * @param backlog a given backlog
	 * @param bindAddr the IP address to be bound to
	 * @throws IOException */
	public ServerSocket createServerSocket(int port, int backlog, InetAddress bindAddr) throws IOException;
	
	/** Creates an unconnected socket.
	 * @return the new socket
	 */
	public Socket createSocket();
	
	/** Creates a TCP socket and connects it to the specified port number at the specified IP address.
	 * @param address remote address
	 * @param port remote port
	 * @return the new socket
	 */
	public Socket createSocket(InetAddress address, int port) throws IOException;
	
	/** Creates a TCP socket and connects it to the specified port number on the named host.
	 * @param host remote host address
	 * @param port remote port
	 * @return  the new socket
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public Socket createSocket(String host, int port) throws UnknownHostException, IOException;

	/** Creates a TCP socket and connects it to the specified remote host on the specified remote port. The Socket will also bind() to the local address and port supplied.
	 * @param host remote host address
	 * @param port remote port
	 * @param localAddr local address
	 * @param localPort local port
	 * @return  the new socket
	 * @throws IOException
	 */
	public Socket createSocket(String host, int port, InetAddress localAddr, int localPort) throws IOException;

	/** Creates a TCP socket and connects it to the specified remote address on the specified remote port. The Socket will also bind() to the local address and port supplied.
	 * @param address remote address
	 * @param port remote port
	 * @param localAddr local address
	 * @param localPort local port
	 * @return  the new socket
	 * @throws IOException
	 */
	public Socket createSocket(InetAddress address, int port, InetAddress localAddr, int localPort) throws IOException;
	
	/** Creates a UDP socket. 
	 * @return the new socket
	 * @throws SocketException
	 */
	public DatagramSocket createDatagramSocket() throws SocketException;

	/** Creates a UDP socket. 
	 * @param port the local port
	 * @return the new socket
	 * @throws SocketException
	 */
	public DatagramSocket createDatagramSocket(int port) throws SocketException;
	
	
	
	/** Gets a socket factory that creates java.net sockets.
	 * @return the factory
	 */
	/*public static SocketFactory getJavanetFactory() {
		return JavanetSocketFactory.getInstance();
	}*/
	
	
	/** Gets a socket factory that creates ipstack sockets.
	 * @return the factory
	 * @throws SocketException 
	 */
	/*public static SocketFactory getIpstackFactory(final Ip4Layer ip4Layer) throws SocketException {
		return IpstackSocketFactory.getInstance(ip4Layer);
	}*/

}
