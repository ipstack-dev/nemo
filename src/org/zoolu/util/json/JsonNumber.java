package org.zoolu.util.json;


public class JsonNumber implements JsonValue {

	double value;
	boolean isFloat=false;

	
	/**
	 * @param value
	 */
	public JsonNumber(double value) {
		this.value=value;
	}

	/**
	 * @param value
	 */
	public JsonNumber(float value) {
		this.value=value;
		isFloat=true;
	}

	/**
	 * @return the value
	 */
	public double getValue() {
		return value;
	}
	
	/**
	 * @return the isInteger
	 */
	public boolean isInteger() {
		return Math.floor(value)==value;
	}

	@Override
	public String toString() {
		return isInteger()? String.valueOf((long)value) : (isFloat?String.valueOf((float)value):String.valueOf(value));
	}
}
