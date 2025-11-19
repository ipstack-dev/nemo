/*
 * Copyright (c) 2018 NetSec Lab - University of Parma (Italy)
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
 *
 * Author(s):
 * Luca Veltri (luca.veltri@unipr.it)
 */

package org.zoolu.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;


/** Mixed collection of various static methods.
 */
public class SystemUtils {

	/** Causes the current thread to sleep for the specified number of milliseconds.
	 * Differently from method {@link Thread#sleep(long)}, it never throws an {@link InterruptedException}.
	 * @param millisecs the time to sleep, in milliseconds */
	public static void sleep(long millisecs) {
		try {  Thread.sleep(millisecs);  } catch (InterruptedException e) {}
	}
	
	/** Exits after a given time, without blocking the current thread.
	 * @param millisecs the time before exiting, in milliseconds */
	public static void exitAfter(final long millisecs) {
		new Thread() {
			public void run() {
				SystemUtils.sleep(millisecs);
				System.exit(0);
			}
		}.start();
	}
	
	/** Runs a method after a given time, without blocking the current thread.
	 * @param millisecs the time to wait, in milliseconds
	 * @param runnable the runnable object with the <code>run()</code> method to be run. */
	public static void runAfter(final long millisecs, final Runnable runnable) {
		new Thread() {
			public void run() {
				SystemUtils.sleep(millisecs);
				runnable.run();
			}
		}.start();
	}
	
	/** Runs in a new thread the static main method of a given class.
	 * @param program the class with the main method to be run
	 * @param args the program arguments
	 * @throws ReflectiveOperationException */
	public static void run(String program, final String[] args) throws ReflectiveOperationException {
		Class<?> program_class=Class.forName(program);
		final Method main_method=program_class.getMethod("main",String[].class);
		new Thread() {
			public void run() {
				try {
					main_method.invoke(null,(Object)args);
				}
				catch (ReflectiveOperationException e) {
					e.printStackTrace();
				}
			}
		}.start();
	}
	
	/** Runs a Runnable object in a new thread. The Runnable method may throw an exception.
	 * <p>
	 * If an exception is thrown, it is caught and the stack trace is printed to the standard error. */
	public static void run(RunnableWithException r) {
		new Thread(()->{
			try { r.run(); } catch (Exception e) { e.printStackTrace(); }
		}).start();		
	}

	/** Runnable that may throw exceptions. */
	public static interface RunnableWithException {
		public void run() throws Exception;
	}

	/** Exits printing the stack trace. */
	public static void exitWithStackTrace() {
		try { throw new RuntimeException("Exit"); }
		catch (RuntimeException e) {
			e.printStackTrace();
			System.exit(0);
		}		
	}
	
	/** Exits after pressing 'Enter'. */
	public static void exitWhenPressingEnter() {
		System.out.println("Press <Enter> to exit");
		run(()->{
			new BufferedReader(new InputStreamReader(System.in)).readLine();
			System.exit(0);
		});
	}
	
	/**
	 * @return the exception stack trace */
	public static String toString(Exception e) {
		return toString(e,false);
	}
	
	/** Gets the exception stack trace.
	 * @param e the exception
	 * @param reason whether considering only the "Caused by:" part of the trace, if present
	 * @return the exception trace */
	public static String toString(Exception e, boolean reason) {
		StringWriter sw= new StringWriter();
		PrintWriter pw= new PrintWriter(sw);
		e.printStackTrace(pw);
		String err= sw.toString();
		if (reason) {
			int causedBy= err.indexOf("Caused by: ");
			if (causedBy>=0) err= err.substring(causedBy+11);
		}
		return err;
	}
	
	/** Loads a library.
	 * @param libnames list of possible names for the library.
	 * It tries to load the library using the given names in sequence until it succeed. Otherwise an Error is thrown.
	 * @throws Error */
	public static void loadLibrary(String... libnames) throws Error {
		StringBuffer sb=new StringBuffer();
		for (String lib: libnames) {
			try {
				System.loadLibrary(lib);
				sb=null;
				break;
			}
			catch (Error err) {
				sb.append(err.getMessage()).append("; ");
			}
		}
		if (sb!=null && sb.length()>0) throw new Error(sb.toString());
	}

	/** Gets only the class name without package.
	 * It is the same as method {@link java.lang.Class#getSimpleName()}
	 * @param c a class
	 * @return the class name */
	public static String getSimpleClassName(Class<?> c) {
		if (c==null) return null;
		/*Package p=c.getPackage();
		if (p==null) return c.getName();
		return c.getName().substring(c.getPackage().getName().length()+1);*/
		return getSimpleClassName(c.getName());
	}  
	
	/** Gets only the class name without package.
	 * @param class_name the class name
	 * @return the class name */
	public static String getSimpleClassName(String class_name) {
		String[] names=class_name.split("\\x2E");
		return names[names.length-1];
	}
	
	/** Reads a line from the standard input ({@link java.lang.System#in}).
	 * This is blocking method that returns only when a CR or LF char is encountered, or if an error occurs.
	 * @return the read line (or <code>null</code> if an error occurred) */
	public static String readLine() {
		try { return new BufferedReader(new InputStreamReader(System.in)).readLine(); } catch (Exception e) { return null; }
	}

	/** Gets a string from an array of objects using the given separator.
	 * @param obj array of objects
	 * @param hdr header
	 * @param trl trailer
	 * @param sep separator
	 * @return the string */
	public static String toString(Object[] obj, String hdr, String trl, String sep) {
		if (obj==null) return null;
		StringBuffer sb=new StringBuffer();
		for (int i=0; i<obj.length; i++) {
			if (i>0 && sep!=null) sb.append(sep);
			if (hdr!=null) sb.append(hdr);
			sb.append(obj[i].toString());
			if (trl!=null) sb.append(trl);
		}
		return sb.toString();
	}


}
