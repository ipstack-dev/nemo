package it.unipr.netsec.nemo.http;


/** Handles HTTP requests.
 */
@FunctionalInterface
public interface HttpServerListener {
	
	/** When a new HTTP requested has been received.
	 * @param req the request handle */
	public void onHttpRequest(HttpRequestHandle req);
	
}
