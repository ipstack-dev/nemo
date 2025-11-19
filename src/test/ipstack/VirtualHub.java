package test.ipstack;


import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Stream;


/** Virtual Ethernet hub, connecting remote hosts via Ethernet-over-UDP tunnels.
 */
public abstract class VirtualHub {


	/** Main method. 
	 * @throws IOException */
	public static void main(String[] args) throws IOException {
		/*Flags flags=new Flags(args);
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
		new EthTunnelHub(port,max_endpoints);*/
		
		args=Stream.concat(Arrays.stream(new String[]{"-hub"}),Arrays.stream(args)).toArray(String[]::new);
		VirtualSwitch.main(args);
	}

}
