package io.ipstack.net.nat;


import java.util.HashMap;
import java.util.HashSet;

import org.zoolu.util.Clock;

import io.ipstack.net.ip4.SocketAddress;


/** NAT table.
 */
public class NatTable {

	/** Entry timeout in seconds */
	public static long TIMEOUT=300; // 5min

	/** Refresh timeout in seconds */
	public static long REFRESH_TIMEOUT=1200; // 20min

	
	/** NAT table. The remote socket address is used as key */
	private HashMap<SocketAddress,HashSet<NatTableEntry>> table=new HashMap<>();

	/** Last refresh time */
	private long last_refresh;

	
	/** Creates a new table. */
	public NatTable() {
		last_refresh=Clock.getDefaultClock().currentTimeMillis();
	}
	
	/** Whether it contains an entry specified through addresses of outgoing packets (received from the internal network).
	 * @param src_soaddr internal source socket address
	 * @param dst_soaddr destination socket address
	 * @return the entry, if present */
	public synchronized boolean containFromInternalAddresses(SocketAddress src_soaddr, SocketAddress dst_soaddr) {
		return getFromInternalAddresses(src_soaddr,dst_soaddr)==null;
	}

	/** Gets an entry from an outgoing packet (received from the internal network).
	 * @param src_soaddr internal source socket address
	 * @param dst_soaddr destination socket address
	 * @return the entry, if present */
	public synchronized NatTableEntry getFromInternalAddresses(SocketAddress src_soaddr, SocketAddress dst_soaddr) {
		refresh();
		HashSet<NatTableEntry> entries=table.get(dst_soaddr);
		if (entries!=null) {
			for (NatTableEntry e : entries) if (src_soaddr.equals(e.getIntSocketAddress()) && isValid(e)) return e;
		}
		return null;
	}

	/** Whether it contains an entry specified through addresses of incoming packets (received from the external network).
	 * @param src_soaddr external source socket address
	 * @param dst_soaddr destination socket address
	 * @return the entry, if present */
	public synchronized boolean containFromExternalAddresses(SocketAddress src_soaddr, SocketAddress dst_soaddr) {
		return getFromExternalAddresses(src_soaddr,dst_soaddr)==null;
	}
	
	/** Gets an entry from an incoming packet (received from the external network).
	 * @param src_soaddr external source socket address
	 * @param dst_soaddr destination socket address
	 * @return the entry, if present */
	public synchronized NatTableEntry getFromExternalAddresses(SocketAddress src_soaddr, SocketAddress dst_soaddr) {
		refresh();
		HashSet<NatTableEntry> entries=table.get(src_soaddr);
		if (entries!=null) {
			for (NatTableEntry e : entries) if (dst_soaddr.equals(e.getExtSocketAddress()) && isValid(e)) return e;
		}
		return null;
	}
	
	/** Adds an entry.
	 * @param e the new entry */
	public synchronized void add(NatTableEntry e) {
		refresh();
		if (e==null) return;
		SocketAddress remote_soaddr=e.getExtRemoteSocketAddress();
		HashSet<NatTableEntry> entries=table.get(remote_soaddr);
		if (entries==null) {
			entries=new HashSet<>();
			table.put(remote_soaddr,entries);
		}
		entries.add(e);
	}

	/** Removes an entry. */
	public synchronized void remove(NatTableEntry e) {
		refresh();
		if (e==null) return;
		SocketAddress remote_soaddr=e.getExtRemoteSocketAddress();
		HashSet<NatTableEntry> entries=table.get(remote_soaddr);
		if (entries!=null) entries.remove(e);
	}
	
	/** Whether the entry is not expired.
	 * @param e the entry
	 * @return true if the entry is still valid */
	private synchronized boolean isValid(NatTableEntry e) {
		long now=Clock.getDefaultClock().currentTimeMillis();
		if (now<e.getTime()+TIMEOUT*1000) {
			e.setTime(now);
			return true;
		}
		else {
			HashSet<NatTableEntry> entries=table.get(e.getExtRemoteSocketAddress());
			if (entries!=null) {
				entries.remove(e);
				if (entries.size()==0) table.remove(e.getExtRemoteSocketAddress());
			}
			return false;			
		}
	}
	
	/** Removes all expired entries. */
	private synchronized void refresh() {
		long now=Clock.getDefaultClock().currentTimeMillis();
		if (now>last_refresh+REFRESH_TIMEOUT*1000) {
			// clean
			for (SocketAddress key : table.keySet()) {
				HashSet<NatTableEntry> entries=table.get(key);
				for (NatTableEntry e : entries) {
					if (now>e.getTime()+TIMEOUT*1000) entries.remove(e);
				}
				if (entries.size()==0) table.remove(key);
			}
		}
	}


}
