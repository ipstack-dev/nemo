package io.ipstack.http;


import java.io.IOException;
import java.util.HashMap;

import io.ipstack.net.ip4.SocketAddress;


/** Contains a HTTP request and the associated response information.
 */
public class HttpRequestHandle {

	/** Default response code */
	public static int DEFAULT_STATUS_CODE=400;
	
	// REQUEST
	
	/** Request method */
	String method;

	/** Request URL */
	HttpRequestURL request_url;
	
	/** Request message */
	HttpRequest req;

	/** Client socket_address */
	SocketAddress client_soaddr=null;

	// RESONSE	

	/** Status code */
	int status_code=DEFAULT_STATUS_CODE;

	/** Header fields of the response */
	HashMap<String,String> resp_hdr=new HashMap<>();

	/** Body of the response */
	byte[] resp_body=null;

	
	/** Creates a new exchange.
	 * @param req request message 
	 * @throws IOException */
	public HttpRequestHandle(HttpRequest req) throws IOException {
		this(req,null);
	}
	

	/** Creates a new exchange.
	 * @param req request message 
	 * @param soaddr the client socket address
	 * @throws IOException */
	public HttpRequestHandle(HttpRequest req, SocketAddress soaddr) throws IOException {
		this.req=req;
		this.client_soaddr=soaddr;
		method=req.getMethod();
		request_url=req.getRequestURL();
	}
	

	/** Gets the client socket address.
	 * @return the socket address */
	public SocketAddress getClientSocketAddress() {
		return client_soaddr;
	}

	/** Gets the request method.
	 * @return the method */
	public String getMethod() {
		return method;
	}

	/** Gets the request URL.
	 * @return the URL */
	public HttpRequestURL getRequestURL() {
		return request_url;
	}

	/** Gets the request.
	 * @return the request */
	public HttpRequest getRequest() {
		return req;
	}

	/** Sets the status code.
	 * @param status_code the status code of the response */
	public void setResponseCode(int status_code) {
		this.status_code=status_code;
	}

	/** Gets the status code of the response.
	 * @return the status code */
	public int getResponseCode() {
		return status_code;
	}
	
	/** Sets the content type of the response.
	 * @param content_type the content type to set */
	public void setResponseContentType(String content_type) {
		resp_hdr.put("Content-Type",content_type);
	}

	/** Gets the content type of the response.
	 * @return the content type */
	public String getResponseContentType() {
		return resp_hdr.get("Content-Type");
	}

	/** Sets a header field of the response.
	 * @param hname header name
	 * @param hvalue header value */
	public void setResponseHeaderField(String hname, String hvalue) {
		resp_hdr.put(hname,hvalue);
	}

	/** Gets header fields of the response.
	 * @return the header fields */
	public HashMap<String,String> getResponseHeaderFields() {
		return resp_hdr;
	}

	/** Sets the body of the response.
	 * @param body the body  */
	public void setResponseBody(byte[] body) {
		this.resp_body=body;
	}
	
	/** Gets the body of the response.
	 * @return the body */
	public byte[] getResponseBody() {
		return resp_body;
	}

}
