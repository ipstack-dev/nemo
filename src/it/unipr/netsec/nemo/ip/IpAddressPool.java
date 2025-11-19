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


import org.zoolu.util.Bytes;

import io.ipstack.net.ip4.Ip4Address;
import io.ipstack.net.ip4.Ip4AddressPrefix;
import io.ipstack.net.ip4.Ip4Prefix;
import io.ipstack.net.ip4.IpAddress;
import io.ipstack.net.ip4.IpPrefix;
import io.ipstack.net.ip6.Ip6Address;
import io.ipstack.net.ip6.Ip6AddressPrefix;


/** IP address pool for dynamic address configuration.
 */
public class IpAddressPool<A extends IpAddress> {
	
	/** Network prefix */
	protected IpPrefix prefix;
	
	/** Address sequence number */
	byte[] sqn;

	
	/** Creates a new address pool.
	 * @param prefix network prefix */
	public IpAddressPool(IpPrefix prefix) {
		this.prefix=prefix;
		sqn=new byte[prefix.getPrefixAddress().length()];
	}
	
	/** Gets network prefix.
	 * @return network prefix */
	public IpPrefix getPrefix() {
		return prefix;
	}
	
	/** Gets a new IP address and prefix length. */
	public synchronized A nextAddressPrefix() {
		Bytes.inc(sqn);
		byte[] addr=Bytes.copy(prefix.getBytes());
		byte[] mask=prefix.getPrefixMask();
		for (int i=0; i<addr.length; i++) addr[i]|=(mask[i]^0xff)&sqn[i];
		IpAddress ip_addr;
		if (prefix instanceof Ip4Prefix) {
			ip_addr=new Ip4Address(addr);
			// skip broadcast address
			if (ip_addr.equals(((Ip4Prefix)prefix).getDirectedBroadcastAddress())) {
				return nextAddressPrefix();
			}			
		} else {
			ip_addr=new Ip6Address(addr);
		}
		// skip network address
		if (ip_addr.equals(prefix.getPrefixAddress())) {
			return nextAddressPrefix();
		}
		if (ip_addr instanceof Ip4Address) return (A)new Ip4AddressPrefix((Ip4Address)ip_addr,prefix.getPrefixLength());			
		else return (A)new Ip6AddressPrefix((Ip6Address)ip_addr,prefix.getPrefixLength());
	}

}
