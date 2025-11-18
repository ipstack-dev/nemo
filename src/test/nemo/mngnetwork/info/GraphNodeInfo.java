package test.nemo.mngnetwork.info;



public class GraphNodeInfo {
		
	String id;
	String type;
	String label;

	public GraphNodeInfo(String id, String type, String label) {
		this.id=id;
		this.type=type;
		this.label=label;
	}

	/**
	 * @return the id */
	public String getId() {
		return id;
	}
	
	/**
	 * @return the node type */
	public String getType() {
		return type;
	}

	/**
	 * @return the node label */
	public String getLabel() {
		return label;
	}

}
