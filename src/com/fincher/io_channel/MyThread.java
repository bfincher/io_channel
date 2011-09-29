package com.fincher.io_channel;

import org.apache.log4j.Logger;

/** <pre> 
 * A Thread wrapper where the idea is that a single threading model should be used for the EC2BMC project.  This
 * thread wrapper provides default behaviors for killing threads and handling exceptions.
 * 
 * To implement, instantiate this class with a Runnable object.   This Runnable should not contain a indefinite loop,
 * instead, it should have code processing a single iteration of it's task.  
 * <p>
 * For example:  A traditional Runnable's run method may look like:
 * <code>
 * 
 * public void run() {
 *     while(!terminate) {
 *         // some action
 *     }    
 * }
 * </code>
 * 
 * Instead the Runnable's run method should look like:
 * <code>
 * 
 * public void run() {
 *     // some action    
 * }
 * </code>
 * <p>
 * 
 * This is because this Class will handle the looping to ensure proper exception handling and shutdown procedures.
 * 
 * An optional ExceptionListener may be set to be notified of Exceptions
 * 
 * </pre>
 *
 * @author Brian Fincher
 *
 */
public class MyThread extends Thread implements Runnable {
	
	private static final Logger logger = Logger.getLogger(MyThread.class);
	
	/** Should this thread terminate? */
	private volatile boolean terminate = false;
	
	/** The user's object that will be invoked upon each thread iteration */
	private final MyRunnableIfc runnable;	
	
	/** Used to notify the user of any exceptions in the thread's body */
	private ExceptionHandlerIfc exceptionHandler = null;
	
	/** Should execution continue after an exception is encountered.  Defaults to true */
	private volatile boolean continueAfterException = true;
	
	/** Constructs a new Ec2bmcThread
	 * @param name The name of this thread
	 * @param runnable To be invoked upon each thread iteration
	 */
	public MyThread(String name, MyRunnableIfc runnable) {
		super(name);
		this.runnable = runnable;
	}	
	
	/** Constructs a new EC2BMC Thread
	 * @param threadGroup The Java Thread Group for this thread
	 * @param name The name of this thread
	 * @param runnable To be invoked upon each thread iteration
	 */
	public MyThread(ThreadGroup threadGroup, String name, MyRunnableIfc runnable) {
		super(threadGroup, name);
		this.runnable = runnable;
	}
	
	/** Should execution continue after an exception is encountered.  Defaults to true */
	public void setContinueAfterException(boolean val) {
		this.continueAfterException = val;
	}
	
	/** Sets a handler that will be called upon exceptions being thrown in this thread's body */
	public void setExceptionHandler(ExceptionHandlerIfc exceptionHandler) {
		this.exceptionHandler = exceptionHandler;
	}
	
	/** Should not be called directly */
	@Override
	public void run() {
		do {		
			try {
				runnable.run();								
			}
			catch (Throwable t) {
				if (exceptionHandler == null) {
					logger.error(getName() + " " + t.getMessage(), t);
				}
				else
					exceptionHandler.onException(t);
				
				if (!continueAfterException) {
					logger.error(getName() + " Execution terminating due to exception");
					terminate = true;
				}
			}
		} while (!terminate && runnable.continueExecution());
		
		logger.debug(getName() + " terminated");
	}	
		
	/** Terminates this thread */
	public void terminate() {
		terminate = true;		
		interrupt();
		runnable.terminate();
	}
	
	/** Has this thread been terminated */
	public boolean isTerminated() {
		return terminate;
	}
	
	/** Gets the runnable object associated with this thread */
	public MyRunnableIfc getRunnable() {
		return runnable;
	}

}
