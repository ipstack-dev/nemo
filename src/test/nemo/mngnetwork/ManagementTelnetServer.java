package test.nemo.mngnetwork;


import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import it.unipr.netsec.ipstack.ip4.Ip4Address;
import it.unipr.netsec.ipstack.ip4.Ip4Packet;
import it.unipr.netsec.ipstack.link.Link;
import it.unipr.netsec.ipstack.util.IpAddressUtils;
import it.unipr.netsec.nemo.ip.Ip4Node;
import it.unipr.netsec.nemo.link.Network;
import it.unipr.netsec.nemo.telnet.Telnet;
import it.unipr.netsec.nemo.telnet.TelnetListener;
import it.unipr.netsec.nemo.telnet.server.TelnetServerSession;


/** TELNET server that allows the management of multiple nodes.
 * <p>
 * Through the command <i>mng</i> it is possible to list all managed nodes (<i>mng list</i>),
 * or switch to the CLI of a given node (<i>mng &lt;ipaddr&gt;</i>).
 */
public class ManagementTelnetServer {

	public static int DEFAULT_PORT=23;
	
	public static String DEFAULT_USER="nemo";

	public static String DEFAULT_PASSWD="nemo";
	
	
	/** Java.net socket server */
	it.unipr.netsec.ipstack.socket.ServerSocket server=null;
	
	/** Whether it is running */
	boolean running=true;

	/** Managed network */
	Network<Ip4Node,Link<Ip4Address,Ip4Packet>> network;

	/** Allowed nodes */
	Map<String,AllowInfo> allow_db;
	
	/** Management node, used as starting node */
	Ip4Node mngnode;
	
	
	/** Creates a new server. */
	public ManagementTelnetServer(final int port, Ip4Node mngnode, final Network<Ip4Node,Link<Ip4Address,Ip4Packet>> network, Map<String,String> passwd_db, Map<String,AllowInfo> allow_db) throws IOException {
		this.mngnode=mngnode;
		this.network=network;
		this.allow_db=allow_db;
		if (passwd_db==null) {
			passwd_db=new HashMap<>();
			passwd_db.put(DEFAULT_USER,DEFAULT_PASSWD);
		}
		final Map<String,String> passwd_map=passwd_db;

		server=new it.unipr.netsec.ipstack.socket.ServerSocket(new java.net.ServerSocket(port));
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					while (running) {
						final TelnetServerSession session=createSession();
						Telnet telnet=new Telnet(server.accept(),new TelnetListener() {
							@Override
							public void onReceivedData(Telnet telnet, byte[] buf, int off, int len) {
								session.processReceivedData(telnet,buf,off,len);
							}
							@Override
							public void onReceivedCommand(Telnet telnet, int command, int option) {
								session.processReceivedCommand(telnet,command,option);
							}
							@Override
							public void onClosed(Telnet telnet) {
								session.processClosed(telnet);
							}
							@Override
							public void onReceivedSubnegotiation(Telnet telnet, int option, byte[] param) {
								session.processReceivedSubnegotiation(telnet,option,param);
							}
						});
						session.start(telnet,passwd_map,"Management server running on port "+port+"\r\n",mngnode);
					}
					
				}
				catch (IOException e) {
					System.out.println("DEBUG: ManagementTelnetServer: run(): terminated");
					e.printStackTrace();
				}
			}				
		}).start();
	}
	
	
	/** Creates a TELNET server session. */
	private TelnetServerSession createSession() {
		TelnetServerSession session=new TelnetServerSession() {
			@Override
			protected void processCommand(String command, String[] args) {
				if (command.equals("manage") || command.equals("mng")) {					
					if (args.length<2) {
						telnet.println("manage: missing argument.");
						telnet.println("Usage:\n  mng list\n  mng <ipaddr>\n");
					}
					else
					if (args[1].equals("list") || args[1].equals("l")) {
						telnet.println("managed nodes:");
						Set<String> nodes=network.getNodeNames();
						if (allow_db!=null) {
							allow_db.get(getAuthenticatedUsername());
							AllowInfo allow_info=allow_db.get(getAuthenticatedUsername());
							if (allow_info!=null) {
								Set<String> allowed_nodes=new HashSet<>();
								for (String n: nodes) if (allow_info.isAllowed(n)) allowed_nodes.add(n);
								nodes=allowed_nodes;
							}
						}
						for (String addr : IpAddressUtils.sort(nodes,Ip4Address.class)) telnet.println(addr);
					}
					else {
						String nodename=args[1];
						Ip4Node ip_node=network.getNode(nodename);
						if (allow_db!=null) {
							AllowInfo allow_info=allow_db.get(getAuthenticatedUsername());
							if (allow_info!=null && !allow_info.isAllowed(nodename)) ip_node=null;
						}
						if (ip_node!=null) {
							ip_stack=ip_node.getIpStack();
							prompt=ip_node.getName()+"> ";
						}
						else {
							telnet.println("ERROR: node "+nodename+" not found. Use 'mng list' to list all nodes.");
						}
					}
				}
				else super.processCommand(command,args);
			}
		};
		return session;
	}

	
	/** Closes the server. */
	public void close() {
		try {
			running=false;
			server.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

}
