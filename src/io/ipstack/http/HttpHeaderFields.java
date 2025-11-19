package io.ipstack.http;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;


/** Map of header field names to values.
 * <p>
 * Header field names, when used as map key, are considered case-insensitive.
 */
public class HttpHeaderFields {

	/** Header values */
	protected HashMap<String,String> values=new HashMap<String,String>();

	/** Header names */
	protected HashMap<String,String> names=new HashMap<String,String>();

	
	public HttpHeaderFields() {
	}
	
	public void put(String name, String value) {
		String key=name.toLowerCase();
		names.put(key,name);
		values.put(key,value);
	}
	
	public void put(HashMap<String,String> hdr) {
		if (hdr!=null) for (String name: hdr.keySet()) this.put(name,hdr.get(name));
	}
	
	public void put(HttpHeaderFields hdr) {
		if (hdr!=null) for (String name: hdr.keySet()) this.put(name,hdr.get(name));
	}
	
	public String get(String name) {
		return values.get(name.toLowerCase());
	}	

	public boolean containsKey(String name) {
		return values.containsKey(name.toLowerCase());
	}	

	public Set<String> keySet() {
		return new HashSet<String>(names.values());
	}
}
