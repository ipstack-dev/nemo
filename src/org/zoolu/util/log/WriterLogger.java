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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.Date;

import org.zoolu.util.DateFormat;
import org.zoolu.util.SystemUtils;


/** Simple logger that writes log messages onto a log file, or output stream (either standard output, {@link java.io.Writer}, {@link java.io.OutputStream},
  * or {@link java.io.PrintStream}).
  * <p>
  * When creating a logger you can also specify a <i>loggingLevel</i> for the log.
  * <p>
  * The attribute <i>loggingLevel</i> is used to manage different levels of verboseness.
  * When adding a log message through the method {@link WriterLogger#log(LoggerLevel, Class, String)}
  * a {@link LoggerLevel} <i>level</i> for the given message is specified; only messages with a log <i>level</i>
  * greater or equal to the logger <i>loggingLevel</i> are logged.
  * <br>
  * With loggingLevel {@link LoggerLevel#OFF} no messages are logged.
  * With loggingLevel {@link LoggerLevel#ALL} all messages are logged.
  * <p>
  * It is also possible to personalize the logger by setting the following parameters:
  * <ul>
  *    <li>{@link MAX_SIZE} : Maximum log size, that is the maximum number of characters that can be written [MB]; it limits the maximum size the log.
  *       When the log size reaches the maximum size, no more messages are logged;</li>
  *    <li>{@link TIME_FORMAT} :  The timestamp format ({@link TimeFormat#ISO8601 ISO8601}, {@link TimeFormat#HHmmssSSS HHmmssSSS}, {@link TimeFormat#YyyyMMddHHmmssSSS YyyyMMddHHmmssSSS}, or {@link TimeFormat#NULL NULL}); NULL means no timestamp;</li>
  *    <li>{@link ROTATATIONS} : The number log rotations (value 0 means no log rotation); used in case a log file is specified;</li>
  *    <li>{@link ROTATATION_TIME_SCALE} : The time unit for log rotations (Calendar.MONTH, Calendar.DAY_OF_MONTH, Calendar.HOUR, or Calendar.MINUTE);</li>
  *    <li>{@link ROTATATION_TIME_VALUE} : The period of the log rotations in terms of number of time units.</li>
  * </ul>
  */
public class WriterLogger implements Logger {
	
	/** Maximum log size, that is the maximum number of characters that can be written [MB] */
	public static long MAX_SIZE= 1024*1024; // 1MB


	/** Supported time formats */
	public static enum TimeFormat { NULL, ISO8601, HHmmssSSS, YyyyMMddHHmmssSSS };
	
	/** The timestamp format (ISO8601, HHmmssSSS, YyyyMMddHHmmssSSS, or NULL) */
	public static TimeFormat TIME_FORMAT= TimeFormat.ISO8601;
	
	/** The number log rotations (value 0 means no log rotation) */	
	public static int ROTATATIONS= 0;
	
	/** The time unit for log rotations (Calendar.MONTH, Calendar.DAY_OF_MONTH, Calendar.HOUR, or Calendar.MINUTE) */
	public static int ROTATATION_TIME_SCALE= Calendar.DAY_OF_MONTH;

	/** The number of time units for log rotations */
	public static int ROTATATION_TIME_VALUE= 1;

	
	/** The log writer */
	protected Writer out= null;

	/** The <i>logging_level</i>.
	  * Only messages with a level greater than or equal to this <i>logging_level</i> are logged. */
	protected LoggerLevel loggingLevel;
	
	/** The maximum size of the log stream/file [bytes]
	  * Value 0 (or negative) indicates no maximum size */
	protected long maxSize= MAX_SIZE;
	  
	/** Time format */
	protected TimeFormat timeFormat= TIME_FORMAT;

	/** The char counter of the already logged data */
	protected long charCounter= 0;

	/** The name of the log file */
	private String fileName= null;

	/** The number of log file rotations */
	private int rotations= ROTATATIONS;

	/** The time unit for log rotations */
	private int rotationTimeScale= ROTATATION_TIME_SCALE;

	/** The number of time units for log rotations */	
	private int rotationTimeValue= ROTATATION_TIME_VALUE;

	/** The time of the next log file rotation */
	private Calendar nextRotation;

	

	/** Creates a new WriterLogger.
	  * @param out the Writer where log messages are written to */
	public WriterLogger(Writer out) {
		init(out,LoggerLevel.INFO);
		maxSize= 0;
	}


	/** Creates a new WriterLogger.
	  * @param out the Writer where log messages are written to 
	  * @param logging_level the logging level */
	public WriterLogger(Writer out, LoggerLevel logging_level) {
		init(out,logging_level);
		maxSize= 0;
	}


	/** Creates a new WriterLogger.
	  * @param out the OutputStream where log messages are written to */
	public WriterLogger(OutputStream out) {
		init(new OutputStreamWriter(out),LoggerLevel.INFO);
		maxSize= 0;
	}


	/** Creates a new WriterLogger.
	  * @param out the OutputStream where log messages are written to
	  * @param loggingLevel the logging level */
	public WriterLogger(OutputStream out, LoggerLevel loggingLevel) {
		init(new OutputStreamWriter(out),loggingLevel);
		maxSize= 0;
	}


	/** Creates a new WriterLogger.
	  * @param fileName the file where log messages are written to */
	public WriterLogger(String fileName) {
		this(fileName,LoggerLevel.INFO,false);
	}


	/** Creates a new WriterLogger.
	  * @param fileName the file where log messages are written to
	  * @param loggingLevel the logging level */
	public WriterLogger(String fileName, LoggerLevel loggingLevel) {
		this(fileName,loggingLevel,false);
	}


	/** Creates a new WriterLogger.
	  * @param fileName the file where log messages are written to
	  * @param loggingLevel the logging level
	  * @param append if <i>true</i>, the file is opened in 'append' mode, that is the new messages are appended to the previously saved file (the file is not rewritten) */
	public WriterLogger(String fileName, LoggerLevel loggingLevel, boolean append) {
		if (loggingLevel!=LoggerLevel.OFF) {
			try {
				this.fileName= fileName;
				if (rotations>0) {
					nextRotation= Calendar.getInstance();
					nextRotation.add(rotationTimeScale,rotationTimeValue);
					if (Files.exists(Paths.get(fileName))) rotateLogFiles();
				}
				Writer out= new OutputStreamWriter(new FileOutputStream(fileName,append));
				init(out,loggingLevel);
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		else {
			init(null,LoggerLevel.OFF);
		}
	}


	/** Initializes the WriterLogger.
	  * @param out the Writer where log messages are written to
	  * @param loggingLevel the logging level */
	private void init(Writer out, LoggerLevel loggingLevel)  {
		this.out= out;
		this.loggingLevel= loggingLevel;
		if (loggingLevel==LoggerLevel.OFF) maxSize= 0;
	}


	/** Sets the logging level.
	  * @param loggingLevel the logging level */
	/*public void setLevel(LoggerLevel loggingLevel)  {
		this.loggingLevel= loggingLevel;
	}*/


	/** Gets the current logging level.
	  * @return the logging level */
	/*public LoggerLevel getLevel()  {
		return loggingLevel;
	}*/


	/** Enables or disables writing a timestamp header.
	  * @param timestamp true for including timestamps */
	/*public void setTimestamp(boolean timestamp)  {
		this.timeFormat= timestamp? TIME_FORMAT : TimeFormat.NULL;
	}*/


	/** Closes the log writer. */
	public synchronized void close() {
		if (out!=null) try {  out.close();  } catch (IOException e) {  e.printStackTrace();  }
		out= null;
	}


	@Override
	public synchronized void log(LoggerLevel level, Class<?> sourceClass, String message) {
		if (level==null) level= LoggerLevel.INFO;
		if (rotations>0) {
			long now= Calendar.getInstance().getTimeInMillis();
			if (now>nextRotation.getTimeInMillis()) {
				while (now>nextRotation.getTimeInMillis()) nextRotation.add(rotationTimeScale,rotationTimeValue);
				try {
					if (out!=null) out.close();
					rotateLogFiles();
					charCounter= 0;
					out= new OutputStreamWriter(new FileOutputStream(fileName,false));
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		if (out!=null && level.getValue()>=loggingLevel.getValue() && (maxSize<=0 || charCounter<maxSize)) {
			StringBuffer sb= new StringBuffer();
			switch (timeFormat) {
				case ISO8601 : sb.append(DateFormat.formatISO8601Compact(new Date(System.currentTimeMillis()))).append(": "); break;
				case YyyyMMddHHmmssSSS : sb.append(DateFormat.formatYyyyMMddHHmmssSSS(new Date(System.currentTimeMillis()))).append(": "); break;
				case HHmmssSSS : sb.append(DateFormat.formatHHmmssSSS(new Date(System.currentTimeMillis()))).append(": "); break;
				case NULL : break; 
			}
			if (level!=LoggerLevel.INFO) sb.append(level.getName()).append(": ");
			//if (source_class!=null) sb.append(source_class.getSimpleName()).append(": ");
			if (sourceClass!=null) sb.append(SystemUtils.getSimpleClassName(sourceClass)).append(": ");
			message= sb.append(message).append("\r\n").toString();
			write(message);
			charCounter+= message.length();
			if (maxSize>0 && charCounter>=maxSize) write("\r\n----MAXIMUM LOG SIZE----\r\nSuccessive logs are lost.");
		}
	}
	
	
	private synchronized void rotateLogFiles() {
		try {
			for (int i=rotations; i>1; i--) {
				Path oldFile= Paths.get(fileName+String.valueOf(i-1));
				Path newFile= Paths.get(fileName+String.valueOf(i));
				if (Files.exists(oldFile)) {
					Files.deleteIfExists(newFile);
					Files.move(oldFile,newFile);
				}
			}
			Path oldFile= Paths.get(fileName);
			Path newFile= Paths.get(fileName+"1");
			Files.deleteIfExists(newFile);
			//if (out!=null) out.close();
			Files.move(oldFile,newFile);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}


	/** Writes a string onto the inner writer.
	  * @param str the string to be written */
	protected synchronized void write(String str) {
		try {
			out.write(str);
			out.flush();
		}
		catch (Exception e) {}
	}


	/** Resets the char counter. */
	protected void reset() {
		charCounter= 0;
	}

}
