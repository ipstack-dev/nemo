package org.zoolu.util.json;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.zoolu.util.Bytes;


/** It provides methods for creating of an object from a JSON string and vice versa.
 */
public class Json {
	
	/** Comment line mark; If different from 'null', each line that starts with this mark is ignored */
	public static String COMMENT_MARK= null;
	
	
	private HashMap<Class<?>,HashMap<String,Field>> classMap= new HashMap<>(); // class -> fieldName -> field

	private String commentMark= COMMENT_MARK;
	
	
	/** Default constructor.
	 */
	public Json() {
	}

	
	/** Creates a new object from a JSON object.
	 * <p>
	 * It creates a Json object and than calls the {@link Json#fromJson(String, Class)} method.
	 * @param json the JSON object
	 * @param c class of the target object. The class must have the empty constructor
	 * @return the new object */
	public static <T> T fromJSON(String json, Class<T> c) {
		return new Json().fromJson(json,c);
	}

	/** Creates a new object from a JSON object.
	 * @param json the JSON object
	 * @param c class of the target object. The class must have the empty constructor
	 * @return the new object */
	public <T> T fromJson(String json, Class<T> c) {
		try {
			//T obj=c.newInstance();
			Constructor<T> ctor= c.getDeclaredConstructor();
			ctor.setAccessible(true);
			T obj=ctor.newInstance();
			jsonToObject(json,obj,true);
			return obj;
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	
	/** Creates a new array from a JSON array.
	 * <p>
	 * It creates a Json object and than calls the {@link Json#fromJsonArray(String, Class)} method.
	 * @param json the JSON array
	 * @param c class of the array elements. The class must have the empty constructor
	 * @return the new array */
	public static Object fromJSONArray(String json, Class<?> c) {
		return new Json().fromJsonArray(json,c);
	}

	
	/** Creates a new array from a JSON array.
	 * @param json the JSON array
	 * @param c class of the array elements. The class must have the empty constructor
	 * @return the new array */
	public Object fromJsonArray(String json, Class<?> c) {
		try {
			return jsonArrayToObject(new JsonParser(json).parseArray(),c);
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	
	/** Creates an object from a JSON object in a file.
	 * <p>
	 * It creates a Json object and than calls the {@link Json#fromJsonFile(File, Class)} method.
	 * @param file file containing the JSON object
	 * @param c the class of the target object. It must have the empty constructor
	 * @return the new object */
	public static Object fromJSONFile(File file, Class<?> c) {
		return new Json().fromJsonFile(file,c);
	}
	
	
	/** Creates an object from a JSON object in a file.
	 * @param file file containing the JSON object
	 * @param c the class of the target object. It must have the empty constructor
	 * @return the new object */
	public Object fromJsonFile(File file, Class<?> c) {
		try {
			//Object obj=c.newInstance();
			Constructor<?> ctor= c.getDeclaredConstructor();
			ctor.setAccessible(true);
			Object obj=ctor.newInstance();
			fromJSONFile(file,obj);
			return obj;
			
		}
		catch (InstantiationException | IllegalAccessException | NoSuchMethodException | SecurityException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
			return null;
		}		
	}

	
	/** Creates an array from a JSON array in a file.
	 * <p>
	 * It creates a Json object and than calls the {@link Json#fromJsonArrayFile(File, Class)} method.
	 * @param file file containing the JSON array
	 * @param c class of the array elements. The class must have the empty constructor
	 * @return the new array */
	public static Object fromJSONArrayFile(File file, Class<?> c) {
		return new Json().fromJsonArrayFile(file,c);
	}

		
	/** Creates an array from a JSON array in a file.
	 * @param file file containing the JSON array
	 * @param c class of the array elements. The class must have the empty constructor
	 * @return the new array */
	public Object fromJsonArrayFile(File file, Class<?> c) {
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
	 * <p>
	 * It creates a Json object and than calls the {@link Json#fromJson(String, Object)} method.
	 * @param json the JSON object
	 * @param obj the object to be set */
	public static void fromJSON(String json, Object obj) {
		new Json().fromJson(json,obj);
	}

	
	/** Sets an object from its representation as JSON object.
	 * @param json the JSON object
	 * @param obj the object to be set */
	public void fromJson(String json, Object obj) {
		try {
			jsonToObject(json,obj,true);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/** Sets an object from its representation as JSON object in a given file.
	 * <p>
	 * It creates a Json object and than calls the {@link Json#fromJsonFile(File, Object)} method.
	 * @param file file containing the JSON array
	 * @param obj the object to be set */
	public static void fromJSONFile(File file, Object obj) {
		new Json().fromJsonFile(file,obj);	
	}
	
	
	/** Sets an object from its representation as JSON object in a given file.
	 * @param file file containing the JSON array
	 * @param obj the object to be set */
	public void fromJsonFile(File file, Object obj) {
		try {
			BufferedReader reader=new BufferedReader(new FileReader(file));
			String line;
			StringBuffer sb=new StringBuffer();
			while ((line=reader.readLine())!=null) {
				if (commentMark!=null && line.trim().startsWith(commentMark)) continue; // skip comments
				sb.append(line);
			}
			reader.close();
			fromJSON(sb.toString(),obj);
			
		}
		catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	
	/** Returns the JSON string representing a given object.
	 * In case of an array object (int[], double[], boolean[], String[], Object[], etc) a JSON array ('[ <code>element</code>, <code>element</code>, ... ]') is returned.
	 * In all other cases, a JSON object ('{ <code>name</code> : <code>value</code>, <code>name</code> : <code>value</code>, ... }') is returned.
	 * <p>
	 * Same as calling static method {@link Json#toJSON(Object)}.
	 * @param obj the source object
	 * @return the JSON object */
	public String toJson(Object obj) {
		return toJSON(obj);
	}

		
	/** Returns the JSON string representing a given object.
	 * In case of an array object (int[], double[], boolean[], String[], Object[], etc) a JSON array ('[ <code>element</code>, <code>element</code>, ... ]') is returned.
	 * In all other cases, a JSON object ('{ <code>name</code> : <code>value</code>, <code>name</code> : <code>value</code>, ... }') is returned.
	 * @param obj the source object
	 * @return the JSON object */
	public static String toJSON(Object obj) {
		if (obj instanceof String[]) return AnyToJson.arrayToJson((String[])obj);
		if (obj instanceof short[]) return AnyToJson.arrayToJson((short[])obj);
		if (obj instanceof int[]) return AnyToJson.arrayToJson((int[])obj);
		if (obj instanceof long[]) return AnyToJson.arrayToJson((long[])obj);
		if (obj instanceof float[]) return AnyToJson.arrayToJson((float[])obj);
		if (obj instanceof double[]) return AnyToJson.arrayToJson((double[])obj);
		if (obj instanceof boolean[]) return AnyToJson.arrayToJson((boolean[])obj);
		if (obj instanceof Object[]) return AnyToJson.arrayToJson((Object[])obj);
		//if (obj instanceof List) return AnyToJson.arrayToJson((List<?>)obj).toString();
		if (obj instanceof Collection) return AnyToJson.collectionToJson((Collection<?>)obj);
		// else
		return AnyToJson.objectToJson(obj,true);
	}

	
	/** Writes the JSON string representing a given object into a file.
	 * <p>
	 * Same as calling static method {@link Json#toJSONFile(Object, File)}.
	 * @param obj the source object
	 * @param file the file where the JSON object hat to be written */
	public void toJsonFile(Object obj, File file) {
		toJSONFile(obj,file);
	}

	
	/** Writes the JSON string representing a given object into a file.
	 * @param obj the source object
	 * @param file the file where the JSON object hat to be written */
	public static void toJSONFile(Object obj, File file) {
		try {
			OutputStream os=new FileOutputStream(file);
			os.write(toJSON(obj).getBytes());
			os.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	
	
	// FORM JSON:
	
	private void jsonToObject(String json, Object obj, boolean fullAccess) throws IOException {
		jsonToObject(new JsonParser(json).parseObject(),obj,fullAccess);
	}
		
	
	private void jsonToObject(JsonObject jsonObj, Object obj, boolean fullAccess) throws IOException {
		Class<?> objClass= obj.getClass();
		HashMap<String,Field> fieldMap= classMap.get(objClass);
		if (fieldMap==null) {
			fieldMap= ObjectInspector.mapAllFields(obj.getClass());	
			classMap.put(objClass,fieldMap);
		}
		for (JsonMember m: jsonObj.getMembers()) {
			String attribute=m.getName();
			JsonValue value=m.getValue();
			if (value==JsonNull.NULL) return;
			// else
			//for (Field f: fields) {
			Field field=fieldMap.get(attribute);
			if (field!=null) {
				Class<?> type=field.getType();
				boolean unlock= !field.isAccessible() && fullAccess;
				if (unlock) field.setAccessible(true);
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
				//if (unlock) field.setAccessible(false);
			}
		}
	}

	
	
	private Object jsonValueToObject(JsonValue value, Class<?> type) throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
		if (isNumber(type)) return getTypedNumber((JsonNumber)value,type);
		else if (type==boolean.class) return new Boolean(((JsonBoolean)value).isTrue());
		else if (type==char.class) return new Character(((JsonString)value).getValue().charAt(0));
		else if (type==java.lang.String.class) return ((JsonString)value).getValue();
		else {
			//Object o=Class.forName(type.getName()).newInstance();
			Constructor<?> ctor= Class.forName(type.getName()).getDeclaredConstructor();
			ctor.setAccessible(true);
			Object o=ctor.newInstance();
			jsonToObject((JsonObject)value,o,true);
			return o;
		}
	}

	
	private Object jsonArrayToObject(JsonArray jsonArray, Class<?> type) throws IOException, InstantiationException, IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
		ArrayList<JsonValue> arrayValues=jsonArray.getValues();
		if (isNumber(type)) return getTypedNumberArray(arrayValues,type);
		else if (type==boolean.class) {
			boolean[] array=new boolean[arrayValues.size()];
			for (int i=0; i<array.length; i++) array[i]=((JsonBoolean)arrayValues.get(i)).isTrue();
			return array;
		}
		else if (type==java.lang.String.class) {
			ArrayList<String> array=new ArrayList<>();
			for (JsonValue val : arrayValues) array.add(((JsonString)val).getValue());
			return array.toArray(new String[0]);
		}
		else {
			ArrayList<Object> array=new ArrayList<>();
			for (JsonValue val : arrayValues) {
				//Object o=type.newInstance();
				Constructor<?> ctor= type.getDeclaredConstructor();
				ctor.setAccessible(true);
				Object o=ctor.newInstance();
				jsonToObject((JsonObject)val,o,true);
				array.add(o);
			}
			Object cArray=Array.newInstance(type,array.size());
			for (int i=0; i<array.size(); i++) Array.set(cArray,i,array.get(i));
			return cArray;
		}
	}
	
	
	// OTHERS:
	
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
		// TODO: reduce this code by using reflection
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
