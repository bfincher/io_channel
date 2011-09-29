package com.fincher.io_channel;

/** An object that can handle exceptions thrown by Threads */
public interface ExceptionHandlerIfc {
	
	/** Used to notify that an Exception occurred	
	 * @param t The exception that occurred
	 */
	public void onException(Throwable t);

}
