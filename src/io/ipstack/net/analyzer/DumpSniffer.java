package io.ipstack.net.analyzer;


import java.io.IOException;
import java.io.PrintStream;

import io.ipstack.net.link.Link;
import io.ipstack.net.link.PromiscuousLinkInterface;
import io.ipstack.net.packet.Address;
import io.ipstack.net.packet.NetInterface;
import io.ipstack.net.packet.NetInterfaceListener;
import io.ipstack.net.packet.Packet;


/** It captures all packets passed to a network interface or sent through a link and writes a dump to an output stream.
 */
public class DumpSniffer extends Sniffer {

	/** Output stream */
	PrintStream out;

	
	/** Create a new sniffer.
	 * @param link the link
	 * @param out the output stream
	 * @throws IOException */
	public DumpSniffer(Link<?,?> link, PrintStream out) throws IOException {
		this(new PromiscuousLinkInterface<>(link),out);
	}

	
	/** Create a new sniffer.
	 * @param ni the network interface
	 * @param out the output stream
	 * @throws IOException */
	public DumpSniffer(NetInterface<?,?> ni, PrintStream out) throws IOException {
		super(ni);
		this.out=out;
	}
	
	
	/** Processes a captured packet.
	 * @param ni the network interface where the packet has been captured
	 * @param pkt the packet */
	protected void processPacket(NetInterface<?,?> ni, Packet<?> pkt) {
		out.println(ProtocolAnalyzer.exploreInner(pkt));
	}
	
}
