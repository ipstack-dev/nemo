package io.ipstack.http.uri;


/** String parameter.
 * It includes both a name and a string value.
 */
public class Parameter {

	String name;	
	String value;
	
	/** Creates a new parameter.
	 * @param str the string representing the name/value pair */
	public Parameter(String str) {
		int index;
		if ((index=str.indexOf('='))>=0) {
			name=str.substring(0,index).trim();
			value=str.substring(index+1).trim();
		}
		else {
			name=str;
			value=null;			
		}
	}
	
	/** Creates a new parameter.
	 * @param name name
	 * @param value value */
	public Parameter(String name, String value) {
		this.name=name;
		this.value=value;
	}
	
	/**
	 * @return the name */
	public String getName() {
		return name;
	}
	
	/**
	 * @return the value */
	public String getValue() {
		return value;
	}
	
	@Override
	public String toString() {
		return value!=null? name+'='+value : name;
	}

}
