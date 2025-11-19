/*
 * Copyright 2018 NetSec Lab - University of Parma
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Author(s):
 * Luca Veltri (luca.veltri@unipr.it)
 */


package io.ipstack.net.packet;


/** It listens for incoming packet.
 * @param <A> the address type
 * @param <P> the packet type
 */
@FunctionalInterface
public interface LayerListener<A extends Address, P extends Packet<A>> {

	/** When a new incoming packet is received.
	 * @param layer the layer that received the packet
	 * @param pkt the packet */
	public void onIncomingPacket(Layer<A,P> layer, P pkt);
	
}
