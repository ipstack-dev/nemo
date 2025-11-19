package it.unipr.netsec.nemo.program;


import java.io.PrintStream;
import java.util.Map;

import io.ipstack.net.stack.IpStack;


/** <i>tcpdump</i> command.
 */
public class Passwd implements Program {

	public static boolean DONT_PRINT_LOCAL_TELNET=true;
	
	PrintStream out;
	Map<String,String> auth_db;
	String user;
	StringBuffer sb=new StringBuffer();
	Boolean running=new Boolean(true);
	
	
	public Passwd(Map<String,String> auth_db, String user) {
		this.auth_db=auth_db;
		this.user=user;
	}

	
	@Override
	public void run(IpStack ip_stack, PrintStream out, String[] args) {
		this.out=out;	
		if (out!=null) out.print("new password of '"+user+"': ");
		synchronized (running) {
			try {
				running.wait();
			}
			catch (InterruptedException e) {
			}
		}
		String pw=sb.toString();
		if (pw.length()>0) {
			//if (out!=null) out.println("\nnew password is: "+pw);
			auth_db.put(user,pw);
			if (out!=null) out.println("\npassword changed");			
		}
	}
	
	
	@Override
	public boolean processInputData(byte[] buf, int off, int len) {
		//System.out.println("DEBUG: passwd: "+new String(buf,off,len));
		int end=off+len;
		byte c=-1;
		while (off<end && (c=buf[off++])!='\r' && c!='\n') {
			sb.append((char)c);
			if (out!=null) out.append('*');
		}
		if (c=='\r' || c=='\n') halt();
		return true;
	}
	
	
	@Override
	public boolean isRunning() {
		return running;
	}
		
	
	@Override
	public void halt() {
		synchronized (running) {
			running.notifyAll();
			running=false;
		}
	}

}
