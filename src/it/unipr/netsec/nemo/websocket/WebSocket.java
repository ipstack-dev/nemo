package it.unipr.netsec.nemo.websocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

import org.zoolu.util.Base64;
import org.zoolu.util.Random;

import io.ipstack.http.HttpInputStream;
import io.ipstack.http.HttpMessage;
import io.ipstack.http.HttpRequest;
import io.ipstack.http.HttpResponse;
import io.ipstack.net.ip4.SocketAddress;


/** A WebSocket.
 */
public class WebSocket {
	
	InputStream is;
	OutputStream os;
	boolean masked;
	
	
	/** creates a WebSocket client connection.
	 * @param endpoint socket address of the WebSocket server
	 * @throws IOException
	 */
	public WebSocket(String endpoint) throws IOException {
		java.net.Socket socket= new java.net.Socket();
		socket.connect(new SocketAddress(endpoint).toInetSocketAddress());
		is= socket.getInputStream();
		os= socket.getOutputStream();
		masked= true;
		
		HashMap<String,String> hdr= new HashMap<>();
		hdr.put("Host",endpoint);
		hdr.put("Connection","Upgrade");
		hdr.put("Upgrade","websocket");
		hdr.put("Origin","null");
		hdr.put("Sec-WebSocket-Version","13");
		byte[] key= new byte[16];
		Random.nextBytes(key);
		String wsKey= Base64.encode(key);
		hdr.put("Sec-WebSocket-Key",wsKey);
		hdr.put("Pragma","no-cache");
		hdr.put("Cache-Control","no-cache");
		
		HttpRequest req= new HttpRequest("GET","/",hdr,null);
		os.write(req.toString().getBytes());
		
		HttpResponse resp= new HttpResponse(HttpMessage.parseHttpMessage(new HttpInputStream(socket)));
		String wsAccept= resp.getHeaderField("Sec-WebSocket-Accept");
		String wsAcceptCheck;
		try {
			wsAcceptCheck=Base64.encode(MessageDigest.getInstance("SHA-1").digest((wsKey+WebSocketFrame.WS_UUID).getBytes()));
		}
		catch (NoSuchAlgorithmException e) {
			throw new IOException(e.getMessage());
		}
		if (!wsAccept.equals(wsAcceptCheck)) throw new IOException("Sec-WebSocket-Accept value mismatches");
	}

	
	/** Creates a connected WebSocket.
	 * @param is
	 * @param os
	 * @param masked
	 */
	WebSocket(InputStream is, OutputStream os, boolean masked) {
		this.is= is;
		this.os= os;
		this.masked= masked;
	}
	
	public void write(byte[] data) throws IOException {
		byte[] maskingkey= null;
		if (masked) {
			maskingkey= new byte[4];
			Random.nextBytes(maskingkey);
		}
		WebSocketFrame frame= new WebSocketFrame(WebSocketFrame.OPCODE_BINARY_FRAME, data, maskingkey, true);
		write(frame);
	}

	
	public void write(String text) throws IOException {
		byte[] maskingkey= null;
		if (masked) {
			maskingkey= new byte[4];
			Random.nextBytes(maskingkey);
		}
		WebSocketFrame frame= new WebSocketFrame(WebSocketFrame.OPCODE_TEXT_FRAME, text.getBytes(), maskingkey, true);
		write(frame);
	}

	
	public void write(WebSocketFrame frame) throws IOException {
		os.write(frame.getBytes());
		os.flush();
	}

	
	public WebSocketFrame read() throws IOException {
		return WebSocketFrame.parseWebsocketFrame(is);
	}
	
	public void close() {
		try {
			is.close();
			os.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
