package io.ipstack.net.socket;


final class DefaultSocketFactory {
	private DefaultSocketFactory() {}

	private static SocketFactory DEFAULT_FACTORY= null;
	
	
	public static SocketFactory getFactory() {
		if (DEFAULT_FACTORY==null) DEFAULT_FACTORY= JavanetSocketFactory.getInstance();
		return DEFAULT_FACTORY;
	}

	public static void setFactory(SocketFactory factory) {
		DEFAULT_FACTORY= factory;
	}

}
