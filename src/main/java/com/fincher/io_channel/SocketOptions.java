package com.fincher.io_channel;

/** IP Socket Options
 * 
 * @author Brian Fincher
 *
 */
public class SocketOptions {
	
	/** The SO_SNDBUF socket setting */
	public int sendBufferSize = -1;
	
	/** The SO_RCVBUF socket setting */
	public int receiveBufferSize = -1;
	
	/** The SO_REUSEADDR socket setting.  Defaults to true */
	public boolean reuseAddress = true;		
	
	/** The SO_TIMEOUT socket setting (in milliseconds).  Defaults to 2000 (2 seconds) */
	public int timeout = 2000;		

}
