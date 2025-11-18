package it.unipr.netsec.ipstack.analyzer;


import java.io.IOException;
import java.io.PrintStream;

import it.unipr.netsec.ipstack.link.Link;
import it.unipr.netsec.ipstack.link.PromiscuousLinkInterface;
import it.unipr.netsec.ipstack.net.Address;
import it.unipr.netsec.ipstack.net.NetInterface;
import it.unipr.netsec.ipstack.net.NetInterfaceListener;
import it.unipr.netsec.ipstack.net.Packet;


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
