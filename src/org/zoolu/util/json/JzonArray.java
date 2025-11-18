package org.zoolu.util.json;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class JzonArray {

	StringBuffer sb=new StringBuffer();	
	
	public JzonArray(String[] vv) {
		int len=vv!=null? vv.length : 0;
		JzonValue[] values=new JzonValue[len];
		for (int i=0; i<values.length; i++) values[i]=new JzonValue(vv[i]);
		init(values);
	}

	public JzonArray(short[] values) {
		int len=values!=null? values.length : 0;
		JzonValue[] jj=new JzonValue[len];
		for (int i=0; i<jj.length; i++) jj[i]=new JzonValue(values[i]);
		init(jj);
	}

	public JzonArray(int[] vv) {
		int len=vv!=null? vv.length : 0;
		JzonValue[] values=new JzonValue[len];
		for (int i=0; i<values.length; i++) values[i]=new JzonValue(vv[i]);
		init(values);
	}

	public JzonArray(long[] vv) {
		int len=vv!=null? vv.length : 0;
		JzonValue[] values=new JzonValue[len];
		for (int i=0; i<values.length; i++) values[i]=new JzonValue(vv[i]);
		init(values);
	}

	public JzonArray(float[] vv) {
		int len=vv!=null? vv.length : 0;
		JzonValue[] values=new JzonValue[len];
		for (int i=0; i<values.length; i++) values[i]=new JzonValue(vv[i]);
		init(values);
	}

	public JzonArray(double[] vv) {
		int len=vv!=null? vv.length : 0;
		JzonValue[] values=new JzonValue[len];
		for (int i=0; i<values.length; i++) values[i]=new JzonValue(vv[i]);
		init(values);
	}

	public JzonArray(boolean[] vv) {
		int len=vv!=null? vv.length : 0;
		JzonValue[] values=new JzonValue[len];
		for (int i=0; i<values.length; i++) values[i]=new JzonValue(vv[i]);
		init(values);
	}

	public JzonArray(Object[] vv) {
		int len=vv!=null? vv.length : 0;
		JzonValue[] values=new JzonValue[len];
		for (int i=0; i<values.length; i++) values[i]=new JzonValue(vv[i]);
		init(values);
	}

	public <T> JzonArray(Collection<T> cc) {
		JzonValue[] values=new JzonValue[cc!=null?cc.size():0];
		Iterator<T> list=cc.iterator();
		for (int i=0; i<values.length; i++) {
			T elem=list.next();
			if (elem instanceof String) values[i]=new JzonValue((String)elem);
			else if (elem instanceof Short || elem instanceof Integer || elem instanceof Long) values[i]=new JzonValue((long)elem);
			else if (elem instanceof Float || elem instanceof Double) values[i]=new JzonValue((double)elem);
			else if (elem instanceof Boolean) values[i]=new JzonValue((boolean)elem);
			else values[i]=new JzonValue(elem);
		}
		init(values);
	}

	/*public <T> JzonArray(List<T> list) {
		int len=list!=null? list.size() : 0;
		JzonValue[] values=new JzonValue[len];
		for (int i=0; i<values.length; i++) {
			T elem=list.get(i);
			if (elem instanceof String) values[i]=new JzonValue((String)list.get(i));
			else if (elem instanceof Integer || elem instanceof Short || elem instanceof Long) values[i]=new JzonValue((long)list.get(i));
			else if (elem instanceof Boolean) values[i]=new JzonValue((boolean)list.get(i));
			else values[i]=new JzonValue(list.get(i));
		}
		init(values);
	}*/

	private void init(JzonValue[] vv) {
		sb.append('[');
		for (int i=0; vv!=null && i<vv.length; i++) {
			if (i>0) sb.append(',');
			sb.append(vv[i].toString());
		}
		sb.append(']');			
	}
	
	@Override
	public String toString() {
		return sb.toString();
	}
}
