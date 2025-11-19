package test.nemo.mngnetwork;

import static java.lang.System.out;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.zoolu.util.Flags;
import org.zoolu.util.json.Json;
import org.zoolu.util.log.DefaultLogger;
import org.zoolu.util.log.LoggerLevel;
import org.zoolu.util.log.WriterLogger;

import io.ipstack.http.HttpServer;
import io.ipstack.net.ethernet.EthAddress;
import io.ipstack.net.ip4.Ip4Address;
import io.ipstack.net.ip4.Ip4AddressPrefix;
import io.ipstack.net.ip4.Ip4Packet;
import io.ipstack.net.ip4.Ip4Prefix;
import io.ipstack.net.link.Link;
import io.ipstack.net.link.LinkInterface;
import io.ipstack.net.tcp.TcpConnection;
import io.ipstack.net.tcp.TcpLayer;
import io.ipstack.net.tuntap.Ip4TuntapInterface;
import io.ipstack.net.tuntap.TuntapSocket;
import it.unipr.netsec.nemo.ip.Ip4Host;
import it.unipr.netsec.nemo.ip.Ip4Node;
import it.unipr.netsec.nemo.ip.Ip4Router;
import it.unipr.netsec.nemo.link.Network;
import it.unipr.netsec.nemo.telnet.Telnet;
import it.unipr.netsec.nemo.telnet.server.TelnetServer;
import it.unipr.netsec.nemo.telnet.server.TelnetServerSession;
import test.nemo.mngnetwork.info.ConfigNetworkInfo;


/** A test network.
 * The network is formed by a backbone link with one or more attached subnetworks.
 * The subnetworks are two-level trees.
 * <br>
 * Each network node (either host or router) runs a TELNET server (port 23).
 * Moreover, each host runs a web server (port 80).
 * <p>
 * The network can be connected to the external network through a GW attached to TUN/TAP interface.
 * In this case, for enabling routing between the external network and this network,
 * the routing of the local OS must be properly configured. 
 * <p>
 * The network can be controlled via management REST API and/or management TELNET server.
 */
public abstract class TestNetwork {
	
	public static String LOOPBACK_ADDR_PREFIX="172.31.";

	public static String DEFAULT_VHUB_SOADDR="127.0.0.1:7001";
	
	public static int DEFAULT_NUM_OF_NETS=3;
	
	public static int DEFAULT_NUM_OF_SERVERS=1;

	public static int DEFAULT_NUM_OF_ACCESS_NETS=2;

	public static int DEFAULT_NUM_OF_HOSTS=1;

	
	public static void main(String[] args) throws IOException {
		Flags flags=new Flags(args);
		boolean verbose=flags.getBoolean("-v","verbose mode");
		String config_file=flags.getString("-f",null,"<file>","configuration file");
		String network_file=flags.getString("-net",null,"<file>","network file");
		boolean noauth=flags.getBoolean("-noauth","disables TELNET authentication");
		boolean nohalt=flags.getBoolean("-nohalt","disables halt command");
		boolean noni=flags.getBoolean("-noni","disables default network configuration");
		boolean noaddr=flags.getBoolean("-noaddr","disables default address configuration");
		boolean noroute=flags.getBoolean("-noroute","disables default rotung table configuration");
		boolean tun=flags.getBoolean("-tun","attaches R0 to TUN interface");
		boolean tap=flags.getBoolean("-tap","attaches R0 to TAP interface");
		boolean macvtap=flags.getBoolean("-macvtap","attaches R0 to MACVTAP interface");
		String dev_name=flags.getString("-dev",null,"dev-name","device name (e.g. tap0");
		String dev_file=flags.getString("-devfile",null,"dev-file","device file (e.g. /dev/tap5)");
		String mac_addr=flags.getString("-eth",null,"mac-addr","Etherent address");
		String addr_prefix=flags.getString("-ipaddr","172.18.0.2/24","ipaddr/len","R0 IP address and prefix length");
		String default_router=flags.getString("-gw",null,"default-router","R0 defaul router");
		boolean help=flags.getBoolean("-h","prints the help message");
		int mng_telnet_port=flags.getInteger("-tlmng",-1,"port","enables management TELNET on the given port");
		int mng_http_port=flags.getInteger("-htmng",-1,"port","enables management REST API on the given port");
		String http_file=flags.getString("-htfile",null,"file","file that users can get via the managment HTTP server");
		String[] num=flags.getStringTuple("-n",4,null,"n s m h","number of sub-networks and hosts (n=number of ISPs, s=number of ISP servers, m=number of ISP access networks, h=number of access hosts)");
		//network_file="..\\test\\network1.json";
		//num=new String[]{"1","1","1","1"};			
		//num=new String[]{"20","2","2","2"};			
		//num=new String[]{"50","1","2","1"};

		if (help) {
			out.println(flags.toUsageString(TestNetwork.class.getName()));
			return;
		}
		
		ManagementHttpServer.FILENAME=http_file;	
		TelnetServerSession.ENABLE_AUTHENTICATION=!noauth;
		TelnetServerSession.ENABLE_HALT=!nohalt;
		if (verbose) {
			DefaultLogger.setLogger(new WriterLogger(out,LoggerLevel.DEBUG));
			Ip4Node.DEBUG=true;
			TcpLayer.DEBUG=true;
			TcpConnection.DEBUG=true;
		}
		else {
			DefaultLogger.setLogger(new WriterLogger(out,LoggerLevel.INFO));
		}
		HttpServer.VERBOSE=true;
		Telnet.VERBOSE=true;
		TelnetServerSession.VERBOSE=true;

		// global
		HashMap<String,String> passwd_db0=new HashMap<>();
		HashMap<String,AllowInfo> allow_db0=new HashMap<>();
		passwd_db0.put(TelnetServer.DEFAULT_USER,TelnetServer.DEFAULT_PASSWD);
		
		out.println("# Start creating the network...");
		
		// TUN/TAP/MAVTAP interface (optional)
		Link<Ip4Address,Ip4Packet> gw_link=null;
		if (tun||tap||macvtap) {
			out.println("opening "+(tun?"TUN":tap?"TAP":"MACVTAP")+" interface");
			TuntapSocket.Type tuntap_type=tun?TuntapSocket.Type.TUN:TuntapSocket.Type.TAP;
			if (dev_name==null) dev_name=tun?"tun0":tap?"tap0":"macvtap0";
			EthAddress eth_addr=mac_addr!=null?new EthAddress(mac_addr):null;
			gw_link=new Link<>();
			Ip4Router gw=new Ip4Router(new Ip4TuntapInterface(tuntap_type,dev_name,dev_file,eth_addr,new Ip4AddressPrefix(addr_prefix)),new LinkInterface<Ip4Address,Ip4Packet>(gw_link,new Ip4AddressPrefix("10.255.0.254/24")));
			gw.getRoutingTable().add(new Ip4Prefix("10.0.0.0/8"),new Ip4Address("10.255.0.1"));
			if (default_router!=null) gw.getRoutingTable().add(Ip4Prefix.ANY,new Ip4Address(default_router));
		}
		
		// network
		TestNetworkInfo test_network_info=new TestNetworkInfo();
		Network<Ip4Node,Link<Ip4Address,Ip4Packet>> network;
		if (network_file!=null) {
			ConfigNetworkInfo network_info=(ConfigNetworkInfo)Json.fromJSONFile(new File(network_file),ConfigNetworkInfo.class);
			network=TestNetworkFactory.getInstance(network_info,passwd_db0);
		}
		else {
			if (config_file!=null) Json.fromJSONFile(new File(config_file),test_network_info);		
			if (num!=null) {
				test_network_info.num_nets=Integer.parseInt(num[0]);
				test_network_info.num_servers=Integer.parseInt(num[1]);
				test_network_info.num_access_nets=Integer.parseInt(num[2]);
				test_network_info.num_hosts=Integer.parseInt(num[3]);
			}
			if (noni) test_network_info.ni_auto_conf=false;
			if (noaddr) test_network_info.addr_auto_conf=false;
			if (noroute) test_network_info.route_auto_conf=false;
			network=TestNetworkFactory.getInstance(gw_link,passwd_db0,test_network_info);
		}

		// configure admin users for the sub-networks
		for (int i=1; i<=test_network_info.num_nets; i++) {
			String username="user"+i;
			passwd_db0.put(username,TelnetServer.DEFAULT_PASSWD);
			AllowInfo allow_info=new AllowInfo();
			allow_info.allowedNames=new String[] {"172.31.0."+i};
			allow_info.allowedPrefixes=new String[] {"172.31."+i+".", "10."+i+"."};
			allow_db0.put(username,allow_info);
		}
		
		// telnet management interface
		if (mng_telnet_port>0) {
			Ip4Host mng_node=new Ip4Host();
			mng_node.setName("mng-host");
			new ManagementTelnetServer(mng_telnet_port,mng_node,network,passwd_db0,allow_db0);
		}
		
		// http management interface
		if (mng_http_port>0) {
			new ManagementHttpServer(mng_http_port,network);			
		}
		
		out.println("# Done.");
	}	

}
