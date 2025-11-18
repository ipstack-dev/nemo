package org.zoolu.util;



public abstract class SystemLogger {

	/** Default logger */
	protected static Logger DEFAULT_LOGGER=null;

	
	/** Sets the default system logger.
	 * @param default_logger the default logger */
	public static void setDefaultLogger(Logger default_logger)  {
		DEFAULT_LOGGER=default_logger;
	}
	
	/** Logs a severe message.
	 * @param src_class the class that log refers to
	  * @param str the message to be logged */
	public static void severe(Class<?> src_class, String str) {
		log(LoggerLevel.SEVERE,src_class,str);
	}

	/** Logs an warning message.
	 * @param src_class the class that log refers to
	  * @param str the message to be logged */
	public static void warning(Class<?> src_class, String str) {
		log(LoggerLevel.WARNING,src_class,str);
	}

	/** Logs an info message.
	 * @param src_class the class that log refers to
	  * @param str the message to be logged */
	public static void info(Class<?> src_class, String str) {
		log(LoggerLevel.INFO,src_class,str);
	}

	/** Logs a debug message.
	 * @param src_class the class that log refers to
	  * @param str the message to be logged */
	public static void debug(Class<?> src_class, String str) {
		log(LoggerLevel.DEBUG,src_class,str);
	}

	/** Logs a trace message.
	 * @param src_class the class that log refers to
	  * @param str the message to be logged */
	public static void trace(Class<?> src_class, String str) {
		log(LoggerLevel.TRACE,src_class,str);
	}

	/** Logs a message.
	 * @param level log level 
	 * @param src_class the class that log refers to
	 * @param str the message to be logged */
	private static void log(LoggerLevel level, Class<?> src_class, String str) {
		if (DEFAULT_LOGGER!=null) DEFAULT_LOGGER.log(level,src_class,str);
	}



}
