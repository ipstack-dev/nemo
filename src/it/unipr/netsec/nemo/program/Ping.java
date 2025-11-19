package it.unipr.netsec.nemo.program;


import java.io.PrintStream;

import org.zoolu.util.Flags;

import io.ipstack.net.icmp4.PingClient;
import io.ipstack.net.ip4.Ip4Address;
import io.ipstack.net.stack.IpStack;


/** <i>ping</i> command.
 */
public class Ping implements Program {
	
	IpStack ip_stack;	
	PrintStream out;
	PingClient ping_client;

	
	@Override
	public void run(IpStack ip_stack, PrintStream out, String[] args) {
		this.ip_stack=ip_stack;
		this.out=out;
		Flags flags=new Flags(args);
		flags.getString(null,null,null,null); // skip the program name
		int count=flags.getInteger("-c",3,"<num>","number of ping requests (default is 3)");
		long time=flags.getInteger("-t",1000,"<msecs>","ping period time (default is 1000 ms)");
		boolean help=flags.getBoolean("-h","prints this help message");
		String target_addr=flags.getString(null,null,"<target>","target address");
		
		if (args.length==1 || help) {
			if (out!=null) out.println(flags.toUsageString("ping").replaceFirst("java ",""));
			return;
		}
		// else
		Ip4Address target_ipaddr=null;
		try {
			target_ipaddr=new Ip4Address(target_addr);
		}
		catch (Exception e) {
			if (out!=null) out.println("Wrong IP address: "+target_addr);
		}
		if (target_ipaddr!=null) {
			ping_client=new PingClient(ip_stack.getIp4Layer(),out);
			ping_client.ping(target_ipaddr,count,time);
			ping_client=null;
		}
	}

	@Override
	public boolean processInputData(byte[] buf, int off, int len) {
		return false;
	}
	
	
	@Override
	public boolean isRunning() {
		return ping_client!=null && ping_client.isRunning();
	}

	
	@Override
	public void halt() {
		if (ping_client!=null) {
			ping_client.halt();
			ping_client=null;
		}
	}

}
