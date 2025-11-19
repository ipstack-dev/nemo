package org.zoolu.util.json;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


public class ObjectInspector {
	private ObjectInspector() {}
	
	/** Gets all public, private, and protected fields, including attributes inherited from all superclasses.
	 * @param type the class type
	 * @return the list of class fields
	 */
	public static List<Field> listAllFields(Class<?> type) {
		ArrayList<Field> fields= new ArrayList<>();
		for (Class<?> c= type; c!=null; c= c.getSuperclass()) fields.addAll(Arrays.asList(c.getDeclaredFields()));
		return fields;
	}

	
	/** Gets all public, private, and protected fields, including attributes inherited from all superclasses.
	 * @param type the class type
	 * @return a field dictionary mapping names to the corresponding class fields.
	 */
	public static HashMap<String,Field> mapAllFields(Class<?> type) {
		HashMap<String,Field> map= new HashMap<>();
		for (Class<?> c= type; c!=null; c= c.getSuperclass()) {
			for (Field f: c.getDeclaredFields()) map.put(f.getName(),f);
		}
		return map;
	}

	
	/** Gets all attributes of an object.
	 * @param obj the object
	 * @param fullAccess whether including also private, and protected attributes
	 * @return the attributes
	 */
	public static List<ObjectAttribute> getObjectAttributes(Object obj, boolean fullAccess) {
		List<Field> fields= listAllFields(obj.getClass());
		ArrayList<ObjectAttribute> attributes= new ArrayList<>();
		for (Field f: fields) {
			Object value= null;
			boolean unlock= !f.isAccessible() && fullAccess;
			if (unlock) f.setAccessible(true);
			try {
				value= f.get(obj);
			}
			catch (IllegalArgumentException|IllegalAccessException e) {
				e.printStackTrace();
			}
			// @@@@@ restore field accessibility
			//if (unlock) f.setAccessible(false);
			if (value!=null) attributes.add(new ObjectAttribute(f.getName(),value));
		}
		return attributes;
	}


	/** Beautifies an array class name.
	 * @return the name of an array class in a readable format (e.g. "int" in place of "[I", "java.lang.String" in place of "[Ljava.lang.String;", etc).
	 */
	public static String beautifyArrayClassName(String arrayClassName) {
		if (arrayClassName.charAt(1)=='L') return arrayClassName.substring(2,arrayClassName.length()-1)+"[]";
		else {
			switch (arrayClassName.charAt(1)) {
				case 'B': return "byte[]";
				case 'C': return "char[]";
				case 'D': return "double[]";
				case 'F': return "float[]";
				case 'I': return "int[]";
				case 'J': return "long[]";
				case 'S': return "short[]";
				case 'Z': return "boolean[]";
				case 'V': return "void[]";
			}
			return null;
		}
	}
}
