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

package io.ipstack.net.link;


import io.ipstack.net.packet.Address;
import io.ipstack.net.packet.Packet;


/** Link interface in promiscuous mode.
 */
public class PromiscuousLinkInterface<A extends Address, P extends Packet<A>> extends LinkInterface<A,P> {

	/** Creates a new interface.
	 * @param link the link to be attached to */
	public PromiscuousLinkInterface(Link<A,P> link) {
		super(link);
	}

	/** Creates a new interface.
	 * @param link the link to be attached to
	 * @param addr the interface address */
	public PromiscuousLinkInterface(Link<A,P> link, A addr) {
		super(link,addr);
	}

	@Override
	public boolean hasAddress(A addr) {
		return true;
	}

}
