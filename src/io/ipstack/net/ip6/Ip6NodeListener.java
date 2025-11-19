package io.ipstack.net.ip6;



/** Receiver for all incoming IP packets.
 */
@FunctionalInterface
public interface Ip6NodeListener {

	/** When a new packet is received for this node.
	 * @param ip_node the IP node
	 * @param ip_pkt the received packet */
	public void onIncomingPacket(Ip6Node ip_node, Ip6Packet ip_pkt);

}
