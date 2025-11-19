package io.ipstack.http.uri;


public class AbsolutePath {

	String[] path=null;

	public AbsolutePath(String str) {
		if (str==null || str.length()==0) return;
		// remove first and last '/'
		if (str.startsWith("/")) str=str.substring(1);
		if (str.length()>0 && str.charAt(str.length()-1)=='/') str=str.substring(0,str.length()-1);
		// parse path
		if (str.length()>0) path=str.split("/");
	}
	
	/**
	 * @return the path */
	public String[] getPath() {
		return path;
	}

	@Override
	public String toString() {
		if (path==null || path.length==0) return "/";
		StringBuffer sb=new StringBuffer();
		for (int i=0;i<path.length; i++) {
			sb.append('/').append(path[i]);
		}
		return sb.toString();
	}
}
