package it.unipr.netsec.nemo.program;


import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;

import org.zoolu.util.Flags;

import it.unipr.netsec.ipstack.analyzer.LibpcapHeader;
import it.unipr.netsec.ipstack.analyzer.LibpcapSniffer;
import it.unipr.netsec.ipstack.analyzer.ProtocolAnalyzer;
import it.unipr.netsec.ipstack.analyzer.DumpSniffer;
import it.unipr.netsec.ipstack.ip4.Ip4Address;
import it.unipr.netsec.ipstack.ip4.Ip4Packet;
import it.unipr.netsec.ipstack.net.NetInterface;
import it.unipr.netsec.ipstack.net.Packet;
import it.unipr.netsec.ipstack.stack.IpStack;
import it.unipr.netsec.nemo.program.tcpdump.AndMatch;
import it.unipr.netsec.nemo.program.tcpdump.Ip4AddressMatch;
import it.unipr.netsec.nemo.program.tcpdump.Match;
import it.unipr.netsec.nemo.program.tcpdump.NotMatch;
import it.unipr.netsec.nemo.program.tcpdump.OrMatch;
import it.unipr.netsec.nemo.program.tcpdump.PortMatch;
import it.unipr.netsec.nemo.program.tcpdump.ProtocolMatch;


/** <i>tcpdump</i> command.
 */
public class Tcpdump implements Program {

	public static boolean DONT_PRINT_LOCAL_TELNET=true;
	
	IpStack ip_stack;	
	PrintStream out;
	DumpSniffer tcpdump;
	LibpcapSniffer libpcap_sniffer;
	Boolean running=new Boolean(true);

	
	@Override
	public void run(IpStack ip_stack, PrintStream out, String[] args) {
		this.ip_stack=ip_stack;
		this.out=out;
		Flags flags=new Flags(args);
		flags.getString(null,null,null,null); // skip the program name
		String ni_name=flags.getString("-i",null,"<interface>","network interface where listening in");
		boolean help=flags.getBoolean("-h","prints this help message");
		// filters:
		final String proto=flags.getString("-p",null,"<proto>","specifies a given protocol");
		final String host=flags.getString("-host",null,"<addr>","specifies a given IP address");
		final String src_addr=flags.getString("-src",null,"<addr>","specifies a given IP source address");
		final String dst_addr=flags.getString("-dst",null,"<addr>","specifies a given IP destination address");
		final int port=flags.getInteger("-port",-1,"<port>","specifies a given transport port");
		final int src_port=flags.getInteger("-sport",-1,"<port>","specifies a given transport source port");
		final int dst_port=flags.getInteger("-dport",-1,"<port>","specifies a given transport destination port");
		final String output_file=flags.getString("-w",null,"<file>","writes the trace to a file, in libpcap format");
		
		if (help) {
			if (out!=null) out.println(flags.toUsageString("tcpdump").replaceFirst("java ",""));
			return;
		}
		// else
		NetInterface<Ip4Address,Ip4Packet> ni=null;
		if (ni_name!=null) {
			for (NetInterface<Ip4Address,Ip4Packet> ni_i : ip_stack.getIp4Layer().getAllInterfaces()) {
				if (ni_i.getName().equals(ni_name)) {
					ni=ni_i;
					break;
				}
			}
			if (ni==null) {
				if (out!=null) errorMessage("interface '"+ni_name+"' not found");
				return;
			}			
		}
		else {
			ArrayList<NetInterface<Ip4Address,Ip4Packet>> all_ni=ip_stack.getIp4Layer().getAllInterfaces();
			if (all_ni.size()==0) {
				if (out!=null) errorMessage("no network interface found");
				return;
			}
			ni=all_ni.size()>1? all_ni.get(1) : all_ni.get(0); // if possible, skip the 'lo' interface
		}
		
		if (output_file!=null) {
			try {
				libpcap_sniffer=new LibpcapSniffer(ni,LibpcapHeader.LINKTYPE_IPV4,output_file);
			}
			catch (IOException e) {
				errorMessage(e.getMessage());
			}
			synchronized (running) {
				try {
					running.wait();
				}
				catch (InterruptedException e) {
				}
			}
			if (libpcap_sniffer!=null) libpcap_sniffer.close();
		}
		else {
			try {
				final ArrayList<Match> rule=new ArrayList<>();
				if (proto!=null) rule.add(new ProtocolMatch(proto));
				if (host!=null) rule.add(new Ip4AddressMatch(new Ip4Address(host),Ip4AddressMatch.Type.ANY));
				if (src_addr!=null) rule.add(new Ip4AddressMatch(new Ip4Address(src_addr),Ip4AddressMatch.Type.SRC));
				if (dst_addr!=null) rule.add(new Ip4AddressMatch(new Ip4Address(dst_addr),Ip4AddressMatch.Type.DST));
				if (port>=0) rule.add(new PortMatch(port,PortMatch.Type.ANY));
				if (src_port>=0) rule.add(new PortMatch(src_port,PortMatch.Type.SRC));
				if (dst_port>=0) rule.add(new PortMatch(dst_port,PortMatch.Type.DST));

				if (DONT_PRINT_LOCAL_TELNET) {
					ArrayList<Ip4Address> addresses=ip_stack.getIp4Layer().getIpNode().getAddresses();
					if (addresses.size()>0) {
						OrMatch addr_match=new OrMatch();
						for (Ip4Address a : addresses) addr_match.add(new Ip4AddressMatch(a,Ip4AddressMatch.Type.ANY));
						rule.add(new NotMatch(new AndMatch(addr_match,new ProtocolMatch(ProtocolMatch.Type.TCP),new PortMatch(23,PortMatch.Type.ANY))));
					}
					/*ArrayList<Match> addr_match=new ArrayList<>();
					for (Ip4Address addr : ip_stack.getIp4Layer().getIpNode().getAddresses()) addr_match.add(new Ip4AddressMatch(addr,Ip4AddressMatch.Type.ANY));
					if (addr_match.size()>0) {
						rule.add(new NotMatch(new AndMatch(new OrMatch(addr_match),new ProtocolMatch(ProtocolMatch.Protocol.TCP),new PortMatch(23,PortMatch.Type.ANY))));
					}*/
				}
				
				tcpdump=new DumpSniffer(ni,out) {
					@Override
					protected void processPacket(NetInterface<?,?> ni, Packet<?> pkt) {
						//if (acceptPacket(proto,port,pkt) && (!DONT_PRINT_LOCAL_TELNET || !isLocalTcp(pkt,23))) super.processPacket(ni,pkt);
						if (rule.size()==0 || new AndMatch(rule).getValue(ProtocolAnalyzer.explore(pkt))) super.processPacket(ni,pkt);
					}
				};
			}
			catch (IOException e) {
				errorMessage(e.getMessage());
			}
			if (out!=null) out.println("tcpdump: start listening on interface "+ni.getName());
			synchronized (running) {
				try {
					running.wait();
				}
				catch (InterruptedException e) {
				}
			}
			if (tcpdump!=null) tcpdump.close();
			if (out!=null) out.println("tcpdump: stopped listening");
		}
	}
	
	
	@Override
	public boolean processInputData(byte[] buf, int off, int len) {
		return false;
	}
	
	
	@Override
	public boolean isRunning() {
		return running;
	}
		
	
	/** Prints an error message, or throws an exception in case of no output stream.
	 * @param str the message to be printed */
	private void errorMessage(String str) {
		str="tcpdump: error: "+str;
		if (out!=null) out.println(str);
		else new RuntimeException(str);
	}

	
	@Override
	public void halt() {
		if (tcpdump!=null) tcpdump.close();
		if (libpcap_sniffer!=null) libpcap_sniffer.close();
		synchronized (running) {
			running.notifyAll();
			running=false;
		}
		tcpdump=null;
	}

}
