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


/** Collections of static methods for doing bit-oriented operations onto integer values.
  */
public final class Bits {
	private Bits() {}
	
	/** Rotates a byte b shifting n bits left.
	  * @param b the byte to be rotated
	  * @param n the number of bits to be shifted to the left
	  * @return the byte value */
	public static byte rotateLeft(byte b, int n) {
		n%=8;
		return (byte)((b << n) | (b >>> (8-n)));
	}

	/** Rotates a byte b shifting n bits right.
	  * @param b the byte to be rotated
	  * @param n the number of bits to be shifted to the right
	  * @return the byte value */
	public static byte rotateRight(byte b, int n) {
		n%=8;
		return (byte)((b >>> n) | (b << (8-n)));
	}

	/** Rotates a short integer (16-bit word) w shifting n bits left.
	  * @param w the short to be rotated
	  * @param n the number of bits to be shifted to the left
	  * @return the short value */
	public static short rotateLeft(short w, int n) {
		n%=16;
		return (short)((w << n) | (w >>> (16-n)));
	}

	/** Rotates a short integer (16-bit word) w shifting n bits right.
	  * @param w the short to be rotated
	  * @param n the number of bits to be shifted to the right
	  * @return the short value */
	public static short rotateRight(short w, int n) {
		n%=16;
		return (short)((w >>> n) | (w << (16-n)));
	}

	/** Rotates a integer (32-bit word) w shifting n bits left.
	  * @param w the integer to be rotated
	  * @param n the number of bits to be shifted to the left
	  * @return the int value */
	public static int rotateLeft(int w, int n) {
		n%=32;
		return (w << n) | (w >>> (32-n));
	}

	/** Rotates a integer (32-bit word) w shifting n bits right.
	  * @param w the integer to be rotated
	  * @param n the number of bits to be shifted to the right
	  * @return the int value */
	public static int rotateRight(int w, int n) {
		n%=32;
		return (w >>> n) | (w << (32-n));
	}

	/** Rotates a long integer (64-bit word) w shifting n bits left.
	  * @param w the long to be rotated
	  * @param n the number of bits to be shifted to the left
	  * @return the long value */
	public static long rotateLeft(long w, int n) {
		n%=64;
		return (w << n) | (w >>> (64-n));
	}

	/** Rotates a long integer (64-bit word) w shifting n bits right.
	  * @param w the long to be rotated
	  * @param n the number of bits to be shifted to the right
	  * @return the long value */
	public static long rotateRight(long w, int n) {
		n%=64;
		return (w >>> n) | (w << (64-n));
	}

}
