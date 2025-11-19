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
	 * @param vv the array values
	 */
	public JsonArray(JsonValue[] vv) {
		values=new ArrayList<>(Arrays.asList(vv));
	}

	/**
	 * @param nn the array values
	 */
	public JsonArray(byte[] nn) {
		values=new ArrayList<>();
		for (byte n: nn) values.add(new JsonNumber(n));
	}
	
	/**
	 * @param nn the array values
	 */
	public JsonArray(short[] nn) {
		values=new ArrayList<>();
		for (short n: nn) values.add(new JsonNumber(n));
	}

	/**
	 * @param nn the array values
	 */
	public JsonArray(int[] nn) {
		values=new ArrayList<>();
		for (int n: nn) values.add(new JsonNumber(n));
	}

	/**
	 * @param nn the array values
	 */
	public JsonArray(long[] nn) {
		values=new ArrayList<>();
		for (long n: nn) values.add(new JsonNumber(n));
	}

	/**
	 * @param nn the array values
	 */
	public JsonArray(float[] nn) {
		values=new ArrayList<>();
		for (float n: nn) values.add(new JsonNumber(n));
	}

	/**
	 * @param nn the array values
	 */
	public JsonArray(double[] nn) {
		values=new ArrayList<>();
		for (double n: nn) values.add(new JsonNumber(n));
	}

	/**
	 * @param bb the array values
	 */
	public JsonArray(boolean[] bb) {
		values=new ArrayList<>();
		for (boolean b: bb) values.add(new JsonBoolean(b));
	}

	/**
	 * @param ss the array values
	 */
	public JsonArray(String[] ss) {
		values=new ArrayList<>();
		for (String s: ss) values.add(new JsonString(s));
	}

	/**
	 * @param oo the array values
	 */
	public JsonArray(Object[] oo) {
		values=new ArrayList<>();
		for (Object o: oo) {
			JsonObject jo= new JsonObject(o);
			values.add(jo);
		}
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
