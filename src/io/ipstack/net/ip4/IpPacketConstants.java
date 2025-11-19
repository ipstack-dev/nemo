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

package io.ipstack.net.ip4;



/** Generic Internet Protocol packet.
  */
public interface IpPacketConstants {
	
	// Some standard IP protocol numbers:
	
	/** Internet Protocol (IP) */
	public static final int IPPROTO_IP=0;
	/** IP in IP (IPIP) encapsulation */
	public static final int IPPROTO_IPIP=4;
	/** Internet Protocol version 6 (IPv6) */
	public static final int IPPROTO_IPV6=41;
	/** Internet Control Message Protocol (ICMP) */
	public static final int IPPROTO_ICMP=1;
	/** Internet Group Management Protocol */
	public static final int IPPROTO_IGMP=2;
	/** Transmission Control Protocol (TCP) */
	public static final int IPPROTO_TCP=6;
	/** User Datagram Protocol (UDP) */
	public static final int IPPROTO_UDP=17;
	/** Stream Control Transport Protocol (SCTP) */
	public static final int IPPROTO_SCTP=132;
	/** Encapsulation Security Payload (ESP) protocol */
	public static final int IPPROTO_ESP=50;
	/** Authentication Header (AH) protocol */
	public static final int IPPROTO_AH=51;
	/** Internet Control Message Protocol for IPv6 (ICMPv6) */
	public static final int IPPROTO_ICMP6=58;
	/** Open Shortest Path First (OSPF) protocol */
	public static final int IPPROTO_OSPF=89;

	/** Experimentation protocol 253 */ 
	public static final int IPPROTO_TEST_253=253;
	/** Experimentation protocol 254 */ 
	public static final int IPPROTO_TEST_254=254;


	/** Gets IP protocol version.
	 * @return version */
	public int getVersion();

}
