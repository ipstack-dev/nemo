package io.ipstack.http;

import java.util.function.Consumer;

import io.ipstack.http.uri.AbsolutePath;
import io.ipstack.http.uri.Parameter;
import io.ipstack.http.uri.Query;


public abstract class HttpRequestHandleProcessor implements Consumer<HttpRequestHandle> {

	String[] methods;
	boolean allowOption= false;
	
	public HttpRequestHandleProcessor(String[] methods) {
		this.methods= methods;
		for (String m: methods) if (m.equals(HttpRequest.OPTIONS)) {
			allowOption= true;
			break;
		}
	}

	@Override
	public void accept(HttpRequestHandle reqHandle) {
		try {
			String method= reqHandle.getMethod();
			HttpRequestURL requestUrl= reqHandle.getRequestURL();
			if (requestUrl==null) {
				reqHandle.setResponseCode(400);
				return;
			}			
			if (allowOption && method.equals(HttpRequest.OPTIONS) && processOption(requestUrl,reqHandle)) return;
			// else
			if (!(method.equals(HttpRequest.GET) || method.equals(HttpRequest.POST) || method.equals(HttpRequest.PUT) || method.equals(HttpRequest.DELETE) || method.equals(HttpRequest.OPTIONS))) {
				reqHandle.setResponseCode(405);
				return;
			}
			String[] resourcePath= new AbsolutePath(requestUrl.getAbsPath()).getPath();
			// else
			if (resourcePath==null || resourcePath.length==0) {
				reqHandle.setResponseCode(404);
				return;
			}
			Parameter[] queryParams= new Query(requestUrl.getQuery()).getQueryParameters();
			// dispatch
			if (method.equals(HttpRequest.GET) && processGet(resourcePath,queryParams,reqHandle)) return;
			// else
			if (method.equals(HttpRequest.POST) && processPost(resourcePath,queryParams,reqHandle)) return;
			// else
			if (method.equals(HttpRequest.PUT) && processPut(resourcePath,queryParams,reqHandle)) return;
			// else
			if (method.equals(HttpRequest.DELETE) && processDelete(resourcePath,queryParams,reqHandle)) return;
		}
		catch (Exception e) {
			e.printStackTrace();
			reqHandle.setResponseCode(400);
			return;
		}
	}
	
	private boolean processOption(HttpRequestURL requestUrl, HttpRequestHandle reqHandle) {
		reqHandle.setResponseCode(200);
		StringBuffer value= new StringBuffer();
		value.append(methods[0]);
		for (int i=1; i< methods.length; ++i) value.append(",").append(methods[i]);
		reqHandle.setResponseHeaderField("Allow",value.toString());
		return true;
	}
	
	abstract protected boolean processGet(String[] resourcePath, Parameter[] queryParams, HttpRequestHandle reqHandle);

	abstract protected boolean processPost(String[] resourcePath, Parameter[] queryParams, HttpRequestHandle reqHandle);

	abstract protected boolean processPut(String[] resourcePath, Parameter[] queryParams, HttpRequestHandle reqHandle);

	abstract protected boolean processDelete(String[] resourcePath, Parameter[] queryParams, HttpRequestHandle reqHandle);
	
}
