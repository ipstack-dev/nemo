package it.unipr.netsec.nemo.telnet;



public interface TelnetListener {

	/** When new data is received.
	 * @param telnet the TELNET session 
	 * @param buf buffer containing the received data
	 * @param off offset within the buffer
	 * @param len data length */
	public void onReceivedData(Telnet telnet, byte[] buf, int off, int len);

	/** When new command is received.
	 * @param telnet the TELNET session 
	 * @param command the command code
	 * @param option the command option code (-1 if no option is present) */
	public void onReceivedCommand(Telnet telnet, int command, int option);
	
	
	/** When new subnegotiation is received.
	 * @param telnet the TELNET session 
	 * @param option the option code
	 * @param param the option parameter */
	public void onReceivedSubnegotiation(Telnet telnet, int option, byte[] param);

	/** When the connection has been closed.
	 * @param telnet the TELNET session */
	public void onClosed(Telnet telnet);

}
