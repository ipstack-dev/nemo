package test.nemo.mngnetwork;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.zoolu.util.json.Json;

import io.ipstack.http.HttpRequest;
import io.ipstack.http.HttpRequestHandle;
import io.ipstack.http.HttpRequestURL;
import io.ipstack.http.HttpServer;
import io.ipstack.http.uri.AbsolutePath;
import io.ipstack.http.uri.Parameter;
import io.ipstack.http.uri.Query;
import io.ipstack.net.icmp4.PingClient;
import io.ipstack.net.ip4.Ip4Address;
import io.ipstack.net.ip4.Ip4Packet;
import io.ipstack.net.link.Link;
import io.ipstack.net.link.LinkInterface;
import io.ipstack.net.packet.NetInterface;
import io.ipstack.net.packet.Route;
import io.ipstack.net.packet.RoutingTable;
import io.ipstack.net.stack.Links;
import io.ipstack.net.util.IpAddressUtils;
import it.unipr.netsec.nemo.ip.Ip4Host;
import it.unipr.netsec.nemo.ip.Ip4Node;
import it.unipr.netsec.nemo.link.Network;
import it.unipr.netsec.nemo.program.Program;
import test.nemo.mngnetwork.info.CommandInfo;
import test.nemo.mngnetwork.info.ConfigNetworkInfo;
import test.nemo.mngnetwork.info.GraphInfo;
import test.nemo.mngnetwork.info.MemoryInfo;
import test.nemo.mngnetwork.info.NetInterfaceInfo;
import test.nemo.mngnetwork.info.HyperGraphInfo;
import test.nemo.mngnetwork.info.NodeInfo;
import test.nemo.mngnetwork.info.PingResultInfo;
import test.nemo.mngnetwork.info.RouteInfo;


/** HTTP server that allows a user to manage multiple nodes through a REST API.
 */
public class ManagementHttpServer {

	/** Name of the file that users can get via HTTP */
	public static String FILENAME=null;	
	
	/** HTTP server */
	HttpServer server;

	/**Managed network */
	Network<Ip4Node,Link<Ip4Address,Ip4Packet>> network;

	
	/** Creates a new server.
	 * @param port server port
	 * @param network the network */
	public ManagementHttpServer(int port, Network<Ip4Node,Link<Ip4Address,Ip4Packet>> network) throws IOException {
		this.network=network;
		server=new HttpServer(port,this::processHttpRequest);
	}
	
	
	private void processHttpRequest(HttpRequestHandle req_handle) {
		try {
			String method=req_handle.getMethod();
			HttpRequestURL request_url=req_handle.getRequestURL();
			if (request_url==null) {
				req_handle.setResponseCode(400);
				return;
			}
			if (method.equals(HttpRequest.OPTIONS) && processOption(request_url,req_handle)) return;
			// else
			String[] resource_path=new AbsolutePath(request_url.getAbsPath()).getPath();
			Parameter[] query_params=new Query(request_url.getQuery()).getQueryParameters();
			// else
			if (resource_path==null || resource_path.length==0) {
				req_handle.setResponseCode(404);
				return;
			}
			if (!(method.equals("DELETE") || method.equals("POST") || method.equals("GET"))) {
				req_handle.setResponseCode(405);
				return;
			}
			// else
			if (method.equals("DELETE") && processDelete(resource_path,query_params,req_handle)) return;
			// else
			if (method.equals("POST") && processPost(resource_path,query_params,req_handle)) return;
			// else
			if (method.equals("GET")) {
				if (resource_path[0].equals("network") && processGetNetwork(resource_path,query_params,req_handle)) return;
				// else
				if (resource_path[0].equals("node") && processGetNode(resource_path,query_params,req_handle)) return;
				// else
				if (resource_path[0].equals("halt") && resource_path.length==1) System.exit(0);
				// else
				if (resource_path[0].equals("memory") && processGetMemory(resource_path,query_params,req_handle)) return;
				// else
				if (resource_path[0].equals("file") && processGetFile(resource_path,query_params,req_handle)) return;
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			req_handle.setResponseCode(400);
			return;
		}
	}
	
	
	private boolean processOption(HttpRequestURL request_url, HttpRequestHandle req_handle) {
		req_handle.setResponseCode(200);
		req_handle.setResponseHeaderField("Allow",HttpRequest.GET+","+HttpRequest.POST+","+HttpRequest.PUT+","+HttpRequest.DELETE+","+HttpRequest.OPTIONS);
		return true;
	}

	
	private boolean processDelete(String[] resource_path, Parameter[] query_params, HttpRequestHandle req_handle) {
		if (resource_path.length==2 && resource_path[0].equals("node")) {
			String name=resource_path[1];
			System.out.println("DEBUG: processDelete(): "+name);
			Ip4Node node=network.getNode(name);
			if (node==null) {
				req_handle.setResponseCode(404);
				return true;				
			}
			// else
			network.removeNode(name);
			node.close();
			req_handle.setResponseCode(200);
			return true;
		}
		return false;
	}
	
	
	private boolean processPost(String[] resource_path, Parameter[] query_params, HttpRequestHandle req_handle) {
		if (resource_path.length==2 && resource_path[0].equals("network") && resource_path[1].equals("rebuild")) {
			try {
				byte[] body=req_handle.getRequest().getBody();
				String config=(body==null || body.length==0)? "{}": new String(body);
				System.out.println("DEBUG: ManagementHttpServer: processPost(): rebuild network: "+config);
				network.rebuild(config);
				req_handle.setResponseCode(200);
				return true;
			}
			catch (Exception e) {
				e.printStackTrace();
				req_handle.setResponseCode(400);
				return true;								
			}
		}
		else
		if (resource_path.length==2 && resource_path[0].equals("node") && resource_path[1].equals("add")) {
			try {
				String body=new String(req_handle.getRequest().getBody());
				System.out.println("DEBUG: processPost(): add node: "+body);
				NodeInfo nodeInfo=Json.fromJSON(body,NodeInfo.class);
				String[] links=nodeInfo.getLinks();
				String label=nodeInfo.getLabel();
				if (network.getNode(label)!=null) throw new RuntimeException("Node label already used");
				Ip4Host node=(links!=null && links.length>0)? new Ip4Host(new LinkInterface<Ip4Address,Ip4Packet>(Links.getLink(links[0])),null) : new Ip4Host();
				node.setName(label);
				if (links!=null && links.length>1) for (int i=1; i<links.length; i++) node.addNetInterface(new LinkInterface<Ip4Address,Ip4Packet>(Links.getLink(links[i])));
				network.addNode(node);
				req_handle.setResponseCode(200);
				req_handle.setResponseContentType("application/json");
				req_handle.setResponseBody(Json.toJSON(nodeInfo).getBytes());
				return true;
			}
			catch (Exception e) {
				e.printStackTrace();
				req_handle.setResponseCode(400);
				return true;								
			}
		}
		else
		if (resource_path.length==3 && resource_path[0].equals("node") && resource_path[2].equals("delete")) {
			try {
				String name=resource_path[1];
				System.out.println("DEBUG: processPost(): delete node: "+name);
				Ip4Node node=network.getNode(name);
				if (node==null) {
					req_handle.setResponseCode(404);
					return true;				
				}
				// else
				network.removeNode(name);
				node.close();
				req_handle.setResponseCode(200);
				return true;
			}
			catch (Exception e) {
				e.printStackTrace();
				req_handle.setResponseCode(400);
				return true;								
			}
		}
		else
		if (resource_path.length==3 && resource_path[0].equals("node") && resource_path[2].equals("cmd")) {
			try {
				String node_name=resource_path[1];
				String body=new String(req_handle.getRequest().getBody());
				System.out.println("DEBUG: processPost(): "+node_name+": cmd "+body);
				String[] cmd=Json.fromJSON(body,CommandInfo.class).getArgs();
				String command=cmd[0];
				if (!command.equals("ip") && !command.equals("curl") && !command.equals("netstat") && !command.equals("top")) {
					req_handle.setResponseCode(404);
					return true;	
				}
				Class<Program> command_class=(Class<Program>)Class.forName("it.unipr.netsec.nemo.program."+(char)(command.charAt(0)-'a'+'A')+command.substring(1));
				Program exec=command_class.getConstructor().newInstance();
				exec.run(network.getNode(node_name).getIpStack(),System.out,cmd);
				req_handle.setResponseCode(200);
				return true;
			}
			catch (Exception e) {
				e.printStackTrace();
				req_handle.setResponseCode(400);
				return true;
			}
		}
		else
		if (resource_path.length==2 && resource_path[0].equals("link") && resource_path[1].equals("prune")) {
			Links.prune();
			req_handle.setResponseCode(200);
			return true;	
		}
		return false;
	}
	
	
	/*private boolean processCmd(HttpRequestHandle req_handle, String node_name, String[] cmd) throws Exception {
		String command=cmd[0];
		if (!command.equals("ip") && !command.equals("curl") && !command.equals("netstat") && !command.equals("top")) {
			req_handle.setResponseCode(404);
			return true;	
		}
		Class<Program> command_class=(Class<Program>)Class.forName("it.unipr.netsec.nemo.program."+(char)(command.charAt(0)-'a'+'A')+command.substring(1));
		Program exec=command_class.getConstructor().newInstance();
		exec.run(network.getNode(node_name).getIpStack(),System.out,cmd);
		req_handle.setResponseCode(200);
		return true;	
	}*/
	
	
	private boolean processGetNetwork(String[] resource_path, Parameter[] query_params, HttpRequestHandle req_handle) {
		if (resource_path.length==2 && resource_path[1].equals("topology")) {
			req_handle.setResponseBody(Json.toJSON(new HyperGraphInfo(network.getNodes(),Arrays.asList(Links.getNames()))).getBytes());
			req_handle.setResponseContentType("application/json");
			req_handle.setResponseCode(200);
			return true;			
		}
		// else
		if (resource_path.length==2 && resource_path[1].equals("simple")) {
			req_handle.setResponseBody(Json.toJSON(new GraphInfo(network.getNodes(),Arrays.asList(Links.getNames()))).getBytes());
			req_handle.setResponseContentType("application/json");
			req_handle.setResponseCode(200);
			return true;								
		}
		// else
		if (resource_path.length==2 && resource_path[1].equals("config")) {
			req_handle.setResponseBody(Json.toJSON(new ConfigNetworkInfo(network.getNodes())).getBytes());
			req_handle.setResponseContentType("application/json");
			req_handle.setResponseCode(200);
			return true;								
		}
		return false;
	}

	
	private boolean processGetNode(String[] resource_path, Parameter[] query_params, HttpRequestHandle req_handle) {
		if (resource_path.length==1) {
			// list
			ArrayList<String> addrs=IpAddressUtils.sort(network.getNodeNames(),Ip4Address.class);
			String[] straddrs=new String[addrs.size()];
			for (int i=0; i<straddrs.length; i++) straddrs[i]=addrs.get(i);
			req_handle.setResponseBody(Json.toJSON(straddrs).getBytes());
			req_handle.setResponseContentType("application/json");
			req_handle.setResponseCode(200);
			return true;
		}
		// else
		String name=resource_path[1];
		Ip4Node node=network.getNode(name);
		if (node==null) {
			req_handle.setResponseCode(404);
			return true;
		}
		// else
		if (resource_path.length==2) {
			// show node
			req_handle.setResponseBody(Json.toJSON(new NodeInfo(node)).getBytes());
			req_handle.setResponseContentType("application/json");
			req_handle.setResponseCode(200);
			return true;
		}
		// else
		if (resource_path.length>=3) {
			if (resource_path[2].equals("ni") && resource_path.length==3) {
				// network interfaces
				ArrayList<NetInterfaceInfo> mng_interfaces=new ArrayList<>();
				for (NetInterface<Ip4Address,Ip4Packet> ni : node.getIpStack().getIp4Layer().getAllInterfaces()) mng_interfaces.add(new NetInterfaceInfo(ni));
				req_handle.setResponseBody(Json.toJSON(mng_interfaces.toArray(new NetInterfaceInfo[0])).getBytes());
				req_handle.setResponseContentType("application/json");
				req_handle.setResponseCode(200);
				return true;
			}
			if (resource_path[2].equals("rt") && resource_path.length==3) {
				// routing-table
				RoutingTable<Ip4Address,Ip4Packet> rt=node.getIpStack().getIp4Layer().getRoutingTable();
				ArrayList<RouteInfo> mng_routes=new ArrayList<>();
				for (Route<Ip4Address,Ip4Packet> r : rt.getAll()) mng_routes.add(new RouteInfo(r));
				req_handle.setResponseBody(Json.toJSON(mng_routes.toArray(new RouteInfo[0])).getBytes());
				req_handle.setResponseContentType("application/json");
				req_handle.setResponseCode(200);
				return true;
			}
			if (resource_path[2].equals("ping") && resource_path.length==4) {
				Ip4Address target=new Ip4Address(resource_path[3]);
				int req_count=1;
				long time=1000;
				long clearing_time=-1;
				if (query_params!=null) {
					for (Parameter param : query_params) {
						if (param.getName().equals("count")) req_count=Integer.parseInt(param.getValue());
						if (param.getName().equals("time")) time=Integer.parseInt(param.getValue());
						if (param.getName().equals("clearing-time")) clearing_time=Integer.parseInt(param.getValue());
					}
				}
				PingClient ping_client=new PingClient(node.getIpStack().getIp4Layer(),null);
				if (clearing_time>0) ping_client.setClearingTime(clearing_time);
				ping_client.ping(target,req_count,time);
				/*int reply_count=ping_client.getReplyCounter();
				int last_ttl=ping_client.getLastTTL();
				long total_time=ping_client.getTotalTime();
				String response=new JzonObject().add("req-count",req_count).add("reply-count",reply_count).add("last-ttl",last_ttl).add("total-time",total_time).toString();
				*/
				PingResultInfo result=new PingResultInfo();
				result.req_count=req_count;
				result.reply_count=ping_client.getReplyCounter();
				result.last_ttl=ping_client.getLastTTL();
				result.total_time=ping_client.getTotalTime();
				req_handle.setResponseContentType("application/json");
				req_handle.setResponseBody(Json.toJSON(result).getBytes());
				req_handle.setResponseCode(200);
				return true;
			}
		}
		return false;
	}

	
	private boolean processGetMemory(String[] resource_path, Parameter[] query_params, HttpRequestHandle req_handle) {
		if (resource_path.length==1) {
			MemoryInfo.Unit unit=MemoryInfo.Unit.MB;
			if (query_params!=null) {
				for (Parameter param : query_params) {
					if (param.getName().equals("unit")) {
						String value=param.getValue().toLowerCase();
						if (value.startsWith("g")) unit=MemoryInfo.Unit.GB;
						if (value.startsWith("m")) unit=MemoryInfo.Unit.MB;
						if (value.startsWith("k")) unit=MemoryInfo.Unit.KB;
						if (value.startsWith("b")) unit=MemoryInfo.Unit.B;						
					}
				}
			}
			req_handle.setResponseBody(Json.toJSON(new MemoryInfo(unit)).getBytes());
			req_handle.setResponseContentType("application/json");
			req_handle.setResponseCode(200);
			return true;
		}
		return false;
	}

	
	private boolean processGetFile(String[] resource_path, Parameter[] query_params, HttpRequestHandle req_handle) throws IOException {
		if (resource_path.length==2 && FILENAME!=null && resource_path[1].equals(FILENAME)) {
			File file=new File(FILENAME);
			if (file.exists()) {
				FileInputStream in=new FileInputStream(file);
				int len=(int)file.length();
				if (len>0) {
					byte[] data=new byte[len];
					in.read(data);
					in.close();
					req_handle.setResponseBody(data);
					req_handle.setResponseContentType("application/octet-stream");
					req_handle.setResponseCode(200);
				}
				else {
					req_handle.setResponseCode(404);
				}
				in.close();
				return true;
			}
			else {
				req_handle.setResponseCode(404);
				return true;
			}
		}
		return false;
	}

	
	/** Closes the server. */
	public void close() {
		server.close();
	}

}
