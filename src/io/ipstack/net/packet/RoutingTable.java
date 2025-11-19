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

import java.util.ArrayList;


/** Routing Table.
  * {@link RoutingFunction Routing function} based on a list of routes.
  * Each route is formed by a possible destination address, an output interface, and the address of the next hop.
  */
public class RoutingTable<A extends Address, P extends Packet<A>> implements RoutingFunction<A,P> {

	/** Table */
	protected ArrayList<Route<A,P>> rt=new ArrayList<>();

	
	/** Creates a new routing table. */
	public RoutingTable() {
	}
	
	
	/** Gets the size of the routing table.
	 * @return the current size */
	public int size() {
		return rt.size();
	}

	
	/** Adds a new route.
	 * @param dest_naddr the destination network address
	 * @param next_hop the next-hop router */
	public void add(A dest_naddr, A next_hop) {
		add(new Route<A,P>(dest_naddr,next_hop,getRoute(next_hop).getOutputInterface()));
	}
	
	
	/** Adds a new route.
	 * @param route the new route */
	public void add(Route<A,P> route) {
		synchronized (rt) {
			rt.add(route);
		}
	}
	
	
	/** Sets a route.
	 * If a route with the same destination is already present the previous route is removed.
	 * @param dest_naddr the destination network address
	 * @param next_hop the next-hop router */
	public void set(A dest_naddr, A next_hop) {
		synchronized (rt) {
			removeAll(dest_naddr);
			add(new Route<A,P>(dest_naddr,next_hop,getRoute(next_hop).getOutputInterface()));			
		}
	}
	
	
	/** Inserts a new route.
	 * @param i the position within the routing table
	 * @param route the new route */
	public void insert(int i, Route<A,P> route) {
		synchronized (rt) {
			rt.add(i,route);			
		}
	}
	
	
	/** Removes a route.
	 * It removes the first occurrence of a route to a given destination.
	 * @param dest_naddr the destination of the route to be removed */
	public void remove(A dest_naddr) {
		remove(dest_naddr,null);
	}

	
	/** Removes a route.
	 * It removes the first occurrence of a route to a given destination.
	 * @param dest_naddr the destination of the route to be removed
	 * @param nexthop the nexthop of the route to be removed */
	public void remove(A dest_naddr, A nexthop) {
		synchronized (rt) {
			for (int i=0; i<rt.size(); i++) {
				Route<A,P> r=rt.get(i);
				if (r.getDestNetAddress().equals(dest_naddr) && (nexthop==null || nexthop.equals(r.getDestNetAddress()))) {
					rt.remove(i);
					return;
				}
			}			
		}
	}

	
	/** Removes all routes for a given destination.
	 * @param dest_naddr the destination of the route to be removed */
	public void removeAll(A dest_naddr) {
		removeAll(dest_naddr,null);
	}

	
	/** Removes all routes for given destination and nexthop.
	 * @param dest_naddr the destination of the route to be removed
	 * @param nexthop the nexthop of the route to be removed */
	public void removeAll(A dest_naddr, A nexthop) {
		synchronized (rt) {
			for (int i=rt.size()-1; i>=0; i--) {
				Route<A,P> r=rt.get(i);
				if (r.getDestNetAddress().equals(dest_naddr) && (nexthop==null || nexthop.equals(r.getDestNetAddress()))) rt.remove(i);
			}
		}
	}

	
	/** Removes all routes. */
	public void removeAll() {
		rt.clear();
	}
	
	
	/** Removes a route.
	 * @param i the index of the route within the routing table */
	public void remove(int i) {
		rt.remove(i);
	}

	
	/** Gets all routes in the routing table.
	 * @return list of routes */
	@SuppressWarnings("unchecked")
	public ArrayList<Route<A,P>> getAll() {
		return (ArrayList<Route<A,P>>)rt.clone();
	}

	
	/** Gets the i-th route.
	 * @param i the index value
	 * @return the route */
	public Route<A,P> get(int i) {
		return rt.get(i);
	}

	
	@Override
	public Route<A,P> getRoute(A dest_addr) {
		for (Route<A,P> route : rt) {
			A route_dest_addr=route.getDestNetAddress();
			if (route_dest_addr instanceof NetAddress) {
				if (((NetAddress)route_dest_addr).contains(dest_addr)) return route;
			}
			else {
				if (route_dest_addr.equals(dest_addr)) return route;
			}
		}
		// else
		return null;
	}

	
	@Override
	/** Gets a string representation of this routing table using tab as column separator.
	 * @return the routing table */
	public String toString() {
		StringBuffer sb=new StringBuffer();
		sb.append("destination\tnext-hop\tinterface\n");
		for (int i=0; i<rt.size(); i++) {
			Route<A,P> route=rt.get(i);
			sb.append(route.getDestNetAddress());
			A next=route.getNextHop();
			sb.append('\t').append(next!=null?next.toString():"none");
			sb.append('\t').append(route.getOutputInterface().getName()).append('\n');
		}
		return sb.toString();
	}

	/** Gets a string representation of this routing table using spaces as column separator.
	 * @return the routing table */
	public String toStringWithSpaces() {
		final int hspace=3; // minimum space between columns
		ArrayList<String> dest=new ArrayList<String>();
		ArrayList<String> next=new ArrayList<String>();
		ArrayList<String> intf=new ArrayList<String>();
		int dest_len=addString(dest,"destination",0);
		int next_len=addString(next,"next-hop",0);
		addString(intf,"interface",0);		
		for (int i=0; i<rt.size(); i++) {
			Route<A,P> route=rt.get(i);
			dest_len=addString(dest,route.getDestNetAddress(),dest_len);
			next_len=addString(next,route.getNextHop(),next_len);
			addString(intf,route.getOutputInterface().getName(),0);
		}
		dest_len+=hspace;
		next_len+=hspace;
		StringBuffer sb=new StringBuffer();
		for (int i=0; i<dest.size(); i++) {
			append(sb,dest.get(i),dest_len);
			append(sb,next.get(i),next_len);
			append(sb,intf.get(i),0);
			if (i<dest.size()-1) sb.append('\n');
		}
		return sb.toString();
	}
	
	private static void append(StringBuffer sb, String str, int len) {
		sb.append(str);
		for (int i=str.length(); i<len; i++) sb.append(' ');
	}
	
	private static int addString(ArrayList<String> list, Object o, int len) {
		String str=o!=null? o.toString() : "none";
		list.add(str);
		int str_len=str.length();
		if (str_len>len) return str_len;
		else return len;
	}

}
