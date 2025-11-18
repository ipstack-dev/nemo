package it.unipr.netsec.nemo.util.expression;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class Or<X> implements BooleanExpression<X> {
	
	List<BooleanExpression<X>> list;
	
	public Or() {
		list=new ArrayList<>();
	}

	public Or(BooleanExpression<X>... list) {
		this.list=Arrays.asList(list);
	}

	public Or(List<BooleanExpression<X>> list) {
		this.list=list;
	}

	@Override
	public boolean getValue(X x) {
		for (BooleanExpression<X> be: list) if (be.getValue(x)) return true;
		return false;
	}

	public Or<X> add(BooleanExpression<X> be) {
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
