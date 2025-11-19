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


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.ipstack.net.icmp6.SolicitedNodeMulticastAddress;
import io.ipstack.net.ip4.Ip4Address;
import io.ipstack.net.ip4.Ip4AddressPrefix;
import io.ipstack.net.ip4.IpAddress;
import io.ipstack.net.ip6.Ip6Address;
import io.ipstack.net.packet.NetInterface;
import io.ipstack.net.packet.Packet;
import it.unipr.netsec.nemo.link.DataLink;
import it.unipr.netsec.nemo.link.DataLinkInterface;


/** An IPv4 link interface.
 */
public class IpLinkInterface<A extends IpAddress, P extends Packet<A>> extends DataLinkInterface<A,P> {

	/** Creates a new interface.
	 * The interface address and prefix length are dynamically obtained from the link
	 * through the method {@link IpLink#nextAddressPrefix()}.
	 * @param link the link to be attached to */
	public IpLinkInterface(IpLink<A,P> link) {
		this(link,link.nextAddressPrefix());
	}
		
	/** Creates a new interface.
	 * @param link the link to be attached to
	 * @param ip_addr the IP address */
	public IpLinkInterface(DataLink<A,P> link, A ip_addr) {
		super(link,ip_addr);
		if (ip_addr instanceof Ip4Address) {
			addAddress((A)Ip4Address.ADDR_BROADCAST);
			addAddress((A)Ip4Address.ADDR_ALL_HOSTS_MULTICAST);
			if (ip_addr instanceof Ip4AddressPrefix) addAddress((A)((Ip4AddressPrefix)ip_addr).getPrefix().getDirectedBroadcastAddress());
		}
		else
		if (ip_addr instanceof Ip6Address) {
			addAddress((A)Ip6Address.ADDR_ALL_HOSTS_INTERFACE_MULTICAST);
			addAddress((A)Ip6Address.ADDR_ALL_HOSTS_LINK_MULTICAST);
			Ip6Address sn_m_addr=new SolicitedNodeMulticastAddress((Ip6Address)ip_addr);
			addAddress((A)sn_m_addr);		
		}
	}
		
	/** Creates a list of link interfaces.
	 * @param links an array of links
	 * @return the new link interfaces */
	public static <A extends IpAddress, P extends Packet<A>> ArrayList<NetInterface<A,P>> createIpLinkInterfaceArray(IpLink<A,P>[] links) {
		return createIpLinkInterfaceArray(Arrays.asList(links));
	}

	/** Creates a list of link interfaces.
	 * @param links a list of links
	 * @return the new link interfaces */
	public static <A extends IpAddress, P extends Packet<A>> ArrayList<NetInterface<A,P>> createIpLinkInterfaceArray(List<IpLink<A,P>> links) {
		ArrayList<NetInterface<A,P>> interfaces=new ArrayList<>();
		for (IpLink<A,P> link : links) interfaces.add(new IpLinkInterface<A,P>(link));
		return interfaces;
	}
	
}
