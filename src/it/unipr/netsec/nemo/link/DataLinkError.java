package it.unipr.netsec.nemo.link;


import io.ipstack.net.packet.Address;
import io.ipstack.net.packet.Packet;


/** Single method interface for getting the packet delay.
 * <p>
 * The delay must not include the transmission delay that should be already taken into account by the output interface.
 */
@FunctionalInterface
public interface DataLinkError<A extends Address, P extends Packet<A>> {

	/** Gets packet error.
	 * @param pkt the original packet
	 * @return the possibly modified packet, or <i>null</i> in case of loss */
	public P getPacketError(P pkt);
}
