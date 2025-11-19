package io.ipstack.net.link;


import java.util.HashMap;
import java.util.List;

import org.zoolu.util.Clock;

import io.ipstack.net.link.Link;
import io.ipstack.net.link.Repeater;
import io.ipstack.net.packet.Address;
import io.ipstack.net.packet.NetInterface;
import io.ipstack.net.packet.NetInterfaceListener;
import io.ipstack.net.packet.Packet;


/** Backward learning bridge.
 *  It uses backward learning for maintaining a map between node addresses and corresponding bridge external interfaces.
 */
public class Bridge<A extends Address, P extends Packet<A>> extends Repeater<A,P> {
	
	/** Expiration time, in milliseconds */
	public static long EXPIRATION_TIME=20000; // 20 secs
	
	/** Switching table. Each entry contains the network interface and last time (in milliseconds) */
	HashMap<A,Object[]> sw_table=new HashMap<>();
	
	/** Monitor port */
	NetInterfaceListener<A,P> monitor=null;

	
	/** Creates a new bridge.
	 * @param links the links the switch is attached to */
	@SafeVarargs
	public Bridge(Link<A,P>... links) {
		super(links);
	}

	
	/** creates a new bridge.
	 * @param net_interfaces the switch network interfaces */
	@SafeVarargs
	public Bridge(NetInterface<A,P>... net_interfaces) {
		super(net_interfaces);
	}

	
	/** creates a new bridge.
	 * @param net_interfaces the switch network interfaces */
	public Bridge(List<NetInterface<A,P>> net_interfaces) {
		super(net_interfaces);
	}

	
	@Override
	protected void processIncomingPacket(NetInterface<A,P> ni, P pkt) {
		//System.out.println("DEBUG: Bridge: processIncomingPacket: "+pkt);
		if (monitor!=null) monitor.onIncomingPacket(ni,pkt);
		long time=Clock.getDefaultClock().currentTimeMillis();
		// backward learning
		A src_addr=pkt.getSourceAddress();
		Object[] addr_entry=sw_table.get(src_addr);
		if (addr_entry==null) sw_table.put(src_addr,new Object[]{ni,new Long(time)});
		else {
			addr_entry[0]=ni;
			addr_entry[1]=new Long(time);
		}
		// get the output interface
		A dst_addr=pkt.getDestAddress();
		addr_entry=sw_table.get(dst_addr);
		if (addr_entry!=null) {
			if (time<((long)addr_entry[1]+EXPIRATION_TIME)) ((NetInterface<A,P>)addr_entry[0]).send(pkt,null);
			else {
				sw_table.remove(dst_addr);
				//System.out.println("DEBUG: Bridge: expired: "+dst_addr);
				broadcast(ni,pkt);
			}
		}
		else {
			//System.out.println("DEBUG: Bridge: not found: "+dst_addr);
			broadcast(ni,pkt);
		}
	}
	
	
	/** Sends a packet through all interfaces except the input interface.
	 * @param ni input interface
	 * @param pkt packet */
	protected void broadcast(NetInterface<A,P> ni, P pkt) {
		super.processIncomingPacket(ni,pkt);
	}
	
	
	/** Clears the switching table. */
	public void clear() {
		sw_table.clear();
	}
	
	
	/** Sets a monitor.
	 * @param monitor listener to be attached the monitor port. */
	public void setMonitor(NetInterfaceListener<A,P> monitor) {
		this.monitor=monitor;
	}
	
	
	@Override
	public void close() {
		clear();
		super.close();
	}

}
