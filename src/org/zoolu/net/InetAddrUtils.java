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

package org.zoolu.net;


import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import org.zoolu.util.Bytes;


/** Methods for handling Internet socket addresses.
  */
public class InetAddrUtils {

	/** Gets a socket address.
	 * @param buf the array containing the socket address
	 * @param off the offset within the array
	 * @param len the socket address length (6 in case of IPv4, 18 in case of IPv6)
	 * @return the socket address */
	public static InetSocketAddress parseInetSocketAddress(byte[] buf, int off, int len) {
		if (len!=6 && len!=18) throw new RuntimeException("Invalid IP address length ("+(len-2)+")");
		byte[] addr=new byte[len-2];
		System.arraycopy(buf,off,addr,0,addr.length);
		try {
			return new InetSocketAddress(InetAddress.getByAddress(addr),Bytes.toInt16(buf,off+addr.length));
		}
		catch (UnknownHostException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	/** Gets a socket address.
	 * @param soaddr the string representing the socket address
	 * @return the socket address
	 * @throws IOException */
	public static InetSocketAddress parseInetSocketAddress(String soaddr) throws IOException {
		/*int index=soaddr.length()-1;
		while (index>=0 && soaddr.charAt(index)!=':') index--;
		if (index<0) throw new IOException("Malformed socket address: port number not found: "+soaddr);
		InetAddress inetaddr;
		inetaddr=InetAddress.getByName(soaddr.substring(0,index));
		int port=Integer.parseInt(soaddr.substring(index+1));
		return new InetSocketAddress(inetaddr,port);*/
		return new InetSocketAddress(parseSocketInetAddress(soaddr),parseSocketPort(soaddr));
	}

	/** Gets a string representation of a socket address.
	 * @param soaddr the socket address
	 * @return the socket address as a string */
	public static String toString(InetSocketAddress soaddr) {
		if (soaddr==null) return null;
		return soaddr.getAddress().getHostAddress()+':'+soaddr.getPort();
	}

	/** Gets the IP address of a socket address.
	 * @param soaddr the socket address
	 * @return the IP address
	 * @throws IOException */
	public static InetAddress parseSocketInetAddress(String soaddr) throws IOException {
		return InetAddress.getByName(parseSocketHost(soaddr));
	}
	
	/** Gets the host part of a socket address.
	 * @param soaddr the socket address
	 * @return the host
	 * @throws IOException */
	public static String parseSocketHost(String soaddr) throws IOException {
		int begin=soaddr.indexOf('[');
		if (begin>=0) {
			int end=soaddr.indexOf(']');
			if (end<0) throw new IOException("Malformed socket address: "+soaddr);
			return soaddr.substring(begin+1,end);
		}
		else {
			int end=soaddr.indexOf(':');
			if (end>=0 && soaddr.indexOf(':',end+1)>0) end=-1; // IPv6 address?
			return end<0? soaddr : soaddr.substring(0,end);
		}
	}
	
	/** Gets the port value of a socket address.
	 * @param soaddr the socket address
	 * @return the port value if present, otherwise -1
	 * @throws IOException */
	public static int parseSocketPort(String soaddr) throws IOException {
		return parseSocketPort(soaddr,-1);
	}
	
	/** Gets the port value of a socket address.
	 * @param soaddr the socket address
	 * @param defaultport the default port value
	 * @return the port value if present, otherwise the default value
	 * @throws IOException */
	public static int parseSocketPort(String soaddr, int defaultport) throws IOException {
		int index=soaddr.indexOf('[');
		if (index>=0) {
			index=soaddr.indexOf(']');
			if (index<0) throw new IOException("Malformed socket address: "+soaddr);
			index++;
		}
		else index=0;
		index=soaddr.indexOf(':',index);
		if (index>=0 && soaddr.indexOf(':',index+1)>0) index=-1; // IPv6 address?
		return index<0? defaultport : Integer.parseInt(soaddr.substring(index+1));
	}

}
