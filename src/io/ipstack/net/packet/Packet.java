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

package io.ipstack.net.packet;



/** Generic network packet.
 * It has a source address and a destination address, and a representation (coding) as an array of bytes
 * according to a proper packet format.
 * @param <A> the address type
 */
public interface Packet<A extends Address> extends Cloneable {
	
	/** Sets the source address.
	 * @param src_addr the IP source address */
	//public void setSourceAddress(A src_addr);
	 
	
	/** Gets the source address.
	 * @return the IP source address */
	public A getSourceAddress();

	
	/** Sets the destination address.
	 * @param dst_addr the IP destination address */
	//public void setDestAddress(A dst_addr);
	 
	
	/** Gets the destination address.
	 * @return the IP destination address */
	public A getDestAddress();

	
	/** Gets the packet length.
	 * @return the total packet length */
	public int getPacketLength();

	
	/** Gets the entire packet in a byte array.
	 * @param buf the buffer where the packet has to be written
	 * @param off the offset within the buffer
	 * @return the total packet length */
	public int getBytes(byte[] buf, int off);

	
	/** Gets the entire packet in a byte array.
	 * @return a new byte array containing the packet */
	public byte[] getBytes();

	
	/** Gets a copy.
	 * @return a copy of this packet */
	public Object clone();

	
	/** Gets a string representation of this packet.
	 * @return a string with the main protocol information of this packet */
	public String toString();

}
