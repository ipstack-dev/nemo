package test.nemo.mngnetwork.info;


public class GraphEdgeInfo {
	
	String from;
	String to;
	String label;

	public GraphEdgeInfo(String from, String to) {
		this(from,to,null);
	}

	public GraphEdgeInfo(String from, String to, String label) {
		this.from=from;
		this.to=to;
		this.label=label;
	}

	/**
	 * @return the source node */
	public String getSource() {
		return from;
	}
	
	/**
	 * @return the target node */
	public String getTarget() {
		return to;
	}

	/**
	 * @return the node label */
	public String getLabel() {
		return label;
	}

}
