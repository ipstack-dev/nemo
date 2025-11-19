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


import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.zoolu.util.SystemUtils;
import org.zoolu.util.log.LoggerLevel;

import io.ipstack.net.ip4.Ip4Address;
import io.ipstack.net.ip4.Ip4AddressPrefix;
import io.ipstack.net.ip4.Ip4Layer;
import io.ipstack.net.ip4.Ip4Packet;
import io.ipstack.net.ip4.Ip4Prefix;
import io.ipstack.net.packet.Address;
import io.ipstack.net.packet.NetInterface;
import io.ipstack.net.packet.Packet;
import io.ipstack.net.packet.Route;
import io.ipstack.net.packet.RoutingTable;
import io.ipstack.net.stack.IpStack;
import it.unipr.netsec.nemo.routing.LinkStateInfo;
import it.unipr.netsec.nemo.routing.RouteInfo;
import it.unipr.netsec.nemo.routing.DynamicRouting;
import it.unipr.netsec.nemo.routing.DynamicRoutingInterface;
import it.unipr.netsec.nemo.telnet.server.TelnetServer;


/** IPv4 Router.
 */
public class Ip4Router extends Ip4Node {

	/** Virtual link used for assigning Unique Local Addresses (ULAs) */
	//private static IpAddressPool LOOPBACK_ADDRESSES=new IpAddressPool(new Ip4Prefix("10.254.0.0/16"));
	private static IpAddressPool<Ip4Address> LOOPBACK_ADDRESSES=new IpAddressPool<>(new Ip4Prefix("172.31.0.0/16"));

	/** Address used as router identifier */
	Ip4Address loopback_addr;
	
	/** Whether is paused */
	boolean paused=false;
	
	/** Dynamic routing mechanism */
	DynamicRouting dynamic_routing=null;


	/** Creates a new router without interfaces.
	 * Loopback address is automatically assigned. */
	public Ip4Router() {
		this((Ip4Address)null);
	}

	/** Creates a new router.
	 * Loopback address is automatically assigned.
	 * @param net_interfaces network interfaces */
	/*public Ip4Router(List<NetInterface<Ip4Address,Ip4Packet>> net_interfaces) {
		this(null,net_interfaces);
	}*/
	
	/** Creates a new router.
	 * Loopback address is automatically assigned.
	 * @param net_interfaces network interfaces */
	@SafeVarargs
	public Ip4Router(NetInterface<Ip4Address,Ip4Packet>... net_interfaces) {
		this(null,Arrays.asList(net_interfaces));
	}
	
	/** Creates a new router.
	 * Addresses are automatically assigned.
	 * @param links the IP links the router is attached to */
	@SafeVarargs
	public Ip4Router(IpLink<Ip4Address,Ip4Packet>... links) {
		this(null,links);
	}
	
	/** Creates a new router without interfaces.
	 * @param loopback_addr address used as router identifier; if <code>null</code>, it is automatically assigned from the unique set {@link #LOOPBACK_ADDRESSES} */
	public Ip4Router(Ip4Address loopback_addr) {
		this(loopback_addr,new ArrayList<NetInterface<Ip4Address,Ip4Packet>>());
	}

	/** Creates a new router.
	 * @param loopback_addr address used as router identifier; if <code>null</code>, it is automatically assigned from the unique set {@link #LOOPBACK_ADDRESSES}
	 * @param net_interfaces network interfaces */
	public Ip4Router(Ip4Address loopback_addr, List<NetInterface<Ip4Address,Ip4Packet>> net_interfaces) {
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
	public Ip4Router(Ip4Address loopback_addr, NetInterface<Ip4Address,Ip4Packet>... net_interfaces) {
		this(loopback_addr,Arrays.asList(net_interfaces));
	}
	
	/** Creates a new router.
	 * @param loopback_addr address used as router identifier; if <code>null</code>, it is automatically assigned from the unique set {@link #LOOPBACK_ADDRESSES}
	 * @param links the IP links the router is attached to */
	@SafeVarargs
	public Ip4Router(Ip4Address loopback_addr, IpLink<Ip4Address,Ip4Packet>... links) {
		this(loopback_addr,IpLinkInterface.createIpLinkInterfaceArray(links));
		ArrayList<NetInterface<Ip4Address,Ip4Packet>> ni=getNetInterfaces();
		for (int i=0; i<ni.size(); i++) {
			if (links[i] instanceof IpLink) ((IpLink<Ip4Address,Ip4Packet>)links[i]).addRouter((Ip4Address)ni.get(i).getAddress());
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
		if (dynamic_routing!=null) {
			ArrayList<LinkStateInfo> lsa=new ArrayList<LinkStateInfo>();
			for (NetInterface<Ip4Address,Ip4Packet> ni: getNetInterfaces()) {
				for (Address addr: ni.getAddresses()) {
					if (addr instanceof Ip4AddressPrefix) {
						Ip4AddressPrefix ip_addr_prefix=(Ip4AddressPrefix)addr;
						lsa.add(new LinkStateInfo(ip_addr_prefix,ip_addr_prefix.getPrefix(),1));
					}
				}
			}
			dynamic_routing.connect(loopback_addr,lsa.toArray(new LinkStateInfo[]{}),new DynamicRoutingInterface() {
				@Override
				public void updateRouting(RouteInfo[] ra) {
					updateRoutingTable(ra);	
				}
				@Override
				public void sendPacket(Address if_addr, Packet pkt) {
					Ip4Router router=Ip4Router.this;
					if (if_addr==null) router.sendPacket((Ip4Packet)pkt);
					else {
						for (NetInterface<Ip4Address,Ip4Packet> ni: router.getNetInterfaces()) {
							if (ni.hasAddress((Ip4Address)if_addr)) ni.send((Ip4Packet)pkt,null);
						}
					}
				}
			});
		}
	}
	
	@Override
	public boolean hasAddress(Ip4Address addr) {
		if (addr.equals(loopback_addr)) return true;
		return super.hasAddress(addr);
	}
	
	@Override
	protected void processReceivedPacket(NetInterface<Ip4Address,Ip4Packet> ni, Ip4Packet pkt) {
		if (!paused) {
			if (dynamic_routing!=null) pkt=(Ip4Packet)dynamic_routing.processReceivedPacket(loopback_addr,ni.getAddress(),pkt);
			if (pkt!=null) super.processReceivedPacket(ni,pkt);
		}
	}

	@Override
	public void sendPacket(Ip4Packet pkt) {
		if (!paused) {
			super.sendPacket(pkt);			
		}
	}

	/** Updates the routing table according to the information obtained from the routing mechanism.
	 * @param ra array of the new routes */
	private synchronized void updateRoutingTable(RouteInfo[] ra) {
		RoutingTable<Ip4Address,Ip4Packet> rt=getRoutingTable();
		rt.removeAll();
		for (RouteInfo ri: ra) {
			Ip4Prefix dest=new Ip4Prefix(ri.getDestination());
			Ip4Address next_hop=ri.getNextHop()!=null? new Ip4Address(ri.getNextHop()) : null;
			Ip4Address interface_addr=new Ip4Address(ri.getInterfaceAddress());
			NetInterface<Ip4Address,Ip4Packet> net_interface=null;
			for (NetInterface<Ip4Address,Ip4Packet> ni: getNetInterfaces()) {
				if (ni.hasAddress(interface_addr)) {
					net_interface=ni;
					break;
				}
			}
			rt.add(new Route<Ip4Address,Ip4Packet>(dest,next_hop,net_interface));
		}
		if (DEBUG) debug("updateRoutingTable():\n"+rt);
	}

	public Ip4Address getAddress() {
		return loopback_addr;
	}
	
	
	

}
