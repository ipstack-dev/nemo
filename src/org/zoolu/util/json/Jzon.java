package org.zoolu.util.json;


import java.util.Collection;


/** Provides method for converting an object into the corresponding JSON object string.
 * <p>
 * In case of an array object (int[], double[], boolean[], String[], Object[], etc) a JSON array ('[ <code>element</code>, <code>element</code>, ... ]') is returned.
 * Iin all other cases, a JSON object ('{ <code>name</code> : <code>value</code>, <code>name</code> : <code>value</code>, ... }') is returned.
 */
public abstract class Jzon {

	public static String toJson(Object obj) {
		if (obj instanceof String[]) return new JzonArray((String[])obj).toString();
		if (obj instanceof int[]) return new JzonArray((int[])obj).toString();
		if (obj instanceof long[]) return new JzonArray((long[])obj).toString();
		if (obj instanceof double[]) return new JzonArray((double[])obj).toString();
		if (obj instanceof boolean[]) return new JzonArray((boolean[])obj).toString();
		if (obj instanceof Object[]) return new JzonArray((Object[])obj).toString();
		//if (obj instanceof List) return new JzonArray((List<?>)obj).toString();
		if (obj instanceof Collection) return new JzonArray((Collection<?>)obj).toString();
		return new JzonValue(obj).toString();
	}

}
