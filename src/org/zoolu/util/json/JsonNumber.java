package org.zoolu.util.json;


public class JsonNumber implements JsonValue {

	double value;

	/**
	 * @param value
	 */
	public JsonNumber(double value) {
		this.value=value;
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
		return isInteger()? String.valueOf((long)value) : String.valueOf(value);
	}
}
