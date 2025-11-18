package org.zoolu.util;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;


/** Class that collects static methods for dealing with arrays.
 */
public abstract class ArrayUtils {

	/** Converts an array of objects to a string.
	 * @param objs array of objects
	 * @return comma-separated list of objects */
	public static String toString(Object[] objs) {
		return toString(Arrays.asList(objs));
	}
	
	/** Converts a list of objects to a string.
	 * @param objs array of objects
	 * @return comma-separated list of objects */
	public static String toString(List<?> objs) {
		StringBuffer sb=new StringBuffer();
		for (int i=0; i<objs.size(); i++) {
			if (i>0) sb.append(", ");
			Object o=objs.get(i);
			sb.append(o!=null?o.toString():"null");
		}
		return sb.toString();
	}

	/** Creates a new ArrayList with one element.
	 * @param e the element
	 * @return the ArrayList */
	public static <E> ArrayList<E> arraylist(E e) {
		ArrayList<E> list=new ArrayList<>();
		if (e!=null) list.add(e);
		return list;
	}

	/** Creates a list with the given elements.
	 * The copy is done in safe mode (synchronized).
	 * @param c collection of elements
	 * @return the list */
	public static <E> ArrayList<E> synchronizedList(Collection<E> c) {
		ArrayList<E> list;
		synchronized (c) {
			list=new ArrayList<>(c);
		}
		return list;
	}

	/** Right-rotates an array.
	 * @param a the array to be rotated */
	/*public static <T> void rotateRight(ArrayList<T> a) {
		a.add(0,a.remove(a.size()-1));
	}*/

	/** Left-rotates an array.
	 * @param a the array to be rotated */
	/*public static <T> void rotateLeft(ArrayList<T> a) {
		a.add(a.remove(0));
	}*/

	/** Right-rotates an array of one element.
	 * @param a the array to be rotated */
	public static void rotateRight(Object a) {
		if (a instanceof byte[]) {
			byte[] aa=(byte[])a;
			byte a1=aa[aa.length-1];
			for (int i=aa.length-1; i>0; i--) aa[i]=aa[i-1];
			aa[0]=a1;
			return;
		}
		else if (a instanceof short[]) {
			short[] aa=(short[])a;
			short a1=aa[aa.length-1];
			for (int i=aa.length-1; i>0; i--) aa[i]=aa[i-1];
			aa[0]=a1;
			return;
		}
		else if (a instanceof int[]) {
			int[] aa=(int[])a;
			int a1=aa[aa.length-1];
			for (int i=aa.length-1; i>0; i--) aa[i]=aa[i-1];
			aa[0]=a1;
			return;
		}
		else if (a instanceof long[]) {
			long[] aa=(long[])a;
			long a1=aa[aa.length-1];
			for (int i=aa.length-1; i>0; i--) aa[i]=aa[i-1];
			aa[0]=a1;
			return;
		}
		else if (a instanceof float[]) {
			float[] aa=(float[])a;
			float a1=aa[aa.length-1];
			for (int i=aa.length-1; i>0; i--) aa[i]=aa[i-1];
			aa[0]=a1;
		}
		else if (a instanceof double[]) {
			double[] aa=(double[])a;
			double a1=aa[aa.length-1];
			for (int i=aa.length-1; i>0; i--) aa[i]=aa[i-1];
			aa[0]=a1;
			return;
		}
		else if (a instanceof boolean[]) {
			boolean[] aa=(boolean[])a;
			boolean a1=aa[aa.length-1];
			for (int i=aa.length-1; i>0; i--) aa[i]=aa[i-1];
			aa[0]=a1;
		}
		else if (a instanceof Object[]) {
			Object[] aa=(Object[])a;
			Object a1=aa[aa.length-1];
			for (int i=aa.length-1; i>0; i--) aa[i]=aa[i-1];
			aa[0]=a1;
			return;
		}
		throw new RuntimeException("It isn't an array");
	}

	/** Left-rotates an array of one element.
	 * @param a the array to be rotated */
	public static void rotateLeft(Object a) {
		if (a instanceof byte[]) {
			byte[] aa=(byte[])a;
			byte a0=aa[0];
			for (int i=0; i<aa.length-1; i++) aa[i]=aa[i+1];
			aa[aa.length-1]=a0;
			return;
		}
		else if (a instanceof short[]) {
			short[] aa=(short[])a;
			short a0=aa[0];
			for (int i=0; i<aa.length-1; i++) aa[i]=aa[i+1];
			aa[aa.length-1]=a0;
			return;
		}
		else if (a instanceof int[]) {
			int[] aa=(int[])a;
			int a0=aa[0];
			for (int i=0; i<aa.length-1; i++) aa[i]=aa[i+1];
			aa[aa.length-1]=a0;
			return;
		}
		else if (a instanceof long[]) {
			long[] aa=(long[])a;
			long a0=aa[0];
			for (int i=0; i<aa.length-1; i++) aa[i]=aa[i+1];
			aa[aa.length-1]=a0;
			return;
		}
		else if (a instanceof float[]) {
			float[] aa=(float[])a;
			float a0=aa[0];
			for (int i=0; i<aa.length-1; i++) aa[i]=aa[i+1];
			aa[aa.length-1]=a0;
			return;
		}
		else if (a instanceof double[]) {
			double[] aa=(double[])a;
			double a0=aa[0];
			for (int i=0; i<aa.length-1; i++) aa[i]=aa[i+1];
			aa[aa.length-1]=a0;
			return;
		}
		else if (a instanceof boolean[]) {
			boolean[] aa=(boolean[])a;
			boolean a0=aa[0];
			for (int i=0; i<aa.length-1; i++) aa[i]=aa[i+1];
			aa[aa.length-1]=a0;
			return;
		}
		else if (a instanceof long[]) {
			long[] aa=(long[])a;
			long a0=aa[0];
			for (int i=0; i<aa.length-1; i++) aa[i]=aa[i+1];
			aa[aa.length-1]=a0;
			return;
		}
		else if (a instanceof Object[]) {
			Object[] aa=(Object[])a;
			Object a0=aa[0];
			for (int i=0; i<aa.length-1; i++) aa[i]=aa[i+1];
			aa[aa.length-1]=a0;
			return;
		}
		throw new RuntimeException("It isn't an array");

	}

}
