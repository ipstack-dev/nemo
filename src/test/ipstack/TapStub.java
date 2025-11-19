package test.ipstack;

import java.io.IOException;

import org.zoolu.util.Flags;
import org.zoolu.util.log.DefaultLogger;
import org.zoolu.util.log.LoggerLevel;
import org.zoolu.util.log.WriterLogger;

import io.ipstack.net.ethernet.EthHub;
import io.ipstack.net.ethernet.EthTunnelHub;
import io.ipstack.net.ethernet.EthTunnelInterface;
import io.ipstack.net.ip4.SocketAddress;
import io.ipstack.net.tuntap.TapInterface;


/** Ethernet repeater connecting a TAP interface to a (possibly remote) virtual hub.
 */
public abstract class TapStub {

	/** Main method. 
	 * @throws IOException */
	public static void main(String[] args) throws IOException {
		Flags flags=new Flags(args);
		int port=flags.getInteger("-p",-1,"<port>","local UDP port");
		String hub_soaddr=flags.getString("-s","127.0.0.1:"+EthTunnelHub.DEFAULT_PORT,"soaddr","Tunnel Hub socket address");
		String dev=flags.getString("-i",null,"dev","TAP interface");
		boolean verbose=flags.getBoolean("-v","verbose mode");
		boolean help=flags.getBoolean("-h","prints this help");
		if (help) {
			System.out.println(flags.toUsageString(TapStub.class.getSimpleName()));
			System.exit(0);
		}
		// else
		if (verbose) {
			DefaultLogger.setLogger(new WriterLogger(System.out,LoggerLevel.DEBUG));
			EthHub.VERBOSE=true;
		}
		new EthHub(new TapInterface(dev,null),new EthTunnelInterface(port,new SocketAddress(hub_soaddr),null));
	}

}
