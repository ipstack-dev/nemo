package it.unipr.netsec.nemo.program;


import java.io.PrintStream;

import it.unipr.netsec.ipstack.stack.IpStack;


/** <i>top</i> command.
 */
public class Top implements Program {

	IpStack ip_stack;

	PrintStream out;

	
	@Override
	public void run(IpStack ip_stack, PrintStream out, String[] args) {
		this.ip_stack=ip_stack;
		this.out=out;
		if (out!=null) {
			Runtime rt=Runtime.getRuntime();
			out.print("memory: total="+formatSize(rt.totalMemory()));
			out.print(" usage="+(formatSize(rt.totalMemory()-rt.freeMemory())));
			out.print(" free="+formatSize(rt.freeMemory()));			
			out.println(" max="+formatSize(rt.maxMemory()));
			//out.println("processors: "+rt.availableProcessors());
		}
	}
	
    public static String formatSize(long v) {
        if (v<1024) return v+"B";
        int z=(63 - Long.numberOfLeadingZeros(v))/10;
        return String.format("%.1f %sB",(double)v/(1L<<(z*10)),"KMGTPE".charAt(z));
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
