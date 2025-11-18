package it.unipr.netsec.nemo.ip;


import java.io.PrintStream;
import java.util.List;

import org.zoolu.util.LoggerLevel;
import org.zoolu.util.SystemUtils;

import it.unipr.netsec.ipstack.icmp6.Ping6Client;
import it.unipr.netsec.ipstack.ip6.Ip6Address;
import it.unipr.netsec.ipstack.ip6.Ip6Layer;
import it.unipr.netsec.ipstack.ip6.Ip6Packet;
import it.unipr.netsec.ipstack.net.NetInterface;
import it.unipr.netsec.ipstack.stack.IpStack;


public abstract class Ip6Node extends it.unipr.netsec.ipstack.ip6.Ip6Node {
	
	/** Debug mode */
	public static boolean DEBUG=false;

	/** Prints a debug message. */
	void debug(String str) {
		//SystemUtils.log(LoggerLevel.DEBUG,toString()+": "+str);
		SystemUtils.log(LoggerLevel.DEBUG,Ip6Host.class.getSimpleName()+"["+getAddress()+"]: "+str);
	}

	
	/** IP stack built on top of this node */
	IpStack ip_stack=null;

	/** Name */
	String name=null;

	
	/** Creates a new node.
	 * @param net_interfaces network interfaces */
	public Ip6Node(List<NetInterface<Ip6Address,Ip6Packet>> net_interfaces) {
		super(net_interfaces);
	}	

	/** Gets the local IP stack.
	 * @return the stack */
	public IpStack getIpStack() {
		if (ip_stack==null) ip_stack=new IpStack(null,new Ip6Layer(this));
		return ip_stack;
	}
	
	/** Sets host name.
	 * @param name host name */
	public void setName(String name) {
		this.name=name;
	}

	@Override
	public String getName() {
		return name!=null? name : getAddress().toString();
	}

	@Override
	public void sendPacket(Ip6Packet ip_pkt) {
		if (DEBUG) debug("sending IP packet: "+ip_pkt);
		super.sendPacket(ip_pkt);
	}
	
	@Override
	protected void processReceivedPacket(NetInterface<Ip6Address,Ip6Packet> ni, Ip6Packet ip_pkt) {
		if (DEBUG) debug("received IP packet: "+ip_pkt);
		super.processReceivedPacket(ni,ip_pkt);
	}

	@Override
	public void close() {
		// TODO
		for (NetInterface<Ip6Address,Ip6Packet> ni : getNetInterfaces()) removeNetInterface(ni);	
	}

	/** Runs a ping telent_session.
	 * It sends a given number of ICMPv6 Echo Request messages and captures the corresponding ICMPv6 Echo Reply responses.
	 * @param target_ip_addr IP address of the target node
	 * @param count the number of ICMP Echo requests to be sent
	 * @param out output where ping results are printed */
	public void ping(final Ip6Address target_ip_addr, int count, final PrintStream out) {
		new Ping6Client(getIpStack().getIp6Layer(),target_ip_addr,count,out);
	}

}
