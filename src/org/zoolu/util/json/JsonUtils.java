package org.zoolu.util.json;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.zoolu.util.Bytes;


/** Collection of methods for creating of an object from a JSON string and vice versa.
 */
public abstract class JsonUtils {
	

	/** Creates a new object from a JSON object.
	 * @param json the JSON object
	 * @param c class of the target object. It must have the empty constructor
	 * @return the new object */
	public static <T> T fromJson(String json, Class<T> c) {
		try {
			T obj=c.newInstance();
			jsonToObject(json,obj,true);
			return obj;
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	
	/** Creates a new array from a JSON array.
	 * @param json the JSON array
	 * @param c class of the array elements. The class must have the empty constructor
	 * @return the new array */
	public static Object fromJsonArray(String json, Class<?> c) {
		try {
			return jsonArrayToObject(new JsonParser(json).parseArray(),c);
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	
	/** Creates an object from a JSON object in a file.
	 * @param file file containing the JSON object
	 * @param c the class of the target object. It must have the empty constructor
	 * @return the new object */
	public static Object fromJsonFile(File file, Class<?> c) {
		try {
			Object obj=c.newInstance();
			fromJsonFile(file,obj);
			return obj;
			
		}
		catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
			return null;
		}		
	}

	
	/** Creates an array from a JSON array in a file.
	 * @param file file containing the JSON array
	 * @param c class of the array elements. The class must have the empty constructor
	 * @return the new array */
	public static Object fromJsonArrayFile(File file, Class<?> c) {
		try {
			BufferedReader reader=new BufferedReader(new FileReader(file));
			String line;
			StringBuffer sb=new StringBuffer();
			while ((line=reader.readLine())!=null) sb.append(line);
			reader.close();			
			return jsonArrayToObject(new JsonParser(sb.toString()).parseArray(),c);
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	
	/** Sets an object from its representation as JSON object.
	 * @param json the JSON object
	 * @param obj the object to be set */
	public static void fromJson(String json, Object obj) {
		try {
			jsonToObject(json,obj,true);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/** Sets an object from its representation as JSON object in a given file.
	 * @param file file containing the JSON array
	 * @param obj the object to be set */
	public static void fromJsonFile(File file, Object obj) {
		try {
			BufferedReader reader=new BufferedReader(new FileReader(file));
			String line;
			StringBuffer sb=new StringBuffer();
			while ((line=reader.readLine())!=null) sb.append(line);
			reader.close();
			fromJson(sb.toString(),obj);
			
		}
		catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	
	/** Gets the JSON object representing a given object.
	 * @param obj the source object
	 * @return the JSON object */
	public static String toJson(Object obj) {
		// TODO: remove the dependence from 'Jzon' implementation
		if (obj instanceof String[]) return new JzonArray((String[])obj).toString();
		if (obj instanceof short[]) return new JzonArray((short[])obj).toString();
		if (obj instanceof int[]) return new JzonArray((int[])obj).toString();
		if (obj instanceof long[]) return new JzonArray((long[])obj).toString();
		if (obj instanceof float[]) return new JzonArray((float[])obj).toString();
		if (obj instanceof double[]) return new JzonArray((double[])obj).toString();
		if (obj instanceof boolean[]) return new JzonArray((boolean[])obj).toString();
		if (obj instanceof Object[]) return new JzonArray((Object[])obj).toString();
		//if (obj instanceof List) return new JzonArray((List<?>)obj).toString();
		if (obj instanceof Collection) return new JzonArray((Collection<?>)obj).toString();
		return new JzonValue(obj).toString();
	}

	
	/** Writes the JSON object representing a given object into a file.
	 * @param obj the source object
	 * @param file the file where the JSON object hat to be wirtten */
	public static void toJsonFile(Object obj, File file) {
		try {
			OutputStream os=new FileOutputStream(file);
			os.write(toJson(obj).getBytes());
			os.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}		
	}

	
	/** Gets list of attributes.
	 * @param obj the source object
	 * @param fullAccess whether it has to get also private attributes
	 * @return the list of all attributes as array of JSON members */
	private ArrayList<JsonMember> getObjectMembers(Object obj, boolean fullAccess) {
		Field[] fields=(fullAccess)? obj.getClass().getDeclaredFields() : obj.getClass().getFields();
		ArrayList<JsonMember> members=new ArrayList<>();
		for (Field field : fields) {
			// @@@@@ force field accessibility
			boolean is_accessible=field.isAccessible();
			if (fullAccess && !is_accessible) field.setAccessible(true);
			try {
				JsonMember member=getMember(obj,field);
				if (member!=null) members.add(member);
			}
			catch (IllegalAccessException e) {
				e.printStackTrace();
			}
			// @@@@@ restore field accessibility
			if (fullAccess && !is_accessible) field.setAccessible(false);
		}
		return members;
	}
	
	
	/** Gets a given attribute.
	 * @param obj the source object
	 * @return the attribute as JSON member */
	private JsonMember getMember(Object obj, Field field) throws IllegalAccessException {
		Object value=field.get(obj);
		if (value==null) return null;
		// else
		Class<?> type=field.getType();
		JsonValue jval=null;
		if (isNumber(type)) jval=new JsonNumber((double)value);
		else
		if (type==boolean.class) jval=new JsonBoolean((Boolean)value);
		else
		if (type==char.class) jval=new JsonString((char)value);
		else
		if (type==java.lang.String.class) jval=new JsonString((String)value);
		else
		if (type.isArray()) jval=toJsonArray();
		else
		jval=toJsonObject(value);
		
		if (jval!=null) return new JsonMember(field.getName(),jval);
		else return null;
	}

	
	private static JsonArray toJsonArray() {
		// TODO
		return null;
	}

	
	private static JsonObject toJsonObject(Object o) {
		// TODO
		return null;
	}

	
	private static void jsonToObject(String json, Object obj, boolean fullAccess) throws IOException {
		jsonToObject(new JsonParser(json).parseObject(),obj,fullAccess);
	}
	
	
	private static void jsonToObject(JsonObject jsonObj, Object obj, boolean fullAccess) throws IOException {
		Field[] fields=(fullAccess)? obj.getClass().getDeclaredFields() : obj.getClass().getFields();
		for (JsonMember m: jsonObj.getMembers()) {
			String attribute=m.getName();
			JsonValue value=m.getValue();
			if (value==JsonNull.NULL) return;
			// else
			for (Field field : fields) {
				String fieldName=field.getName();
				if (attribute.equals(fieldName)) {
					Class<?> type=field.getType();
					// @@@@@ force field accessibility
					boolean is_accessible=field.isAccessible();
					if (fullAccess && !is_accessible) field.setAccessible(true);
					try {
						if (!type.isArray()) field.set(obj,jsonValueToObject(value,type));
						else {
							// array
							type=type.getComponentType();
							if (type==byte.class) field.set(obj,Bytes.fromFormattedHex(((JsonString)value).getValue()));
							else field.set(obj,jsonArrayToObject((JsonArray)value,type));
						}
					}
					catch (Exception e) {
						e.printStackTrace();
					}
					// @@@@@ restore field accessibility
					if (fullAccess && !is_accessible) field.setAccessible(false);
					break;
				} 
			}
		}
	}
	
	
	private static Object jsonValueToObject(JsonValue value, Class<?> type) throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException {
		if (isNumber(type)) return getTypedNumber((JsonNumber)value,type);
		else
		if (type==boolean.class) return new Boolean(((JsonBoolean)value).isTrue());
		else
		if (type==char.class) return new Character(((JsonString)value).getValue().charAt(0));
		else
		if (type==java.lang.String.class) return ((JsonString)value).getValue();
		else {
			Object o=Class.forName(type.getName()).newInstance();
			jsonToObject((JsonObject)value,o,true);
			return o;
		}
	}

	
	private static Object jsonArrayToObject(JsonArray jsonArray, Class<?> type) throws IOException, InstantiationException, IllegalAccessException {
		ArrayList<JsonValue> arrayValues=jsonArray.getValues();
		if (isNumber(type)) return getTypedNumberArray(arrayValues,type);
		else
		if (type==boolean.class) {
			boolean[] array=new boolean[arrayValues.size()];
			for (int i=0; i<array.length; i++) array[i]=((JsonBoolean)arrayValues.get(i)).isTrue();
			return array;
		}
		else
		if (type==java.lang.String.class) {
			ArrayList<String> array=new ArrayList<>();
			for (JsonValue val : arrayValues) array.add(((JsonString)val).getValue());
			return array.toArray(new String[0]);
		}
		else {
			ArrayList<Object> array=new ArrayList<>();
			for (JsonValue val : arrayValues) {
				Object o=type.newInstance();
				jsonToObject((JsonObject)val,o,true);
				array.add(o);
			}
			Object cArray=Array.newInstance(type,array.size());
			for (int i=0; i<array.size(); i++) Array.set(cArray,i,array.get(i));
			return cArray;
		}
	}

	
	private static boolean isNumber(Class<?> type) {
		return type==byte.class || type==short.class || type==int.class || type==long.class || type==float.class || type==double.class;
	}
	
	
	private static final Object getTypedNumber(JsonNumber num, Class<?> type) {
		return getTypedNumber(num.getValue(),type);
	}

	
	private static final Object getTypedNumber(double value, Class<?> type) {
		if (type==byte.class) return new Byte((byte)value);
		if (type==short.class) return new Short((short)value);
		if (type==int.class) return new Integer((int)value);
		if (type==long.class) return new Long((long)value);
		if (type==float.class) return new Float(value);
		if (type==double.class) return new Double(value);
		// else
		throw new RuntimeException("Invalid number type: "+type.toString());
	}

	
	private static final Object getTypedNumberArray(ArrayList<JsonValue> arrayValues, Class<?> type) {
		// TODO: try to reduce this code by using reflection
		if (type==byte.class) {
			byte[] array=new byte[arrayValues.size()];
			for (int i=0; i<array.length; i++) array[i]=(byte)((JsonNumber)arrayValues.get(i)).getValue();
			return array;
		}
		if (type==short.class) {
			short[] array=new short[arrayValues.size()];
			for (int i=0; i<array.length; i++) array[i]=(short)((JsonNumber)arrayValues.get(i)).getValue();
			return array;
		}
		if (type==int.class) {
			int[] array=new int[arrayValues.size()];
			for (int i=0; i<array.length; i++) array[i]=(int)((JsonNumber)arrayValues.get(i)).getValue();
			return array;
		}
		if (type==long.class) {
			long[] array=new long[arrayValues.size()];
			for (int i=0; i<array.length; i++) array[i]=(long)((JsonNumber)arrayValues.get(i)).getValue();
			return array;
		}
		if (type==float.class) {
			float[] array=new float[arrayValues.size()];
			for (int i=0; i<array.length; i++) array[i]=(float)((JsonNumber)arrayValues.get(i)).getValue();
			return array;
		}
		if (type==double.class) {
			double[] array=new double[arrayValues.size()];
			for (int i=0; i<array.length; i++) array[i]=(double)((JsonNumber)arrayValues.get(i)).getValue();
			return array;
		}
		// else
		throw new RuntimeException("Invalid number type: "+type.toString());
	}

}
