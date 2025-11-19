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

import java.util.List;

/** A loopback interface.
 * <p>
 * Sent packets are passed back to the interface listeners.
 * @param <A> the address type
 * @param <P> the packet type
 */
public class LoopbackInterface<A extends Address, P extends Packet<A>> extends NetInterface<A,P> {

	/** Creates a new interface.
	 * @param addr interface address */
	public LoopbackInterface(A addr) {
		super(addr);
	}

	
	/** Creates a new interface.
	 * @param addrs interface addresses */
	public LoopbackInterface(List<A> addrs) {
		super(addrs);
	}

	
	/** Sends a packet.
	 * @param dest_addr the address of the destination interface */
	public void send(P pkt, A dest_addr) {
		// promiscuous mode
		for (NetInterfaceListener<A,P> li : promiscuous_listeners) {
			try { li.onIncomingPacket(this,pkt); } catch (Exception e) {
				e.printStackTrace();
			}
		}
		// non-promiscuous mode
		for (NetInterfaceListener<A,P> li : listeners) {
			try { li.onIncomingPacket(this,pkt); } catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
