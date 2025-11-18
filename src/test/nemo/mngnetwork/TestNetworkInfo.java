package test.nemo.mngnetwork;



public class TestNetworkInfo {

	public String loopback_addr_prefix=TestNetwork.LOOPBACK_ADDR_PREFIX;
	public int num_nets=TestNetwork.DEFAULT_NUM_OF_NETS; // r1Num - number of first level routers
	public int num_servers=TestNetwork.DEFAULT_NUM_OF_SERVERS; // h2Num - number of hosts behind the first level router
	public int num_access_nets=TestNetwork.DEFAULT_NUM_OF_ACCESS_NETS; // r2Num - number of second level routers for each first level router
	public int num_hosts=TestNetwork.DEFAULT_NUM_OF_HOSTS; // h3Num - number of attached hosts for each second level router
	public boolean ni_auto_conf=true;
	public boolean addr_auto_conf=true;
	public boolean route_auto_conf=true;
	
}
