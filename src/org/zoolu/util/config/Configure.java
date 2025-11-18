package org.zoolu.util.config;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;

import org.zoolu.util.json.JsonUtils;


/** Configure handles the attributes of a given object, allowing the reading from and saving to a file.
  * <p>
  * Through Configure it is possible to access of the attributes of a given object, setting the values,
  * reading them from a configuration file or URL, and/or saving them to a file.
  */
public class Configure {
	
	/** String 'NONE' used as undefined value (i.e. null). */
	public static String NONE="NONE";


	/** The object that should be configured. */
	ReflectiveObject ref;



	public static Object fromFile(File file, Class<?> c) throws IOException, InstantiationException, IllegalAccessException {
		Object obj=c.newInstance();
		fromFile(file,obj);
		return obj;
	}
	
	public static void fromFile(File file, Object obj) throws IOException {
		new Configure(obj,true).readFrom(new FileReader(file));
	}

	public static void toFile(Object obj, File file) throws IOException {
		new Configure(obj,true).writeTo(new FileWriter(file));
	}
	
	
	/** Creates a new configuration.
	  * @param obj The object that should be configured */
	public Configure(Object obj) {
		this(obj,false);
	}


	/** Creates a new configuration.
	  * @param obj The object that should be configured
	  * @param full_access Whether accessing also protected, private, and package friendly fields */
	public Configure(Object obj, boolean full_access) {
		ref=new ReflectiveObject(obj,full_access);
	}


	/** Loads configuration from the given file. */
	public void load(String file) {
		if (file==null) {
			return;
		}
		//else
		try {
			readFrom(new FileReader(file));
		}
		catch (Exception e) {
			System.err.println("WARNING: error reading file \""+file+"\"");
			return;
		}
	}


	/** Loads configuration from the given URL. */
	public void load(URL url) {
		if (url==null) {
			return;
		}
		//else
		try {
			readFrom(new InputStreamReader(url.openStream()));
		}
		catch (Exception e) {
			System.err.println("WARNING: error reading from \""+url+"\"");
			return;
		}
	}


	/** Saves configuration on the given file. */
	public void save(String file) {
		if (file==null) return;
		//else
		try {
			writeTo(new FileWriter(file));
		}
		catch (IOException e) {
			System.err.println("ERROR writing on file \""+file+"\"");
		}         
	}


	/** Reads configuration from the given Reader. */
	private void readFrom(Reader rd) throws java.io.IOException {
		BufferedReader in=new BufferedReader(rd);           
		while (true) {
			String line=null;
			try { line=in.readLine(); } catch (Exception e) { e.printStackTrace(); System.exit(0); }
			if (line==null) break;
		
			if (!line.startsWith("#")) {
				parseLine(line);
			}
		} 
		in.close();
	}


	/** Writes the configuration to the given Writer. */
	private void writeTo(Writer wr) throws java.io.IOException {
		BufferedWriter out=new BufferedWriter(wr);
		out.write(toString());
		out.close();
	}


	/** Parses a text line. */
	protected void parseLine(String line) {
		String attribute;
		String value;
		int index=line.indexOf("=");
		if (index>0) {
			attribute=line.substring(0,index).trim();
			value=line.substring(index+1).trim();
		}
		else {
			attribute=line;
			value="";
		}
		parseAttribute(attribute,value);
	}
	

	/** Gets a string with all parameters (and values). */
	public String toString() {
		String[] attributes=ref.getAttributes();
		StringBuffer sb=new StringBuffer();
		for (String a: attributes) {
			if (ref.getAttributeValue(a)!=null)	sb.append(a).append('=').append(ref.getAttributeStringValue(a)).append('\n');
		}
		return sb.toString();
	}


	/** Parses an attribute value. */
	protected void parseAttribute(String attribute, String value) {
		ref.setAttribute(attribute,value);
	}


	/** Mangles array class name. */
	private static String mangleArrayClassType(String array_class_name) {
		if (array_class_name.charAt(1)=='L') return array_class_name.substring(2,array_class_name.length()-1);
		else return manglePrimitiveType(array_class_name.charAt(1));
	}


	/** Mangles primitive type. */
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
