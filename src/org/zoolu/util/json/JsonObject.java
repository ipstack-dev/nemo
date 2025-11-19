package org.zoolu.util.json;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.zoolu.util.Bytes;


/** A JSON object.
 * <p>
 * Can be created by adding the various JSON object members and/or from a Java object.
 */
public class JsonObject implements JsonValue {
	
	ArrayList<JsonMember> members;

	
	/** Creates an empty JSON object.
	 */
	public JsonObject() {
		this.members=new ArrayList<>();
	}
	
	/** Creates a JSON object from a Java object.
	 * @param obj the Java object
	 */
	public JsonObject(Object obj) {
		this(obj,true);
	}
	
	/** Creates a JSON object from a Java object.
	 * @param obj the Java object
	 * @param fullAccess whether including also protected and private attribute (if any)
	 */
	public JsonObject(Object obj, boolean fullAccess) {
		this();
		List<ObjectAttribute> attributes= ObjectInspector.getObjectAttributes(obj,fullAccess);
		for (ObjectAttribute a: attributes) {
			String name= a.name;
			Object value= a.value;
			if (value==null) continue;	
			if (name.charAt(name.length()-1)=='_') name= name.substring(0,name.length()-1);
			
			if (value instanceof java.lang.String) members.add(new JsonMember(name,new JsonString(AnyToJson.escape((String)value))));
			else if (value instanceof Integer) members.add(new JsonMember(name,new JsonNumber((Integer)value)));
			else if (value instanceof Byte) members.add(new JsonMember(name,new JsonNumber((Byte)value)));
			else if (value instanceof Short) members.add(new JsonMember(name,new JsonNumber((Short)value)));
			else if (value instanceof Long) members.add(new JsonMember(name,new JsonNumber((Long)value)));
			else if (value instanceof Float) members.add(new JsonMember(name,new JsonNumber((Float)value)));
			else if (value instanceof Double) members.add(new JsonMember(name,new JsonNumber((Double)value)));
			else if (value instanceof Boolean) members.add(new JsonMember(name,new JsonBoolean((Boolean)value)));
			else if (value instanceof Character) members.add(new JsonMember(name,new JsonString(String.valueOf((Character)value))));
			else if (value.getClass().getName().charAt(0)=='[') {
				if (value instanceof java.lang.String[]) members.add(new JsonMember(name,new JsonArray((String[])value)));
				else if (value instanceof int[]) members.add(new JsonMember(name,new JsonArray((int[])value)));
				else if (value instanceof byte[]) members.add(new JsonMember(name,new JsonString(Bytes.toHex((byte[])value))));
				else if (value instanceof short[]) members.add(new JsonMember(name,new JsonArray((short[])value)));
				else if (value instanceof long[]) members.add(new JsonMember(name,new JsonArray((long[])value)));
				else if (value instanceof float[]) members.add(new JsonMember(name,new JsonArray((float[])value)));
				else if (value instanceof double[]) members.add(new JsonMember(name,new JsonArray((double[])value)));
				else if (value instanceof boolean[]) members.add(new JsonMember(name,new JsonArray((boolean[])value)));
				else members.add(new JsonMember(name,new JsonArray((Object[])value)));
			}
			else members.add(new JsonMember(name,new JsonObject(value)));
		}		
	}
	
	/**  Creates a JSON object.
	 * @param members members of the JSON object
	 */
	public JsonObject(ArrayList<JsonMember> members) {
		this.members=members;
	}
	
	/**  Creates a JSON object from a Java object.
	 * @param members members of the JSON object
	 */
	public JsonObject(JsonMember... members) {
		this.members=new ArrayList<>(Arrays.asList(members));
	}
	
	/** Gets object members.
	 * @return the object members
	 */
	public ArrayList<JsonMember> getMembers() {
		return members;
	}
	
	/** Adds a member.
	 * @param member the JSON member
	 * @return the JSON object
	 */
	public JsonObject add(JsonMember member) {
		members.add(member);
		return this;
	}
	
	/** Adds a member.
	 * @param name member name
	 * @param value member value
	 * @return the JSON object
	 */
	public JsonObject add(String name, JsonObject value) {
		return add(new JsonMember(name,value));
	}

	
	/** Adds a number member.
	 * @param name member name
	 * @param value member value
	 * @return the JSON object
	 */
	public JsonObject add(String name, long value) {
		return add(new JsonMember(name,new JsonNumber(value)));
	}
	
	/** Adds a number member.
	 * @param name member name
	 * @param value member value
	 * @return the JSON object
	 */
	public JsonObject add(String name, float value) {
		return add(new JsonMember(name,new JsonNumber(value)));
	}

	/** Adds a number member.
	 * @param name member name
	 * @param value member value
	 * @return the JSON object
	 */
	public JsonObject add(String name, double value) {
		return add(new JsonMember(name,new JsonNumber(value)));
	}
	
	/** Adds a boolean (true/false) member.
	 * @param name member name
	 * @param value member value
	 * @return the JSON object
	 */
	public JsonObject add(String name, boolean value) {
		return add(new JsonMember(name,new JsonBoolean(value)));
	}
	
	/** Adds a string member.
	 * @param name member name
	 * @param value member value
	 * @return the JSON object
	 */
	public JsonObject add(String name, String value) {
		return add(new JsonMember(name,new JsonString(value)));
	}
	
	/** Adds an object member.
	 * @param name member name
	 * @param value member value
	 * @return the JSON object
	 */
	public JsonObject add(String name, Object value) {
		return add(new JsonMember(name,new JsonObject(value)));
	}

	/** Adds an array member.
	 * @param name member name
	 * @param value member value
	 * @return the JSON object
	 */
	public JsonObject add(String name, short[] value) {
		return add(new JsonMember(name,new JsonArray(value)));
	}
	
	/** Adds an array member.
	 * @param name member name
	 * @param value member value
	 * @return the JSON object
	 */
	public JsonObject add(String name, int[] value) {
		return add(new JsonMember(name,new JsonArray(value)));
	}
	
	/** Adds an array member.
	 * @param name member name
	 * @param value member value
	 * @return the JSON object
	 */
	public JsonObject add(String name, long[] value) {
		return add(new JsonMember(name,new JsonArray(value)));
	}
	
	/** Adds an array member.
	 * @param name member name
	 * @param value member value
	 * @return the JSON object
	 */
	public JsonObject add(String name, boolean[] value) {
		return add(new JsonMember(name,new JsonArray(value)));
	}
	
	/** Adds an array member.
	 * @param name member name
	 * @param value member value
	 * @return the JSON object
	 */
	public JsonObject add(String name, float[] value) {
		return add(new JsonMember(name,new JsonArray(value)));
	}
	
	/** Adds an array member.
	 * @param name member name
	 * @param value member value
	 * @return the JSON object
	 */
	public JsonObject add(String name, double[] value) {
		return add(new JsonMember(name,new JsonArray(value)));
	}
	
	/** Adds an array member.
	 * @param name member name
	 * @param value member value
	 * @return the JSON object
	 */
	public JsonObject add(String name, String[] value) {
		return add(new JsonMember(name,new JsonArray(value)));
	}
	
	/** Adds an array member.
	 * @param name member name
	 * @param value member value
	 * @return the JSON object
	 */
	public JsonObject add(String name, Object[] value) {
		return add(new JsonMember(name,new JsonArray(value)));
	}

	
	@Override
	public String toString() {
		StringBuffer sb=new StringBuffer();
		sb.append('{');
		boolean first=true;
		for (JsonMember m : members) {
			if (first) first=false; else sb.append(',');
			sb.append(m.toString());
		}
		sb.append('}');
		return sb.toString();
	}
}
