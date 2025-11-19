package io.ipstack.net.ip4;



/** Receiver for all incoming IP packets.
 */
@FunctionalInterface
public interface Ip4NodeListener {

	/** When a new packet is received for this node.
	 * @param ip_node the IP node
	 * @param ip_pkt the received packet */
	public void onIncomingPacket(Ip4Node ip_node, Ip4Packet ip_pkt);

}
