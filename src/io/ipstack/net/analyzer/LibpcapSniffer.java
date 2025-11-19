package io.ipstack.net.analyzer;


import java.io.IOException;

import io.ipstack.net.ethernet.EthAddress;
import io.ipstack.net.ip4.Ip4Address;
import io.ipstack.net.ip4.Ip4EthInterface;
import io.ipstack.net.ip6.Ip6Address;
import io.ipstack.net.ip6.Ip6EthInterface;
import io.ipstack.net.link.Link;
import io.ipstack.net.link.PromiscuousLinkInterface;
import io.ipstack.net.packet.Address;
import io.ipstack.net.packet.NetInterface;
import io.ipstack.net.packet.Packet;
import io.ipstack.net.rawsocket.ethernet.RawEthInterface;


/** Libpcap-compatible sniffer.
 * It captures all packets passed to a network interface or sent through a link and writes them to a file using standard libpcap format.
 */
public class LibpcapSniffer extends Sniffer {

	/** Whether to skip SSH packets (TCP port 22) */
	boolean no_ssh=false;
	
	/** The libpcap writer */
	LibpcapWriter out;

	
	/** Create a new sniffer.
	 * @param link the link
	 * @param type link type (e.g. {@link LibpcapHeader#LINKTYPE_ETHERNET}, {@link LibpcapHeader#LINKTYPE_IPV4}, {@link LibpcapHeader#LINKTYPE_IPV6})
	 * @param file_name the pcap file where packets will be written
	 * @throws IOException */
	public LibpcapSniffer(Link<?,?> link, int type, String file_name) throws IOException {
		this(new PromiscuousLinkInterface<>(link),type,file_name);
	}

	
	/** Create a new sniffer.
	 * @param ni the network interface
	 * @param file_name the pcap file where packets will be written
	 * @throws IOException */
	public LibpcapSniffer(NetInterface<?,?> ni, String file_name) throws IOException {
		this(ni,guessType(ni),file_name);
	}

	
	/** Create a new sniffer.
	 * @param ni the network interface
	 * @param type the interface type  (e.g. {@link LibpcapHeader#LINKTYPE_ETHERNET}, {@link LibpcapHeader#LINKTYPE_IPV4}, {@link LibpcapHeader#LINKTYPE_IPV6})
	 * @param file_name the pcap file where packets will be written
	 * @throws IOException */
	public LibpcapSniffer(NetInterface<?,?> ni, int type, String file_name) throws IOException {
		super(ni);
		out=new LibpcapWriter(type,file_name);
	}

	
	/** Whether to skip SSH packets (TCP port 22).
	 * @param no_ssh <i>true</i> to skip SSH packets (TCP port 22) */
	public void skipSSH(boolean no_ssh) {
		this.no_ssh=no_ssh;
	}
	
	
	@Override
	void processPacket(NetInterface<?, ?> ni, Packet<?> pkt) {
		if (!no_ssh || ProtocolAnalyzer.exploreInner(pkt).toString().indexOf(":22 ")<0) out.write(pkt);
	}

	
	@Override
	public void close() {
		super.close();
		out.close();
	}
	
	
	/** Gets the network interface type.
	 * @param ni the interface
	 * @return the type */
	private static int guessType(NetInterface<?,?> ni) {
		Address addr=ni.getAddress();
		if(ni instanceof RawEthInterface || (addr!=null && addr instanceof EthAddress)) return LibpcapHeader.LINKTYPE_ETHERNET;
		if(ni instanceof Ip4EthInterface || (addr!=null && addr instanceof Ip4Address)) return LibpcapHeader.LINKTYPE_IPV4;
		if(ni instanceof Ip6EthInterface || (addr!=null && addr instanceof Ip6Address)) return LibpcapHeader.LINKTYPE_IPV6;
		throw new RuntimeException("unable to guess the interface type of '"+ni.getClass().getSimpleName()); 
	}
	
}
