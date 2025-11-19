package io.ipstack.net.stack;


import java.io.PrintStream;

import io.ipstack.net.ethernet.EthAddress;
import io.ipstack.net.ethernet.EthTunnelInterface;
import io.ipstack.net.ip4.Ip4Address;
import io.ipstack.net.ip4.Ip4AddressPrefix;
import io.ipstack.net.ip4.Ip4EthInterface;
import io.ipstack.net.ip4.Ip4Layer;
import io.ipstack.net.ip4.Ip4Packet;
import io.ipstack.net.ip4.Ip4Prefix;
import io.ipstack.net.ip4.SocketAddress;
import io.ipstack.net.link.Link;
import io.ipstack.net.link.LinkInterface;
import io.ipstack.net.packet.NetInterface;
import io.ipstack.net.packet.Route;
import io.ipstack.net.packet.RoutingTable;
import io.ipstack.net.stack.IpInterfaceUtils;
import io.ipstack.net.stack.IpStack;
import io.ipstack.net.stack.Links;


/** IP configuration utility.
 * It shows/manipulates interfaces, addresses, and the routing table.
 * <p>
 * It implements the same interface used by the linux <code>ip</code> command-line tool.
 * </p>
 */
public class IpCommand {
	
	Ip4Layer ip4_layer;
	PrintStream out;

	
	public IpCommand(IpStack ip_stack, PrintStream out) {
		this.ip4_layer=ip_stack.getIp4Layer();
		this.out=out;
	}
	
	
	public void command(String[] args, int off) {		
		if (args.length<=off) {
			errorMessage(null,"missing arguments");
			help();
			return;
		}
		if (args[off].equals("a") || args[off].equals("addr") || args[off].equals("address")) {
			address(args,off+1);
			return;
		}
		if (args[off].equals("r") || args[off].equals("route")) {
			route(args,off+1);
			return;
		}
		if (args[off].equals("l") || args[off].equals("link")) {
			link(args,off+1);
			return;
		}
		if (args[off].equals("f") || args[off].equals("forward") || args[off].equals("forwarding")) {
			forwarding(args,off+1);
			return;
		}
		// else
		if (!(args[off].equals("h") || args[off].equals("help"))) errorMessage(null,"wrong arguments: "+toString(args,off));
		help();
	}
	

	/** Concatenates the elements of a string array starting from a given index.
	 * @param ss the array
	 * @param off the index of the first element to be added
	 * @return the resulting string */
	private static String toString(String[] ss, int off) {
		StringBuffer sb=new StringBuffer();
		for (int i=off; i<ss.length; i++) {
			if (i>off) sb.append(' ');
			sb.append(ss[i]);
		}
		return sb.toString();
	}
	
	
	/** Prints help. */
	private void help() {
		if (out!=null) out.println("Usage:\n  ip address <options>\n  ip route <options>\n  ip link <options>\n  forwarding <options>");
	}

	
	/** 'ip route' command. */
	private void route(String[] args, int off) {
		if (args.length>off) {
			if (args[off].equals("s") || args[off].equals("show")) {
				routeShow();
				return;
			}
			if (args.length>=off+4 && (args[off].equals("a") || args[off].equals("add")) && args[off+2].equals("via")) {
				if (args.length==off+4 || (args.length==off+6 && args[off+4].equals("dev"))) {
					routeAdd(args,off+1);
					return;					
				}
			}
			if (args.length>=off+2 && (args[off].equals("d") || args[off].equals("del") || args[off].equals("delete"))) {
				if (args.length==off+2 || (args.length>=off+4 && args[off+2].equals("via"))) {
					if (args.length==off+2 || args.length==off+4 || (args.length==off+6 && args[off+4].equals("dev"))) {
						routeDelete(args,off+1);
						return;					
					}					
				}
			}
			errorMessage("route","wrong arguments: "+toString(args,off));
		}
		if (out!=null) out.println("Usage:\n  ip route add <destaddr/len> via <nexthop> dev <device>\n  ip route delete <destaddr/len> via <nexthop> dev <device>\n  ip route show");
	}

	
	/** 'ip route show' command. */
	public void routeShow() {
		if (out!=null) out.println(ip4_layer.getRoutingTable().toStringWithSpaces());
	}

	
	/** 'ip route add' command. */
	public void routeAdd(String[] args, int off) {
		Ip4AddressPrefix dest=null;
		try {
			if (!args[off].equals("default")) dest=new Ip4AddressPrefix(args[off]);
		}
		catch (Exception e) {
			errorMessage("route","invalid destination IP address/len: "+args[off]);
			return;
		}
		Ip4Address nexthop=null;
		try {			
			if (!args[off+2].equals("none")) nexthop=new Ip4Address(args[off+2]);
		}
		catch (Exception e) {
			errorMessage("route","invalid nexthop IP address: "+args[off+2]);
			return;
		}
		NetInterface<Ip4Address,Ip4Packet> ni=null;
		if (args.length==off+5) {
			ni=ip4_layer.getNetInterface(args[off+4]);
			if (ni==null) {
				errorMessage("route","unknown device: "+args[off+4]);
			}			
		}
		else {
			if (nexthop!=null) {
				if (nexthop.isMulticast()) {
					errorMessage("route","nexthop can't be a multicast address");
				}
				else {
					for (NetInterface<Ip4Address,Ip4Packet> ni_i : ip4_layer.getIpNode().getNetInterfaces()) {
						for (Ip4Address addr_i : ni_i.getAddresses()) {
							if (addr_i instanceof Ip4AddressPrefix && ((Ip4AddressPrefix)addr_i).getPrefix().contains(nexthop)) {
								ni=ni_i;
								break;
							}
						}
						if (ni!=null) break;
					}
				}		
			}			
		}
		if (ni!=null) {
			Ip4Prefix dest_prefix=dest!=null? dest.getPrefix() : Ip4Prefix.ANY;
			ip4_layer.getRoutingTable().add(new Route<Ip4Address,Ip4Packet>(dest_prefix,nexthop,ni));
			if (out!=null) out.println("Route added");
		}
		else {
			errorMessage("route","no network interface found for nexthop "+nexthop);
		}
	}

	
	/** 'ip route del' command. */
	public void routeDelete(String[] args, int off) {
		Ip4Prefix dest=null;
		try {			
			if (!args[off].equals("default")) dest=new Ip4Prefix(args[off]);
		}
		catch (Exception e) {
			errorMessage("route","invalid destination IP address/len: "+args[off]);
			return;
		}
		Ip4Address nexthop=null;
		NetInterface<Ip4Address,Ip4Packet> ni=null;
		if (args.length>=off+3) {
			try {			
				if (!args[off+2].equals("none")) nexthop=new Ip4Address(args[off+2]);
			}
			catch (Exception e) {
				errorMessage("route","invalid nexthop IP address: "+args[off+2]);
				return;
			}
			if (args.length==off+5) {
				ni=ip4_layer.getNetInterface(args[off+4]);
				if (ni==null) {
					errorMessage("route","unknown device: "+args[off+4]);
					return;
				}				
			}
		}
		RoutingTable<Ip4Address,Ip4Packet> rt=ip4_layer.getRoutingTable();
		boolean deleted=false;
		if (dest==null) dest=Ip4Prefix.ANY;
		for (int i=rt.size()-1; i>=0; i--) {
			Route<Ip4Address,Ip4Packet> r=rt.get(i);
			if (r.getDestNetAddress().equals(dest) && (nexthop==null || nexthop.equals(r.getNextHop())) && (ni==null || ni==r.getOutputInterface())) {
				rt.remove(i);
				deleted=true;
			}
		}			
		if (deleted) {
			if (out!=null) out.println("Route deleted");
		}
		else {
			if (out!=null) out.println("Route not found");
		}
	}


	/** 'ip addr' command. */
	private void address(String[] args, int off) {
		if (args.length>off) {
			if (args[off].equals("s") || args[off].equals("show")) {
				addressShow();
				return;
			}
			if (args.length>=off+4 && (args[off].equals("a") || args[off].equals("add")) && args[off+2].equals("dev")) {
				addressAdd(args,off+1);
				return;
			}
			if (args.length>=off+4 && (args[off].equals("d") || args[off].equals("del") || args[off].equals("delete")) && args[off+2].equals("dev")) {
				addressDelete(args,off+1);
				return;
			}
			errorMessage("addr","wrong arguments: "+toString(args,off));
		}
		if (out!=null) out.println("Usage:\n  ip address add <ipaddr/len> dev <device>\n  ip address delete <ipaddr/len>\n  ip address show");
	}
	
	
	/** 'ip addr show' command. */
	public void addressShow() {
		if (out!=null) {
			StringBuffer sb=new StringBuffer();
			for (NetInterface<Ip4Address,Ip4Packet> ni : ip4_layer.getAllInterfaces()) {
				sb.append(ni.getName()+": <"+IpInterfaceUtils.getType(ni)+",UP>");
				String ph_addr=IpInterfaceUtils.getPhAddress(ni);
				if (ph_addr!=null) sb.append(" ["+ph_addr+"]");
				sb.append("\n");
				for (String addr : IpInterfaceUtils.getAddresses(ni)) sb.append("    inet "+addr+"\n");
			}
			out.print(sb.toString());
		}
	}
	
	
	/** 'ip addr add' command. */
	public void addressAdd(String[] args, int off) {
		Ip4AddressPrefix ipaddr_prefix;
		try {			
			ipaddr_prefix=new Ip4AddressPrefix(args[off]);
		}
		catch (Exception e) {
			errorMessage("addr","invalid IP address/len: "+args[off]);
			return;
		}
		String dev=args[off+2];
		NetInterface<Ip4Address,Ip4Packet> ni=ip4_layer.getNetInterface(dev);
		if (ni!=null) {
			ip4_layer.getIpNode().addAddress(ni,ipaddr_prefix);
			if (out!=null) out.println("Address added");
		}
		else {
			errorMessage("addr","unknown device: "+dev);
		}
	}

	
	/** 'ip addr del' command. */
	public void addressDelete(String[] args, int off) {
		Ip4AddressPrefix ipaddr_prefix;
		try {			
			ipaddr_prefix=new Ip4AddressPrefix(args[off]);
		}
		catch (Exception e) {
			errorMessage("addr","invalid IP address/len: "+args[off]);
			return;
		}
		String dev=args[off+2];
		NetInterface<Ip4Address,Ip4Packet> ni=ip4_layer.getNetInterface(dev);
		if (ni!=null) {
			if (ni.hasAddress(ipaddr_prefix)) {
				ip4_layer.getIpNode().removeAddress(ni,ipaddr_prefix);
				if (out!=null) out.println("Address deleted");
			}
			else {
				errorMessage("addr","unknown address: "+ipaddr_prefix);
			}
		}
		else {
			errorMessage("addr","unknown device: "+dev);
		}
	}

	
	/** 'ip link' command. */
	private void link(String[] args, int off) {
		if (args.length>off) {
			if (args[off].equals("s") || args[off].equals("show")) {
				addressShow();
				return;
			}
			if ((args[off].equals("l") || args[off].equals("list"))) {
				linkList();
				return;
			}
			if (args.length>=off+2 && (args[off].equals("a") || args[off].equals("add"))) {
				linkAdd(args,off+1);
				return;
			}
			if (args.length>=off+2 && (args[off].equals("d") || args[off].equals("del") || args[off].equals("delete"))) {
				linkDelete(args,off+1);
				return;
			}
			errorMessage("link","wrong arguments: "+toString(args,off));
		}
		if (out!=null) out.println("Usage:\n  ip link add <device-type>\n  ip link del <device-name>\n  ip link show");
	}
	
	
	/** 'ip link list' command. */
	public void linkList() {
		if (out!=null) {
			StringBuffer sb=new StringBuffer();
			for (String name : Links.getNames()) {
				sb.append(name).append('\n');
			}
			out.print(sb.toString());
		}
	}


	
	/** 'ip link add' command. */
	public void linkAdd(String[] args, int off) {
		String type=args[off];
		if (type.startsWith("int/")) {
			String link_name=type.substring(4);
			Link<Ip4Address,Ip4Packet> link=Links.getLink(link_name);
			NetInterface<Ip4Address,Ip4Packet> ni=new LinkInterface<Ip4Address,Ip4Packet>(link);
			ip4_layer.getIpNode().addNetInterface(ni);
			if (out!=null) out.println("Interface added");
		}
		else
		if (type.startsWith("ext/")) {
			try {
				SocketAddress hub_soaddr=new SocketAddress(type.substring(4));
				Ip4EthInterface eth=new Ip4EthInterface(new EthTunnelInterface(hub_soaddr,EthAddress.generateAddress()));
				ip4_layer.getIpNode().addNetInterface(eth);
				if (out!=null) out.println("Interface added");
			}
			catch (Exception e) {
				errorMessage("link","invalid external link type: "+type);
			}
		}
		else {
			errorMessage("link","invalid link type: "+type);			
		}
	}
	
	
	/** 'ip link del' command. */
	public void linkDelete(String[] args, int off) {
		String dev=args[off];
		NetInterface<Ip4Address,Ip4Packet> ni=ip4_layer.getNetInterface(dev);
		if (ni!=null) {
			ip4_layer.getIpNode().removeNetInterface(ni);
			if (out!=null) out.println("Interface removed");
		}
		else {
			errorMessage("link","unknown device: "+dev);
		}
	}
	
	
	/** 'ip forwarding' command. */
	public void forwarding(String[] args, int off) {
		if (args.length>off) {
			if (args[off].equals("y") || args[off].equals("yes")) {
				ip4_layer.setForwarding(true);
				return;
			}
			if (args[off].equals("n") || args[off].equals("no")) {
				ip4_layer.setForwarding(false);
				return;
			}
			errorMessage("forwarding","wrong arguments: "+toString(args,off));
		}
		if (out!=null) out.println("Usage:\n  ip forwarding yes|no");
	}
	

	/** Prints an error message, or throws an exception in case of no output stream.
	 * @param command the 'ip' sub-command (e.g. 'addr', 'route', etc)
	 * @param str the message to be printed */
	private void errorMessage(String command, String str) {
		str=command!=null?"ip "+command+": "+str:"ip: "+str;
		if (out!=null) out.println(str);
		else new RuntimeException(str);
	}

}
