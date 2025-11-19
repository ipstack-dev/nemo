package io.ipstack.net.analyzer;


import static io.ipstack.net.analyzer.LibpcapHeader.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.zoolu.util.Bytes;
import org.zoolu.util.DateFormat;

import io.ipstack.net.ethernet.EthPacket;
import io.ipstack.net.ip4.Ip4Packet;
import io.ipstack.net.ip6.Ip6Packet;
import io.ipstack.net.packet.Packet;


/** Libpcap Packet Record.
 * <p>
 * It contains the packet timestamp (seconds and microseconds), the actual packet length, all packet octects or a portion of the packet.
 * <!--<p>
 * @see <a href="https://wiki.wireshark.org/Development/LibpcapFileFormat">wiki.wireshark.org/Development/LibpcapFileFormat</a> for details.
 * -->
 */
public class LibpcapRecord {

	
	/** Packet type */
	int type;

	/** Timestamp seconds [32bit] */
	long ts_sec=0;
	
	/** Timestamp microseconds [32bit] */
	long ts_usec=0;
	
	/** Number of octets of packet saved in file [32bit] */
	//int incl_len=0;
	
	/** Actual length of packet [32bit] */
	int orig_len=0;
	
	/** Packet data */
	byte[] data=null;

	
	
	/** Creates an empty Libpcap packet record.
	 * @param type packet type */
	public LibpcapRecord(int type) {
		this.type=type;
	}

	/** Creates a new Libpcap record.
	 * @param timestamp the timestamp, in milliseconds
	 * @param pkt the packet */
	/*public LibpcapRecord(long timestamp, Packet pkt) {
		ts_sec=timestamp/1000;
		ts_usec=(timestamp%1000)*1000;
		data=pkt.getBytes();
		orig_len=data.length;
	}*/
	
	/** Creates a new Libpcap record.
	 * @param ts_sec timestamp seconds
	 * @param ts_usec timestamp microseconds
	 * @param type packet type
	 * @param pkt the packet */
	public LibpcapRecord(long ts_sec, long ts_usec, int type, Packet<?> pkt) {
		this.ts_sec=ts_sec;
		this.ts_usec=ts_usec;
		this.type=type;
		data=pkt.getBytes();
		orig_len=data.length;
	}
	
	/** Gets timestamp seconds.
	 * @return the timestamp seconds */
	public long getTimestampSeconds() {
		return ts_sec;
	}
	
	/** Gets timestamp microseconds.
	 * @return the timestamp microseconds */
	public long getTimestampMicroseconds() {
		return ts_usec;
	}
	
	/** Gets packet type.
	 * @return the type */
	public int getType() {
		return type;
	}
	
	/** Gets original packet length.
	 * @return the length */
	public int getOriginalLength() {
		return orig_len;
	}
	
	/** Gets packet data.
	 * @return the byte array containing the packet */
	public byte[] getPacketData() {
		return data;
	}
	
	/** Gets packet.
	 * @return the packet */
	public Packet<?> getPacket() {
		if (data==null) return null;
		if (!hasOriginalPacket()) throw new RuntimeException("The original packet is not completely present");
		switch (type) {
		case LINKTYPE_ETHERNET : return EthPacket.parseEthPacket(data);
		case LINKTYPE_IPV4 : return Ip4Packet.parseIp4Packet(data);
		case LINKTYPE_IPV6 : return Ip6Packet.parseIp6Packet(data);
		default : throw new RuntimeException("Unable to create a packet of type "+type);
		}
	}
	
	/** Whether the record contains the entire original packet.
	 * @return 'true' if the entire packet is present */
	public boolean hasOriginalPacket() {
		return data!=null && data.length==orig_len;
	}

	/** Reads the packet record from an InputStream.
	 * @param is the InputStream where the packet record is read from
	 * @return the number of bytes that have been read */
	public synchronized int read(InputStream is) throws IOException {	
		ts_sec=readInt32(is);
		ts_usec=readInt32(is);
		int incl_len=(int)readInt32(is);
		orig_len=(int)readInt32(is);
		data=new byte[incl_len];
		int len=is.read(data);
		if (len!=incl_len) throw new IOException("Too few bytes availables ("+len+"<"+incl_len+")");
		return 16+len;
	}
	
	/** Writes the packet record to an OutputStream.
	 * @param os the OutputStream where the packet record is written to
	 * @return the number of bytes that have been written */
	public int write(OutputStream os) throws IOException {	
		writeInt32(os,ts_sec);
		writeInt32(os,ts_usec);
		writeInt32(os,data.length);
		writeInt32(os,orig_len);
		os.write(data);
		return 16+data.length;
	}

	@Override
	public String toString() {	
		Packet<?> pkt=getPacket();
		String microsecs=String.valueOf(ts_usec);
		while (microsecs.length()<6) microsecs+='0';		
		return String.valueOf(ts_sec)+'.'+microsecs+" "+ProtocolAnalyzer.exploreInner(pkt);

	}

	
	private static byte[] INT32_BUFFER=new byte[4];
	
	private synchronized long readInt32(InputStream is) throws IOException {
		int len=is.read(INT32_BUFFER);
		if (len!=4) throw new IOException("Too few bytes availables ("+len+")");
		return Bytes.toInt32LittleEndian(INT32_BUFFER);
	}

	private synchronized void writeInt32(OutputStream os, long n) throws IOException {
		Bytes.fromInt32LittleEndian(n,INT32_BUFFER,0);
		os.write(INT32_BUFFER);
	}


}
