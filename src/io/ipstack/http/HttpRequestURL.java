package io.ipstack.http;


import java.io.IOException;


public class HttpRequestURL {
	
	public static enum Scheme { HTTP, HTTPS }
	Scheme scheme=null;
	String hostport=null;
	String abspath=null;
	String query=null;

	
	/** Creates a new request URL.
	 * @param scheme HHTP or HTTPS scheme
	 * @param hostport host:port part
	 * @param abspath absolute path
	 * @param query query string */
	public HttpRequestURL(Scheme scheme, String hostport, String abspath, String query) {
		this.scheme=scheme;
		this.hostport=hostport;
		this.abspath=abspath;
		this.query=query;
	}
		
	/** Creates a new request URL.
	 * @param url the URL
	 * @throws IOException */
	public HttpRequestURL(String url) throws IOException {
		if (url.startsWith("http")) {
			int begin=url.indexOf("://");
			if (begin<0) throw new IOException("Malformed request URL: "+url);
			scheme=url.startsWith("https")? Scheme.HTTPS : Scheme.HTTP;
			begin+=3;
			int end=url.indexOf('/',begin);
			if (end<0) {
				hostport=url.substring(begin);
				url="";
			}
			else {
				hostport=url.substring(begin,end);
				url=url.substring(end);
			}
		}
		int query_index;
		if ((query_index=url.indexOf('?'))>=0) {
			query=url.substring(query_index+1);
			url=url.substring(0,query_index);
		}
		// deny the usage of ".."
		if (url.indexOf("..")>=0) throw new IOException("Malformed request URL: previous dir is not allowed");
		// remove all double '/'
		while (url.indexOf("//")>=0) url=url.replaceAll("//","/");
		if (url.length()>0) abspath=url;		
	}
		
	/**
	 * @return the scheme */
	public Scheme getScheme() {
		return scheme;
	}

	/**
	 * @return the hostport part */
	public String getHostPort() {
		return hostport;
	}

	/**
	 * @return the absolute path */
	public String getAbsPath() {
		return abspath;
	}
	
	/**
	 * @return the query */
	public String getQuery() {
		return query;
	}
	
	@Override
	public String toString() {
		StringBuffer sb=new StringBuffer();
		if (scheme!=null) sb.append(scheme).append("://").append(hostport);
		if (abspath!=null && !abspath.equals("/")) {
			if (abspath.length()>0 && abspath.charAt(0)!='/') sb.append('/');
			sb.append(abspath);
		}
		if (sb.length()==0) sb.append('/');
		if (query!=null && query.length()>0) {
			sb.append('?').append(query);
		}
		return sb.toString();
	}

}
