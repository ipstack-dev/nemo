package io.ipstack.net.analyzer;


import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.zoolu.util.Clock;

import io.ipstack.net.packet.Packet;


/** Libpcap file writer.
 * Packets added to a LibpcapWriter are written to a file using standard libpcap format.
 */
public class LibpcapWriter {

	/** Libpcap output file */
	FileOutputStream out;

	/** start time in milliseconds */
	long start_millisecs=0;

	/** start time in nanoseconds */
	long start_nanosecs=0;
	
	/** Packet type */
	int type;

	
	/** Creates a new pcap writer.
	 * @param type the interface type (see types in class {@link io.ipstack.net.analyzer.LibpcapHeader LibpcapHeader})
	 * @param file_name the pcap file where packets will be written
	 * @throws IOException */
	public LibpcapWriter(int type, String file_name) throws IOException {
		this.type=type;
		out=new FileOutputStream(file_name);
		LibpcapHeader ph=new LibpcapHeader(type);
		ph.write(out);
	}

	
	/** Writes a new packet.
	 * @param pkt the packet to be written */
	public synchronized void write(Packet<?> pkt) {
		long now_nanosecs=Clock.getDefaultClock().nanoTime();
		if (start_nanosecs<=0) start_nanosecs=now_nanosecs;
		long t_usecs=((now_nanosecs-start_nanosecs)+start_millisecs*1000000)/1000;
		try {
			new LibpcapRecord(t_usecs/1000000,t_usecs%1000000,type,pkt).write(out);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	/** Closes the file. */
	public synchronized void close() {
		if (out!=null) {
			try { out.close(); } catch (IOException e) {}
			finally { out=null; }
		}
	}
	
}
