package io.ipstack.http.uri;

public class Query {

	Parameter[] query_params=null;

	public Query(String str) {
		// remove first and last '/'
		if (str!=null && str.length()>0) {
			String[] params=str.split("&");
			query_params=new Parameter[params.length];
			for (int i=0; i<params.length; i++) query_params[i]=new Parameter(params[i]);
		}
	}

	/**
	 * @return the query parameters */
	public Parameter[] getQueryParameters() {
		return query_params;
	}

	@Override
	public String toString() {
		StringBuffer sb=new StringBuffer();
		if (query_params!=null && query_params.length>0) {
			for (int i=0; i<query_params.length; i++) {
				if (i>0) sb.append('&');
				sb.append(query_params[i].toString());
			}
		}
		return sb.toString();
	}
}
