package org.zoolu.util.json;


import java.io.IOException;
import java.util.ArrayList;

import org.zoolu.util.Parser;


public class JsonParser {
	
	private static final char[] VALUE_SEPARATORS=new char[]{',',']','}',' ','\t','\r','\n'};
	
	Parser par;
	
	public JsonParser(String str) {
		this.par=new Parser(str);
	}

	public JsonObject parseObject() throws IOException {
		ArrayList<JsonMember> members=new ArrayList<>();
		par.skipWSPCRLF();
		char c=par.getChar();
		if (c!='{') throw new IOException("Malformed JSON object: '{' is missing: "+c);
		boolean first=true;
		while (true) {
			par.skipWSPCRLF();
			c=par.nextChar();
			if (c=='}') {
				par.skipChar();
				return new JsonObject(members);
			}
			// else
			if (first) first=false;
			else
			if (c!=',') throw new IOException("Malformed JSON object: it was expected ',', it has been found '"+c+"'");
			else {
				par.skipChar().skipWSPCRLF();
				c=par.nextChar();
			}
			if (c=='"') {
				String name=par.getStringUnquoted();
				par.skipWSPCRLF();
				c=par.getChar();
				if (c!=':') throw new IOException("Malformed JSON object: it was expected ':', it has been found '"+c+"'");
				JsonValue value=parseValue();
				members.add(new JsonMember(name,value));
				par.skipWSPCRLF();
				first=false;
			}
			else throw new IOException("Malformed JSON object: it was expected '\"', it has been found '"+c+"'");
		}
	}

	
	public JsonValue parseValue() throws IOException {
		par.skipWSPCRLF();
		char c=par.nextChar();
		if (c=='{') return parseObject();
		else
		if (c=='[') return parseArray();
		else
		if (c=='\"') return new JsonString(par.getEscapedUnquotedString());
		else {
			String value=par.getWord(VALUE_SEPARATORS);
			if (value.equals("true")) return JsonBoolean.TRUE;
			if (value.equals("false")) return JsonBoolean.FALSE;
			if (value.equals("null")) return JsonNull.NULL;
			// else
			return new JsonNumber(Double.parseDouble(value));
		}
	}

	
	public JsonArray parseArray() throws IOException {
		ArrayList<JsonValue> values=new ArrayList<>();
		par.skipWSPCRLF();
		char c=par.getChar();
		if (c!='[') throw new IOException("Malformed JSON array: '[' is missing: "+c);
		boolean first=true;
		while (true) {
			par.skipWSPCRLF();
			c=par.nextChar();
			if (c==']') {
				par.skipChar();
				return new JsonArray(values);
			}
			// else
			if (first) first=false;
			else				
			if (c!=',') throw new IOException("Malformed JSON object: it was expected ',', it has been found '"+c+"'");
			else {
				par.skipChar().skipWSPCRLF();
				c=par.nextChar();
			}
			values.add(parseValue());
		}
	}

	
	public String getRemainngString() {
		return par.getRemainingString();
	}
}
