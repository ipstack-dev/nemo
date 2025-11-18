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



/** Base64 encoder and decoder.
  * It can be used for base64-encoding a byte array and/or
  * for base64-decoding a base64 string.
  * <p>
  * This implementation is faster than the one provided by Sun
  * through classes sun.misc.BASE64Encoder and sun.misc.BASE64Decoder.
  * (the comparison has been done on JDK1.4.2 VM for Windows).
  * <p>
  * Note: The performances could be further increased by implementing
  * the char-to-byte conversion (i.e. the base64 decoding) by using
  * a proper static mapping array.
  */
public class Base64 {
	
	/** Array of base64 chars */
	static final String B64CHARS="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";   

						  
	/** Converts base64 int to char. */
	/*private static char intToChar(int i) {
		return B64CHARS.charAt(i);
	}*/


	/** Converts base64 char to int.
	 * If the given char is not a valid base64 char it returns -1.
	 * @param c the base64 char
	 * @return the integer value */
	private static int charToInt(char c) {
		if (c>='a' && c<='z') return 26+c-'a';
		if (c>='A' && c<='Z') return c-'A';
		if (c>='0' && c<='9') return 52+c-'0';
		if (c=='+') return 62;
		if (c=='/') return 63;
		return -1;
	}


	/** Encodes in base64 a given array of bytes.
	 * @param input the input byte array
	 * @return the base64 string */
	public static String encode(byte[] input) {
		
		StringBuffer sb=new StringBuffer();

		int len_floor3=((input.length)/3)*3;
		for (int i=0; i<len_floor3; ) {
			byte bin0=input[i++];
			byte bin1=input[i++];
			byte bin2=input[i++];
			int ch0=((bin0>>>2)&0x3F);
			int ch1=((bin0&0x3)<<4) + ((bin1>>>4)&0xF);
			int ch2=((bin1&0xF)<<2) + ((bin2>>>6)&0x3);
			int ch3=((bin2&0x3F));
			sb.append(B64CHARS.charAt(ch0)).append(B64CHARS.charAt(ch1)).append(B64CHARS.charAt(ch2)).append(B64CHARS.charAt(ch3));
		} 
	
		int len_mod3=(input.length)%3;
		if (len_mod3==1) {
			byte bin0=input[len_floor3];
			int ch0=((bin0>>>2)&0x3F);
			int ch1=(bin0&0x3)<<4;        
			sb.append(B64CHARS.charAt(ch0)).append(B64CHARS.charAt(ch1)).append("==");
		}
		else 
		if (len_mod3==2) {
			byte bin0=input[len_floor3];
			byte bin1=input[len_floor3+1];
			int ch0=((bin0>>>2)&0x3F);
			int ch1=((bin0&0x3)<<4) + ((bin1>>>4)&0xF);
			int ch2=((bin1&0xF)<<2);     
			sb.append(B64CHARS.charAt(ch0)).append(B64CHARS.charAt(ch1)).append(B64CHARS.charAt(ch2)).append("=");
		}
		return sb.toString();
	}

 
	/** Justifies a string fitting a given line length.
	 * @param str the input string
	 * @param len the line length
	 * @return the output string */
	public static String justify(String str, int len) {
		StringBuffer sb=new StringBuffer();
		char[] buff=str.toCharArray();
		int begin=0;
		int end=len; 
		while (end<buff.length) {
			sb.append(buff,begin,len);
			sb.append("\r\n");
			begin=end;
			end+=len;
		}
		sb.append(buff,begin,buff.length-begin);
		return sb.toString();
	}


	/** Trims a string removing all non-base64 chars.
	 * @param str the input string
	 * @return the output string */
	public static String trim(String str) {
		int len=0;
		char[] buff=str.toCharArray();
		for (int i=0; i<buff.length; i++) {
			char c=buff[i];
			if ((c>='a' && c<='z') || (c>='A' && c<='Z') || (c>='0' && c<='9') || c=='+' || c=='/' || c=='=') buff[len++]=buff[i];
		}
		return new String(buff,0,len);
	}


  /** Decodes a given base64 string.
   * @param str64 the base64 string
   * @return the decoded byte array */
	public static byte[] decode(String str64) {
		if ((str64.length()/4)*4!=str64.length()) return null;
		// else
		/*int str_len=str64.length();
		if (str64.charAt(str_len-1)=='=') str_len--;
		if (str64.charAt(str_len-1)=='=') str_len--;

		int str_len_floor4=((str_len)/4)*4;      
		int len_floor3=((str_len)/4)*3;
		//int len_mod3=3-(str64.length()-str_len); // ERROR
		int len_mod3=(str_len-str64.length()+3)%3; // CORRECT
		byte[] output=new byte[len_floor3+len_mod3];    
		*/
		int pad_len=0;
		while (str64.charAt(str64.length()-1-pad_len)=='=') pad_len++;
		int str_len_floor4=((str64.length()-pad_len)/4)*4;
		int len_floor3=((str_len_floor4)/4)*3;
		int len_mod3=(3-pad_len)%3;
		byte[] output=new byte[len_floor3+len_mod3];
		
		int k=0;
		for (int i=0; i<str_len_floor4; ) {
			int ch0=charToInt(str64.charAt(i++));
			int ch1=charToInt(str64.charAt(i++));
			int ch2=charToInt(str64.charAt(i++));
			int ch3=charToInt(str64.charAt(i++)); 
			int bin0=(ch0<<2) + (ch1>>>4);
			int bin1=(ch1%16<<4) + (ch2>>>2);
			int bin2=(ch2%4<<6) + ch3;  
			output[k++]=(byte)bin0;
			output[k++]=(byte)bin1;
			output[k++]=(byte)bin2;
		}
		
		if (len_mod3==1) {
			int ch0=charToInt(str64.charAt(str_len_floor4));
			int ch1=charToInt(str64.charAt(str_len_floor4+1));
  	      int bin0=(ch0<<2) + (ch1>>>4);
			output[len_floor3]=(byte)bin0;           
	   }
	   else 
		if (len_mod3==2) {
			int ch0=charToInt(str64.charAt(str_len_floor4));
			int ch1=charToInt(str64.charAt(str_len_floor4+1));
			int ch2=charToInt(str64.charAt(str_len_floor4+2));
			int bin0=(ch0<<2) + (ch1>>>4);
			int bin1=(ch1%16 <<4) + (ch2>>>2);
			output[len_floor3]=(byte)bin0;
			output[len_floor3+1]=(byte)bin1;   
 	   }
		return output;
	}

}
