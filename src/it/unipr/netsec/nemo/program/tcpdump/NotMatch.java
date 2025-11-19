package it.unipr.netsec.nemo.program.tcpdump;


import java.util.ArrayList;

import io.ipstack.net.packet.Packet;
import it.unipr.netsec.nemo.util.expression.Not;


public class NotMatch extends Not<ArrayList<Packet<?>>> implements Match {
	
	public NotMatch(Match expression) {
		super(expression);
	}

}
