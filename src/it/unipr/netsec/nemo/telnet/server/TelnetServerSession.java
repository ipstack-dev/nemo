package it.unipr.netsec.nemo.telnet.server;


import java.util.ArrayList;
import java.util.Map;

import org.zoolu.util.Bytes;
import org.zoolu.util.LoggerLevel;
import org.zoolu.util.SystemUtils;

import it.unipr.netsec.ipstack.stack.IpStack;
import it.unipr.netsec.nemo.program.Program;
import it.unipr.netsec.nemo.ip.Ip4Node;
import it.unipr.netsec.nemo.program.Passwd;
import it.unipr.netsec.nemo.telnet.Telnet;

import static org.zoolu.util.LoggerLevel.*;


/** Handler of a TELNET server session.
 */
public class TelnetServerSession {
	
	/** Verbose mode */
	public static boolean VERBOSE=false;

	/** Logs a message. */
	private void log(LoggerLevel level, String str) {
		SystemUtils.log(level,null,toString()+": "+str);
	}

	
	public static boolean ENABLE_AUTHENTICATION=true;
	public static boolean ENABLE_ECHO=true;
	public static boolean ENABLE_HALT=false;

	private static byte[] WILL_ECHO=new byte[] { (byte)Telnet.IAC, (byte)Telnet.CMD_WILL, (byte)Telnet.OPT_Echo };
	private static byte[] WILL_SUPPRESS_GOA=new byte[] { (byte)Telnet.IAC, (byte)Telnet.CMD_WILL, (byte)Telnet.OPT_Suppress_Go_Ahead };
	private static byte[] DO_SUPPRESS_GOA=new byte[] { (byte)Telnet.IAC, (byte)Telnet.CMD_DO, (byte)Telnet.OPT_Suppress_Go_Ahead };
	private static byte[] DO_TERMINAL_TYPE=new byte[] { (byte)Telnet.IAC, (byte)Telnet.CMD_DO, (byte)Telnet.OPT_Terminal_Type };
	private static byte[] SUBNEGOTIATION_REQUEST_TERMINAL_TYPE=new byte[] { (byte)Telnet.IAC, (byte)Telnet.CMD_SB, (byte)Telnet.OPT_Terminal_Type,  (byte)0x01, (byte)Telnet.IAC, (byte)Telnet.CMD_SE };

	private static byte ESC=(byte)0x1b;
	private static byte LB=(byte)'[';

	private static byte[] CTR_C=new byte[] { (byte)0x03 };	
	private static byte[] EL0=new byte[] { ESC, LB, (byte)'K' }; // Erase Line = Esc[K
	private static byte[] CUU=new byte[] { ESC, LB, (byte)'A' }; // Cursor Up = Esc[A
	private static byte[] CUD=new byte[] { ESC, LB, (byte)'B' }; // Cursor Down = Esc[B
	private static byte[] CUR=new byte[] { ESC, LB, (byte)'C' }; // Cursor Right = Esc[C
	private static byte[] CUL=new byte[] { ESC, LB, (byte)'D' }; // Cursor Left = Esc[D

	
	/** TELNET socket */
	protected Telnet telnet=null;

	/** IP stack */
	protected IpStack ip_stack;

	/** Prompt string */
	protected String prompt;

	/** Receiver text buffer */
	String buffer="";

	/** Whether it is running */
	boolean running=true;
			
	/** Whether doing echo */ 
	boolean echo=false;

	/** Receiver lock */ 
	Object receiver=0;
	
	/** Last executed command */ 
	Program exec=null;
	
	/** History */
	ArrayList<String> history=new ArrayList<>();
	
	int history_index=-1;
	
	/** The welcome message */ 
	String welcome_message;
	
	/** Password database */
	Map<String,String> passwd_db; 

	/** The provided user name */ 
	String user=null;

	/** Whether the user has been authenticated */ 
	boolean authenticated=false;
	
	/** Number of remaining authentication attempts */ 
	int auth_attempts=3;

	/** Creates a new server. */
	public TelnetServerSession() {
	}

	
	/** Gets the authenticated user.
	 * @return the authenticated user name or <i>null</i>*/
	public String getAuthenticatedUsername() {
		return authenticated? user : null;
	}

	
	/** Starts the server.
	 * @param telnet TELNET session
	 * @param passwd_db password database
	 * @param welcome_message welcome message to be displayed
	 * @param ip_machine the IP node */
	public void start(Telnet telnet, Map<String,String> passwd_db, String welcome_message, Ip4Node ip_machine) {
		if (VERBOSE) log(DEBUG,"starting...");
		this.telnet=telnet;
		this.passwd_db=passwd_db;
		this.ip_stack=ip_machine.getIpStack();
		this.welcome_message=welcome_message;
		final String host=""+ip_machine.getName();
		prompt=host+"> ";
		if (!ENABLE_AUTHENTICATION) {
			user="anonymous";
			authenticated=true;
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				receiver();
			}				
		}).start();
		if (VERBOSE) log(INFO,"started");
		SystemUtils.runAfter(500,new Runnable() {
			@Override
			public void run() {
				if (!authenticated) authenticate();
				else welcome();
			}								
		});
	}

	
	/** Starts authentication. */
	private void authenticate() {
		authenticated=false;
		user=null;
		telnet.print("user: ");
	}

	
	/** Prints welcome message. */
	private void welcome() {
		telnet.println(welcome_message);
		telnet.print(prompt);		
	}
	
	
	/** Processes incoming TELNET commands. */
	public void processReceivedCommand(Telnet telnet, int command, int option) {
		if (VERBOSE) log(DEBUG,"processReceivedCommand(): "+command+(option>=0? ", "+option : ""));
		if (command==Telnet.CMD_Erase_Line) {
			if (exec!=null) exec.halt();
		}
		/*else
		if (command==Telnet.CMD_WILL && option==Telnet.OPT_Terminal_Type) {
			//telnet.send(DO_TERMINAL_TYPE);
			//telnet.send(SUBNEGOTIATION_REQUEST_TERMINAL_TYPE);
			telnet.send(Bytes.concat(DO_TERMINAL_TYPE,SUBNEGOTIATION_REQUEST_TERMINAL_TYPE));

		}*/
		else
		if (ENABLE_ECHO && command==Telnet.CMD_DO && option==Telnet.OPT_Echo) {
			telnet.send(WILL_ECHO);
			echo=true;
		}
		else
		if (ENABLE_ECHO && command==Telnet.CMD_WILL && option==Telnet.OPT_Suppress_Go_Ahead) {
			telnet.send(DO_SUPPRESS_GOA);
		}
		else
		if (ENABLE_ECHO && command==Telnet.CMD_DO && option==Telnet.OPT_Suppress_Go_Ahead) {
			telnet.send(WILL_SUPPRESS_GOA);
		}
	}

	
	/** Processes incoming TELNET sub-negotiations. */
	public void processReceivedSubnegotiation(Telnet telnet, int option, byte[] param) {
		if (VERBOSE) log(DEBUG,"processReceivedSubnegotiation(): "+option+(param!=null? ", 0x"+Bytes.toHex(param) : ""));
	}

	
	/** Processes incoming data. */
	public void processReceivedData(Telnet telnet, byte[] buf, int off, int len) {
		if (exec!=null && !exec.isRunning()) exec=null;
		if (exec!=null && exec.processInputData(buf,off,len)) return;
		// else
		synchronized (receiver) {
			if (Bytes.indexOf(CTR_C,buf,off,len)>=0) {
				if (exec!=null) exec.halt();
				buffer="";
			}
			else
			if (Bytes.indexOf(CUU,buf,off,len)>=0) {
				// cursor up
				if (history_index>=0) {
					telnet.print("\r"+new String(EL0)+prompt+history.get(history_index));
					buffer=history.get(history_index);
					receiver.notifyAll();
					history_index--;
					if (history_index<0) history_index=history.size()-1;
				}
			}
			else
			if (Bytes.indexOf(CUD,buf,off,len)>=0) {
				// cursor down
				if (history_index>=0) {
					history_index++;
					if (history_index==history.size()) history_index=0;
					telnet.print("\r"+new String(EL0)+prompt+history.get(history_index));
					buffer=history.get(history_index);
					receiver.notifyAll();
				}
			}
			else
			if (Bytes.indexOf(CUR,buf,off,len)>=0 || Bytes.indexOf(CUL,buf,off,len)>=0) {
				// cursor right or left
				// ...
			}
			else {
				buffer+=new String(buf,off,len);
				//if (VERBOSE) log("processReceivedData(): buffer: "+buffer);
				if (echo && (authenticated || user==null)) telnet.send(buf,off,len);
				receiver.notifyAll();
			}
		}
	}

	
	/** When a session finishes. */
	public void processClosed(Telnet telnet) {
		if (VERBOSE) log(DEBUG,"processClosed()");
	}

	
	/** Processes the received data as cli instructions. */
	private void receiver() {
		while (running) {
			ArrayList<String> lines=new ArrayList<>();
			synchronized (receiver) {
				try { receiver.wait(); } catch (InterruptedException e) {}
				int eol;
				buffer=terminalBeautify(buffer);
				while ((eol=buffer.indexOf('\n'))>=0) {
					String line=buffer.substring(0,eol+1).trim();
					buffer=buffer.substring(eol+1);
					lines.add(line);
				}
			}
			while (running && lines.size()>0) {
				String line=lines.remove(0);
				if (line.length()>0) {
					String[] args=line.split(" ");
					String command=args[0];
					if (VERBOSE) log(DEBUG,"processReceivedData(): "+line);
					// authentication
					if (user==null) {
						user=command;
						telnet.print("password: ");
						continue;
					}
					else
					if (!authenticated) {
						String passwd=command;
						if (passwd_db.containsKey(user) && passwd_db.get(user).equals(passwd)) {
							if (VERBOSE) log(INFO,"processReceivedData(): Authentication success");
							telnet.println("\n");
							authenticated=true;
							welcome();
							continue;
						}
						else {
							if (VERBOSE) log(INFO,"processReceivedData(): Authentication failure: "+user+'/'+passwd);
							telnet.println("\nAuthentication failure\n");
							if (--auth_attempts>0) {
								authenticate();
								continue;
							}
							else {
								user=null;
								telnet.close();
								continue;
							}
						}
					}
					// else
					history.add(line);
					history_index=history.size()-1;
					
					processCommand(command,args);
					
					telnet.println("");
					telnet.print(prompt);
				}
				else {
					telnet.print(prompt);
				}
			}		
		}
	}
	
	
	/** Processes a command of the cli.
	 * @param command the command name
	 * @param args arguments */
	protected void processCommand(String command, String[] args) {
		if (command.equals("exit")) {
			telnet.close();
		}
		else 
		if (ENABLE_HALT && command.equals("halt")) {
			telnet.close();
			SystemUtils.exitAfter(1000);
		}
		else 
		if (command.equals("passwd")) {
			exec=new Passwd(passwd_db,user);
			exec.run(ip_stack,telnet.getPrintStream(),args);
		}
		else {
			exec=null;
			try {
				Class<Program> command_class=(Class<Program>)Class.forName("it.unipr.netsec.nemo.program."+(char)(command.charAt(0)-'a'+'A')+command.substring(1));
				//Constructor<Executable> command_constructor=command_class.getConstructor(new Class[] { IpStack.class, PrintStream.class });
				//exec=command_constructor.newInstance(ip_stack,telnet.getPrintStream());
				exec=command_class.getConstructor().newInstance();
			}
			catch (Exception e) {
				telnet.println("Command '"+command+"' not found.");
			}
			if (exec!=null) try {
				exec.run(ip_stack,telnet.getPrintStream(),args);
			}
			catch (Exception e) {
				e.printStackTrace(telnet.getPrintStream());
				telnet.println(command+": error: "+e.getMessage());
			}
		}		
	}
	
	/** Properly mangles the given string.
	 * <ul>
	 * <li>Removes text control characters like del etc.</li>
	 * <ul>
	 * @param str the string to mangle
	 * @return the new string */
	private static String terminalBeautify(String str) {
		StringBuffer sb=new StringBuffer();
		for (int i=0; i<str.length(); i++) {
			char c=str.charAt(i);
			if ((c>=0x20 && c<0x7e) || c==0x09 || c==0x0a || c==0x0d) sb.append(c);
			else
			switch (c) {
				case 0x08 : if (sb.length()>0) sb.deleteCharAt(sb.length()-1); break;
				case 0x7f : if (sb.length()>0) sb.deleteCharAt(sb.length()-1); break;
			}
		}
		return sb.toString();
	}


	@Override
	public String toString() {
		String local=telnet!=null? telnet.getLocalEndpoint() : null;
		String remote=telnet!=null? telnet.getRemoteEndpoint() : null;
		return TelnetServerSession.class.getSimpleName()+'['+local+"]["+remote+']';
	}

}
