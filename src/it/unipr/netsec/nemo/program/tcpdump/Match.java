package it.unipr.netsec.nemo.program.tcpdump;


import java.util.ArrayList;

import it.unipr.netsec.ipstack.net.Packet;
import it.unipr.netsec.nemo.util.expression.BooleanExpression;


@FunctionalInterface
public interface Match extends BooleanExpression<ArrayList<Packet<?>>> {

	public boolean getValue(ArrayList<Packet<?>> pp);

}
