package test.nemo.mngnetwork;


public class AllowInfo {

	String[] allowedNames;
	String[] allowedPrefixes;
	
	public boolean isAllowed(String name) {
		if (allowedNames!=null) for (String n: allowedNames) if (name.equals(n)) return true;
		if (allowedPrefixes!=null) for (String p: allowedPrefixes) if (name.startsWith(p)) return true;
		return false;
	}
}
