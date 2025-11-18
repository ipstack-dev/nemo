package it.unipr.netsec.nemo.program;


import java.io.PrintStream;
import java.util.Arrays;
import java.util.Hashtable;

import it.unipr.netsec.ipstack.stack.IpStack;
import it.unipr.netsec.ipstack.tcp.ConnectionIdentifier;
import it.unipr.netsec.ipstack.tcp.TcpLayerListener;
import it.unipr.netsec.ipstack.udp.UdpLayerListener;


/** <i>netstat</i> command.
 */
public class Netstat implements Program {

	IpStack ip_stack;

	PrintStream out;

	
	@Override
	public void run(IpStack ip_stack, PrintStream out, String[] args) {
		this.ip_stack=ip_stack;
		this.out=out;
		if (out!=null) {
			StringBuffer sb=new StringBuffer();
			Hashtable<Integer,UdpLayerListener> udp_servers=ip_stack.getUdpLayer().getListeners();
			Integer[] udp_ports=udp_servers.keySet().toArray(new Integer[0]);
			Arrays.sort(udp_ports);
			for (Integer port : udp_ports) {
				sb.append("UDP\t"+port+"\tlistening\n");
			}
			Hashtable<Integer,TcpLayerListener> tcp_servers=ip_stack.getTcpLayer().getServerListeners();
			Integer[] tcp_ports=tcp_servers.keySet().toArray(new Integer[0]);
			Arrays.sort(tcp_ports);
			for (Integer port : tcp_ports) {
				sb.append("TCP\t"+port+"\tlistening\n");			
			}
			Hashtable<ConnectionIdentifier,TcpLayerListener> tcp_conns=ip_stack.getTcpLayer().getConnectionListeners();
			for (ConnectionIdentifier conn : tcp_conns.keySet()) {
				sb.append("TCP\t"+conn.getLocalSocketAddress()+"\t"+conn.getRemoteSocketAddress()+"\tconnected\n");			
			}
			out.print(sb.toString());
		}
	}

	@Override
	public boolean processInputData(byte[] buf, int off, int len) {
		return false;
	}

	@Override
	public boolean isRunning() {
		return false;
	}

	@Override
	public void halt() {
	}

}
