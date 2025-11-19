package test;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

import org.zoolu.util.Flags;
import org.zoolu.util.SystemUtils;
import org.zoolu.util.log.DefaultLogger;
import org.zoolu.util.log.WriterLogger;

import io.ipstack.http.HttpClient;
import io.ipstack.http.HttpResponse;


/** Command-line HTTP client.
 * <p>
 * It uses <i>curl</i>-like arguments.
 */
public abstract class Kurl {

	public static void main(String[] args) throws IOException {
		Flags flags=new Flags(args);
		boolean help=flags.getBoolean("-h","prints this message");
		boolean verbose=flags.getBoolean("-v","verbose mode; prints all response header fields");
		String method=flags.getString("-X","GET","method","HTTP method (default is GET)");
		String data=flags.getString("-d",null,"data","ascii data to send");
		String inputfile=flags.getString("-i",null,"file","reads sending data from file");
		String outputfile=flags.getString("-o",null,"file","writes returned data to file");
		String data_type=flags.getString("-t",null,"type","sending content type");
		HashMap<String,String> hdr=new HashMap<>();
		/*String[] hf=flags.getStringTuple("-H",2,null,"hname hvalue","adds this header field");
		while (hf!=null) {
			hdr.put(hf[0],hf[1]);
			hf=flags.getStringTuple("-H",2,null,null,null);
		}*/
		String hf=flags.getString("-H",null,"header","adds this header field");
		while (hf!=null) {
			int index=hf.indexOf(':');
			hdr.put(hf.substring(0,index).trim(),hf.substring(index+1).trim());
			hf=flags.getString("-H",null,null,null);
		}
		String resource_uri=flags.getString(null,null,"uri","resourse URI");
		
		if (help || resource_uri==null) {
			System.out.println(flags.toUsageString(Kurl.class.getName()));
			return;
		}

		byte[] req_body=null;
		if (data!=null) req_body=data.getBytes();
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
		
		HttpClient client=new HttpClient();
		HttpResponse resp=client.request(method,resource_uri,hdr,data_type,req_body);
		System.out.println("Response: "+resp.getFirstLine());
		if (verbose) {
			DefaultLogger.setLogger(new WriterLogger(System.out));
			for (String hname : resp.getHeaderFields()) System.out.println(hname+": "+resp.getHeaderField(hname));
		}
		else {
			String content_type=resp.getHeaderField("Content-Type");
			if (content_type!=null) System.out.println("Content-Type: "+content_type);
			System.out.println("Content-Length: "+resp.getHeaderField("Content-Length"));			
		}
		byte[] resp_body=resp.getBody();
		if (resp_body!=null) {
			if (outputfile!=null) {
				FileOutputStream file=new FileOutputStream(outputfile);
				file.write(resp_body);
				file.close();
			}
			else {
				System.out.println("\n"+new String(resp_body));
			}			
		}
		
		SystemUtils.sleep(500);
		String running_method="rawsocket.Socket.recvfrom(Native Method)";
		for (Thread t: Thread.getAllStackTraces().keySet()) {
			if (Arrays.toString(t.getStackTrace()).contains(running_method)) {
				if (verbose) System.out.println("Method "+running_method+" still running: forcing the exit");
				SystemUtils.exitAfter(500);
			}
		}
	}

}
