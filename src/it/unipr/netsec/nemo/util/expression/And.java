package it.unipr.netsec.nemo.util.expression;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class And<X> implements BooleanExpression<X> {
	
	List<BooleanExpression<X>> list;
	
	public And() {
		list=new ArrayList<>();
	}

	public And(BooleanExpression<X>... list) {
		this.list=Arrays.asList(list);
	}

	public And(List<BooleanExpression<X>> list) {
		this.list=list;
	}

	@Override
	public boolean getValue(X x) {
		for (BooleanExpression<X> be: list) if (!be.getValue(x)) return false;
		return true;
	}
	
	public And<X> add(BooleanExpression<X> be) {
		try {
			list.add(be);
		}
		catch (UnsupportedOperationException e) {
			list=new ArrayList<>(list);
			list.add(be);
		}
		return this;
	}

}
