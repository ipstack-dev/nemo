package org.zoolu.util.json;


/** A JSON object that can be an empty object '{}' or '{' <code>members</code> '}'.
 * Where <code>members</code> is one or more comma-separated pairs formed by a <code>name</code> and a <code>value</code>.
 * <p>
 * The <code>value</code> can be: a string, a number, a boolean, an array, or a JSON object.
 */
public class JzonObject {

	StringBuffer sb=null;
	
	private JzonObject add(String name, JzonValue value) {
		if (sb==null) (sb=new StringBuffer()).append('{'); else sb.append(',');
		sb.append("\""+name+"\":"+value.toString());
		return this;
	}
	
	public JzonObject add(String name, String value) {
		return add(name,new JzonValue(value));
	}
	
	public JzonObject add(String name, long value) {
		return add(name,new JzonValue(value));
	}
	
	public JzonObject add(String name, boolean value) {
		return add(name,new JzonValue(value));
	}
	
	public JzonObject add(String name, JzonObject value) {
		return add(name,new JzonValue(value));
	}
	
	public JzonObject add(String name, JzonArray value) {
		return add(name,new JzonValue(value));
	}
	
	public JzonObject add(String name, String[] value) {
		return add(name,new JzonArray(value));
	}

	public JzonObject add(String name, int[] value) {
		return add(name,new JzonArray(value));
	}

	public JzonObject add(String name, long[] value) {
		return add(name,new JzonArray(value));
	}

	public JzonObject add(String name, boolean[] value) {
		return add(name,new JzonArray(value));
	}

	public JzonObject add(String name, Object[] value) {
		return add(name,new JzonValue(value));
	}
	
	public JzonObject add(String name, Object value) {
		return add(name,new JzonValue(value));
	}
	
	@Override
	public String toString() {
		if (sb==null) return "{}";
		return sb.append('}').toString();
	}
	
}
