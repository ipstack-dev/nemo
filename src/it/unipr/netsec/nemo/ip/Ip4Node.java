package it.unipr.netsec.nemo.ip;


import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;

import org.zoolu.util.LoggerLevel;
import org.zoolu.util.SystemUtils;

import it.unipr.netsec.ipstack.icmp4.PingClient;
import it.unipr.netsec.ipstack.ip4.Ip4Address;
import it.unipr.netsec.ipstack.ip4.Ip4Layer;
import it.unipr.netsec.ipstack.ip4.Ip4Packet;
import it.unipr.netsec.ipstack.net.NetInterface;
import it.unipr.netsec.ipstack.stack.IpStack;
import it.unipr.netsec.nemo.telnet.server.TelnetServer;


public abstract class Ip4Node extends it.unipr.netsec.ipstack.ip4.Ip4Node {
	
	/** Debug mode */
	public static boolean DEBUG=false;

	/** Prints a debug message. */
	void debug(String str) {
		//SystemUtils.log(LoggerLevel.DEBUG,toString()+": "+str);
		SystemUtils.log(LoggerLevel.DEBUG,Ip4Host.class.getSimpleName()+"["+getAddress()+"]: "+str);
	}

	
	/** IP stack built on top of this node */
	IpStack ip_stack=null;
	
	/** Name */
	String name=null;
	
	/** Management TELNET server */
	TelnetServer telnet_mng_server=null;

	/** TELNET server */
	TelnetServer telnet_server=null;

	
	/** Creates a new node.
	 * @param net_interfaces network interfaces */
	public Ip4Node(List<NetInterface<Ip4Address,Ip4Packet>> net_interfaces) {
		super(net_interfaces);
	}

	/** Gets the local IP stack.
	 * @return the stack */
	public synchronized IpStack getIpStack() {
		if (ip_stack==null) ip_stack=new IpStack(new Ip4Layer(this),null);
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
	public void sendPacket(Ip4Packet ip_pkt) {
		if (DEBUG) debug("sending IP packet: "+ip_pkt);
		super.sendPacket(ip_pkt);
	}
	
	@Override
	protected void processReceivedPacket(NetInterface<Ip4Address,Ip4Packet> ni, Ip4Packet ip_pkt) {
		if (DEBUG) debug("received IP packet: "+ip_pkt);
		super.processReceivedPacket(ni,ip_pkt);
	}
	
	@Override
	public void close() {
		if (telnet_mng_server!=null) telnet_mng_server.close();
		if (telnet_server!=null) telnet_server.close();
		if (ip_stack!=null) ip_stack.close();
		super.close();
		if (DEBUG) debug("closed");
	}

	/** Runs a ping telent_session.
	 * It sends a given number of ICMP Echo Request messages and captures the corresponding ICMP Echo Reply responses.
	 * @param target_ip_addr IP address of the target node
	 * @param count the number of ICMP Echo requests to be sent
	 * @param out output where ping results are printed */
	public void ping(final Ip4Address target_ip_addr, int count, final PrintStream out) {
		PingClient ping_client=new PingClient(getIpStack().getIp4Layer(),out);
		ping_client.ping(target_ip_addr,count);
	}
			
	/** Starts a TELNET server running on the hosting machine and attached to this host.
	 * @param port TELNET server port on the hosting machine
	 * @param passwd_db username/password database; if <i>null</i>, default user and password is used */
	public void startTelnetMngServer(int port, Map<String,String> passwd_db) {
		try {
			telnet_mng_server=new TelnetServer(this,false,port,passwd_db);
		}
		catch (IOException e) {
			System.err.println("Error when starting a TELNET server on port "+port);
			e.printStackTrace();
		}
	}

	/** Starts a TELNET server on this host (on standard TCP port 21).
	 * @param auth_db username/password database; if <i>null</i>, default user and password is used */
	public void startTelnetServer(Map<String,String> auth_db) {
		try {
			telnet_server=new TelnetServer(this,auth_db);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}


}
