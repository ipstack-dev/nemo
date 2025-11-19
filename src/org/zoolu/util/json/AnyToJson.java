package org.zoolu.util.json;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.zoolu.util.Bytes;


/** Collection of static methods for converting an object or an object array to a JSON string.
 * <p>
 * The conversion is performed directly without creating any {@link JsonObject} or {@link JsonArray} objects.
 */
class AnyToJson {
	private AnyToJson() {}
	
	
	// TO JSON:
	
	/** Gets the JSON string representation of a non-array object.
	 * @param obj the object
	 * @param fullAccess whether accessing also protected and private members
	 * @return JSON string
	 */
	static String objectToJson(Object obj, boolean fullAccess) {
		StringBuffer sb=new StringBuffer();
		sb.append('{');
		boolean first=true;
		List<ObjectAttribute> attributes= ObjectInspector.getObjectAttributes(obj,fullAccess);
		for (ObjectAttribute a: attributes) {
			String name= a.name;
			Object value= a.value;
			if (value==null) continue;	
			if (name.charAt(name.length()-1)=='_') name= name.substring(0,name.length()-1);
			sb.append(first?'"':",\"").append(name).append('"').append(':');
			first=false;
			
			if (value instanceof java.lang.String) sb.append('"').append(escape((String)value)).append('"');
			else if (value instanceof Integer) sb.append((Integer)value);
			else if (value instanceof Byte) sb.append((Byte)value);
			else if (value instanceof Short) sb.append((Short)value);
			else if (value instanceof Long) sb.append((Long)value);
			else if (value instanceof Float) sb.append((Float)value);
			else if (value instanceof Double) sb.append((Double)value);
			else if (value instanceof Boolean) sb.append((Boolean)value);
			else if (value instanceof Character) sb.append('"').append((Character)value).append('"');
			else if (value.getClass().getName().charAt(0)=='[') {
				if (value instanceof java.lang.String[]) sb.append(arrayToJson((String[])value));
				else if (value instanceof int[]) sb.append(arrayToJson((int[])value));
				else if (value instanceof byte[]) sb.append('"').append(Bytes.toHex((byte[])value)).append('"');
				else if (value instanceof short[]) sb.append(arrayToJson((short[])value));
				else if (value instanceof long[]) sb.append(arrayToJson((long[])value));
				else if (value instanceof float[]) sb.append(arrayToJson((float[])value));
				else if (value instanceof double[]) sb.append(arrayToJson((double[])value));
				else if (value instanceof boolean[]) sb.append(arrayToJson((boolean[])value));
				else sb.append(objectArrayToJson((Object[])value,fullAccess));
			}
			else sb.append(objectToJson(value,fullAccess));
		}
		sb.append('}');
		return sb.toString();
	}
	
	
	/** Gets the JSON string representation of an array of objects.
	 * @param obj the array
	 * @param fullAccess whether accessing also protected and private members
	 * @return JSON string
	 */
	private static String objectArrayToJson(Object[] aa, boolean fullAccess) {
		StringBuffer sb=new StringBuffer();
		sb.append('[');		
		boolean first=true;
		for (Object o: aa) {
			if (first) first=false; else sb.append(',');
			sb.append(objectToJson(o,fullAccess));
		}
		sb.append(']');
		return sb.toString();
	}
	
	
	static String arrayToJson(String[] vv) {
		int len=vv!=null? vv.length : 0;
		String[] values=new String[len];
		for (int i=0; i<values.length; i++) values[i]='"'+escape(vv[i])+'"';
		return arrayToString(values);
	}

	
	static String arrayToJson(byte[] vv) {
		return '['+Bytes.toHex(vv)+']';
	}

	
	static String arrayToJson(short[] vv) {
		int len=vv!=null? vv.length : 0;
		String[] values=new String[len];
		for (int i=0; i<values.length; i++) values[i]=String.valueOf(values[i]);
		return arrayToString(values);
	}

	
	static String arrayToJson(int[] vv) {
		int len=vv!=null? vv.length : 0;
		String[] values=new String[len];
		for (int i=0; i<values.length; i++) values[i]=String.valueOf(vv[i]);
		return arrayToString(values);
	}

	
	static String arrayToJson(long[] vv) {
		int len=vv!=null? vv.length : 0;
		String[] values=new String[len];
		for (int i=0; i<values.length; i++) values[i]=String.valueOf(vv[i]);
		return arrayToString(values);
	}

	
	static String arrayToJson(float[] vv) {
		int len=vv!=null? vv.length : 0;
		String[] values=new String[len];
		for (int i=0; i<values.length; i++) values[i]=String.valueOf(vv[i]);
		return arrayToString(values);
	}

	
	static String arrayToJson(double[] vv) {
		int len=vv!=null? vv.length : 0;
		String[] values=new String[len];
		for (int i=0; i<values.length; i++) values[i]=String.valueOf(vv[i]);
		return arrayToString(values);
	}

	
	static String arrayToJson(boolean[] vv) {
		int len=vv!=null? vv.length : 0;
		String[] values=new String[len];
		for (int i=0; i<values.length; i++) values[i]=String.valueOf(vv[i]);
		return arrayToString(values);
	}

	static String arrayToJson(Object[] vv) {
		int len=vv!=null? vv.length : 0;
		String[] values=new String[len];
		for (int i=0; i<values.length; i++) values[i]=objectToJson(vv[i],true);
		return arrayToString(values);
	}

	
	static <T> String collectionToJson(Collection<T> cc) {
		String[] values=new String[cc!=null?cc.size():0];
		Iterator<T> list=cc.iterator();
		for (int i=0; i<values.length; i++) {
			T elem=list.next();
			if (elem instanceof String) values[i]='"'+escape(((String)elem))+'"';
			else if (elem instanceof Short || elem instanceof Integer || elem instanceof Long) values[i]=String.valueOf((long)elem);
			else if (elem instanceof Float || elem instanceof Double) values[i]=String.valueOf((double)elem);
			else if (elem instanceof Boolean) values[i]=String.valueOf((boolean)elem);
			else values[i]=objectToJson(elem,true);
		}
		return arrayToString(values);
	}
	
	
	// OTHERS:
	
	static String escape(String str) {
		return str.replace((CharSequence)"\"",(CharSequence)"\\\"");
	}


	private static String arrayToString(String[] vv) {
		StringBuffer sb=new StringBuffer();	 
		sb.append('[');
		for (int i=0; vv!=null && i<vv.length; i++) {
			if (i>0) sb.append(',');
			sb.append(vv[i]);
		}
		sb.append(']');
		return sb.toString();
	}



}
