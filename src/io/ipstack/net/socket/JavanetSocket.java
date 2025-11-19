package io.ipstack.net.socket;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.SocketException;


/** Socket API that may wrap different TCP socket implementations.
 * I actually can wrap two socket implementations: {@link io.ipstack.net.tcp.Socket} or {@link java.net.Socket javanet_socket}.
 */
public class JavanetSocket implements Socket {

	/** Java.net socket */
	java.net.Socket javanet_socket=null;

	
	/** Creates a new socket. */
	public JavanetSocket(java.net.Socket javanet_socket) {
		this.javanet_socket=javanet_socket;
	}


	@Override
	public InetAddress getInetAddress() {
		return javanet_socket.getInetAddress();
	}

	@Override
	public InetAddress getLocalAddress() {
		return javanet_socket.getLocalAddress();
	}

	@Override
	public int getPort() {
		return javanet_socket.getPort();
	}

	@Override
	public int getLocalPort() {
		return javanet_socket.getLocalPort();
	}

	@Override
	public SocketAddress getRemoteSocketAddress() {
		return javanet_socket.getRemoteSocketAddress();
	}

	@Override
	public SocketAddress getLocalSocketAddress() {
		return javanet_socket.getLocalSocketAddress();
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return javanet_socket.getInputStream();
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		return javanet_socket.getOutputStream();
	}

	@Override
	public void close() throws IOException {
		javanet_socket.close();
	}
	
	@Override
	public void setTcpNoDelay(boolean on) throws SocketException {
		javanet_socket.setTcpNoDelay(on);
	}

	@Override
	public boolean getTcpNoDelay() throws SocketException {
		return javanet_socket.getTcpNoDelay();
	}

	@Override
	public io.ipstack.net.tcp.TcpLayer getTcpLayer() {
		return null;
	}

}
