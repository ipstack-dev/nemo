/*
 * Copyright (c) 2023 Luca Veltri, University of Parma
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

import java.security.NoSuchAlgorithmException;


/** Static methods for obtaining default message-digest algorithms.
 * In case the algorithm is not supported a RuntimeException is thrown in place of NoSuchAlgorithmException.
 */
public class MessageDigest {
	private MessageDigest() {}
	
	/**
	 * @return default java implementation of MD5 hash algorithm.
	 */
	public static java.security.MessageDigest getMD5() {
		return getInstance("MD5");
	}

	
	/**
	 * @return default java implementation of SHA1 hash algorithm.
	 */
	public static java.security.MessageDigest getSHA1() {
		return getInstance("SHA-1");
	}
	
	
	/**
	 * @return default java implementation of SHA2-256 hash algorithm.
	 */
	public static java.security.MessageDigest getSHA256() {
		return getInstance("SHA-256");
	}
	
	
	/**
	 * @return default java implementation of the given algorithm.
	 * Note: In case the algorithm is not supported a RuntimeException is thrown.
	 */
	public static java.security.MessageDigest getInstance(String algo) {
		try {
			return java.security.MessageDigest.getInstance(algo);
		}
		catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e.getMessage());
		}		
	}
	
}
