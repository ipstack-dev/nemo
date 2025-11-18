package org.zoolu.util.json;


public class JsonMember {

	String name;
	JsonValue value;
	
	/**
	 * @param name
	 * @param value
	 */
	public JsonMember(String name, JsonValue value) {
		this.name=name;
		this.value=value;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the value
	 */
	public JsonValue getValue() {
		return value;
	}
	
	@Override
	public String toString() {
		return "\""+name+"\":"+value.toString();
	}
	
	
}
