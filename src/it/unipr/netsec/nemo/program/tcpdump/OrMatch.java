package it.unipr.netsec.nemo.program.tcpdump;


import java.util.ArrayList;
import java.util.List;

import io.ipstack.net.packet.Packet;
import it.unipr.netsec.nemo.util.expression.Or;


public class OrMatch extends Or<ArrayList<Packet<?>>> implements Match {
	
	public OrMatch() {
		super();
	}

	public OrMatch(Match... list) {
		super(list);
	}

	public OrMatch(List<Match> list) {
		super(list.toArray(new Match[0]));
	}

}
