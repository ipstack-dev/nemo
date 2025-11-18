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


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;


/** Class that collects static methods related to byte arrays.
  */
public abstract class Bytes {
	
	// BYTE ARRAYS:

	/** Copies an array of bytes into another array.
	 * @param src the source array to be copied
	 * @param dst the destination array */
	public static void copy(byte[] src, byte[] dst) {
		copy(src,0,dst,0,src.length);
	}

	/** Copies an array of bytes into another array.
	 * @param src the source array to be copied
	 * @param dst the destination buffer where the array has to be copied
	 * @param dst_off the offset within the buffer */
	public static void copy(byte[] src, byte[] dst, int dst_off) {
		copy(src,0,dst,dst_off,src.length);
	}

	/** Copies an array of bytes into another array.
	 * @param src the buffer containing the source array to be copied
	 * @param src_off the offset within the buffer
	 * @param dst the destination buffer where the array has to be copied
	 * @param dst_off the offset within the buffer
	 * @param len the number of bytes to be copied */
	public static void copy(byte[] src, int src_off, byte[] dst, int dst_off, int len) {
		System.arraycopy(src,src_off,dst,dst_off,len);
	}

	/** Gets a new array containing a copy of an array of bytes.
	  * @param src the array to be copied
	  * @return a copy of the original array */
	public static byte[] copy(byte[] src) {
		return copy(src,0,src.length);
	}

	/** Gets a new array containing a copy of an array of bytes.
	  * @param src the buffer containing the array to be copied
	  * @param off the offset within the buffer
	  * @param len the length of the array
	  * @return a copy of the original array */
	public static byte[] copy(byte[] src, int off, int len) {
		byte[] dst=new byte[len];
		System.arraycopy(src,off,dst,0,len);
		return dst;
	}
	
	/** Compares two arrays of bytes.
	 * @param a1 one array
	 * @param a2 the other array
	 * @return <i>true</i> if the two arrays are equal */
	public static boolean match(byte[] a1, byte[] a2) {
		return match(a1,0,a1.length,a2,0,a2.length);
	}
	
	/** Compares two arrays of bytes.
	 * @param buf1 the buffer containing the first array
	 * @param off1 the offset within the buffer
	 * @param len1 the length of the first array
	 * @param buf2 the buffer containing the second array
	 * @param off2 the offset within the buffer
	 * @param len2 the length of the second array
	 * @return <i>true</i> if the two arrays are equal */
	public static boolean match(byte[] buf1, int off1, int len1, byte[] buf2, int off2, int len2) {
		if (len1!=len2) return false;
		for (int end1=off1+len1; off1<end1; ) if (buf1[off1++]!=buf2[off2++]) return false;
		return true;
	}
	
	/** Compares two arrays of bytes.
	 * @param a1 one array
	 * @param a2 the other array
	 * @return <ul>
	 *   <li> 0 if the two arrays are equal;
	 *   <li> 1 if they are different and, starting from left, all bits are equal and the length of the first array is greater than than the second, or the first bit that differs has value '1' in the first array (and '0' in the second one);
	 *   <li> -1 if they are different and, starting from left, all bits are equal and the length of the first array is lesser than than the second, or the first bit that differs has value '0' in the first array (and '1' in the second one)
	 * </ul> */
	public static int compare(byte[] a1, byte[] a2) {
		return compare(a1,0,a1.length,a2,0,a2.length);
	}

	/** Compares two arrays of bytes.
	 * @param buf1 the buffer containing the first array
	 * @param off1 the offset within the buffer
	 * @param len1 the length of the first array
	 * @param buf2 the buffer containing the second array
	 * @param off2 the offset within the buffer
	 * @param len2 the length of the second array
	 * @return <ul>
	 *   <li> 0 if the two arrays are equal;
	 *   <li> 1 if they are different and, starting from left, all bits are equal and the length of the first array is greater than than the second, or the first bit that differs has value '1' in the first array (and '0' in the second one);
	 *   <li> -1 if they are different and, starting from left, all bits are equal and the length of the first array is lesser than than the second, or the first bit that differs has value '0' in the first array (and '1' in the second one)
	 * </ul> */
	public static int compare(byte[] buf1, int off1, int len1, byte[] buf2, int off2, int len2) {
		int len=len1<len2? len1 : len2;
		for (int i=0; i<len; i++) { 
			int diff=(buf1[off1++]&0xff)-(buf2[off2++]&0xff);
			if (diff!=0) return diff>0? 1 : -1;
		}
		// else
		return len1==len2? 0 : len1>len2? 1 : -1;
	}
	
	/** Finds the first occurrence of one array of bytes within a second array of bytes.
	 * @param a1 the array to be searched
	 * @param buf2 the buffer containing the second array 
	 * @param off2 the offset within the second array
	 * @param len2 the length of the second array
	 * @return the index within the second array of the first occurrence of the first array, or -1 if not found. */
	public static int indexOf(byte[] a1, byte[] buf2, int off2, int len2) {
		return indexOf(a1,0,a1.length,buf2,off2,len2);
	}

	/** Finds the first occurrence of one array of bytes within a second array of bytes.
	 * @param buf1 the buffer containing the array to be searched
	 * @param off1 the offset within the buffer
	 * @param len1 the length of the array to be searched
	 * @param buf2 the buffer containing the second array 
	 * @param off2 the offset within the buffer
	 * @param len2 the length of the second array
	 * @return the index within the second array of the first occurrence of the first array, or -1 if not found. */
	public static int indexOf(byte[] buf1, int off1, int len1, byte[] buf2, int off2, int len2) {
		for (int i=0; i<=(len2-len1); i++)
			if (match(buf1,off1,len1,buf2,off2+i,len1)) return i;
		return -1;
	}

	/** Assigns the specified byte value to each element of the array of bytes.
	 * @param a the array of bytes
	 * @param val the value to be assigned */
	public static void fill(byte[] a, byte val) {
		fill(a,0,a.length,val);
	}

	/** Assigns the specified byte value to each element of an array of bytes.
	 * @param buf the buffer containing the array
	 * @param off the offset within the buffer
	 * @param len the length of the array
	 * @param val the value to be assigned */
	public static void fill(byte[] buf, int off, int len, byte val) {
		Arrays.fill(buf,off,off+len,val);
	}

	/** Concatenates arrays of bytes.
	  * @param aa array of bytes
	  * @return a new byte array containing the concatenation of the arrays. */
	public static byte[] concat(byte[]... aa) {
		int len=0;
		for (byte[] src: aa) len+=src.length;
		byte[] dst=new byte[len];
		int off=0;
		for (byte[] src: aa) {
			System.arraycopy(src,0,dst,off,src.length);
			off+=src.length;
		}
		return dst;
	}

	/** Concatenates two arrays of bytes.
	  * @param src1 buffer containing the 1st array
	  * @param src2 buffer containing the 2nd array
	  * @return a new byte array containing the concatenation of the two arrays. */
	public static byte[] concat(byte[] src1, byte[] src2) {
		return concat(src1,0,src1.length,src2,0,src2.length);
	}

	/** Concatenates two arrays of bytes.
	  * @param src1 buffer containing the first array
	  * @param src2 buffer containing the second array
	  * @param dst3 destination buffer
	  * @param off offset within the destination buffer
	  * @return the length of the concatenation of the two arrays. */
	public static int concat(byte[] src1, byte[] src2, byte[] dst3, int off) {
		return concat(src1,0,src1.length,src2,0,src2.length,dst3,off);
	}

	/** Concatenates two arrays of bytes.
	  * @param src1 buffer containing the first array
	  * @param off1 offset of the first array
	  * @param len1 length of the first array
	  * @param src2 buffer containing the second array
	  * @param off2 offset of the second array
	  * @param len2 length of the second array
	  * @return a new byte array containing the concatenation of the two arrays. */
	public static byte[] concat(byte[] src1, int off1, int len1, byte[] src2, int off2, int len2) {
		byte[] dst3=new byte[len1+len2];
		concat(src1,off1,len1,src2,off2,len2,dst3,0);
		return dst3;
	}
	
	/** Concatenates two arrays of bytes.
	  * @param src1 buffer containing the first array
	  * @param off1 offset of the first array
	  * @param len1 length of the first array
	  * @param src2 buffer containing the second array
	  * @param off2 offset of the second array
	  * @param len2 length of the second array
	  * @param dst3 buffer for the resulting concatenation array
	  * @param off3 offset of the resulting array
	  * @return the length of the concatenation of the two arrays. */
	public static int concat(byte[] src1, int off1, int len1, byte[] src2, int off2, int len2, byte[] dst3, int off3) {
		copy(src1,off1,dst3,off3,len1);
		copy(src2,off2,dst3,off3+len1,len2);
		return len1+len2;
	}
	
	/** Reverses the byte order of a given array.
	  * It transforms little-endian to big-endian byte array, or vice versa.
	  * @param data the buffer containing the array */
	public static void reverseOrder(byte[] data) {
		//for (int i=0,j=data.length-1; i<data.length/2; i++,j--) { byte b=data[i]; data[i]=data[j]; data[j]=b; }
		reverseOrder(data,0,data.length);
	}

	/** Reverses the byte order of a block of bytes.
	  * It transforms little-endian to big-endian, or vice versa.
	 * @param buf buffer containing the block of bytes
	 * @param off offset of the bytes within the buffer
	 * @param len number of bytes to reverse */
	public static void reverseOrder(byte[] buf, int off, int len) {
		int end=off+len/2;
		for (int i=off,j=off+len-1; i<end; i++,j--) {
			byte b=buf[i];
			buf[i]=buf[j];
			buf[j]=b;
		}
	}

	/** Creates a copy of a given array with reverse byte order.
	  * It transforms little-endian to big-endian byte array, or vice versa.
	  * @param src the source array
	  * @return the new array */
	public static byte[] reverseOrderCopy(byte[] src) {
		byte[] dst=new byte[src.length];
		reverseOrderCopy(src,0,dst,0,src.length);
		return dst;
	}
	
	/** Creates a copy of a given array with reverse byte order.
	  * It transforms little-endian to big-endian byte array, or vice versa.
	  * @param src the source array
	  * @param off offset of the original array
	  * @param len number of bytes to reverse
	  * @return the new array */
	public static byte[] reverseOrderCopy(byte[] src, int off, int len) {
		byte[] dst=new byte[len];
		reverseOrderCopy(src,off,dst,0,len);
		return dst;
	}
	
	/** Creates a copy of a given array with reverse byte order.
	  * It transforms little-endian to big-endian byte array, or vice versa.
	  * @param src original array
	  * @param dst the new array */
	public static void reverseOrderCopy(byte[] src, byte[] dst) {
		reverseOrderCopy(src,0,dst,0,src.length);
	}

	/** Creates a copy of a given array with reverse byte order.
	  * It transforms little-endian to big-endian byte array, or vice versa.
	 * @param src buffer containing the original array
	 * @param src_off offset of the original array
	 * @param dst destination buffer
	 * @param dst_off offset within the destination buffer
	 * @param len number of bytes to reverse */
	public static void reverseOrderCopy(byte[] src, int src_off, byte[] dst, int dst_off, int len) {
		int dst_end=dst_off+len;
		for (int i=dst_off,j=src_off+len-1; i<dst_end; i++,j--) dst[i]=src[j];
	}


	// UNSIGNED INTEGERS:

	/** Gets the unsigned representation of a byte, returned as a <code>short</code>. The same as getting <code>(short)(b &amp; 0xFF)</code>.
	  * @param b the byte value
	  * @return the unsigned integer value (as <b>short</b>) of the given byte. */
	public static short uByte(byte b) {
		return (short)(b&0xFF);
	} 

	/** Gets the unsigned representation of a 32-bit integer, returned as a <code>long</code>. The same as getting <code>(long)n &amp; 0x00000000FFFFFFFF</code>.
	  * @param n the integer value
	  * @return the unsigned integer value (as <b>long</b>) of the given integer. */
	public static long uInt(int n) {
		return n&0xFFFFFFFFL;
	} 


	// HEX STRINGS:

	/** Array of hexadecimal digits (from '0' to 'f'). */
	//private static char[] HEX_DIGITS={'0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'};
	//private static char[] HEX_DIGITS={'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};

	/** Array of hexadecimal values (from 0 to 15). */
	/*private static int[] HEX_VALUES=new int[256];	
	static {
		for (char c=0; c<255; c++) HEX_VALUES[c]=(c>='0' && c<='9')? c-'0' : (c>='a' && c<='f')? c-'W' : (c>='A' && c<='F')? c-'7' : -1;
	}*/

	/** Gets the hexadecimal digit.
	 * The argument should be between 0 and 15. Values outside this interval give an undefined result. 
	 * @param d an integer between 0 and 15
	 * @return the hexadecimal digit, that is one of the following characters: '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', or 'f' */
	private static char hexDigit(int d) {
		//return (char)(d<0? invalidHexDigit(d) : d<10? '0'+d : d<16? '7'+d : invalidHexDigit(d)); // uppercase (note: '7'='A'-10)
		return (char)(d<0? invalidHexDigit(d) : d<10? '0'+d : d<16? 'W'+d : invalidHexDigit(d)); // lowercase (note: 'W'='a'-10)
	}

	/** Whether a character is a hexadecimal digit (that is '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', or 'f').
	  * @param c the character
	  * @return true of it is a valid hexadecimal digit */
	private static boolean isHexChar(char c) {
		return (c>='0' && c<='9') || (c>='a' && c<='f') || (c>='A' && c<='F');
	}

	/** Gets the value of a hexadecimal digit.
	 * Both upper and lower case characters from 'a' to 'f' are valid input symbols.
	 * No check is performed on the argument; the result for non-hexadecimal character is undefined.
	 * @param c the hexadecimal symbol, that is one of the following characters: '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'A', 'B', 'C', 'D', 'E', or 'F'
	 * @return the integer value (between 0 and 15) of the hexadecimal digit */
	/*private static int hexValue(char c) {
		//return HEX_VALUES[c];
		return c<='9'? c-'0' : (c<='F'? c-'7': c-'W');
	}*/

	/** Gets the value of a hexadecimal digit.
	 * Both upper and lower case characters from 'a' to 'f' are valid input symbols.
	 * A RuntimeException is thrown in case of invalid character.
	 * @param c the hexadecimal digit
	 * @return the integer value (between 0 and 15) of the given hexadecimal digit or throws a RuntimeException if it is not an hexadecimal digit */
	private static int parseHexDigit(char c) {
		return c<'0'? invalidHexChar(c) : c<='9'? c-'0' : c<'A'? invalidHexChar(c) : c<='F'? c-'7': c<'a'? invalidHexChar(c) : c-'W';
	}
	
	/** Throws a NumberFormatException for an invalid hexadecimal character.
	 * @param c the hexadecimal digit */	
	private static int invalidHexChar(char c) {
		throw new NumberFormatException("Invalid hexadecimal character '"+c+'\'');
	}

	/** Throws a NumberFormatException for an invalid hexadecimal digit value.
	 * @param n the digit value */	
	private static int invalidHexDigit(int n) {
		throw new NumberFormatException("Invalid hexadecimal digit value '"+n+'\'');
	}

	/** Gets a hexadecimal representation of a 1-byte integer.
	  * @param n the integer
	  * @return the hexadecimal representation */
	public static String int8ToHex(int n) {
		return new String(new char[]{hexDigit((n>>4)&0xF),hexDigit(n&0xF)});
	}

	/** Gets a hexadecimal representation of a 2-byte integer.
	  * @param n the integer
	  * @return the hexadecimal representation */
	public static String int16ToHex(int n) {
		return int8ToHex((n>>8)&0xFF)+int8ToHex(n&0xFF);
	}

	/** Gets a hexadecimal representation of a 4-byte integer.
	  * @param n the integer
	  * @return the hexadecimal representation */
	public static String int32ToHex(long n) {
		return int16ToHex((int)(n>>16)&0xFFFF)+int16ToHex((int)n&0xFFFF);
	}

	/** Gets a hexadecimal representation of an array of bytes.
	 * @param data the byte array
	 * @return the hexadecimal string */
	public static String toHex(byte[] data) {
		if (data==null) return null;
		// else
		return toHex(data,0,data.length);
	}
	
	/** Gets a hexadecimal representation of an array of bytes.
	 * @param buf buffer containing the byte array
	 * @param off the offset within the array
	 * @param len the number of bytes
	 * @return the hexadecimal string */
	public static String toHex(byte[] buf, int off, int len) {
		char[] str=new char[len*2];
		int end=off+len, index=0;
		while (off<end) {
			byte b=buf[off++];
			int c=(b>>4)&0x0f;
			str[index++]=(char)(c<10?c+'0':c-10+'a');
			c=b&0x0f;
			str[index++]=(char)(c<10?c+'0':c-10+'a');
		}
		return new String(str);
	}
	
	/** Gets a hexadecimal representation of an array of bytes.
	  * The hexadecimal values are formatted in blocks of a given size, separated by a given symbol.
	  * Example, in case of two-byte blocks (block size = 2) separated by colons, the string will be formatted as "aabb:ccdd:..:eeff". 
	  * @param data the byte array
	  * @param c the block separator
	  * @param num the number of bytes within each block
	  * @return the hexadecimal string */
	public static String toFormattedHex(byte[] data, char c, int num) {
		return toFormattedHex(data,0,data.length,c,num);
	}

	/** Gets a hexadecimal representation of an array of bytes.
	  * The hexadecimal values are formatted in blocks of a given size, separated by a given symbol.
	  * Example, in case of two-byte blocks (block size = 2) separated by colons, the string will be formatted as "aabb:ccdd:..:eeff". 
	  * @param buf the buffer containing the byte array
	  * @param off the offset within the array
	  * @param len the length of the array
	  * @param c the block separator
	  * @param num the number of bytes within each block
	  * @return the hexadecimal string */
	public static String toFormattedHex(byte[] buf, int off, int len, char c, int num) {
		int str_len=len*2+((len+num-1)/num)-1;
		char[] str=new char[str_len];
		int index=0;
		for (int i=0; i<len; i++) {
			byte b=buf[off+i];
			//str[index++]=HEX_DIGITS[(b&0xF0)>>4];
			//str[index++]=HEX_DIGITS[b&0x0F];
			str[index++]=hexDigit((b&0xF0)>>4);
			str[index++]=hexDigit(b&0x0F);
			if (((i+1)%num)==0 && index<str_len) str[index++]=c;
		}
		return new String(str);
	}  

	/** Gets an unformatted hexadecimal string.
	  * It removes any non-hexadecimal character from the string. 
	  * @param str the formatted hexadecimal string
	  * @return the unformatted hexadecimal string */
	public static String trimHex(String str) {
		StringBuffer sb=new StringBuffer();
		char c;
		for (int i=0; i<str.length(); i++) if (isHexChar(c=str.charAt(i))) sb.append(c);
		return sb.toString();
	}

	/** Gets bytes represented by a given hexadecimal string.
	 * @param str the hexadecimal string
	 * @return the array of bytes */
	public static byte[] fromHex(String str) {
		byte[] data=new byte[str.length()/2];
		fromHex(str,data,0);
		return data;
	}
	
	/** Gets bytes represented by a given hexadecimal string.
	 * @param str the hexadecimal string
	 * @param buf the buffer where the array of bytes will be written
	 * @param off the offset within the buffer
	 * @return the length of the array */
	public static int fromHex(String str, byte[] buf, int off) {
		int len=(str.length()+1)/2;
		for (int i=0; i<len; i++) buf[off+i]=(byte)((parseHexDigit(str.charAt(i*2))<<4)|parseHexDigit(str.charAt(i*2+1)));
		return len;
	}
	// Single-method implementation:
	/*public static int fromHex(String str, byte[] buf, int off) {
		if (str.length()%2!=0) throw new RuntimeException("String length must be multiple of 2: '"+str.length()+'\'');
		int len=str.length()/2;
		for (int i=0; i<len; i++) {
			int c=str.charAt(i*2);
			int v1=c<'0'? -1 : c<='9'? c-'0' : c<'A'? -1 : c<='F'? c-'7': c<'a'? -1 : c-'W';
			c=str.charAt(i*2+1);
			int v2=c<'0'? -1 : c<='9'? c-'0' : c<'A'? -1 : c<='F'? c-'7': c<'a'? -1 : c-'W';
			if (v1<0 || v2<0) throw new NumberFormatException("Invalid hexadecimal string '"+str+'\'');
			buf[off++]=(byte)((v1<<4)|v2);
		}
		return len;
	}*/

	/** Gets bytes represented by a given hexadecimal string.
	 * The string may also include colon ':' characters that separate blocks of hexadecimal values (e.g. aa:bb:cc:dd:..:ff). 
	 * <p>
	 * The same as {@link #fromFormattedHex(String, char)} with <i>c=':'</i>
	 * @param str the string with hexadecimal characters possibly separated by colon
	 * @return the array of bytes */
	public static byte[] fromFormattedHex(String str) {
		return fromFormattedHex(str,':');
	}

	/**Gets bytes represented by a given hexadecimal string.
	 * The string may also include colon ':' characters that separate blocks of hexadecimal values (e.g. aa:bb:cc:dd:..:ff). 
	 * <p>
	 * The same as {@link #fromFormattedHex(String, char, byte[], int)} with <i>c=':'</i>
	 * @param str the string with hexadecimal characters possibly separated by colon
	 * @param buf the buffer where the array of bytes will be written
	 * @param off the offset within the buffer
	 * @return the length of the array of bytes */
	public static int fromFormattedHex(String str, byte[] buf, int off) {
		return fromFormattedHex(str,':',buf,off);
	}
	
	/** Gets bytes represented by a given hexadecimal string.
	 * @param str the string with hexadecimal characters possibly separated by the given character
	 * @param c the separator character
	 * @return the array of bytes */
	public static byte[] fromFormattedHex(String str, char c) {
		int len=0;
		for (int i=0; i<str.length(); i++) if (str.charAt(i)!=c) len++;
		byte[] data=new byte[len/2];
		fromFormattedHex(str,c,data,0);
		return data;
	}

	/** Gets bytes represented by a given hexadecimal string.
	 * @param str the string with hexadecimal characters possibly separated by the given character
	 * @param c the separator character
	 * @param buf the buffer where the array of bytes will be written
	 * @param off the offset within the buffer
	 * @return the length of the array of bytes */
	public static int fromFormattedHex(String str, char c, byte[] buf, int off) {
		int index=off;
		char c0,c1;
		for (int i=0; i<str.length(); ) {
			while ((c0=str.charAt(i++))==c);
			while ((c1=str.charAt(i++))==c);
			buf[index++]=(byte)((parseHexDigit(c0)<<4)+parseHexDigit(c1));
		}
		return index-off;
	}


	// ASCII STRINGS:

	/** Gets ASCII representation of an array of bytes.
	  * Non-ASCII bytes are encoded as '.'.
	  * @param data the array of bytes
	  * @return the ASCII string */
	public static String toAscii(byte[] data) {
		return toAscii(data,0,data.length);
	}  
	
	/** Gets ASCII representation of an array of bytes.
	  * Non-ASCII bytes are encoded as '.'.
	  * @param buf buffer containing the array of bytes
	  * @param off the offset within the buffer
	  * @param len the number of bytes
	  * @return the ASCII string */
	public static String toAscii(byte[] buf, int off, int len) {
		char[] str=new char[len];
		int index=0;
		int end=off+len;
		while (off<end) {
			byte b=buf[off++];
			str[index++]=(b>=32 && b<127)? (char)b : '.';
		}
		return new String(str);
	}  

	// BINARY STRINGS:

	/** Gets binary representation of an array of bytes.
	 * @param data the byte array
	 * @return the binary string */
	public static String toBinary(byte[] data) {
		return toBinary(data,0,data.length);
	}

	/** Gets binary representation of an array of bytes.
	 * @param buf buffer containing the byte array
	 * @param off the offset within the buffer
	 * @param len the length of the array
	 * @return the binary string */
	public static String toBinary(byte[] buf, int off, int len) {
		StringBuffer sb=new StringBuffer();
		int end=off+len;
		for (int i=off; i<end; i++) {
			int b=buf[i];
			for (int k=7; k>=0; k--) {
				sb.append((b>>k)&0x01);
				//if (k==4) sb.append(" ");
			}
			//if (i<(end-1)) sb.append(" ");
		}
		return sb.toString();
	}
	
	
	// INTEGERS:

	/** Transforms a two-byte array into a 16-bit integer; the 2-byte array is in big-endian byte order (the big end, most significant byte, is stored first, at the lowest storage address).
	  * @param src the two-byte array representing an integer in big-endian byte order
	  * @return the 16-bit integer */
	public static int toInt16(byte[] src) {
		return toInt16(src,0);
	}
	
	/** Reads a 16-bit integer from a two-byte array; the two-byte array is in big-endian byte order (the big end, most significant byte, is stored first, at the lowest storage address).
	  * @param src a buffer containing the two-byte array representing an integer in big-endian byte order
	  * @param off the offset within the buffer
	  * @return the 16-bit integer */
	public static int toInt16(byte[] src, int off) {
		return ((src[off]&0xff)<<8) | (src[off+1]&0xff);
	}

	/** Transforms a two-byte array into a 16-bit integer; the two-byte array is in little-endian byte order (the little end, least significant byte, is stored first, at the lowest storage address).
	  * @param src the four-byte array representing an integer in little-endian byte order
	  * @return the 16-bit integer */
	public static int toInt16LittleEndian(byte[] src) {
		return toInt16LittleEndian(src,0);
	}
	
	/** Transforms a two-byte array into a 16-bit integer; the two-byte array is in little-endian byte order (the little end, least significant byte, is stored first, at the lowest storage address).
	  * @param src a buffer containing the four-byte array representing an integer in little-endian byte order
	  * @param off the offset within the buffer
	  * @return the 16-bit integer */
	public static int toInt16LittleEndian(byte[] src, int off) {
		return (((int)(src[off+1]&0xff))<<8)+(src[off]&0xff);
	}

	/** Transforms a 4-byte array into a 32-bit integer; the 4-byte array is in big-endian byte order (the big end, most significant byte, is stored first, at the lowest storage address).
	  * @param src the 4-byte array representing an integer in big-endian byte order
	  * @return the 32-bit integer */
	public static long toInt32(byte[] src) {
		return toInt32(src,0);
	}
	
	/** Transforms a 4-byte array into a 32-bit integer; the 4-byte array is in big-endian byte order (the big end, most significant byte, is stored first, at the lowest storage address).
	  * @param src a buffer containing the 4-byte array representing an integer in big-endian byte order
	  * @param off the offset within the buffer
	  * @return the 32-bit integer */
	public static long toInt32(byte[] src, int off) {
		return ((((((long)(src[off]&0xff)<<8)+(src[off+1]&0xff))<<8)+(src[off+2]&0xff))<<8)+(src[off+3]&0xff);
	}

	/** Transforms a four-byte array into a 32-bit integer; the four-byte array is in little-endian byte order (the little end, least significant byte, is stored first, at the lowest storage address).
	  * @param src the four-byte array representing an integer in little-endian byte order
	  * @return the 32-bit integer */
	public static long toInt32LittleEndian(byte[] src) {
		return toInt32LittleEndian(src,0);
	}
	
	/** Transforms a four-byte array into a 32-bit integer; the four-byte array is in little-endian byte order (the little end, least significant byte, is stored first, at the lowest storage address).
	  * @param src a buffer containing the four-byte array representing an integer in little-endian byte order
	  * @param off the offset within the buffer
	  * @return the 32-bit integer */
	public static long toInt32LittleEndian(byte[] src, int off) {
		return ((((((long)(src[off+3]&0xff)<<8)+(src[off+2]&0xff))<<8)+(src[off+1]&0xff))<<8)+(src[off]&0xff);
	}

	/** Gets the integer encoded in a byte array in big-endian byte order (the big end, most significant byte, is stored first, at the lowest storage address).
	  * @param src the byte array representing an integer in big-endian byte order
	  * @return the integer */
	public static long toInt(byte[] src) {
		return toInt(src,0,src.length);
	}
	
	/** Gets the integer encoded in a byte array in big-endian byte order (the big end, most significant byte, is stored first, at the lowest storage address).
	  * @param src a buffer containing the byte array representing an integer in big-endian byte order
	  * @param off the offset within the buffer
	  * @param len the number of bytes
	  * @return the integer */
	public static long toInt(byte[] src, int off, int len) {
		long val=0;
		for (int i=off, end=off+len; i<end; i++) val=(val<<8) | (src[i]&0xff);
		return val;
	}

	/** Gets the integer encoded in a byte array in little-endian byte order.
	  * @param src the byte array representing an integer in little-endian byte order
	  * @return the integer */
	public static long toIntLittleEndian(byte[] src) {
		return toIntLittleEndian(src,0,src.length);
	}
	
	/** Gets the integer encoded in a byte array in little-endian byte order.
	  * @param src a buffer containing the byte array representing an integer in little-endian byte order
	  * @param off the offset within the buffer
	  * @param len the number of bytes
	  * @return the integer */
	public static long toIntLittleEndian(byte[] src, int off, int len) {
		long val=0;
		for (int i=off+len-1; i>=off; i--) val=(val<<8) | (src[i]&0xff);
		return val;
	}

	/** Transforms a 16-bit integer into a two-byte array in big-endian byte order (the big end, most significant byte, is stored first, at the lowest storage address).
	  * @param val the 16-bit integer
	  * @return a two-byte array representing the given integer, in big-endian byte order */
	public static byte[] fromInt16(int val) {
		byte[] b=new byte[2];
		fromInt16(val,b,0);
		return b;
	}

	/** Writes a 16-bit integer into a two-byte array in big-endian byte order (the big end, most significant byte, is stored first, at the lowest storage address).
	  * @param val the 16-bit integer
	  * @param dst a buffer for the two-byte big-endian representation of the given integer
	  * @param off the offset within the buffer */
	public static void fromInt16(int val, byte[] dst, int off) {
		dst[off]=(byte)(val>>8);
		dst[off+1]=(byte)val;
	}

	/** Transforms a 16-bit integer into a two-byte array in little-endian byte order (the little end, least significant byte, is stored first, at the lowest storage address).
	  * @param n the 16-bit integer
	  * @return a two-byte array representing the given integer, in little-endian byte order */
	public static byte[] fromInt16LittleEndian(int n) {
		byte[] dst=new byte[2];
		fromInt16LittleEndian(n,dst,0);
		return dst;
	}

	/** Transforms a 16-bit integer into a two-byte array copied into a given buffer; the 2-bytes array is in little-endian byte order (the little end, least significant byte, is stored first, at the lowest storage address).
	  * @param val the 16-bit integer
	  * @param dst a buffer for the two-byte little-endian representation of the given integer
	  * @param off the offset within the buffer */
	public static void fromInt16LittleEndian(int val, byte[] dst, int off) {
		dst[off+1]=(byte)(val>>8);
		dst[off]=(byte)val;
	}

	/** Transforms a 32-bit integer into a four-byte array in big-endian byte order (the big end, most significant byte, is stored first, at the lowest storage address).
	  * @param val the 32-bit integer
	  * @return a four-byte array representing the given integer, in big-endian byte order */
	public static byte[] fromInt32(long val) {
		byte[] dst=new byte[4];
		fromInt32(val,dst,0);
		return dst;
	}

	/** Transforms a 32-bit integer into a four-byte array in big-endian byte order (the big end, most significant byte, is stored first, at the lowest storage address).
	  * @param val the 32-bit integer
	  * @param dst a buffer for the four-byte big-endian representation of the given integer
	  * @param off the offset within the buffer */
	public static void fromInt32(long val, byte[] dst, int off) {
		dst[off]=(byte)(val>>24);
		dst[off+1]=(byte)(val>>16);
		dst[off+2]=(byte)(val>>8);
		dst[off+3]=(byte)val;
	}

	/** Transforms a 32-bit integer into a four-byte array in little-endian byte order (the little end, least significant byte, is stored first, at the lowest storage address).
	  * @param n the 32-bit integer
	  * @return a four-byte array representing the given integer, in little-endian byte order */
	public static byte[] fromInt32LittleEndian(long n) {
		byte[] dst=new byte[4];
		fromInt32LittleEndian(n,dst,0);
		return dst;
	}

	/** Transforms a 32-bit integer into a four-byte array copied into a given buffer; the 4-bytes array is in little-endian byte order (the little end, least significant byte, is stored first, at the lowest storage address).
	  * @param n the 32-bit integer
	  * @param dst a buffer for the four-byte little-endian representation of the given integer
	  * @param off the offset within the buffer */
	public static void fromInt32LittleEndian(long n, byte[] dst, int off) {
		dst[off+3]=(byte)(n>>24);
		dst[off+2]=(byte)(n>>16);
		dst[off+1]=(byte)(n>>8);
		dst[off]=(byte)n;
	}

	/** Transforms a 48-bit integer into a six-byte array in big-endian byte order (the big end, most significant byte, is stored first, at the lowest storage address).
	  * @param val the 48-bit integer
	  * @return a six-byte array representing the given integer, in big-endian byte order */
	public static byte[] fromInt48(long val) {
		byte[] dst=new byte[6];
		fromInt48(val,dst,0);
		return dst;
	}

	/** Transforms a 48-bit integer into a six-byte array in big-endian byte order (the big end, most significant byte, is stored first, at the lowest storage address).
	  * @param val the 48-bit integer
	  * @param dst a buffer for the six-byte big-endian representation of the given integer
	  * @param off the offset within the buffer */
	public static void fromInt48(long val, byte[] dst, int off) {
		dst[off]=(byte)(val>>40);
		dst[off+1]=(byte)(val>>32);
		dst[off+2]=(byte)(val>>24);
		dst[off+3]=(byte)(val>>16);
		dst[off+4]=(byte)(val>>8);
		dst[off+5]=(byte)val;
	}

	/** Transforms a 64-bit signed integer (as long) into an eight-byte array in big-endian byte order (the big end, most significant byte, is stored first, at the lowest storage address).
	  * @param val the 64-bit integer
	  * @return an eight-byte array representing the given integer, in big-endian byte order */
	public static byte[] fromInt64(long val) {
		byte[] dst=new byte[8];
		fromInt64(val,dst,0);
		return dst;
	}

	/** Transforms a 64-bit signed integer (as long) into an eight-byte array in big-endian byte order (the big end, most significant byte, is stored first, at the lowest storage address).
	  * @param val the 64-bit integer
	  * @param dst a buffer for the eight-byte big-endian representation of the given integer
	  * @param off the offset within the buffer */
	public static void fromInt64(long val, byte[] dst, int off) {
		dst[off]=(byte)(val>>56);
		dst[off+1]=(byte)(val>>48);
		dst[off+2]=(byte)(val>>40);
		dst[off+3]=(byte)(val>>32);
		dst[off+4]=(byte)(val>>24);
		dst[off+5]=(byte)(val>>16);
		dst[off+6]=(byte)(val>>8);
		dst[off+7]=(byte)val;
	}

	/** Gets a byte array representation of an integer, in big-endian byte order (the big end, most significant byte, is stored first, at the lowest storage address).
	  * @param val the integer
	  * @param len the number of bytes
	  * @return an n-byte array representing the given integer, in big-endian byte order */
	public static byte[] fromInt(long val, int len) {
		byte[] dst=new byte[len];
		fromInt(val,dst,0,len);
		return dst;
	}

	/** Gets a byte array representation of an integer, in big-endian byte order (the big end, most significant byte, is stored first, at the lowest storage address).
	  * @param val the integer
	  * @param dst a buffer for the n-byte big-endian representation of the given integer
	  * @param off the offset within the buffer
	  * @param len the number of bytes */
	public static void fromInt(long val, byte[] dst, int off, int len) {
		for (int i=off+len-1; i>=off; i--, val>>>=8) dst[i]=(byte)val;
	}
		
	/** Gets a byte array representation of an integer, in little-endian byte order.
	  * @param val the integer
	  * @param len the number of bytes
	  * @return an n-byte array representing the given integer, in little-endian byte order */
	public static byte[] fromIntLittleEndian(long val, int len) {
		byte[] dst=new byte[len];
		fromIntLittleEndian(val,dst,0,len);
		return dst;
	}

	/** Gets a byte array representation of an integer, in little-endian byte order.
	  * @param val the integer
	  * @param dst a buffer for the n-byte little-endian representation of the given integer
	  * @param off the offset within the buffer
	  * @param len the number of bytes */
	public static void fromIntLittleEndian(long val, byte[] dst, int off, int len) {
		for (int i=off, end=off+len; i<end; i++, val>>>=8) dst[i]=(byte)val;
	}

	/** Adds 1 to the number represented by the given array of bytes, module 2^n, where n is the length (in bits) of the array.
	  * @param buf the buffer containing the array */
	public static void inc(byte[] buf) {
		for (int i=buf.length-1; i>=0; i--) {
			if ((buf[i]=(byte)((buf[i]&0xFF)+1))!=0) break;
		}
	}

	/** Converts a Java object into an array of bytes using serialization.
	 * @param obj the object to serialize
	 * @return the array of bytes
	 * @throws IOException */
	public static byte[] fromJavaObject(Object obj) throws IOException {
		ByteArrayOutputStream bos=new ByteArrayOutputStream();
		ObjectOutputStream oos=new ObjectOutputStream(bos);
		oos.writeObject(obj);
		oos.flush();
		byte[] data=bos.toByteArray();
		oos.close();
		bos.close();
		return data;
	}

	/** Gets a Java object from an array of bytes using serialization.
	 * @param data the array of bytes where an object has been serialized
	 * @return the object
	 * @throws IOException
	 * @throws ClassNotFoundException */
	public static Object toJavaObject(byte[] data) throws IOException, ClassNotFoundException {
		ByteArrayInputStream bis=new ByteArrayInputStream(data);
		ObjectInputStream ois=new ObjectInputStream(bis);
		Object obj=ois.readObject();
		ois.close();
		bis.close();
		return obj;
	}

}
