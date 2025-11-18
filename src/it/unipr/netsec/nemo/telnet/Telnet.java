package it.unipr.netsec.nemo.telnet;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

import org.zoolu.util.LoggerLevel;
import org.zoolu.util.SystemUtils;

import it.unipr.netsec.ipstack.ip4.SocketAddress;
import it.unipr.netsec.ipstack.socket.Socket;
import it.unipr.netsec.ipstack.tcp.TcpLayer;


/** A TELNET session.
 */
public class Telnet {
	
	public static boolean VERBOSE=false;
	
	private void log(String str) {
		//SystemUtils.log(LoggerLevel.INFO,this.getClass(),str);
		SystemUtils.log(LoggerLevel.INFO,null,toString()+": "+str);
	}
	
	
	public static final int NUL=0; // NO OP
	public static final int BEL=7; // Ring "terminal bell"
	public static final int BS=8; // Backspace (move cursor left)
	public static final int HT=9; // Horizontal tab (move cursor right)
	public static final int LF=10; // Line feed (LF)
	public static final int VT=11; // Vertical tab (move cursor down)
	public static final int FF=12; // Form Feed (FF)
	public static final int CR=13; // Carriage return (CR)
	
	public static final int IAC=255; // Interpret As Command (IAC)
	
	public static final int CMD_SE=240; // End of subnegotiation parameters
	public static final int CMD_NOP=241; // No operation
	public static final int CMD_Data_Mark=242; // The data stream portion of a Synch.
	public static final int CMD_Break=243; // NVT character BRK.
	public static final int CMD_Interrupt_Process=244; // The function IP.
	public static final int CMD_Abort_Output=245; // The function AO.
	public static final int CMD_Are_You_There=246; // The function AYT.
	public static final int CMD_Erase_Character=247; // The function EC.
	public static final int CMD_Erase_Line=248; // The function EL.
	public static final int CMD_Go_Ahead=249; // The GA signal.
	public static final int CMD_SB=250; // Indicates that what follows is sub-negotiation of the indicated option.
	
	public static final int CMD_WILL=251; // Indicates the desire to begin performing, or confirmation that you are now performing, the indicated option.
	public static final int CMD_WONT=252; // Indicates the refusal to perform, or continue performing, the indicated option.
	public static final int CMD_DO=253; // Indicates the request that the other party perform, or confirmation that you are expecting the other party to perform, the indicated option.
	public static final int CMD_DONT=254; // Indicates the demand that the other party stop performing, or confirmation that you are no longer expecting the other party to perform, the indicated option.	

	public static final int OPT_BT=0; // Binary Transmission [RFC856]
	public static final int OPT_Echo=1; // Echo [RFC857]
	public static final int OPT_Reconnection=2; // Reconnection
	public static final int OPT_Suppress_Go_Ahead=3; // Suppress Go Ahead [RFC858]
	public static final int OPT_Approx_Message_Size_Negotiation=4; // Approx Message Size Negotiation
	public static final int OPT_Status=5; // Status [RFC859]
	public static final int OPT_Timing_Mark=6; // Timing Mark [RFC860]
	public static final int OPT_Remote_Controlled_Trans_And_Echo =7; // Remote Controlled Trans and Echo [RFC726]
	public static final int OPT_Output_Line_Width=8; // Output Line Width
	public static final int OPT_Output_Page_Size=9; // Output Page Size
	public static final int OPT_Output_CR_Disposition=10; // Output Carriage-Return Disposition [RFC652]
	public static final int OPT_Output_Horizontal_Tab_Stops=11; // Output Horizontal Tab Stops [RFC653]
	public static final int OPT_Output_Horizontal_Tab_Disposition=12; // Output Horizontal Tab Disposition [RFC654]
	public static final int OPT_Output_Formfeed_Disposition=13; // Output Formfeed Disposition [RFC655]
	public static final int OPT_Output_Vertical_Tabstops=14; // Output Vertical Tabstops [RFC656]
	public static final int OPT_OutputVertical_Tab_Disposition=15; // Output Vertical Tab Disposition [RFC657]
	public static final int OPT_Output_LF_Disposition=16; // Output Linefeed Disposition [RFC658]
	public static final int OPT_Extended_ASCII=17; // Extended ASCII [RFC698]
	public static final int OPT_Logout=18; // Logout [RFC727]
	public static final int OPT_Byte_Macro=19; // Byte Macro [RFC735]
	public static final int OPT_Data_Entry_Terminal=20; // Data Entry Terminal [RFC1043][RFC732]
	public static final int OPT_SUPDUP=21; // SUPDUP [RFC736][RFC734]
	public static final int OPT_SUPDUP_Output=22; // SUPDUP Output [RFC749]
	public static final int OPT_Send_Location=23; // Send Location [RFC779]
	public static final int OPT_Terminal_Type=24; // Terminal Type [RFC1091]
	public static final int OPT_End_Of_Record=25; // End of Record [RFC885]
	public static final int OPT_TACACS_User_Identification=26; // TACACS User Identification [RFC927]
	public static final int OPT_Output_Marking=27; // Output Marking [RFC933]
	public static final int OPT_Terminal_Location_Number=28; // Terminal Location Number [RFC946]
	public static final int OPT_Telnet_3270_Regime=29; // Telnet 3270 Regime [RFC1041]
	public static final int OPT_X3_PAD=30; // X.3 PAD [RFC1053]
	public static final int OPT_Negotiate_About_Window_Size=31; // Negotiate About Window Size [RFC1073]
	public static final int OPT_Terminal_Speed=32; // Terminal Speed [RFC1079]
	public static final int OPT_Remote_Flow_Control=33; // Remote Flow Control [RFC1372]
	public static final int OPT_Linemode=34; // Linemode [RFC1184]
	public static final int OPT_X_Display_Location=35; // X Display Location [RFC1096]
	public static final int OPT_Environment=36; // Environment Option [RFC1408]
	public static final int OPT_Authentication=37; // Authentication Option [RFC2941]
	public static final int OPT_Encryption=38; // Encryption Option [RFC2946]
	public static final int OPT_New_Environment=39; // New Environment Option [RFC1572]
	public static final int OPT_TN3270E=40; // TN3270E [RFC2355]
	public static final int OPT_XAUTH=41; // XAUTH
	public static final int OPT_CHARSET=42; // CHARSET [RFC2066]
	public static final int OPT_Telnet_RSP=43; // Telnet Remote Serial Port (RSP)
	public static final int OPT_Com_Port_Control=44; // Com Port Control Option [RFC2217]
	public static final int OPT_Suppress_Local_Echo=45; // Telnet Suppress Local Echo
	public static final int OPT_Start_TLS=46; // Telnet Start TLS
	public static final int OPT_KERMIT=47; // KERMIT [RFC2840]
	public static final int OPT_SEND_URL=48; // SEND-URL
	public static final int OPT_FORWARD_X=49; // FORWARD_X
	public static final int OPT_TELOPT_PRAGMA_LOGON=138; // TELOPT PRAGMA LOGON
	public static final int OPT_TELOPT_SSPI_LOGON=139; // TELOPT SSPI LOGON
	public static final int OPT_TELOPT_PRAGMA_HEARTBEAT=140; // TELOPT PRAGMA HEARTBEAT
	public static final int OPT_Extended_Options_List=255; // Extended-Options-List [RFC861]
	
	static int BUFFER_SIZE=100;
	
	InputStream in;
	OutputStream out=null;
	OutputStream beautified_out=null;
	TelnetListener listener;
	
	byte[] buffer=new byte[BUFFER_SIZE];
	int pos=0;
	boolean running=true;
	//it.unipr.netsec.ipstack.tcp.Socket ipstack_socket=null;
	//java.net.Socket javanet_socket=null;
	Socket socket=null;
	
	
	/** Creates a new TELNET session.
	 * @param socket TCP socket
	 * @param listener listener of this TELNET session */
	public Telnet(Socket socket, TelnetListener listener) throws IOException {
		this(socket.getInputStream(),socket.getOutputStream(),listener);
		this.socket=socket;
	}

	
	/** Creates a new TELNET session.
	 * @param in input stream
	 * @param out output stream
	 * @param listener listener of this TELNET session */
	protected Telnet(final InputStream in, final OutputStream out, TelnetListener listener) {
		this.in=in;
		this.out=out;
		beautified_out=new BeautifiedOutputStream(out);
		this.listener=listener;
		new Thread(new Runnable() {
			@Override
			public void run() {
				Telnet.this.run(in,out);
			}
		}).start();
	}

	
	public void send(byte[] data) {
		send(data,0,data.length);
	}

	
	public void send(byte[] buf, int off, int len) {
		if (out!=null) {
			synchronized (out) {
				try {
					out.write(buf,off,len);
					out.flush();
				}
				catch (IOException e) {
					e.printStackTrace();
					out=null;
				}				
			}
		}
	}

	
	public void print(String str) {
		if (out!=null) {
			synchronized (out) {
				try {
					beautified_out.write(str.getBytes());
					beautified_out.flush();
				}
				catch (IOException e) {
					e.printStackTrace();
					out=null;
				}				
			}
		}
	}

	
	public void println(String str) {
		print(str+"\r\n");
	}

	
	public PrintStream getPrintStream() {
		return new PrintStream(beautified_out) {
			@Override
			public void print(String str) {
				synchronized (out) {
					super.print(str);
				}
			}
			@Override
			public void println(String str) {
				try {
					synchronized (out) {
						super.println(str);
					}										
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
	}	

	
	private void run(InputStream in, OutputStream out) {
		if (VERBOSE) log("run(): Connection started");
		try {
			while(running) {
				do {
					int c=in.read();
					if (c==IAC) {
						c=in.read();
						if (c==IAC) buffer[pos++]=(byte)c;
						else
						if (c==CMD_SB) {
							int opt=in.read();
							int b;
							ArrayList<Integer> param_buf=new ArrayList<>();
							while ((b=in.read())!=IAC) param_buf.add(b);
							if (in.read()!=CMD_SE) continue; // if IAC+SE is not found, discard everything
							if (listener!=null) {
								byte[] param=new byte[param_buf.size()];
								for (int i=0; i<param.length; i++) param[i]=(byte)param_buf.get(i).intValue();
								listener.onReceivedSubnegotiation(this,opt,param);
							}
						}
						else {
							int opt=-1;
							if (c>=CMD_WILL && c<=CMD_DONT) opt=in.read();
							if (listener!=null) listener.onReceivedCommand(this,c,opt);
						}
					}
					else {
						buffer[pos++]=(byte)c;
					}					
				} while (in.available()>0 && pos<buffer.length);
				if (pos>0) {
					if (listener!=null) listener.onReceivedData(Telnet.this,buffer,0,pos);
					pos=0;	
				}
			}
		}
		catch (IOException e) {
			if (VERBOSE) log("run(): "+e.getMessage());
			close();
		}
	}
	
	
	public void close() {
		if (running) {
			if (VERBOSE) log("close()");
			running=false;
			try {
				if (in!=null) {
					in.close();
					in=null;
				}
				if (out!=null) {
					out.close();
					out=null;
				}
				if (socket!=null) socket.close();
				if (listener!=null) listener.onClosed(this);
			}
			catch (IOException e) {
				e.printStackTrace();
			}			
		}
	}
	
	
	/** Gets the local endpoint.
	 * @return the port or socket address of the local endpoint */
	public String getLocalEndpoint() {
		if (socket!=null) {
			TcpLayer tcp_layer=socket.getTcpLayer();
			if (tcp_layer!=null) return tcp_layer.getIpLayer().getAddress().toString()+':'+socket.getLocalPort();
			else return String.valueOf(socket.getLocalPort());
		}
		return null;
	}
	

	/** Gets the remote endpoint.
	 * @return the socket address of the remote endpoint */
	public String getRemoteEndpoint() {
		if (socket!=null) return new SocketAddress(socket.getRemoteSocketAddress()).toString();
		return null;
	}
	

	@Override
	public String toString() {
		return getClass().getSimpleName()+'['+getLocalEndpoint()+"]["+getRemoteEndpoint()+']';
	}
	
}
