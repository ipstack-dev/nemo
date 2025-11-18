package test.nemo.mngnetwork.info;


import test.nemo.mngnetwork.UniqueID;


public class HyperLinkInfo {

	String id;
	String label;

	public HyperLinkInfo() {
	}

	public HyperLinkInfo(String id, String label) {
		this.id=id;
		this.label=label;
	}
	
	public HyperLinkInfo(String label) {
		this.id=UniqueID.getId(label);
		this.label=label;
	}
	
	/**
	 * @return the id */
	public String getId() {
		return id;
	}	
	
	/**
	 * @return the label */
	public String getLabel() {
		return label;
	}

}
