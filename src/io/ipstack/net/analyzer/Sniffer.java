package io.ipstack.net.analyzer;


import java.io.IOException;
import java.io.PrintStream;

import io.ipstack.net.link.Link;
import io.ipstack.net.link.PromiscuousLinkInterface;
import io.ipstack.net.packet.Address;
import io.ipstack.net.packet.NetInterface;
import io.ipstack.net.packet.NetInterfaceListener;
import io.ipstack.net.packet.Packet;


/** It captures all packets passed to a network interface or sent through a link and calls the (abstract) method {@link Sniffer#processPacket(NetInterface, Packet)}.
 */
public abstract class Sniffer {

	/** Network interface handler */
	NetInterfaceHandler<?,?> ni_handler;

	
	/** Create a new sniffer.
	 * @param link the link
	 * @throws IOException */
	protected Sniffer(Link<?,?> link) throws IOException {
		this(new PromiscuousLinkInterface<>(link));
	}

	
	/** Create a new sniffer.
	 * @param ni the network interface
	 * @throws IOException */
	protected Sniffer(NetInterface<?,?> ni) throws IOException {
		ni_handler=new NetInterfaceHandler<>(ni);
	}
	
	
	/** Stops capturing. */
	public void close() {
		ni_handler.close();
	}
	
	
	/** Processes a captured packet.
	 * @param ni the network interface where the packet has been captured
	 * @param pkt the packet */
	abstract void processPacket(NetInterface<?,?> ni, Packet<?> pkt);

	
	/** Handler of the network interface. */
	private class NetInterfaceHandler<A extends Address, P extends Packet<A>> {		
		NetInterface<A,P> ni;
		NetInterfaceListener<A,P> listener;
		
		public NetInterfaceHandler(NetInterface<A,P> ni) {
			this.ni=ni;
			listener=new NetInterfaceListener<A,P>() {
				@Override
				public void onIncomingPacket(NetInterface<A,P> ni, P pkt) {
					processPacket(ni,pkt);
				}
			};
			ni.addPromiscuousListener(listener);
		}
		
		public void close() {
			ni.removePromiscuousListener(listener);
		}
	}
	
}
