package it.unipr.netsec.nemo.websocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.util.HashMap;

import org.zoolu.util.Base64;
import org.zoolu.util.log.DefaultLogger;
import org.zoolu.util.log.LoggerLevel;

import io.ipstack.http.HttpRequest;
import io.ipstack.http.HttpRequestHandle;
import io.ipstack.http.HttpResponse;
import io.ipstack.net.ip4.SocketAddress;
import io.ipstack.net.socket.JavanetServerSocket;
import io.ipstack.net.socket.ServerSocket;
import io.ipstack.net.socket.Socket;
import io.ipstack.net.tcp.TcpLayer;

import static org.zoolu.util.log.LoggerLevel.*;


/** WebSocket server.
 * It listens for new WebSocket connections.
 */
public class WebSocketServer {
	
	/** Verbose mode */
	public static boolean VERBOSE= false;

	/** Logs a message. */
	private void log(LoggerLevel level, String str) {
		DefaultLogger.log(level,null,toString()+": "+str);
	}

	
	/** Server socket */
	ServerSocket serverSocket;
	
	
	/** Creates a new HTTP server.
	 * @param tcp_layer TCP layer
	 * @param server_port server port
	 * @throws IOException */
	public WebSocketServer(TcpLayer tcp_layer, int server_port) throws IOException {
		serverSocket= new io.ipstack.net.tcp.ServerSocket(tcp_layer,server_port);
		if (VERBOSE) log(INFO,"new WebSocket server on port "+serverSocket.getLocalPort()); 
	}
	
	
	/** Creates a new HTTP server.
	 * @param server_port server port
	 * @throws IOException */
	public WebSocketServer(int server_port) throws IOException {
		serverSocket= new JavanetServerSocket(new java.net.ServerSocket(server_port));
		if (VERBOSE) log(INFO,"new WebSocket server on port "+serverSocket.getLocalPort()); 
	}
	
	
	/** Accepts a new incoming WebSocket connection.
	 * @return the new WebSocket
	 * @throws IOException 
	 */
	public WebSocket accept() throws IOException {
		Socket socket= serverSocket.accept();
		if (VERBOSE) log(INFO,"serve(): connected from "+new SocketAddress(socket.getRemoteSocketAddress()));
				
		SocketAddress client_soaddr= new SocketAddress(socket.getRemoteSocketAddress());
		InputStream is= socket.getInputStream();
		OutputStream os= socket.getOutputStream();
			
		// request
		HttpRequest req=HttpRequest.parseHttpRequest(is);
		if (VERBOSE) log(DEBUG,"Request: "+req+"-----End-of-message-----");
		HttpRequestHandle req_handle=new HttpRequestHandle(req,client_soaddr);		
		try {
			String method= req_handle.getMethod();
			if (method.equals(HttpRequest.OPTIONS)) {
				req_handle.setResponseCode(200);
				req_handle.setResponseHeaderField("Allow",HttpRequest.GET+","+HttpRequest.OPTIONS);
			}
			else
			if (method.equals(HttpRequest.GET)) {
				//HttpRequestURL request_url= req_handle.getRequestURL();
				//String resource= request_url.getAbsPath();
				String wsKey= req_handle.getRequest().getHeaderField("Sec-WebSocket-Key");
				if (wsKey==null) throw new IOException("Sec-WebSocket-Key header field is missed");
				req_handle.setResponseCode(101);
				HashMap<String,String> hdr= req_handle.getResponseHeaderFields();
				hdr.put("Connection","Upgrade");
				hdr.put("Upgrade","websocket");
				String wsAccept= Base64.encode(MessageDigest.getInstance("SHA-1").digest((wsKey+WebSocketFrame.WS_UUID).getBytes()));
				hdr.put("Sec-WebSocket-Accept",wsAccept);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			req_handle.setResponseCode(400);
		}
		// response
		HashMap<String,String> hdr=req_handle.getResponseHeaderFields();
		HttpResponse resp=new HttpResponse(req_handle.getResponseCode(),hdr,req_handle.getResponseBody());
		if (VERBOSE) log(DEBUG,"Response: "+resp+"-----End-of-message-----");
		os.write(resp.getBytes());
		os.flush();
		if (resp.getStatusCode()!=101) {
			socket.close();
			throw new IOException("WebSocket handshake failed with HTTP response "+resp.getStatusCode());
		}
		return new WebSocket(is,os,false);
	}

		
	/** Closes the server. */
	public void close() {
		try {
			serverSocket.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}	
	
	
	@Override
	public String toString() {
		String local;
		TcpLayer tcp_layer=serverSocket.getTcpLayer();
		if (tcp_layer!=null) local=""+tcp_layer.getIpLayer().getAddress()+':'+serverSocket.getLocalPort();
		else local=""+serverSocket.getLocalPort();
		return WebSocketServer.class.getSimpleName()+'['+local+']';
	}


}
