package io.ipstack.net.nat;


import org.zoolu.util.Clock;

import io.ipstack.net.ip4.SocketAddress;


/** An entry of a NAT table.
 * <br>
 * It includes the three socket addresses:
 * <ul>
 * <li>internal host socket address</li>
 * <li>external NAT socket address</li>
 * <li>external remote socket address</li>
 * </ul>
 */
public class NatTableEntry {

	private SocketAddress int_soaddr, ext_soaddr, ext_remote_soaddr;
	private long time;
	
	
	/** Creates a new entry.
	 * @param int_soaddr internal socket address that has to masquerade
	 * @param ext_soaddr external socket address that the it has to masquerade as
	 * @param ext_remote_soaddr external remote socket address */
	public NatTableEntry(SocketAddress int_soaddr, SocketAddress ext_soaddr, SocketAddress ext_remote_soaddr) {
		this.int_soaddr=int_soaddr;
		this.ext_soaddr=ext_soaddr;
		this.ext_remote_soaddr=ext_remote_soaddr;
		time=Clock.getDefaultClock().currentTimeMillis();
	}
	
	/** Gets the internal socket address
	 * @return the socket address */
	public SocketAddress getIntSocketAddress() {
		return int_soaddr;
	}
	
	/** Gets the external socket address
	 * @return the socket address */
	public SocketAddress getExtSocketAddress() {
		return ext_soaddr;
	}

	/** Gets the external remote socket address
	 * @return the socket address */
	public SocketAddress getExtRemoteSocketAddress() {
		return ext_remote_soaddr;
	}
	
	/** Gets entry time.
	 * @return the time */
	public long getTime() {
		return time;
	}
	
	/** Sets entry time.
	 * @param time the new time */
	public void setTime(long time) {
		this.time=time;
	}
	
	@Override
	public String toString() {
		return int_soaddr+","+ext_soaddr+","+ext_remote_soaddr;
	}

}
