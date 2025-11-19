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

package it.unipr.netsec.nemo.routing;


import io.ipstack.net.packet.Address;
import io.ipstack.net.packet.Packet;


/** Generic routing mechanism, that is able to dynamically compute the routes toward destinations.
 * It can be either a centralized system (e.g. SDN controller) or a distributed routing protocol (like OSPF or RIP).
 * <p>
 * The routing mechanism is implemented as an independent agent that the router connect to through the {@link #connect(Address, LinkStateInfo[], DynamicRoutingInterface)} method.
 * Any node can contribute to the routing mechanism by providing local routing information ({@link LinkStateInfo Link-State information})
 * when connecting to the routing mechanism through the method {@link #connect(Address, LinkStateInfo[], DynamicRoutingInterface)}.
 * <p>
 * The routing mechanism changes the routing table of a node by calling the {@link DynamicRoutingInterface#updateRouting(RouteInfo[])} method.
 * that should be implemented by the node.
 * <br>
 * The routing mechanism may optionally process incoming packets by implementing the {@link #processReceivedPacket(Address, Address, Packet)} method,
 * and/or send packets by using the {@link DynamicRoutingInterface#sendPacket(Address, Packet)} method.
 * <p>
 * It is up to the routing mechanism how the provided routing information is used to compute the routes.
 * Distance-Vector and Link-State routing protocols, as well as Software Defined Networking mechanisms, are possible implementations.
 */
public interface DynamicRouting {

	/** Connects a node to the routing mechanism.
	 * @param node_addr the node address
	 * @param lsa array of link state info of the node
	 * @param routing_interface the routing interface of the node */
	public void connect(Address node_addr, LinkStateInfo[] lsa, DynamicRoutingInterface routing_interface);
	
	/** Disconnects a node from the routing mechanism.
	 * @param node_addr the node address */
	public void disconnect(Address node_addr);
	
	/** When a node receives a new packet.
	 * @param node_addr the node address
	 * @param if_addr input interface address
	 * @param pkt the received packet
	 * @return the original packet (if not processed by the routing mechanism) or <i>null</i> */
	public Packet processReceivedPacket(Address node_addr, Address if_addr, Packet pkt);
	
	
}
