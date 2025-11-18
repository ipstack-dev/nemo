package test.nemo.mngnetwork;


import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;


/** Associates a unique string ID to objects.
 */
public abstract class UniqueID {
	
	public static boolean GLOBAL=false;
	
	private static AtomicLong COUNTER=new AtomicLong();
	
	static HashMap<Object,String> MAP=new HashMap<>();

	
	/** Creates a new id.
	 * @return the ID */
	private static String createID() {
		if (GLOBAL) return UUID.randomUUID().toString().replace("-", "");
		else return String.valueOf(COUNTER.getAndIncrement());
	}
	
	
	/** Gets the object id.
	 * @param obj the object
	 * @return the id */
	public static String getId(Object obj) {
		String id=MAP.get(obj);
		if (id!=null) return id;
		// else
		id=createID();
		MAP.put(obj,id);
		return id;
	}
	
}
