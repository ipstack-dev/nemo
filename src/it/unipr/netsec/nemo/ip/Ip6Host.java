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


import org.zoolu.util.ArrayUtils;

import it.unipr.netsec.ipstack.ip6.Ip6Address;
import it.unipr.netsec.ipstack.ip6.Ip6Packet;
import it.unipr.netsec.ipstack.ip6.Ip6Prefix;
import it.unipr.netsec.ipstack.net.NetInterface;
import it.unipr.netsec.nemo.link.DataLink;


/** IPv6 Host.
 * It is an IP node with a PING client.
 */
public class Ip6Host extends Ip6Node {
	
	/** Creates a new host.
	 * @param ni network interface
	 * @param gw default router */
	public Ip6Host(NetInterface<Ip6Address,Ip6Packet> ni, Ip6Address gw) {
		super(ArrayUtils.arraylist(ni));
		if (DEBUG) debug("RT: \n"+getRoutingTable());
		if (gw!=null) getRoutingTable().add(Ip6Prefix.ANY,gw);
	}

	/** Creates a new host.
	 * @param link attached IP link
	 * @param addr the IP address
	 * @param gw default router */
	public Ip6Host(DataLink<Ip6Address,Ip6Packet> link, Ip6Address addr, Ip6Address gw) {
		this(new IpLinkInterface<Ip6Address,Ip6Packet>(link,addr),gw);
	}
		
	/** Creates a new host.
	 * The IP address and default router are automatically configured
	 * @param link attached IP link */
	public Ip6Host(IpLink<Ip6Address,Ip6Packet> link) {
		this(new IpLinkInterface<Ip6Address,Ip6Packet>(link),(link.getRouters().length>0?(Ip6Address)link.getRouters()[0]:null));
	}

}
