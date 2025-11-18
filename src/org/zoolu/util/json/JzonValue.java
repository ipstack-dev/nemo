package org.zoolu.util.json;


import java.util.List;

import org.zoolu.util.config.ReflectiveObject;


class JzonValue {
	String str;

	public JzonValue(String s) {
		str='"'+escape(s)+'"';
	}

	public JzonValue(long n) {
		str=String.valueOf(n);
	}

	public JzonValue(double n) {
		str=String.valueOf(n);
	}

	public JzonValue(boolean b) {
		str=String.valueOf(b);
	}	

	public JzonValue(JzonObject o) {
		str=o.toString();
	}	

	public JzonValue(JzonArray a) {
		str=a.toString();
	}	

	public JzonValue(Object[] a) {
		this(a,true);
	}

	protected JzonValue(Object[] a, boolean fullAccess) {
		StringBuffer sb=new StringBuffer();
		sb.append('[');		
		boolean first=true;
		for (Object o : a) {
			if (first) first=false; else sb.append(',');
			sb.append(new JzonValue(o,fullAccess).toString());
		}
		sb.append(']');
		str=sb.toString();
	}

	public JzonValue(Object obj) {
		this(obj,true);
	}

	protected JzonValue(Object obj, boolean fullAccess) {
		StringBuffer sb=new StringBuffer();
		sb.append('{');
		ReflectiveObject ref=new ReflectiveObject(obj,fullAccess);
		String[] attributes=ref.getAttributes();
		//System.out.println("JsonValue: DEBUG: attributes: "+attributes.length);
		boolean first=true;
		for (String a : attributes) {
			String type=ref.getAttributeType(a);
			String value=ref.getAttributeStringValue(a);
			if (value==null) continue;
			if (first) first=false; else sb.append(',');
			String name=a.charAt(a.length()-1)=='_'? a.substring(0,a.length()-1) : a;
			sb.append('"').append(name).append('"').append(':');
			//if (value==null) sb.append("null");
			//else
			if (type.equals("byte") || type.equals("short") || type.equals("int") || type.equals("long") || type.equals("float") || type.equals("double")) sb.append(value);
			else
			if (type.equals("boolean")) sb.append(value=="yes"?"true":"false");
			else
			if (type.equals("char") || type.equals("java.lang.String")) sb.append('"').append(escape(value)).append('"');
			else
			if (type.substring(type.length()-2,type.length()).equals("[]")) {
				String elemType=type.substring(0,type.length()-2);
				if (elemType.equals("byte")) sb.append('"').append(value).append('"');
				else
				if (elemType.equals("short") || elemType.equals("int") || elemType.equals("long") || elemType.equals("float") || elemType.equals("double")) sb.append('[').append(value).append(']');
				else
				if (elemType.equals("java.lang.String")) sb.append(new JzonArray((String[])ref.getAttributeValue(a)).toString());
				else sb.append(new JzonValue((Object[])ref.getAttributeValue(a),fullAccess).toString());
			}
			else {
				sb.append(new JzonValue(ref.getAttributeValue(a),fullAccess).toString());
			};				
		}
		sb.append('}');
		str=sb.toString();
	}
	
	@Override
	public String toString() { return str; }
	
	
	static String escape(String str) {
		return str.replace((CharSequence)"\"",(CharSequence)"\\\"");
	}
}
