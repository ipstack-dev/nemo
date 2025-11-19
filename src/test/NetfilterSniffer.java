package test;


import java.io.IOException;
import java.util.Date;

import org.zoolu.util.DateFormat;
import org.zoolu.util.Flags;
import org.zoolu.util.SystemUtils;

import io.ipstack.net.analyzer.LibpcapHeader;
import io.ipstack.net.analyzer.LibpcapWriter;
import io.ipstack.net.analyzer.ProtocolAnalyzer;
import io.ipstack.net.ip4.Ip4Packet;
import it.unipr.netsec.netfilter.NetfilterQueue;
import it.unipr.netsec.netfilter.PacketHandler;
import test.ipstack.VirtualSwitch;


/** Captures all netfilter queued packets.
 * <p>
 * In order to pass all incoming packets to this program you could use the following Linux command:
 * <pre>
 * sudo iptables -A INPUT -j NFQUEUE --queue-num 0
 * </pre>
 */
public final class NetfilterSniffer {
	private NetfilterSniffer() {}
	
	
	static boolean VERBOSE=false;
	static LibpcapWriter WRITER=null;
	
	private static int processPacket(byte[] buf, int len) {
		Ip4Packet ipPkt=Ip4Packet.parseIp4Packet(buf);
		if (WRITER!=null) {
			WRITER.write(ipPkt);
			if (VERBOSE) printPacket(ipPkt);
		}
		else printPacket(ipPkt);
		return len;
	}
	
	private static void printPacket(Ip4Packet ipPkt) {
		System.out.println(DateFormat.formatYyyyMMddHHmmssSSS(new Date())+ProtocolAnalyzer.exploreInner(ipPkt));
	}

	
   /** The main method. 
 * @throws IOException */
	public static void main(String[] args) throws IOException {
		Flags flags=new Flags(args);
		String pcap=flags.getString("-pcap",null,"file","writes packets to a pcap file");
		VERBOSE=flags.getBoolean("-v","verbose mode also when writing to file");
		boolean help=flags.getBoolean("-h","prints this help");

		if (help) {
			System.out.println(flags.toUsageString(VirtualSwitch.class.getSimpleName()));
			System.exit(0);
		}
		
		if (pcap!=null) {
			WRITER=new LibpcapWriter(LibpcapHeader.LINKTYPE_IPV4,pcap);
		}
		
		new NetfilterQueue(0,NetfilterSniffer::processPacket).start();
	}	

}
