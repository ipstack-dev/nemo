package it.unipr.netsec.nemo.util.expression;


public class Not<X> implements BooleanExpression<X> {
	
	BooleanExpression<X> expression;
	
	public Not(BooleanExpression<X> expression) {
		this.expression=expression;
	}

	@Override
	public boolean getValue(X x) {
		return !expression.getValue(x);
	}

}
