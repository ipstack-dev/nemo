package org.zoolu.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Integer range that can be used as Iterable in for-each loops for iterating over a range of integers.
 */
public class Range implements Iterable<Integer> {

	private int begin, end, step;
	
	/** Range [0, end).
	 * @param end end integer (not included) of the range
	 */
	public Range(int end) {
		this(0,end,1);
	}

	/** Range [begin, end).
	 * @param begin begin integer of the range
	 * @param end first integer not included in the range
	 */
	public Range(int begin, int end) {
		this(begin,end,1);
	}

	/** Range [begin, end).
	 * @param begin first integer of the range
	 * @param end first integer not included in range
	 * @param step incrementing delta (must not be zero)
	 */
	public Range(int begin, int end, int step) {
		this.begin= begin;
		this.end= end;
		this.step= step;
		if (step==0) throw new NoSuchElementException("Step cannot be zero.");
	}

	@Override
    public Iterator<Integer> iterator() {
        return new Iterator<Integer>() {
            private int next= begin;

            @Override
            public boolean hasNext() {
                return next<end;
            }

            @Override
            public Integer next() {
                if ((step>0 && next<end) || (step<0 && next>end)) { int current= next; next+=step; return current; }
                else throw new NoSuchElementException("Out of range.");
            }
        };
    }
}

