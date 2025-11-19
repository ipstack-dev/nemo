package it.unipr.netsec.nemo.program;


import java.io.PrintStream;

import io.ipstack.net.stack.IpStack;


/** A program that can be executed with arguments and be stopped.
 */
public interface Program {

	/** Executes the program.
	 * @param ip_stack the IP stack
	 * @param out the output
	 * @param args program arguments */
	public void run(IpStack ip_stack, PrintStream out, String[] args);
	
	/** Processes input data.
	 * @param buf buffer containing the input data
	 * @param off offset within the buffer
	 * @param len data length
	 * @return whether the data have been processed */
	public boolean processInputData(byte[] buf, int off, int len);
	
	/** Whether it is running.
	 * @return <i>true</i> if it is running */
	public boolean isRunning();

	/** Stops the program. */
	public void halt();
}
