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

package it.unipr.netsec.nemo.ip;


import it.unipr.netsec.ipstack.ip4.IpAddress;
import it.unipr.netsec.ipstack.ip4.IpPrefix;
import it.unipr.netsec.ipstack.net.Packet;
import it.unipr.netsec.nemo.link.DataLink;

import java.util.ArrayList;


/** An IP link.
 * It extends {@link it.unipr.netsec.nemo.link.DataLink} by providing methods for
 * dynamic IP configuration and router discovery.
 */
public class IpLink<A extends IpAddress, P extends Packet<A>> extends DataLink<A,P> {
	
	/** IP addresses */
	protected IpAddressPool<A> addresses;
	
	/** Router list */
	protected ArrayList<A> routers=new ArrayList<>();

	/** Address sequence number */
	byte[] sqn;

	
	/** Creates a new link.
	 * @param prefix network prefix */
	public IpLink(IpPrefix prefix) {
		super();
		this.addresses=new IpAddressPool<A>(prefix);
	}
	
	/** Creates a new link.
	 * @param bit_rate bit rate
	 * @param prefix network prefix */
	public IpLink(long bit_rate, IpPrefix prefix) {
		super(bit_rate);
		this.addresses=new IpAddressPool<A>(prefix);
	}
	
	/** Gets network prefix.
	 * @return network prefix */
	public IpPrefix getPrefix() {
		return addresses.getPrefix();
	}
	
	/** Adds a router.
	 * @param router router address */
	public void addRouter(A router) {
		synchronized (routers) {
			routers.add(router);
		}
	}
	
	/** Removes a router.
	 * @param router address of the router to be removed */
	public void removeRouter(A router) {
		synchronized (routers) { 
			for (int i=0; i<routers.size(); i++) {
				A addr=routers.get(i);
				if (addr.equals(router)) {
					routers.remove(i);
				}
			}
		}
	}

	/** Gets all routers.
	 * @return array of router addresses */
	public IpAddress[] getRouters() {
		synchronized (routers) { 
			return routers.toArray(new IpAddress[]{});
		}
	}
	
	/** Gets a new IP address and prefix length. */
	public synchronized A nextAddressPrefix() {
		return addresses.nextAddressPrefix();
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName()+'['+addresses.getPrefix().toString()+']';
	}
}
