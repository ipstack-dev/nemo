package org.zoolu.util.json;


public class JsonString implements JsonValue {
	
	String value;

	/**
	 * @param value
	 */
	public JsonString(String value) {
		this.value=value;
	}

	/**
	 * @param chars
	 */
	public JsonString(char... chars) {
		this.value=new String(chars);
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return unescape(value);
	}

	@Override
	public String toString() {
		return "\""+value+"\"";
	}
	
	public static String unescape(String str) {
		return str.replace((CharSequence)"\\\"",(CharSequence)"\"");
	}

	
}
