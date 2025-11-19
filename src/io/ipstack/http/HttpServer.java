package io.ipstack.http;

import static org.zoolu.util.log.LoggerLevel.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.function.Consumer;

import org.zoolu.util.Clock;
import org.zoolu.util.DateFormat;
import org.zoolu.util.SystemUtils;
import org.zoolu.util.log.DefaultLogger;
import org.zoolu.util.log.LoggerLevel;

import io.ipstack.net.ip4.SocketAddress;
import io.ipstack.net.socket.JavanetServerSocket;
import io.ipstack.net.socket.ServerSocket;
import io.ipstack.net.socket.Socket;
import io.ipstack.net.tcp.TcpLayer;


/** Simple HTTP server.
 * It passes all HTTP requests, by mean of a corresponding {@link HttpRequestHandle}, to a given {@link Consumer<HttpRequestHandle>}.
 */
public class HttpServer {
	
	/** Verbose mode */
	public static boolean VERBOSE=false;

	/** Logs a message. */
	private void log(LoggerLevel level, String str) {
		DefaultLogger.log(level,null,toString()+": "+str);
	}

	
	/** Default server port */
	public static int DEFAULT_PORT=80; // 8080

	/** Server name */
	public static String SERVER_NAME="Nemo Httpd";

	/** Server socket */
	ServerSocket server_socket;

	/** Server listener */
	Consumer<HttpRequestHandle> listener;
	
	/** Whether it is running */
	boolean running=true;
	
	
	/** Creates a new HTTP server.
	 * @param tcp_layer TCP layer
	 * @param server_port server port
	 * @param listener server listener that handles HTTP requests
	 * @throws IOException */
	public HttpServer(TcpLayer tcp_layer, int server_port, Consumer<HttpRequestHandle> listener) throws IOException {
		this.server_socket=new io.ipstack.net.tcp.ServerSocket(tcp_layer,server_port);
		this.listener=listener;
		start();
	}
	
	/** Creates a new HTTP server.
	 * @param server_port server port
	 * @param listener server listener that handles HTTP requests
	 * @throws IOException */
	public HttpServer(int server_port, Consumer<HttpRequestHandle> listener) throws IOException {
		this(new java.net.ServerSocket(server_port),listener);
	}
	
	/** Creates a new HTTP server.
	 * @param serverSocket server socket
	 * @param listener server listener that handles HTTP requests
	 * @throws IOException */
	public HttpServer(java.net.ServerSocket serverSocket, Consumer<HttpRequestHandle> listener) throws IOException {
		this.server_socket=new JavanetServerSocket(serverSocket);
		this.listener=listener;
		start();
	}
	
	/** Starts the server. */
	private void start() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				serve();
			}
		}).start();
	}
	
	/** Starts the server. */
	private void serve() {
		try {
			if (VERBOSE) log(INFO,"serve(): start listening on port "+server_socket.getLocalPort()); 
			while (running) {
				final Socket socket=server_socket.accept();
				if (VERBOSE) log(INFO,"serve(): connected from "+new SocketAddress(socket.getRemoteSocketAddress())); 
				new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							serve(new SocketAddress(socket.getRemoteSocketAddress()),socket.getInputStream(),socket.getOutputStream());
							socket.close();	
						}
						catch (IOException e) {
							//e.printStackTrace();
							if (VERBOSE) {
								log(INFO,"IOException serving the connection: "+e.getMessage());
								log(DEBUG,SystemUtils.toString(e.getStackTrace(),"\t",null,"\n"));
							}
						}		
					}				
				}).start();
			}
		}
		catch (Exception e) {
			// general Exception is captured in place of IOException, for safety
			//e.printStackTrace();
			log(INFO,"serve(): Exception: "+e.getMessage());
			log(DEBUG,SystemUtils.toString(e.getStackTrace(),"\t",null,"\n"));
		}
		log(INFO,"serve(): terminated");
	}
		
	/** Handles a new TCP connection.
	 * @param client socket address
	 * @param is the TCP input stream
	 * @param os the TCP output stream
	 * @throws IOException */
	private void serve(SocketAddress client_soaddr, InputStream is, OutputStream os) throws IOException {
		// request
		HttpRequest req=HttpRequest.parseHttpRequest(is);
		if (VERBOSE) log(DEBUG,"Request: "+req+"-----End-of-message-----");
		HttpRequestHandle request_handle=new HttpRequestHandle(req,client_soaddr);
		listener.accept(request_handle);	
		// response
		HashMap<String,String> hdr=request_handle.getResponseHeaderFields();
		if (!hdr.containsKey("Date")) hdr.put("Date",DateFormat.formatEEEddMMMyyyyhhmmss(new Date(Clock.getDefaultClock().currentTimeMillis())));
		if (!hdr.containsKey("Server")) hdr.put("Server",SERVER_NAME);
		if (req.getHeaderField("Origin")!=null && !hdr.containsKey("Access-Control-Allow-Origin")) hdr.put("Access-Control-Allow-Origin","*");
		HttpResponse resp=new HttpResponse(request_handle.getResponseCode(),hdr,request_handle.getResponseBody());
		if (VERBOSE) log(DEBUG,"Response: "+resp+"-----End-of-message-----");
		os.write(resp.getBytes());
		os.flush();
	}
	
	/** Stops the server. */
	public void close() {
		if (!running) return;
		running=false;
		try {
			server_socket.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}	
	
	@Override
	public String toString() {
		String local;
		TcpLayer tcp_layer=server_socket.getTcpLayer();
		if (tcp_layer!=null) local=""+tcp_layer.getIpLayer().getAddress()+':'+server_socket.getLocalPort();
		else local=""+server_socket.getLocalPort();
		return HttpServer.class.getSimpleName()+'['+local+']';
	}


}
