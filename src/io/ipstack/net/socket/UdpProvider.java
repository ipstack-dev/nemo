/*
 * Copyright (c) 2020 Luca Veltri, University of Parma
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

package io.ipstack.net.socket;


import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;


/** UdpProvider provides an UDP send/receive service.
  * On the receiver side it waits for UDP datagrams and passes them
  * to the UdpProviderListener.
  * <p> If the attribute <i>alive_time</i> has a non-zero value, the UdpProvider stops
  * after <i>alive_time</i> milliseconds of inactivity.
  * <p> When a new packet is received, the onReceivedPacket(UdpProvider,DatagramPacket)
  * method is fired.
  * <p> Method onServiceTerminated(UdpProvider) is fired when the the UdpProvider stops 
  * receiving packets.
  */
public class UdpProvider extends Thread {
	
	/** The reading buffer size */
	public static final int BUFFER_SIZE=65535;
	  
	/** Default value for the maximum time that the UDP receiver can remain active after been halted (in milliseconds) */
	public static final int DEFAULT_SOCKET_TIMEOUT=2000; // 2sec 

	/** UDP socket */
	protected DatagramSocket socket;  

	/** UdpProvider listener */
	protected UdpProviderListener listener;

	/** Maximum time that the UDP receiver can remain active after been halted (in milliseconds) */
	protected int socket_timeout;

	/** Maximum time that the UDP receiver remains active without receiving UDP datagrams (in milliseconds) */
	protected long alive_time;

	/** Minimum size for received packets. Shorter packets are silently discarded. */
	protected int minimum_length; 

	/** Whether it has been halted */
	protected boolean stop; 

	/** Whether it is running */
	protected boolean is_running; 


	  
	/** Creates a new UdpProvider. */ 
	public UdpProvider(DatagramSocket socket, UdpProviderListener listener) {
		init(socket,0,listener);
		start();
	}


	/** Creates a new UdpProvider. */ 
	public UdpProvider(DatagramSocket socket, long alive_time, UdpProviderListener listener) {
		init(socket,alive_time,listener);
		start();
	}


	/** Inits the UdpProvider. */ 
	private void init(DatagramSocket socket, long alive_time, UdpProviderListener listener) {
		this.listener=listener;
		this.socket=socket;
		this.socket_timeout=DEFAULT_SOCKET_TIMEOUT;
		this.alive_time=alive_time;
		this.minimum_length=0; 
		this.stop=false; 
		this.is_running=true; 
	}


	/** Gets the UDP socket. */ 
	public DatagramSocket getSocket() {
		return socket;
	}


	/** Sets a new UDP socket. */ 
	/*public void setSocket(DatagramSocket socket) {
		this.socket=socket;
	}*/


	/** Whether the service is running. */
	public boolean isRunning() {
		return is_running;
	}


	/** Sets the maximum time that the UDP service can remain active after been halted. */
	public void setSoTimeout(int timeout) {
		socket_timeout=timeout;
	}


	/** Gets the maximum time that the UDP service can remain active after been halted. */
	public int getSoTimeout() {
		return socket_timeout;
	}


	/** Gets the maximum time that the UDP receiver remains active without receiving UDP datagrams (in milliseconds). */
	public long getAliveTime() {
		return alive_time;
	}


	/** Sets the minimum size for received packets.
	  * Packets shorter than that are silently discarded. */
	public void setMinimumReceivedDataLength(int len) {
		minimum_length=len;
	}


	/** Gets the minimum size for received packets.
	  * Packets shorter than that are silently discarded. */
	public int getMinimumReceivedDataLength() {
		return minimum_length;
	}


	/** Sends a UDP packet. */
	public void send(DatagramPacket packet) throws IOException {
		if (!stop) socket.send(packet);
	}


	/** Stops running. */
	public void halt() {
		stop=true;
	}


	/** The main thread. */
	public void run() {
		
		byte[] buf=new byte[BUFFER_SIZE];
		DatagramPacket packet=new DatagramPacket(buf, buf.length);
					
		Exception error=null;
		long expire=0;
		if (alive_time>0) expire=System.currentTimeMillis()+alive_time;
		try {
			socket.setSoTimeout(socket_timeout);
			// loop
			while(!stop) {
				try {
					socket.receive(packet);
				}
				catch (InterruptedIOException ie) {
					if (alive_time>0 && System.currentTimeMillis()>expire) halt();
					continue;
				}
				if (packet.getLength()>=minimum_length) {
					//if (listener!=null) listener.onReceivedPacket(this,packet);
					if (listener!=null)
						try { listener.onReceivedPacket(this,packet);
						}
						catch (Exception e) {
							e.printStackTrace();
						}
					if (alive_time>0) expire=System.currentTimeMillis()+alive_time;
				}
				packet=new DatagramPacket(buf, buf.length);
			}
		}
		catch (Exception e) {
			error=e;
			stop=true;
		} 
		is_running=false;
		if (listener!=null) listener.onServiceTerminated(this,error);
		listener=null;
	}

	
	/** Gets a String representation of the Object. */
	public String toString() {
		//if (socket.isClosed()) return "udp:closed";
		//else return "udp:"+socket.getLocalAddress().getHostAddress()+":"+socket.getLocalPort();
		return getClass().getSimpleName()+'['+socket.toString()+']';
	}

}
