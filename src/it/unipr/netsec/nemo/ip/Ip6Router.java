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

import org.zoolu.util.LoggerLevel;
import org.zoolu.util.SystemUtils;

import it.unipr.netsec.ipstack.ip6.Ip6Address;
import it.unipr.netsec.ipstack.ip6.Ip6AddressPrefix;
import it.unipr.netsec.ipstack.ip6.Ip6Packet;
import it.unipr.netsec.ipstack.ip6.Ip6Prefix;
import it.unipr.netsec.ipstack.net.Address;
import it.unipr.netsec.ipstack.net.NetInterface;
import it.unipr.netsec.ipstack.net.Packet;
import it.unipr.netsec.ipstack.routing.Route;
import it.unipr.netsec.ipstack.routing.RoutingTable;
import it.unipr.netsec.nemo.routing.LinkStateInfo;
import it.unipr.netsec.nemo.routing.RouteInfo;
import it.unipr.netsec.nemo.routing.DynamicRouting;
import it.unipr.netsec.nemo.routing.DynamicRoutingInterface;


/** IPv6 router.
 * */
public class Ip6Router extends Ip6Node {

	/** Virtual link used for assigning Unique Local Addresses (ULAs) */
	private static IpLink<Ip6Address,Ip6Packet> LOOPBACK_ADDRESSES=new IpLink<>(new Ip6Prefix("fd00::/16"));

	/** Address used as router identifier */
	Ip6Address loopback_addr;
	
	/** Whether is paused */
	boolean paused=false;
	
	/** Dynamic routing mechanism */
	DynamicRouting dynamic_routing=null;

	
	/** Creates a new router without interfaces.
	 * Loopback address is automatically assigned. */
	public Ip6Router() {
		this((Ip6Address)null);
	}

	/** Creates a new router.
	 * Loopback address is automatically assigned.
	 * @param net_interfaces network interfaces */
	/*public Ip6Router(List<NetInterface<Ip6Address,Ip6Packet>> net_interfaces) {
		this(null,net_interfaces);
	}*/
	
	/** Creates a new router.
	 * Loopback address is automatically assigned.
	 * @param net_interfaces network interfaces */
	@SafeVarargs
	public Ip6Router(NetInterface<Ip6Address,Ip6Packet>... net_interfaces) {
		this(null,Arrays.asList(net_interfaces));
	}
	
	/** Creates a new router.
	 * Addresses are automatically assigned.
	 * @param links the IP links the router is attached to */
	@SafeVarargs
	public Ip6Router(IpLink<Ip6Address,Ip6Packet>... links) {
		this(null,links);
	}
	
	/** Creates a new router without interfaces.
	 * @param loopback_addr address used as router identifier; if <code>null</code>, it is automatically assigned from the unique set {@link #LOOPBACK_ADDRESSES} */
	public Ip6Router(Ip6Address loopback_addr) {
		this(loopback_addr,new ArrayList<NetInterface<Ip6Address,Ip6Packet>>());
	}

	/** Creates a new router.
	 * @param loopback_addr address used as router identifier; if <code>null</code>, the first address of the first interface is used
	 * @param net_interfaces network interfaces */
	public Ip6Router(Ip6Address loopback_addr, List<NetInterface<Ip6Address,Ip6Packet>> net_interfaces) {
		super(net_interfaces);
		setForwarding(true);
		//if (loopback_addr==null) loopback_addr=net_interfaces[0].getAddress();
		if (loopback_addr==null) loopback_addr=LOOPBACK_ADDRESSES.nextAddressPrefix();
		this.loopback_addr=loopback_addr;
	}

	/** Creates a new router.
	 * @param loopback_addr address used as router identifier; if <code>null</code>, it is automatically assigned from the unique set {@link #LOOPBACK_ADDRESSES}
	 * @param net_interfaces network interfaces */
	@SafeVarargs
	public Ip6Router(Ip6Address loopback_addr, NetInterface<Ip6Address,Ip6Packet>... net_interfaces) {
		this(loopback_addr,Arrays.asList(net_interfaces));
	}
	
	/** Creates a new router.
	 * @param loopback_addr address used as router identifier; if <code>null</code>, it is automatically assigned from the unique set {@link #LOOPBACK_ADDRESSES}
	 * @param links the IP links the router is attached to */
	@SafeVarargs
	public Ip6Router(Ip6Address loopback_addr,IpLink<Ip6Address,Ip6Packet>... links) {
		this(loopback_addr,IpLinkInterface.createIpLinkInterfaceArray(links));
		ArrayList<NetInterface<Ip6Address,Ip6Packet>> ni=getNetInterfaces();
		for (int i=0; i<ni.size(); i++) {
			if (links[i] instanceof IpLink) ((IpLink<Ip6Address,Ip6Packet>)links[i]).addRouter((Ip6Address)ni.get(i).getAddress());
		}
	}
	
	/** Gets the loopback address.
	 * @return the address */
	public Address getLoopbackAddress() {
		return loopback_addr;
	}

	/** Pauses the router.
	 * @param paused <i>true</i> to pause, <i>false</i> to resume */
	public void pause(boolean paused) {
		this.paused=paused;
	}

	/** Sets dynamic routing.
	 * @param dynamic_routing the dynamic routing mechanism */
	public void setDynamicRouting(DynamicRouting dynamic_routing) {
		if (this.dynamic_routing!=null) {
			this.dynamic_routing.disconnect(loopback_addr);
		}
		this.dynamic_routing=dynamic_routing;
		if (dynamic_routing!=null ) {
			ArrayList<LinkStateInfo> lsa=new ArrayList<LinkStateInfo>();
			for (NetInterface<Ip6Address,Ip6Packet> ni: getNetInterfaces()) {
				for (Address addr: ni.getAddresses()) {
					if (addr instanceof Ip6AddressPrefix) {
						Ip6AddressPrefix ip_addr_prefix=(Ip6AddressPrefix)addr;
						lsa.add(new LinkStateInfo(ip_addr_prefix,ip_addr_prefix.getPrefix(),1));
					}
				}
			}
			dynamic_routing.connect(loopback_addr,lsa.toArray(new LinkStateInfo[]{}), new DynamicRoutingInterface() {
				@Override
				public void updateRouting(RouteInfo[] ra) {
					updateRoutingTable(ra);	
				}
				@Override
				public void sendPacket(Address if_addr, Packet pkt) {
					Ip6Router router=Ip6Router.this;
					if (if_addr==null) router.sendPacket((Ip6Packet)pkt);
					else {
						for (NetInterface<Ip6Address,Ip6Packet> ni: router.getNetInterfaces()) {
							if (ni.hasAddress((Ip6Address)if_addr)) ni.send((Ip6Packet)pkt,null);
						}
					}
				}
			});
		}
	}
	
	@Override
	public boolean hasAddress(Ip6Address addr) {
		if (addr.equals(loopback_addr)) return true;
		return super.hasAddress(addr);
	}

	@Override
	public Ip6Address getAddress() {
		return loopback_addr;
	}

	@Override
	protected void processReceivedPacket(NetInterface<Ip6Address,Ip6Packet> ni, Ip6Packet pkt) {
		if (!paused) {
			if (dynamic_routing!=null) pkt=(Ip6Packet)dynamic_routing.processReceivedPacket(loopback_addr,ni.getAddress(),pkt);
			if (pkt!=null) super.processReceivedPacket(ni,pkt);
		}
	}

	@Override
	public void sendPacket(Ip6Packet pkt) {
		if (!paused) {
			super.sendPacket(pkt);			
		}
	}

	/** Updates the routing table according to the information obtained from the routing mechanism.
	 * @param ra array of the new routes */
	private synchronized void updateRoutingTable(RouteInfo[] ra) {
		RoutingTable<Ip6Address,Ip6Packet> rt=getRoutingTable();
		rt.removeAll();
		for (RouteInfo ri: ra) {
			Ip6Prefix dest=new Ip6Prefix(ri.getDestination());
			Ip6Address next_hop=ri.getNextHop()!=null? new Ip6Address(ri.getNextHop()) : null;
			Ip6Address interface_addr=new Ip6Address(ri.getInterfaceAddress());
			NetInterface<Ip6Address,Ip6Packet> net_interface=null;
			for (NetInterface<Ip6Address,Ip6Packet> ni: getNetInterfaces()) {
				if (ni.hasAddress(interface_addr)) {
					net_interface=ni;
					break;
				}
			}
			rt.add(new Route<Ip6Address,Ip6Packet>(dest,next_hop,net_interface));
		}
		if (DEBUG) debug("updateRoutingTable():\n"+rt);
	}

}
