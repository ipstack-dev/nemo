package test.ipstack;

import java.io.IOException;

import org.zoolu.util.Flags;
import org.zoolu.util.log.DefaultLogger;
import org.zoolu.util.log.LoggerLevel;
import org.zoolu.util.log.WriterLogger;

import io.ipstack.net.analyzer.LibpcapHeader;
import io.ipstack.net.analyzer.LibpcapWriter;
import io.ipstack.net.ethernet.EthAddress;
import io.ipstack.net.ethernet.EthHub;
import io.ipstack.net.ethernet.EthPacket;
import io.ipstack.net.ethernet.EthTunnelHub;
import io.ipstack.net.ethernet.EthTunnelInterface;
import io.ipstack.net.ethernet.EthTunnelSwitch;
import io.ipstack.net.ip4.SocketAddress;
import io.ipstack.net.packet.NetInterface;
import io.ipstack.net.tuntap.TapInterface;


/** Virtual Ethernet switch, connecting possibly remote hosts via Ethernet-over-UDP tunnels.
 * <p>
 * It may work also as Ethernet hub ('-hub' option).
 * <p>
 * Optionally it can be attached to a local TAP interface ('-tap dev' option).
 */
public abstract class VirtualSwitch {


	/** Main method. 
	 * @throws IOException */
	public static void main(String[] args) throws IOException {
		Flags flags=new Flags(args);
		int port=flags.getInteger("-p",EthTunnelHub.DEFAULT_PORT,"port","UDP port (default "+EthTunnelHub.DEFAULT_PORT+")");
		int size=flags.getInteger("-n",EthTunnelHub.DEFAULT_SWITCH_SIZE,"size","number of switch external ports (default "+EthTunnelHub.DEFAULT_SWITCH_SIZE+")");
		boolean hub=flags.getBoolean("-hub","works as Ehernet hub instead of switch");
		String tapdev=flags.getString("-tap",null,"dev","attaches the switch to the given TAP device");
		String pcap=flags.getString("-pcap",null,"file","captures packets on the monitor port and writes them to a pcap file");
		boolean verbose=flags.getBoolean("-v","verbose mode");
		boolean help=flags.getBoolean("-h","prints this help");
		if (help) {
			System.out.println(flags.toUsageString(VirtualSwitch.class.getSimpleName()));
			System.exit(0);
		}
		// else
		if (verbose) {
			DefaultLogger.setLogger(new WriterLogger(System.out,LoggerLevel.DEBUG));
			EthTunnelHub.DEBUG=true;
			EthTunnelSwitch.DEBUG=true;
		}
		if (tapdev!=null) size++;
		EthTunnelHub sw=hub? new EthTunnelHub(port,size) : new EthTunnelSwitch(port,size);
		
		if (pcap!=null) {
			LibpcapWriter writer=new LibpcapWriter(LibpcapHeader.LINKTYPE_ETHERNET,pcap);
			sw.setMonitor((ni,pkt)->writer.write(pkt));
		}

		if (tapdev!=null) {
			NetInterface<EthAddress,EthPacket> tap=new TapInterface(tapdev,null);
			new EthHub(tap,new EthTunnelInterface(new SocketAddress("127.0.0.1:"+port),null));
		}
	}

}
