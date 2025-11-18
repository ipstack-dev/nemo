package org.zoolu.util.json;


import java.util.ArrayList;
import java.util.Arrays;


public class JsonObject implements JsonValue {
	
	ArrayList<JsonMember> members;

	
	/**
	 * @param members members of the JSON object
	 */
	public JsonObject(ArrayList<JsonMember> members) {
		this.members=members;
	}

	/**
	 * @param members members of the JSON object
	 */
	public JsonObject(JsonMember... members) {
		this.members=new ArrayList<>(Arrays.asList(members));
	}

	/**
	 * @return the members
	 */
	public ArrayList<JsonMember> getMembers() {
		return members;
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
