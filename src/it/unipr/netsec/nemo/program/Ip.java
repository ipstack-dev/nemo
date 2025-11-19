package it.unipr.netsec.nemo.program;


import java.io.PrintStream;

import io.ipstack.net.stack.IpCommand;
import io.ipstack.net.stack.IpStack;


/** <i>ip</i> command.
 */
public class Ip implements Program {
	
	@Override
	public void run(IpStack ip_stack, PrintStream out, String[] args) {
		if (args.length==0 || !args[0].equals("ip")) {
			if (out!=null) out.println("It is not 'ip' command");
		}
		new IpCommand(ip_stack,out).command(args,1);
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
