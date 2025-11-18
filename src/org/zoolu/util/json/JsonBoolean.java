package org.zoolu.util.json;


public class JsonBoolean implements JsonValue {
	
	public static final JsonBoolean TRUE=new JsonBoolean(true);
	public static final JsonBoolean FALSE=new JsonBoolean(false);

	boolean isTrue;

	/**
	 * @param isTrue
	 */
	public JsonBoolean(boolean isTrue) {
		this.isTrue=isTrue;
	}

	/**
	 * @return the isTrue
	 */
	public boolean isTrue() {
		return isTrue;
	}
	
	@Override
	public String toString() {
		return isTrue? "true" :  "false";
	}
}
