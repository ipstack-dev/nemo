package it.unipr.netsec.nemo.link;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

import org.zoolu.util.json.JsonUtils;

import it.unipr.netsec.ipstack.link.Link;
import it.unipr.netsec.ipstack.net.Node;


/** Generic network of nodes interconnected by links.
 */
public class Network<N extends Node<?,?>, L extends Link<?,?>> {

	/** All nodes. */
	private HashMap<String,N> nodes=new HashMap<>();
	
	/**All links. */
	HashMap<String,L> links=new HashMap<>();
	
	
	

	/**Creates an empty network. */
	public Network() {
	}

	
	/** Destroys the network. */
	public void clear() {
		for (N node: nodes.values()) node.close();
		nodes.clear();
		links.clear();		
	}
	
	/** Re-builds the network. */
	public void rebuild() {
		rebuild(null);
	}
	
	/** Re-builds the network.
	 * @param cfg configuration string */
	public void rebuild(String cfg) {
		// to be implemented by sub-classes
	}

	/** Gets a given node.
	 * @param name node name
	 * @return the node */
	public N getNode(String name) {
		return nodes.get(name);
	}
	
	/** Gets all nodes.
	 * @return all nodes */
	public Collection<N> getNodes() {
		return nodes.values();
	}
	
	/** Gets node names.
	 * @return all node names */
	public Set<String> getNodeNames() {
		return nodes.keySet();
	}
	
	/** Creates and adds a new node. */
	public synchronized void addNode(N node) {
		String name=node.getName();
		if (nodes.containsKey(name)) throw new RuntimeException("Node already exists: "+name);
		nodes.put(name,node);
	}
	
	/** Removes a node. */
	public synchronized void removeNode(N node) {
		String name=node.getName();
		if (nodes.containsKey(name)) nodes.remove(name);
	}

	/** Removes a node. */
	public synchronized void removeNode(String name) {
		if (nodes.containsKey(name)) removeNode(nodes.get(name));	
	}
	
	
	public synchronized L getLink(String name) {
		return links.get(name);
	}

	public synchronized Collection<L> getLinks() {
		return links.values();
	}

	public synchronized String getLinkName(L link) {
		for (String name : links.keySet()) {
			if (links.get(name)==link) return name;
		}
		return null;
	}

	public synchronized Set<String> getLinkNames() {
		return links.keySet();
	}
	
	public synchronized void addLink(String name, L link) {
		if (!links.containsKey(name)) links.put(name,link);
	}

	/** Removes unused links.*/
	public synchronized void prune() {
		ArrayList<String> toPurge=new ArrayList<>();
		for (String name : links.keySet()) if (links.get(name).numberOfInterfaces()==0) toPurge.add(name); 
		for (String name : toPurge) links.remove(name);
	}
	
	@Override
	public String toString() {
		StringBuffer sb=new StringBuffer();
		sb.append('{');
		sb.append("\"nodes\":");
		sb.append(JsonUtils.toJson(nodes.keySet().toArray()));
		sb.append(',');
		sb.append("\"links\":");		
		sb.append(JsonUtils.toJson(links.keySet().toArray()));
		sb.append('}');
		return sb.toString();
	}


}
