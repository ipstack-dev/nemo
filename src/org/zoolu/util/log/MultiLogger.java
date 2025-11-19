package org.zoolu.util.log;

import java.util.HashSet;


/** Multiple logger. It allows the sending of log messages to multiple loggers.
 */
public class MultiLogger implements Logger {

	HashSet<Logger> loggerSet= new HashSet<>();
	
	/**
	 * @param loggerList one ore more loggers
	 */
	public MultiLogger(Logger... loggerList) {
		for (Logger l: loggerList) loggerSet.add(l);
	}
	
	/** Adds a new logger
	 * @param logger the new logger
	 */
	public void add(Logger logger) {
		loggerSet.add(logger);
	}

	@Override
	public void log(LoggerLevel level, Class<?> sourceClass, String message) {
		for (Logger l: loggerSet) l.log(level,sourceClass,message);
	}

}
