package io.ipstack.http;


import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;


public class HttpRequest extends HttpMessage {

	public static String GET="GET";
	public static String HEAD="HEAD";
	public static String POST="POST";
	public static String PUT="PUT";
	public static String DELETE="DELETE";
	public static String CONNECT="CONNECT";
	public static String OPTIONS="OPTIONS";
	public static String TRACE="TRACE";
	public static String PATCH="PATCH";
	
	/*public HttpRequest() {
	}*/

	public HttpRequest(String method, String request_url, HashMap<String, String> header_fields, byte[] body) {
		super(method+" "+request_url+" HTTP/1.1",header_fields,body);
	}

	public HttpRequest(HttpMessage msg) {
		super(msg);
		// TODO: check the syntax
	}
	
	public static HttpRequest parseHttpRequest(InputStream is) throws IOException {
		return new HttpRequest(HttpMessage.parseHttpMessage(is));
	}
	
	public String getMethod() {
		String[] request_line_fields=first_line.split("\\s+");
		return request_line_fields[0];
	}

	public HttpRequestURL getRequestURL() throws IOException {
		String[] request_line_fields=first_line.split("\\s+");
		String url=request_line_fields[1];
		return new HttpRequestURL(url);
	}

	public String getVersion() {
		String[] request_line_fields=first_line.split("\\s+");
		return request_line_fields[2];
	}

}
