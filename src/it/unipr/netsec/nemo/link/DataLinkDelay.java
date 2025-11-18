package it.unipr.netsec.nemo.link;


import it.unipr.netsec.ipstack.net.Address;
import it.unipr.netsec.ipstack.net.Packet;


/** Single method interface for getting the packet delay.
 * <p>
 * The delay must not include the transmission delay that should be already taken into account by the output interface.
 */
@FunctionalInterface
public interface DataLinkDelay<A extends Address, P extends Packet<A>> {

	/** Gets packet delay.
	 * @param pkt the packet
	 * @return packet delay in nanoseconds */
	public long getPacketDelay(P pkt);
}
