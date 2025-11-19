package org.zoolu.util;


/** Some static math methods.
 */
public class MathUtils {
	private MathUtils() {}
	
	/**
	 * @return base-2 truncated logarithm of an integer
	 */
	public static int intLog2(long n) {
		if (n<=0) throw new RuntimeException("Invalid log argument (must be > 0): "+n);
		return 63 - Long.numberOfLeadingZeros(n);
	}
	
	/**
	 * @return base-2 truncated logarithm of an integer
	 */
	public static int intLog2(int n) {
		if (n<=0) throw new RuntimeException("Invalid log argument (must be > 0): "+n);
		return 31 - Long.numberOfLeadingZeros(n);
	}

}
