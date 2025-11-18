package it.unipr.netsec.ipstack.link;


import java.util.HashMap;
import java.util.List;

import org.zoolu.util.Clock;

import it.unipr.netsec.ipstack.link.Link;
import it.unipr.netsec.ipstack.link.Repeater;
import it.unipr.netsec.ipstack.net.Address;
import it.unipr.netsec.ipstack.net.NetInterface;
import it.unipr.netsec.ipstack.net.Packet;


/** Backward learning bridge.
 *  It uses backward learning for maintaining a map between node addresses and corresponding bridge external interfaces.
 */
public class Bridge<A extends Address, P extends Packet<A>> extends Repeater<A,P> {
	
	/** Expiration time, in milliseconds */
	public static long EXPIRATION_TIME=20000; // 20 secs
	
	/** Switching table. Each entry contains the network interface and last time (in milliseconds) */
	HashMap<A,Object[]> sw_table=new HashMap<>();

	
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

	
	/** Processes an incoming packet.
	 * @param ni input network interface
	 * @param pkt incoming packet */
	protected void processIncomingPacket(NetInterface<A,P> ni, P pkt) {
		//System.out.println("DEBUG: Bridge: processIncomingPacket: "+pkt);
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
	
	
	@Override
	public void close() {
		clear();
		super.close();
	}

}
