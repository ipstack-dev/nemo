package it.unipr.netsec.nemo.program;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.util.HashMap;

import org.zoolu.util.Flags;

import io.ipstack.http.HttpMessage;
import io.ipstack.http.HttpRequest;
import io.ipstack.http.HttpRequestURL;
import io.ipstack.http.HttpResponse;
import io.ipstack.net.socket.JavanetSocket;
import io.ipstack.net.socket.Socket;
import io.ipstack.net.stack.IpStack;


/** Command-line HTTP client.
 */
public class Curl implements Program {
	
	IpStack ip_stack;	
	PrintStream out;	
	Socket socket=null;
	boolean is_running=true;

	
	@Override
	public void run(IpStack ip_stack, PrintStream out, String[] args) {
		try {
			this.ip_stack=ip_stack;
			this.out=out;
			Flags flags=new Flags(args);
			flags.getString(null,null,null,null); // skip the program name
			boolean help=flags.getBoolean("-h","prints this message");
			boolean verbose=flags.getBoolean("-v","verbose mode; prints all response header fields");
			String method=flags.getString("-m","GET","method","HTTP method (default is GET)");
			String data=flags.getString("-d",null,"data","ascii data to send");
			String inputfile=flags.getString("-i",null,"file","reads sending data from file");
			String outputfile=flags.getString("-o",null,"file","writes returned data to file");
			String data_type=flags.getString("-t",null,"type","sending content type");
			HashMap<String,String> hdr=new HashMap<>();
			String[] hf=flags.getStringTuple("-a",2,null,"hname hvalue","adds this header field");
			while (hf!=null) {
				hdr.put(hf[0],hf[1]);
				hf=flags.getStringTuple("-a",2,null,null,null);
			}
			String resource_uri=flags.getString(null,null,"uri","resourse URI");
			
			if (help || resource_uri==null) {
				out.println("Usage: "+Curl.class.getSimpleName().toLowerCase()+" [options] uri"+flags.toString());
				return;
			}
			
			// request
			byte[] req_body=null;
			if (data!=null)req_body=data.getBytes();
			else
			if (inputfile!=null) {
				File file=new File(inputfile);
				if (file.length()>0) {
					long len=file.length();
					if (len>Integer.MAX_VALUE) throw new RuntimeException("File '"+inputfile+"' is too big ("+len+"B) to be sent");
					req_body=new byte[(int)len];
					FileInputStream fis=new FileInputStream(file);
					fis.read(req_body);
					fis.close();
				}
			}
			if (!resource_uri.startsWith("http")) resource_uri="http://"+resource_uri; 
			HttpRequestURL req_url=new HttpRequestURL(resource_uri);
			String hostport=req_url.getHostPort();
			int colon=hostport.indexOf(':');
			String host=colon>0?hostport.substring(0,colon):hostport;
			InetAddress iaddr=InetAddress.getByName(host);
			int port=colon>0?Integer.parseInt(hostport.substring(colon+1)):80;
			socket=ip_stack!=null? new io.ipstack.net.tcp.Socket(ip_stack.getTcpLayer(),iaddr,port) : new JavanetSocket(new java.net.Socket(iaddr,port));
			if (!is_running) {
				socket.close();
				return;
			}
			InputStream is=socket.getInputStream();
			OutputStream os=socket.getOutputStream();
			if (req_body!=null && data_type!=null) hdr.put("Content-Type",data_type);
			if (!hdr.containsKey("Host")) hdr.put("Host",host);
			String abspath=req_url.getAbsPath();
			if (abspath==null) abspath="/";
			String query=req_url.getQuery();
			if (query!=null) abspath+='?'+query;
			HttpRequest req=new HttpRequest(method.toUpperCase(),abspath,hdr,req_body);
			os.write(req.toString().getBytes());
			
			// response
			HttpResponse resp=new HttpResponse(HttpMessage.parseHttpMessage(is));
			socket.close();		
			out.println("Response: "+resp.getFirstLine());
			if (verbose) {
				for (String hname : resp.getHeaderFields()) out.println(hname+": "+resp.getHeaderField(hname));
			}
			else {
				String content_type=resp.getHeaderField("Content-Type");
				if (content_type!=null) out.println("Content-Type: "+content_type);
				out.println("Content-Length: "+resp.getHeaderField("Content-Length"));			
			}
			byte[] resp_body=resp.getBody();
			if (resp_body!=null) {
				if (outputfile!=null) {
					FileOutputStream file=new FileOutputStream(outputfile);
					file.write(resp_body);
					file.close();
				}
				else {
					out.println("\n"+new String(resp_body));
				}			
			}			
		}
		catch (IOException e) {
			out.println(e.getMessage());
		}
		is_running=false;
	}

	
	@Override
	public boolean processInputData(byte[] buf, int off, int len) {
		halt();
		return false;
	}
	
	
	@Override
	public boolean isRunning() {
		return is_running;
	}

	
	@Override
	public void halt() {
		if (is_running) try { if (socket!=null) socket.close(); } catch (IOException e) {}
		is_running=false;
	}

}
