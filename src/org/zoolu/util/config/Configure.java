package org.zoolu.util.config;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;


/** Configure handles the attributes of a given object, allowing the reading from and saving to a file.
  * <p>
  * Through Configure it is possible to access of the attributes of a given object, setting the values,
  * reading them from a configuration file or URL, and/or saving them to a file.
  */
public class Configure {
	
	/** String 'NONE' used as undefined value (i.e. null) */
	public static String NONE="NONE";


	/** The object that should be configured */
	ReflectiveObject ref;


	/** Reads object attributes from a given file.
	 * @param file
	 * @param c
	 * @return
	 * @throws IOException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 */
	public static <T> T fromFile(File file, Class<T> c) throws IOException, InstantiationException, IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
		//T obj=c.newInstance();
		Constructor<T> ctor= c.getDeclaredConstructor();
		ctor.setAccessible(true);
		T obj=ctor.newInstance();
		fromFile(file,obj);
		return obj;
	}

	
	/** Reads object attributes from a given file.
	 * @param file
	 * @param obj
	 * @throws IOException
	 */
	public static void fromFile(File file, Object obj) throws IOException {
		new Configure(obj,true).load(file);
	}

	
	/** Writes object attributes to the given file.
	 * @param obj
	 * @param file
	 * @throws IOException
	 */
	public static void toFile(Object obj, File file) throws IOException {
		new Configure(obj,true).save(file);
	}

	
	/** Writes object attributes to the given file by replacing existing values.
	 * @param obj
	 * @param file
	 * @throws IOException
	 */
	public static void updateFile(Object obj, File file) throws IOException {
		new Configure(obj,true).saveChanges(file);
	}

	
	/** Creates a new configuration.
	 * @param obj The object that should be configured
	 */
	public Configure(Object obj) {
		this(obj,false);
	}


	/** Creates a new configuration.
	  * @param obj The object that should be configured
	  * @param full_access Whether accessing also protected, private, and package friendly fields */
	public Configure(Object obj, boolean full_access) {
		ref=new ReflectiveObject(obj,full_access);
	}


	/** Reads configuration from a given file.
	 * @param file the file name
	 * @throws IOException 
	 */
	public void load(String file) throws IOException {
		load(new File(file));
	}


	/** Reads configuration from a given file.
	 * @param file the file
	 * @throws IOException 
	 */
	public void load(File file) throws IOException {
		if (file==null) return;
			load(new FileReader(file));
	}


	/** Reads configuration from a given URL.
	 * @param url the URL
	 * @throws IOException 
	 */
	public void load(URL url) throws IOException {
		if (url==null) return;
		load(new InputStreamReader(url.openStream()));
	}


	/** Reads configuration from the given reader.
	 * @param rd the reader
	 * @throws java.io.IOException
	 */
	private void load(Reader rd) throws java.io.IOException {
		BufferedReader in=new BufferedReader(rd);           
		while (true) {
			String line=null;
			try { line=in.readLine(); } catch (Exception e) { e.printStackTrace(); System.exit(0); }
			if (line==null) break;
		
			if (!line.startsWith("#")) {
				String[] attributeValuePair=parseAttributeValuePair(line);
				ref.setAttribute(attributeValuePair[0],attributeValuePair[1]);
			}
		} 
		in.close();
	}


	/** Writes configuration on a given file.
	 * @param file file name
	 * @throws IOException 
	 */
	public void save(String file) throws IOException {
		save(new File(file));    
	}


	/** Writes configuration on a given file.
	 * @param file the file
	 * @throws IOException 
	 */
	public void save(File file) throws IOException {
		if (file==null) return;
		BufferedWriter out=new BufferedWriter(new FileWriter(file));
		out.write(toString());
		out.close();
	}


	/** Writes configuration on a given file.
	 * If the file already exists, the object attributes are updated (if present), or appended (if not present).
	 * All other lines are kept unchanged.
	 * @param file file name
	 */
	public void saveChanges(String file) {
		saveChanges(new File(file));
	}

	
	/** Writes configuration on a given file.
	 * If the file already exists, the object attributes are updated (if present), or appended (if not present).
	 * All other lines are kept unchanged.
	 * @param file the file
	 */
	public void saveChanges(File file) {
		if (file==null) return;
		try {
			// read
			BufferedReader in=new BufferedReader(new FileReader(file));
			ArrayList<String[]> storedAttributes=new ArrayList<>();
			HashMap<String,Integer> attributeMap=new HashMap<>();
			String line=in.readLine();
			while (line!=null) {
				String[] attributeValuePair=parseAttributeValuePair(line);
				storedAttributes.add(attributeValuePair);
				attributeMap.put(attributeValuePair[0],storedAttributes.size()-1);
				line=in.readLine();
			}
			in.close();
			// update
			for (String a: ref.getAttributes()) {
				if (ref.getAttributeValue(a)!=null)	{
					String value=ref.getAttributeStringValue(a);
					if (attributeMap.containsKey(a)) {
						int index=attributeMap.get(a);
						storedAttributes.get(index)[1]=value;
					}
					else {
						storedAttributes.add(new String[]{a,value});
					}
				}
			}
			
			// write
			BufferedWriter out=new BufferedWriter(new FileWriter(file));
			for (String[] attributeValuePair: storedAttributes) {
				out.write(attributeValuePair[0]);
				String value=attributeValuePair[1];
				if (value!=null && value.length()>0) out.write('='+attributeValuePair[1]);
				out.write('\n');
			}
			out.close();
		}
		catch (IOException e) {
			System.err.println("ERROR updating file \""+file+"\"");
		}         
	}


	/** Parse an attribute and value pair.
	 * @param line the line to parse
	 * @return attribute and value pair
	 */
	private static String[] parseAttributeValuePair(String line) {
		String[] attributeValuePair= new String[2];
		int index=line.indexOf("=");
		if (index>0) {
			attributeValuePair[0]=line.substring(0,index).trim();
			attributeValuePair[1]=line.substring(index+1).trim();
		}
		else {
			attributeValuePair[0]=line.trim();
			attributeValuePair[1]="";
		}
		return attributeValuePair;
	}
	
	
	@Override
	public String toString() {
		String[] attributes=ref.getAttributes();
		StringBuffer sb=new StringBuffer();
		for (String a: attributes) {
			if (ref.getAttributeValue(a)!=null)	sb.append(a).append('=').append(ref.getAttributeStringValue(a)).append('\n');
		}
		return sb.toString();
	}


	/** Mangles array class name.
     * @param array_class_name
	 * @return the type identifier
	 */
	private static String mangleArrayClassType(String array_class_name) {
		if (array_class_name.charAt(1)=='L') return array_class_name.substring(2,array_class_name.length()-1);
		else return manglePrimitiveType(array_class_name.charAt(1));
	}


	/** Mangles primitive type.
	 * @param type
	 * @return the type identifier
	 */
	private static String manglePrimitiveType(char type) {
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

}
