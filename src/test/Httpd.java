package test;

import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.zoolu.util.Flags;
import org.zoolu.util.Random;
import org.zoolu.util.log.DefaultLogger;
import org.zoolu.util.log.LoggerLevel;
import org.zoolu.util.log.WriterLogger;

import io.ipstack.http.HttpRequest;
import io.ipstack.http.HttpRequestHandle;
import io.ipstack.http.HttpRequestURL;
import io.ipstack.http.HttpServer;
import io.ipstack.http.MimeTypes;


/** Very simple HTTP server.
 * <p>
 * It provides GET access to a given directory.
 * <p>
 * In addition it adds a test resource {@link #TEST_RESOURCE} returning a test html page displaying a random value.
 */
public final class Httpd {
	private Httpd() {} // no instance
	
	/** Verbose mode */
	public static boolean VERBOSE=false; 

	/** Logs a message. */
	private static void log(String str) {
		DefaultLogger.log(LoggerLevel.INFO,Httpd.class,str);
	}


	/** Root directory */
	public static String ROOT_DIRECTORY="html";

	/** Default resource */
	public static String DEFAULT_RESOURCE="index.html";

	/** Test resource */
	public static String TEST_RESOURCE="test";

	/** Additional text file extension */
	public static String[] TEXT_TYPES=new String[] {"java", "config", "cfg"};

	
	/** Handles HTTP requests.
	 * @param req_handle HTTP request 
	 */
	static private void handleHttpRequest(HttpRequestHandle req_handle) {
		try {
			String method=req_handle.getMethod();
			if (method.equals(HttpRequest.OPTIONS)) {
				req_handle.setResponseCode(200);
				req_handle.setResponseHeaderField("Allow",HttpRequest.GET+","+HttpRequest.OPTIONS);
			}
			else
			if (method.equals(HttpRequest.GET)) {
				HttpRequestURL request_url=req_handle.getRequestURL();
				String resource=request_url.getAbsPath();
				if (resource.equals("/")) resource=DEFAULT_RESOURCE;
				while (resource.startsWith("/")) resource=resource.substring(1);
				log(req_handle.getClientSocketAddress()+": "+method+" "+resource);
				if (TEST_RESOURCE!=null && resource.equals(TEST_RESOURCE)) {
					String resource_value="<html>\r\n" + 
							"<body>\r\n" + 
							"<h1>Test page</h1>\r\n" +
							"<p>Random value: "+Random.nextHexString(8)+"</p>\r\n" +
							"</body>\r\n" + 
							"</html>";
					req_handle.setResponseBody(resource_value.getBytes());
					req_handle.setResponseContentType("text/html");
					req_handle.setResponseCode(200);
				}
				else {
					try {
						Path file_location=Paths.get(ROOT_DIRECTORY+'/'+resource);
						byte[] data=Files.readAllBytes(file_location);
						req_handle.setResponseBody(data);
						//String content_type="application/octet-stream";
						String content_type=MimeTypes.getType(resource);
						for (String type : TEXT_TYPES) if (resource.endsWith('.'+type)) content_type="text/plain";
						req_handle.setResponseContentType(content_type);
						req_handle.setResponseCode(200);
					}
					catch (IOException e) {
						//e.printStackTrace();
						req_handle.setResponseCode(404);
					}
				}
			}
		}
		catch (Exception e) {
			//e.printStackTrace();
			req_handle.setResponseCode(400);
		}
	}			

	
	/** The main method. */
	public static void main(String[] args) throws IOException {
		Flags flags=new Flags(args);
		VERBOSE|=flags.getBoolean("-v","verbose mode");
		boolean very_verbose=flags.getBoolean("-vv","very verbose mode");
		boolean help=flags.getBoolean("-h","prints this message");
		int server_port=flags.getInteger("-p",HttpServer.DEFAULT_PORT,"<port>","server port (default is "+HttpServer.DEFAULT_PORT+")");
		ROOT_DIRECTORY=flags.getString("-d",ROOT_DIRECTORY,"dir","root directory");
		TEST_RESOURCE=flags.getString("-r",TEST_RESOURCE,"name","name of the test resource (use 'none' to disable)");
		if (TEST_RESOURCE.equals("none")) TEST_RESOURCE=null;
		
		if (help) {
			System.out.println(flags.toUsageString(Httpd.class));
			System.exit(0);					
		}
		if (very_verbose) {
			VERBOSE=true;
			HttpServer.VERBOSE=true;
		}
		if (VERBOSE) {
			DefaultLogger.setLogger(new WriterLogger(System.out));
		}
		new HttpServer(server_port,Httpd::handleHttpRequest);
	}


}
