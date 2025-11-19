/*
 * Copyright 2018 NetSec Lab - University of Parma
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Author(s):
 * Luca Veltri (luca.veltri@unipr.it)
 */

package io.ipstack.net.tuntap;

import java.io.IOException;

import org.zoolu.util.Bytes;
import org.zoolu.util.SystemUtils;
import org.zoolu.util.log.DefaultLogger;
import org.zoolu.util.log.LoggerLevel;


/** TUN/TAP socket.
 */
public class TuntapSocket {
	
	/** Debug mode */
	public static boolean DEBUG=false;

	/** Prints a debug message. */
	private void debug(String str) {
		DefaultLogger.log(LoggerLevel.DEBUG,getClass(),str);
	}	
	

	/** Interface types, TUN or TAP */
	public enum Type { TUN, TAP }
	
	
	/** Loads the tuntap library */
	static {
		SystemUtils.loadLibrary("tuntap-64","tuntap-32","tuntap");
	}


	/** The interface type */
	Type type;
	
	/** Receiver buffer */
	//private byte[] recv_buffer=new byte[65535];
	
	/** File descriptor of the TUN/TAP socket */
	int fd;
 
	
	/** Creates a new TUN/TAP socket.
	 * @param type interface type (TUN or TAP)
	 * @param name name of the interface (e.g. "tun0"); if <i>null</i>, a new interface is added
	 * @throws IOException */
	public TuntapSocket(Type type, String name) throws IOException {
		this(type,name,null);
	}
			
	/** Creates a new TUN/TAP socket.
	 * @param type interface type (TUN or TAP)
	 * @param name name of the interface (e.g. "tun0"); if <i>null</i>, a new interface is added
	 * @param dev_file device file (if any) or <i>null</i>
	 * @throws IOException */
	public TuntapSocket(Type type, String name, String dev_file) throws IOException {
		this.type=type;
		boolean is_tun=type==Type.TUN;
		fd=dev_file==null? open(is_tun,name) : openDev(is_tun,name,dev_file);
		if (fd<0) throw new IOException("Unable to open the "+getType()+" interface '"+name+"'"+(dev_file!=null? " ("+dev_file+")" : ""));
	}
	
	/** Gets the interface type.
	 * @return "TUN" or "TAP" */
	public String getType() {
		return type==Type.TUN?"TUN":"TAP";
	}

	/** Sends a packet.
	 * @param data the packet to be sent
	 * @throws IOException */
	public void send(byte[] data) throws IOException {
		send(data,0,data.length);
	}

	/** Sends a packet.
	 * @param buf the buffer used for passing the packet
	 * @param off the offset within the buffer
	 * @param len the length of the packet
	 * @throws IOException */
	public void send(byte[] buf, int off, int len) throws IOException {
		if (DEBUG) debug("send(): "+Bytes.toHex(buf,off,len));
		len=write(fd,buf,off,len);
		if (len<0) throw new IOException("Send failure ("+len+")");
	}

	/** Sends a packet.
	 * @param pkt the packet to send. It is expected a TunPacket or an EthPacket depending on whether it is a TUN or TAP interface
	 * @throws IOException */
	/*public <A extends Address, P extends Packet<A>> void send(P pkt) throws IOException {
		byte[] data=pkt.getBytes();
		int len=write(fd,data,0,data.length);
		if (len<0) throw new IOException("Send failure ("+len+")");
	}*/

	/** Receives a packet.
	 * @return the received packet */
	/*public byte[] receive() throws IOException {
		synchronized (recv_buffer) {
			int len=receive(recv_buffer,0);
			byte[] data=new byte[len];
			System.arraycopy(recv_buffer,0,data,0,len);
			return data;
		}
	}*/
	
	/** Receives a packet.
	 * @param buf the buffer used for returning the received packet
	 * @param off the offset within the buffer
	 * @return the number of characters read, i.e. the packet length 
	 * @throws IOException */
	public int receive(byte[] buf, int off) throws IOException {
		int len=read(fd,buf,off);
		if (len<0) throw new IOException("Receive failure ("+len+")");
		if (DEBUG) debug("receive(): "+Bytes.toHex(buf,off,len));
		return len;		
	}
	
	/** Receives a packet.
	 * @return the received packet. It is a TunPacket or an EthPacket depending on whether it is a TUN or TAP interface
	 * @throws IOException */
	/*public Packet<?> receive() throws IOException {
		Packet<?> pkt=null;
		synchronized (recv_buffer) {
			int len=read(fd,recv_buffer,0);
			if (len<0) throw new IOException("Receive failure ("+len+")");
			byte[] data=new byte[len];
			System.arraycopy(recv_buffer,0,data,0,len);
			pkt=type==Type.TUN? new TunPacket(data,0,len) : EthPacket.parseEthPacket(data,0,len);
		}
		return pkt;
	}*/
	
	
	/** Closes the TUN/TAP socket. */
	public void close() {
		close(fd);
	}


	
	// *************************** Native methods: ***************************

	/** Opens a TUN/TAP socket.
	 * @param is_tun whether it is a TUN interface
	 * @param name name of the interface (e.g. "tun0")
	 * @return the new TUN/TAP identifier in case of success, -1 on error */
	private native int open(boolean is_tun, String name);
	 
	/** Opens a TUN/TAP socket.
	 * @param is_tun whether it is a TUN interface
	 * @param name name of the interface (e.g. "tun0")
	 * @param dev_file device file (if any), or <i>null</i>
	 * @return the new TUN/TAP identifier in case of success, -1 on error */
	private native int openDev(boolean is_tun, String name, String dev_file);

	/** Closes the TUN/TAP socket.
	 * @param fd file descriptor of the TUN/TAP interface */
	private native void close(int fd);
	 
	/** Writes a raw packet.
	 * @param fd file descriptor of the TUN/TAP interface
	 * @param buf the buffer used for passing the packet
	 * @param off the offset within the buffer
	 * @param len the length of the packet
	 * @return the number of characters written, i.e. the packet length in case of success; -1 on error */
	private native int write(int fd, byte[] buf, int off, int len);

	/** Reads a raw packet.
	 * @param fd file descriptor of the TUN/TAP interface
	 * @param buf the buffer used for returning the received packet
	 * @param off the offset within the buffer
	 * @return the number of characters read, i.e. the packet length */
	private native int read(int fd, byte[] buf, int off);

}
