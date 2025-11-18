package org.zoolu.util.json;


import java.util.ArrayList;
import java.util.Arrays;


public class JsonArray implements JsonValue {

	ArrayList<JsonValue> values;
	
	/**
	 * @param values the array values
	 */
	public JsonArray(ArrayList<JsonValue> values) {
		this.values=values;
	}

	/**
	 * @param values the array values
	 */
	public JsonArray(JsonValue... values) {
		this.values=new ArrayList<>(Arrays.asList(values));
	}

	/**
	 * @return the values
	 */
	public ArrayList<JsonValue> getValues() {
		return values;
	}
	
	@Override
	public String toString() {
		StringBuffer sb=new StringBuffer();
		sb.append('[');
		boolean first=true;
		for (JsonValue v : values) {
			if (first) first=false; else sb.append(',');
			sb.append(v.toString());
		}
		sb.append(']');
		return sb.toString();
		
	}
}
