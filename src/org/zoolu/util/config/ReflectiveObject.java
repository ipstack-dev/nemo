package org.zoolu.util.config;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.zoolu.util.Bytes;
import org.zoolu.util.Parser;


/** Allows to inspect and modify attribute values of an object using attribute names and value strings.
 */
public class ReflectiveObject {
	
	Object obj;
	
	Field[] fields;
	
	boolean fullAccess;
	
	
	public ReflectiveObject(Object obj, boolean fullAccess) {
		this.obj=obj;
		this.fullAccess=fullAccess;
		//fields=(fullAccess)? obj.getClass().getDeclaredFields() : obj.getClass().getFields();
		fields=getAllFields(obj.getClass());
	}

	
	/** Gets attributes. */
	public String[] getAttributes() {
		String[] attributes=new String[fields.length];
		for (int i=0; i<fields.length; i++) {
			attributes[i]=fields[i].getName();
		}
		return attributes;
	}

	
	/** Gets attribute type. */
	public String getAttributeType(String attribute) {
		String type=null;
		for (int i=0; i<fields.length; i++) {
			String field_name=fields[i].getName();
			if (attribute.equals(field_name)) {
				Class<?> field_class=fields[i].getType();
				if (field_class.isArray()) {
					type=getArrayClassTypeString(field_class.getName())+"[]";
				}
				else type=field_class.getName();
				break;
			}
		}
		return type;
	}

	
	/** Gets attribute value as string. */
	public Object getAttributeValue(String attribute) {
		Object value=null;
		//java.lang.reflect.Field[] fields=target.getClass().getFields();
		for (int i=0; i<fields.length; i++) {
			String field_name=fields[i].getName();
			if (attribute.equals(field_name)) {
				Class<?> field_class=fields[i].getType();
				// @@@@@ force field accessibility
				boolean is_accessible=fields[i].isAccessible();
				if (fullAccess && !is_accessible) fields[i].setAccessible(true);
				try {
					value=fields[i].get(obj);
				}
				catch (Exception e) {
					e.printStackTrace();
				}
				// @@@@@ restore field accessibility
				if (fullAccess && !is_accessible) fields[i].setAccessible(false);
				break; 
			}
		}
		return value;
	}
	
	
	/** Gets attribute value as string. */
	public String getAttributeStringValue(String attribute) {
		String value=null;
		//java.lang.reflect.Field[] fields=target.getClass().getFields();
		for (int i=0; i<fields.length; i++) {
			String field_name=fields[i].getName();
			if (attribute.equals(field_name)) {
				Class<?> field_class=fields[i].getType();
				String field_class_name=field_class.getName();
				// @@@@@ force field accessibility
				boolean is_accessible=fields[i].isAccessible();
				if (fullAccess && !is_accessible) fields[i].setAccessible(true);
				try {
					if (field_class.isArray()) {
						Object value_obj=fields[i].get(obj);
						int len=(value_obj!=null)? java.lang.reflect.Array.getLength(value_obj) : 0;
						if (len>0) {
							if (getArrayClassTypeString(field_class_name).equals("byte")) {
								value=Bytes.toHex((byte[])value_obj);
							}
							else {
								StringBuffer sb=new StringBuffer();
								sb.append(java.lang.reflect.Array.get(value_obj,0));
								for (int j=1; j<len; j++) sb.append(',').append(java.lang.reflect.Array.get(value_obj,j));
								value=sb.toString();								
							}
						}
					}
					else {
						if (field_class_name.equals("boolean")) value=(((Boolean)fields[i].get(obj)).booleanValue())? "yes":"no";
						else {
							Object value_obj=fields[i].get(obj);
							if (value_obj!=null) value=value_obj.toString();
						}
					}
				}
				catch (Exception e) {
					e.printStackTrace();
				}
				// @@@@@ restore field accessibility
				if (fullAccess && !is_accessible) fields[i].setAccessible(false);
				break; 
			}
		}
		return value;
	}
	
	
	/** Sets an attribute value. */
	public void setAttribute(String attribute, String value) {
		//if (value==null) value="null";
		if (value==null) return;
		// else
		final char[] DELIM={' ',','};
		Parser par=new Parser(value);
		//java.lang.reflect.Field[] fields=target.getClass().getFields();
		for (int i=0; i<fields.length; i++) {
			String field_name=fields[i].getName();
			if (attribute.equals(field_name)) {
				Class<?> field_class=fields[i].getType();
				String field_class_name=field_class.getName();
				// @@@@@ force field accessibility
				boolean is_accessible=fields[i].isAccessible();
				if (fullAccess && !is_accessible) fields[i].setAccessible(true);
				try {
					if (field_class.isArray()) {
						field_class_name=getArrayClassTypeString(field_class_name);
						Object value_obj=fields[i].get(obj);
						int len=(value_obj!=null)? java.lang.reflect.Array.getLength(value_obj) : 0;
						if (field_class_name.equals("java.lang.String")) {
							String[] str_array=par.getWordArray(DELIM);
							if (len>0) {
								String[] aux=new String[len+str_array.length];
								for (int j=0; j<aux.length; j++) aux[j]=(j<len)? (String)java.lang.reflect.Array.get(value_obj,j) : str_array[j-len];
								str_array=aux;
							}
							fields[i].set(obj,str_array);
						}
						else
						if (field_class_name.equals("int")) {
							int[] int_array=par.getIntArray(DELIM);
							if (len>0) {
								int[] aux=new int[len+int_array.length];
								for (int j=0; j<aux.length; j++) aux[j]=(j<len)? ((Integer)java.lang.reflect.Array.get(value_obj,j)).intValue() : int_array[j-len];
								int_array=aux;
							}
							fields[i].set(obj,int_array);
						}
						else
						if (field_class_name.equals("long")) {
							long[] long_array=par.getLongArray(DELIM);
							if (len>0) {
								long[] aux=new long[len+long_array.length];
								for (int j=0; j<aux.length; j++) aux[j]=(j<len)? ((Long)java.lang.reflect.Array.get(value_obj,j)).longValue() : long_array[j-len];
								long_array=aux;
							}
							fields[i].set(obj,long_array);
						}
						else
						if (field_class_name.equals("byte")) {
							String val=par.getString();
							fields[i].set(obj,Bytes.fromFormattedHex(val));
						}
					}
					else {
						if (field_class_name.equals("java.lang.String")) fields[i].set(obj,par.getRemainingString().trim());
						else
						if (field_class_name.equals("byte")) fields[i].set(obj,new Byte((byte)par.getInt()));
						else
						if (field_class_name.equals("char")) fields[i].set(obj,new Character(par.getChar()));
						else
						if (field_class_name.equals("double")) fields[i].set(obj,new Double(par.getDouble()));
						else
						if (field_class_name.equals("float")) fields[i].set(obj,new Float((float)par.getDouble()));
						else
						if (field_class_name.equals("int")) fields[i].set(obj,new Integer(par.getInt()));
						else
						if (field_class_name.equals("long")) fields[i].set(obj,new Long(par.getInt()));
						else
						if (field_class_name.equals("short")) fields[i].set(obj,new Short((short)par.getInt()));
						else
						if (field_class_name.equals("boolean")) {
							String val=par.getString().toLowerCase();
							fields[i].set(obj,new Boolean(val.equals("true") || val.equals("y") || val.equals("yes")));
						}
					}
				}
				catch (Exception e) {
					e.printStackTrace();
				}
				// @@@@@ restore field accessibility
				if (fullAccess && !is_accessible) fields[i].setAccessible(false);
				break;
			} 
		}
	}
	

	/** Gets class name of array elements in a readable format (e.g. "int" in place of "[I", "String" in place of "[Ljava.lang.String", etc). */
	static String getArrayClassTypeString(String array_class_name) {
		if (array_class_name.charAt(1)=='L') return array_class_name.substring(2,array_class_name.length()-1);
		else return getPrimitiveTypeString(array_class_name.charAt(1));
	}
	
	
	/** Gets a primitive type in a readable format (e.g. "int" in place of "I", "boolean" in place of "Z", etc). */
	static String getPrimitiveTypeString(char type) {
		switch (type) {
			case 'B': return "byte";
			case 'C': return "char";
			case 'D': return "double";
			case 'F': return "float";
			case 'I': return "int";
			case 'J': return "long";
			case 'S': return "short";
			case 'Z': return "boolean";
			case 'V': return "void";
		}
		return null;
	}

	
	/** gets all public, private, and protected fields, including attributes inherited from all superclasses
	 * @param type
	 * @return the list of fields
	 */
	public static Field[] getAllFields(Class<?> type) {
		List<Field> fields= new ArrayList<Field>();
		for (Class<?> c= type; c!=null; c= c.getSuperclass()) {
			fields.addAll(Arrays.asList(c.getDeclaredFields()));
		}
		return fields.toArray(new Field[0]);
	}
	
}
