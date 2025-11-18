package it.unipr.netsec.nemo.util.expression;


/** A boolean expression of a variable.
 */
@FunctionalInterface
public interface BooleanExpression<X> {

	/** Evaluates the expression as function of a given variable.
	 * @param x the value of the variable
	 * @return the value of the expression */
	public boolean getValue(X x);

}
