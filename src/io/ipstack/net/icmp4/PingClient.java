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

package io.ipstack.net.icmp4;


import java.io.PrintStream;

import org.zoolu.util.Clock;
import org.zoolu.util.Timer;
import org.zoolu.util.TimerListener;

import io.ipstack.net.icmp4.IcmpMessage;
import io.ipstack.net.icmp4.message.IcmpEchoReplyMessage;
import io.ipstack.net.icmp4.message.IcmpEchoRequestMessage;
import io.ipstack.net.ip4.Ip4Address;
import io.ipstack.net.ip4.Ip4Layer;
import io.ipstack.net.ip4.Ip4Packet;


/** PING client.
 * It sends ICMP Echo Request messages to a remote node and captures possible
 * ICMP Echo Reply messages.
 */
public class PingClient {
	
	/** Default time waited before ending [milliseconds] */
	public static long DEFAULT_CLEARING_TIME=3000;

	/** Maximum number of backlogged departures */
	static int BACKLOGGED_DEPARTURES=300; // 5min

	/** Whether running in blocking mode */
	Boolean blocking=true;

	/** ICMP layer */
	IcmpLayer icmp_layer;
	
	/** Identifier in the ICMP Echo request */
	int echo_id;

	/** Payload data in the ICMP Echo request */
	byte[] echo_data;
	
	/** IP address of the target node */
	Ip4Address target_ip_addr;
	
	/** Ping period time */
	long ping_time;
	
	/** Time waited before ending [milliseconds] */
	 long clearing_time=DEFAULT_CLEARING_TIME;

	/** Output */
	PrintStream out;

	/** This ICMP listener */
	IcmpLayerListener this_icmp_listener;
	
	/** Starting time */
	long start_time;

	/** Time for receiving the last packet */
	long total_time=-1;

	/** Number of ping requests */
	int req_count;

	/** Counter of received replies */
	int reply_count;
	
	/** Last received TTL value */
	int last_ttl=-1;
	
	/** Departure times, for computing the RTTs */
	long[] departure_time=new long[BACKLOGGED_DEPARTURES];
	
	/** Lock used in blocking mode */
	//Boolean lock=true;

	/** Whether it is running */
	Boolean is_running=new Boolean(true);

	
	/** Creates and runs a ping session.
	 * @param ip_layer IP layer
	 * @param out output where ping results are printed */
	public PingClient(Ip4Layer ip_layer, PrintStream out) {
		icmp_layer=ip_layer.getIcmpLayer();	
		this.out=out;		
	}

	/** Creates and runs a ping session.
	 * @param target_ip_addr IP address of the target node
	 * @param count the number of ICMP Echo requests to be sent */
	public void ping(Ip4Address target_ip_addr, int count) {
		ping(target_ip_addr,count,1000);
	}

	
	/** Creates and runs a ping session.
	 * @param target_ip_addr IP address of the target node
	 * @param count the number of ICMP Echo requests to be sent
	 * @param ping_time ping period time */
	public void ping(Ip4Address target_ip_addr, int count, long ping_time) {
		ping(0,"01234567890123456789".getBytes(),target_ip_addr,count,ping_time);
	}

	
	/** Creates and runs a ping session.
	 * @param echo_id identifier in the ICMP Echo request
	 * @param echo_data payload data in the ICMP Echo request
	 * @param target_ip_addr IP address of the target node
	 * @param count the number of ICMP Echo requests to be sent
	 * @param ping_time ping period time */
	public void ping(final int echo_id, byte[] echo_data, final Ip4Address target_ip_addr, int count, final long ping_time) {
		this.echo_id=echo_id;
		this.echo_data=echo_data;
		this.target_ip_addr=target_ip_addr;
		this.ping_time=ping_time;
		req_count=0;
		reply_count=0;
		println("PING "+target_ip_addr+" "+echo_data.length+" bytes of data:");
		start_time=Clock.getDefaultClock().currentTimeMillis();
		this_icmp_listener=new IcmpLayerListener() {
			@Override
			public void onReceivedIcmpMessage(IcmpLayer icmp_layer, Ip4Packet ip_pkt) {
				IcmpMessage icmp_msg=new IcmpMessage(ip_pkt);
				//System.out.println("DEBUG: PingClinet: ICMP message ("+icmp_msg.getType()+") received from "+icmp_msg.getSourceAddress()+" (target="+target_ip_addr+")");
				if (icmp_msg.getSourceAddress().equals(target_ip_addr) && icmp_msg.getType()==IcmpMessage.TYPE_Echo_Reply) {
					IcmpEchoReplyMessage icmp_echo_reply=new IcmpEchoReplyMessage(icmp_msg);
					//System.out.println("DEBUG: PingClinet: ICMP Echo reply: id="+icmp_echo_reply.getIdentifier()+" sqn="+icmp_echo_reply.getSequenceNumber());
					if (icmp_echo_reply.getIdentifier()==echo_id) {
						int sqn=icmp_echo_reply.getSequenceNumber();
						long now=Clock.getDefaultClock().currentTimeMillis();
						long rtt_time=now-departure_time[sqn%BACKLOGGED_DEPARTURES];
						total_time=now-start_time;
						println(""+icmp_echo_reply.getEchoData().length+" bytes from "+icmp_msg.getSourceAddress()+": icmp_sqn="+icmp_echo_reply.getSequenceNumber()+" ttl="+ip_pkt.getTTL()+" time="+rtt_time+" ms");
						reply_count++;
						last_ttl=ip_pkt.getTTL();
					}
				}					
			}
		};
		icmp_layer.addListener(this_icmp_listener);
		
		/*for (int sqn=0; sqn<count; sqn++) {
			IcmpEchoRequestMessage icmp_echo_request=new IcmpEchoRequestMessage(ip_layer.getSourceAddress(target_ip_addr),target_ip_addr,echo_id,sqn,echo_data);
			DefaultLogger.log(LoggerLevel.DEBUG,"Ping: ICMP Echo request at time "+Clock.getDefaultClock().currentTimeMillis()+": id="+icmp_echo_request.getIdentifier()+" sqn="+icmp_echo_request.getSequenceNumber());
			ip_layer.send(icmp_echo_request.toIp4Packet());
			Clock.getDefaultClock().sleep(start_time+(sqn+1)*ping_time-Clock.getDefaultClock().currentTimeMillis());
		}		
		// sleep extra time before ending
		Clock.getDefaultClock().sleep(2*ping_time);
		ip_layer.removeListener(this_ip_listener);
		*/
		ping(0,count);
		
		synchronized (is_running) {
			if (blocking) try { is_running.wait(); } catch (InterruptedException e) {}
		}
	}
	
	
	/** Sends a given number of PING requests.
	 * @param sqn starting sequence number
	 * @param count the number of requests to be sent */
	private void ping(final int sqn, final int count) {
		if (!is_running || count==0) return;
		// else
		long now=Clock.getDefaultClock().currentTimeMillis();
		departure_time[sqn%BACKLOGGED_DEPARTURES]=now;
		Ip4Address src_ip_addr=icmp_layer.getSourceAddress(target_ip_addr);
		if (src_ip_addr!=null) {
			IcmpEchoRequestMessage icmp_echo_request=new IcmpEchoRequestMessage(src_ip_addr,target_ip_addr,echo_id,sqn,echo_data);
			icmp_layer.send(icmp_echo_request);		
		} else {
			println("No route to destination "+target_ip_addr);
		}
		req_count++;
		if (count>1) {
			// sends other PING requests
			TimerListener timer_listener=new TimerListener() {
				@Override
				public void onTimeout(Timer t) {
					ping(sqn+1,count-1);
				}
			};
			//Clock.getDefaultClock().newTimer(ping_time,0,timer_listener).start();
			long next_time=(sqn+1)*ping_time+start_time-now;
			Clock.getDefaultClock().newTimer(next_time,0,timer_listener).start();
		}
		else {
			// wait a while before ending
			TimerListener timer_listener=new TimerListener() {
				@Override
				public void onTimeout(Timer t) {
					synchronized (is_running) {
						if (is_running) {
							halt();
						}
					}
				}
			};
			Clock.getDefaultClock().newTimer(clearing_time,0,timer_listener).start();
		}
	}
		
	
	/** Whether it is running.
	 * @return <i>true</i> if it is running */
	public boolean isRunning() {
		return is_running;
	}

	
	/** Stops running. */
	public void halt() {
		synchronized (is_running) {
			icmp_layer.removeListener(this_icmp_listener);
			printResult();
			is_running.notifyAll();
			is_running=false;
		}
	}
	
	
	/** Sets the clearing time.
	 * @param t the timeout in milliseconds */
	public void setClearingTime(long t) {
		this.clearing_time=t;
	}
	
	
	/** Gets the number of sent requests.
	 * @return the number of requests */
	public int getRequestCounter() {
		return req_count;
	}

	
	/** Gets the number of received replies.
	 * @return the number of replies */
	public int getReplyCounter() {
		return reply_count;
	}

	
	/** Gets the last received TTL value.
	 * @return the TTL value */
	public int getLastTTL() {
		return last_ttl;
	}

	
	/** Gets the time for receiving the last packet.
	 * @return the time in milliseconds */
	public long getTotalTime() {
		return total_time;
	}

	
	/** Prints result. */
	private void printResult() {
		println("\n--- "+target_ip_addr+" ping statistics ---");
		if (total_time<0) total_time=Clock.getDefaultClock().currentTimeMillis()-start_time;
		println(""+req_count+" packets transmitted, "+reply_count+" received, "+(((req_count-reply_count)*10000/req_count)/(double)100)+"% packet loss, total time "+total_time+"ms");
	}

	
	/** Prints out a string.
	 * @param str the string to be printed */
	private void println(String str) {
		if (out!=null) out.println(str);
	}

}
