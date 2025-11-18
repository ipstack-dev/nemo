/*
 * Copyright (c) 2018 Luca Veltri, University of Parma
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND. IN NO EVENT
 * SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package org.zoolu.util;


import java.util.ArrayList;


/** Buffer of bit strings.
  * A BitStringBuffer object represents a variable-length string of bits (0 or 1).
  * Bits can be appended to the tail of the string (by means of the method {@link BitStringBuffer#append(BitString)} or {@link BitStringBuffer#append(BitStringBuffer)}),
  * read from any interval (by means of the method {@link BitStringBuffer#substring(int,int)}),
  * or extracted from the head of the string (by means of the method {@link BitStringBuffer#poll(int)}).
  * <p>
  * The methods are synchronized, so that BitStringBuffer objects are safe for use by multiple threads.
  */
public class BitStringBuffer {
	
	/** Buffer of bit strings */
	ArrayList<BitString> buffer;



	/** Creates a new BitStringBuffer. */
	public BitStringBuffer() {
		buffer=new ArrayList<>();
	}


	/** Gets the total number of bits.
	 * @return the length */
	public synchronized int length() {
		int len=0;
		for (int i=0; i<buffer.size(); i++) len+=buffer.get(i).length();
		return len;
	}


	/** Removes all bits from this BitStringBuffer. */
	public synchronized void clear() {
		buffer.clear();
	}


	/** Appends a BitString.
	 * @param bit_string the bits to add
	 * @return the resulting BitStringBuffer */
	public synchronized BitStringBuffer append(BitString bit_string) {
		buffer.add(bit_string);
		return this;
	}


	/** Appends a BitStringBuffer.
	 * @param bit_buffer the bits to add
	 * @return the resulting BitStringBuffer */
	public synchronized BitStringBuffer append(BitStringBuffer bit_buffer) {
		for (int i=0; i<bit_buffer.buffer.size(); i++) buffer.add(bit_buffer.buffer.get(i));
		return this;
	}


	/** Appends one bit. */
	/*public synchronized BitStringBuffer append(boolean bit) {
		return append(new BitString(new boolean[]{bit}));
	}*/


	/** Returns a new BitStringBuffer that contains a substring of this BitStringBuffer.
	 * The substring begins at the specified index <i>begin</i> and extends to the bit at index <i>end</i> - 1. Thus the length of the substring is end-begin.
	 * @param begin the index of the first bit 
	 * @param end the index of the bit next to the last one (the first not included)
	 * @return the new BitStringBuffer */
	public synchronized BitStringBuffer substring(int begin, int end) {
		BitStringBuffer bb=new BitStringBuffer();
		int index=0, i=0;
		while (index<begin) {
			BitString bitstr=buffer.get(i++);
			int len_i=bitstr.length();
			index+=len_i;
			if (index>begin) {
				if (index<=end) bb.append(bitstr.substring(len_i-(index-begin)));
				else bb.append(bitstr.substring(len_i-(index-begin),len_i-(index-end)));
			}
		}
		while (index<end) {
			BitString bitstr=buffer.get(i++);
			int len_i=bitstr.length();
			index+=len_i;
			if (index<=end) bb.append(bitstr);
			else bb.append(bitstr.substring(0,len_i-(index-end)));
		}
		return bb;
	}


	/** Gets the first <i>n</i> bits.
	  * @return the first n bits in a new BitStringBuffer */
	/*public synchronized BitStringBuffer getBits(int n) {
		if (n>=length()) return this;
		// else
		BitStringBuffer bb=new BitStringBuffer();
		int len=0, i=0;
		while (len<n) {
			BitString bitstr=(BitString)buffer.elementAt(i);
			if (len+bitstr.length()<n) {
				bb.append(bitstr);
				len+=bitstr.length();
				i++;
			}
			else {
				bb.append(bitstr.substring(0,n-len));
				len=n;
			}
		}
		return bb;
	}*/


	/** Retrieves and removes the first <i>n</i> bits of this BitStringBuffer.
	  * If <i>L</i> was the length of BitStringBuffer before calling this method, after this method
	  * the length of BitStringBuffer is <i>L-n</i>.  
	  * @param n the number of heading bits that have to be retrieved and removed
	  * @return a new BitStringBuffer containing the <i>n</i> bits that has been removed (bits from <i>0</i> to <i>n-1</i> of the original BitStringBuffer) */
	public synchronized BitStringBuffer poll(int n) {
		BitStringBuffer bb=new BitStringBuffer();
		if (n>=length()) {
			bb.buffer=buffer;
			buffer=new ArrayList<>();
			return bb;
		}
		// else
		int len=0;
		while (len<n) {
			BitString bitstr=buffer.get(0);
			if (len+bitstr.length()<=n) {
				bb.append(bitstr);
				len+=bitstr.length();
				buffer.remove(0);
			}
			else {
				bb.append(bitstr.substring(0,n-len));
				buffer.set(0,bitstr.substring(n-len));
				len=n;
			}
		}
		return bb;
	}


	/** Gets the BitString of this BitStringBuffer.
	 * @return the BitString */
	public synchronized BitString toBitString() {
		return new BitString(buffer.toArray(new BitString[0]));
	}


	@Override
	public synchronized String toString() {
		StringBuffer sb=new StringBuffer();
		for (int i=0; i<buffer.size(); i++) sb.append(buffer.get(i).toString());
		return sb.toString();
	}

}
	
