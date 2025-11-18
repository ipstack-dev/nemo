package test.ipstack;


import java.net.SocketException;

import org.zoolu.util.Flags;
import org.zoolu.util.LoggerLevel;
import org.zoolu.util.LoggerWriter;
import org.zoolu.util.SystemUtils;

import it.unipr.netsec.ipstack.ethernet.EthTunnelHub;


/** virtual Ethernet hub, connecting remote hosts via Ethernet-over-UDP tuunels.
 */
public abstract class VirtualHub {


	/** Main method. 
	 * @throws SocketException */
	public static void main(String[] args) throws SocketException {
		Flags flags=new Flags(args);
		int port=flags.getInteger("-p",EthTunnelHub.DEFAULT_PORT,"<port>","local UDP port (default "+EthTunnelHub.DEFAULT_PORT+")");
		int max_endpoints=flags.getInteger("-n",EthTunnelHub.DEFAULT_SWITCH_SIZE,"<num>","maximum number of endpoints (default "+EthTunnelHub.DEFAULT_SWITCH_SIZE+")");
		boolean verbose=flags.getBoolean("-v","verbose mode");
		boolean help=flags.getBoolean("-h","prints this help");
		if (help) {
			System.out.println(flags.toUsageString(VirtualHub.class.getSimpleName()));
			System.exit(0);
		}
		// else
		if (verbose) {
			SystemUtils.setDefaultLogger(new LoggerWriter(System.out,LoggerLevel.DEBUG));
			EthTunnelHub.DEBUG=true;
		}
		new EthTunnelHub(port,max_endpoints);
	}

}
