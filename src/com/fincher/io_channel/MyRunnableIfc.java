package com.fincher.io_channel;

/** An extension of Runnable that supplies a method to determine if this Runnable should continue to be executed
 * 
 * @author Brian Fincher
 *
 */
public interface MyRunnableIfc extends Runnable {
	
	/** Should this Runnable continue to be executed
	 * 
	 * @return True if execution should continue for another iteration
	 */
	public boolean continueExecution();
	
	
	/** Called when the parent thread is terminating to give the Runnable an opportunity to
	 * clean up.
	 */
	public void terminate();

}
