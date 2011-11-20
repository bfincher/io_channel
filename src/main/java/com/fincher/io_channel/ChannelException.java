package com.fincher.io_channel;

/** An exception related to IOChannels
 * 
 * @author Brian Fincher
 *
 */
public class ChannelException extends Exception {
	
	/** Construct a new ChannelException
	 * 
	 * @param msg The Exception message
	 */
	public ChannelException(String msg) {
		super(msg);
	}
	
	/** Construct a new ChannelException
	 * 
	 * @param msg The Exception message
	 * @param cause The nested exception
	 */
	public ChannelException(String msg, Throwable cause) {
		super(msg, cause);
	}
	
	/** Construct a new ChannelException
	 * 
	 * @param cause The nested exception
	 */
	public ChannelException(Throwable cause) {
		super(cause);
	}

}
