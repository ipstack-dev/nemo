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

package io.ipstack.net.stack;


import java.io.IOException;
import java.util.ArrayList;

import io.ipstack.net.packet.Address;
import io.ipstack.net.packet.NetInterface;
import io.ipstack.net.packet.Packet;
import io.ipstack.net.packet.Route;


public interface NetConfiguration<A extends Address, P extends Packet<A>> {

	/** Adds a configuration command.
	 * @param command the configuration command
	 * @return this object
	 * @throws IOException */
	public NetConfiguration<A,P> add(String command) throws IOException;

	/** Adds a network interface.
	 * @param name the network interface name
	 * @param ni the network interface
	 * @return this object */
	public NetConfiguration<A,P> add(String name, NetInterface<A,P> ni);

	/** Adds a route.
	 * @param r the route
	 * @return this object */
	public NetConfiguration<A,P> add(Route<A,P> r);

	/** Gets network interfaces. */
	public ArrayList<NetInterface<A,P>> getNetInterfaces();
	
	/** Gets routes. */
	public ArrayList<Route<A,P>> getRoutes();

}
