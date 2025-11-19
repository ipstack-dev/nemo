package it.unipr.netsec.nemo.http;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;


/** Simple HTTP client.
 */
public class HttpClient {
	
	static final int DEFAULT_HTTP_PORT=80;
	
	static final int DEFAULT_HTTPS_PORT=443;

	
	/** Creates a new client. */
	public HttpClient() {
	}
	
	/** Sends a request.
	 * @param method the method (e.g. GET, POST, etc.)
	 * @param req_url request URL
	 * @param hdr (optional) additional header fields, as hdrname-to-hdrvalue table
	 * @param content_type (optional) content type
	 * @param body (optional) payload
	 * @return the response
	 * @throws IOException */
	public HttpResponse request(String method, String req_url, HashMap<String,String> hdr, String content_type, byte[] body) throws IOException {
		if (!req_url.startsWith("http")) req_url="http://"+req_url; 
		return request(method,new HttpRequestURL(req_url),hdr,content_type,body);
	}

	
	/** Sends a request.
	 * @param method the method (e.g. GET, POST, etc.)
	 * @param req_url request URL
	 * @param hdr (optional) additional header fields, as hdrname-to-hdrvalue table
	 * @param content_type (optional) content type
	 * @param body (optional) payload
	 * @return the response
	 * @throws IOException */
	public HttpResponse request(String method, HttpRequestURL req_url, HashMap<String,String> hdr, String content_type, byte[] body) throws IOException {
		if (hdr==null) hdr=new HashMap<String,String>();
		String hostport=req_url.getHostPort();
		int colon=hostport.indexOf(':');
		String host=colon>0?hostport.substring(0,colon):hostport;
		InetAddress iaddr=InetAddress.getByName(host);
		boolean secure=req_url.getScheme()==HttpRequestURL.Scheme.HTTPS;
		int port=colon>0?Integer.parseInt(hostport.substring(colon+1)):secure?DEFAULT_HTTPS_PORT:DEFAULT_HTTP_PORT;
		Socket socket;
		if (secure) {
			SSLSocket ssl_socket=(SSLSocket)SSLSocketFactory.getDefault().createSocket(host,port);
			socket=ssl_socket;
		}
		else {
			socket=new Socket(iaddr,port);
		}
		InputStream is=socket.getInputStream();
		OutputStream os=socket.getOutputStream();
		if (body!=null && content_type!=null) hdr.put("Content-Type",content_type);
		if (!hdr.containsKey("Host")) hdr.put("Host",host);
		String abspath=req_url.getAbsPath();
		HttpRequest req=new HttpRequest(method.toUpperCase(),abspath!=null?abspath:"/",hdr,body);
		os.write(req.toString().getBytes());
		
		HttpResponse resp=new HttpResponse(HttpMessage.parseHttpMessage(is));
		socket.close();		
		return resp;
	}

}
