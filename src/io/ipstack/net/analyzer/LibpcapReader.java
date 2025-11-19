package io.ipstack.net.analyzer;


import java.io.FileInputStream;
import java.io.IOException;


/** Libpcap file reader.
 */
public class LibpcapReader {

	/** Libpcap input file */
	FileInputStream in;

	LibpcapHeader ph=new LibpcapHeader();

	
	/** Create a new pcap reader.
	 * @param file_name the pcap file where packets are read from
	 * @throws IOException */
	public LibpcapReader(String file_name) throws IOException {
		in=new FileInputStream(file_name);
		ph.read(in);
	}

	
	/** Returns the pcap header.
	 * @return the header */
	public LibpcapHeader getHeader()  {
		return ph;
	}
	
	
	/** Whether there are more bytes to read.
	 * @return 'true' if more bytes are available 
	 * @throws IOException */
	public boolean hasMore() throws IOException  {
		return in.available()>0;
	}
	
	
	/** Reads next packet record.
	 * @return the packet record
	 * @throws IOException */
	public synchronized LibpcapRecord read() throws IOException {
		LibpcapRecord pr=new LibpcapRecord(ph.getLinkType());
		pr.read(in);
		return pr;
	}
	
	
	/** Reads next packet record.
	 * @param pr the packet record
	 * @throws IOException */
	public synchronized void read(LibpcapRecord pr) throws IOException {
		if (pr.getType()!=ph.getLinkType()) throw new RuntimeException("Incompatible record type ("+pr.getType()+"<>"+ph.getLinkType()+")");
		pr.read(in);
	}
	
	
	/** Stops capturing and closes the file. */
	public synchronized void close() {
		if (in!=null) {
			try { in.close(); } catch (IOException e) {}
			finally { in=null; }
		}
	}
	
}
