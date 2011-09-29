package com.fincher.io_channel;

/** Used as a callback mechanism to notify message listeners of available data
 * 
 * @author Brian Fincher
 *
 * @param <T>
 */

public interface MessageHandlerIfc <T> {			
		
	/** A message has arrived that should be processed
	 * 
	 * @param data The message to be processed
	 */
	public void handleMessage(T data);

}
