package it.unipr.netsec.nemo.program.tcpdump;


import java.util.ArrayList;
import java.util.List;

import io.ipstack.net.packet.Packet;
import it.unipr.netsec.nemo.util.expression.And;


public class AndMatch extends And<ArrayList<Packet<?>>> implements Match {
	
	public AndMatch() {
		super();
	}

	public AndMatch(Match... list) {
		super(list);
	}

	public AndMatch(List<Match> list) {
		super(list.toArray(new Match[0]));
	}

}
