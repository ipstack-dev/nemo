/*
 * Copyright (c) 2024 Luca Veltri, University of Parma
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND. IN NO EVENT
 * SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package org.zoolu.util.log;



/** Tt provides access to a shared (static) logger.
 * <p>
 * By default no logger is set. If needed, the logger has to be first set by means of the {@link DefaultLogger#setLogger(Logger)} method.
 */
public class DefaultLogger {
	private DefaultLogger() {}

	/** Default logger */
	protected static Logger LOGGER=null;

	
	/** Sets the default logger.
	 * @param default_logger the default logger */
	public static void setLogger(Logger default_logger)  {
		LOGGER=default_logger;
	}
	
	/** Gets the default logger.
	 * @return the default logger */
	public static Logger getLogger()  {
		return LOGGER;
	}
	
	/** Logs a severe message.
	 * @param str the message to be logged */
	public static void severe(String str) {
		severe((Class<?>)null,str);
	}

	/** Logs a severe message.
	 * @param src_class the class that log refers to
	 * @param str the message to be logged */
	public static void severe(Class<?> src_class, String str) {
		log(LoggerLevel.SEVERE,src_class,str);
	}

	/** Logs an warning message.
	 * @param str the message to log */
	public static void warning(String str) {
		warning((Class<?>)null,str);
	}

	/** Logs an warning message.
	 * @param src_class the class that log refers to
	 * @param str the message to log */
	public static void warning(Class<?> src_class, String str) {
		log(LoggerLevel.WARNING,src_class,str);
	}

	/** Logs an info message.
	 * @param str the message to log */
	public static void info(String str) {
		info((Class<?>)null,str);
	}

	/** Logs an info message.
	 * @param src_class the class that log refers to
	 * @param str the message to log */
	public static void info(Class<?> src_class, String str) {
		log(LoggerLevel.INFO,src_class,str);
	}

	/** Logs a debug message.
	 * @param str the message to log */
	public static void debug(String str) {
		debug((Class<?>)null,str);
	}

	/** Logs a debug message.
	 * @param src_class the class that log refers to
	 * @param str the message to log */
	public static void debug(Class<?> src_class, String str) {
		log(LoggerLevel.DEBUG,src_class,str);
	}

	/** Logs a trace message.
	 * @param str the message to log */
	public static void trace(String str) {
		trace((Class<?>)null,str);
	}

	/** Logs a trace message.
	 * @param src_class the class that log refers to
	 * @param str the message to log */
	public static void trace(Class<?> src_class, String str) {
		log(LoggerLevel.TRACE,src_class,str);
	}

	/** Logs a message.
	 * @param level log level 
	 * @param str the message to log */
	public static void log(LoggerLevel level, String str) {
		log(level,(Class<?>)null,str);
	}
	
	/** Logs a message.
	 * @param level log level 
	 * @param src_class the class that log refers to
	 * @param str the message to log */
	public static void log(LoggerLevel level, Class<?> src_class, String str) {
		if (LOGGER!=null) LOGGER.log(level,src_class,str);
	}



}
