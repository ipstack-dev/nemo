package it.unipr.netsec.nemo.telnet.server;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.zoolu.util.LoggerLevel;
import org.zoolu.util.SystemUtils;

import it.unipr.netsec.ipstack.socket.ServerSocket;
import it.unipr.netsec.ipstack.socket.Socket;
import it.unipr.netsec.nemo.ip.Ip4Node;
import it.unipr.netsec.nemo.telnet.Telnet;
import it.unipr.netsec.nemo.telnet.TelnetListener;


/** TELNET server on IP node.
 * It supports some Linux-like network configuration and diagnostic tools such as <b>ip</b> and <b>ping</b> commands.
 */
public class TelnetServer {
	
	/** Verbose mode */
	public static boolean VERBOSE=false;

	/** Logs a message. */
	private void log(String str) {
		SystemUtils.log(LoggerLevel.INFO,getClass(),str);
	}
	
	
	public static int DEFAULT_PORT=23;
	
	public static String DEFAULT_USER="nemo";

	public static String DEFAULT_PASSWD="nemo";

	
	/** Server socket */
	ServerSocket socket_server=null;

	/** Whether it is running */
	boolean running=true;

	
	/** Creates a new TELNET server.
	 * @param ip_node the IP node where the server is attached to
	 * @param passwd_db password database
	 * @throws IOException */
	public TelnetServer(Ip4Node ip_node, Map<String,String> passwd_db) throws IOException {
		this(ip_node,true,DEFAULT_PORT,passwd_db);
	}

	
	/** Creates a new TELNET server.
	 * @param ip_node the IP node where the server is attached to. The IP stack of this machine is used by the various commands like <i>ip</i>, <i>ping</i>, etc
	 * @param telent_on_ip_stack whether running also the TELNET server on the given IP machine or running it on the underlying machine (using the standard TCP socket)
	 * @param port the TCP server port
	 * @param passwd_db authentication database
	 * @throws IOException */
	public TelnetServer(final Ip4Node ip_node, final boolean telent_on_ip_stack, final int port, Map<String,String> passwd_db) throws IOException {
		if (VERBOSE) log("running on node "+ip_node.getName()+" port "+port);
		if (passwd_db==null) {
			passwd_db=new HashMap<>();
			passwd_db.put(DEFAULT_USER,DEFAULT_PASSWD);
		}
		final Map<String,String> passwd_map=passwd_db;
			
		if (telent_on_ip_stack) socket_server=new ServerSocket(new it.unipr.netsec.ipstack.tcp.ServerSocket(ip_node.getIpStack().getTcpLayer(),port));
		else socket_server=new ServerSocket(new java.net.ServerSocket(port));

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					while (running) {
						final TelnetServerSession session=new TelnetServerSession();
						final TelnetListener listener=new TelnetListener() {
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
						};
						try {
							final Telnet telnet=new Telnet(socket_server.accept(),listener);
							session.start(telnet,passwd_map,welcomeMessage(ip_node,port),ip_node);	
						}
						catch (IOException e)
						{	if (running) throw e;
						}
					}
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).start();
					
	}
	
	
	/** Gets the the welcome message. 
	 * @param ip_node the IP node
	 * @param port TELENET port
	 * @return the message */
	private String welcomeMessage(Ip4Node ip_node, int port) {
		return "Telent server running on "+ip_node.getName()+", port "+port+"\r\n";
	}

	
	/** Closes the server. */
	public void close() {
		if (!running) return;
		try {
			running=false;
			if (socket_server!=null) socket_server.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
		
}

