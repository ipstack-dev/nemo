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


import io.ipstack.net.ip6.Ip6Packet;


/** General transport packet over IP.
 * It uses source and destination port identifiers, and may have a checksum.
 * <p>
 * Examples: UDP, TCP, and SCTP.
 */
public interface TransportPacket {	

	/** Gets the source port number.
	 * @return source port */
	public int getSourcePort();

	/** Gets the destination port number.
	 * @return destination port */
	public int getDestPort();

	/** Gets source socket address.
	 * @return the socket address */
	public SocketAddress getSourceSocketAddress();

	/** Sets source socket address.
	 * @param soaddr the socket address */
	public void setSourceSocketAddress(SocketAddress soaddr);

	/** Gets destination socket address.
	 * @return the socket address */
	public SocketAddress getDestSocketAddress();

	/** Sets destination socket address.
	 * @param soaddr the socket address */
	public void setDestSocketAddress(SocketAddress soaddr);

	/** Whether the checksum is correct, unspecified, wrong.
	 * @return 1= correct checksum, 0= unspecified checksum, -1= wrong checksum */
	public int getChecksumCheck();

	/** Gets an IPv4 packet containing this UDP datagram.
	 * @return the IPv4 packet */
	public Ip4Packet toIp4Packet();

	/** Gets an IPv6 packet containing this UDP datagram.
	 * @return the IPv6 packet */
	public Ip6Packet toIp6Packet();
	
}
