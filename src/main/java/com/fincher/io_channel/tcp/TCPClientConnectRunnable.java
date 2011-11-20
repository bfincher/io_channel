package com.fincher.io_channel.tcp;

import java.net.InetSocketAddress;
import java.net.Socket;

import org.apache.log4j.Logger;

import com.fincher.thread.MyRunnableIfc;

/** Used to connect a TCP Client socket
 * 
 * @author Brian Fincher
 *
 */
class TCPClientConnectRunnable implements MyRunnableIfc {
	
	private static Logger logger = Logger.getLogger(TCPClientConnectRunnable.class);
	
	/** Should this thread continue to execute */
	private boolean continueExecution = true;
	
	/** The parent object */
	private final TCPClientChannel parent;
	
	/** The remote address to which this client is trying to connect */
	private final InetSocketAddress remoteAddress;
	
	/** Constructs a new TCPClientConnectRunnable
	 * @param parent The parent object
	 * @param remoteAddress The remote address to which this client is trying to connect
	 */
	public TCPClientConnectRunnable(TCPClientChannel parent, InetSocketAddress remoteAddress) {
		this.parent = parent;
		this.remoteAddress = remoteAddress;
	}
	
	@Override
	/** Should this thread continue to execute
	 * @return true if execution should continue
	 */
	public boolean continueExecution() {
		return continueExecution;
	}
	
	@Override
	/** The body of the thread */
	public void run() {
		try {
			Socket socket = new Socket(remoteAddress.getAddress(), remoteAddress.getPort());																					
			
			socket.setSoTimeout(2000);
			
			logger.info(parent.getId() + " connection established.  Local port = " + socket.getLocalPort());
			parent.connectionEstablished(socket);
			continueExecution = false;
		}
		catch (Exception e) {
			logger.error(parent.getId() + " " + e.getMessage(), e);
			
			try {
				Thread.sleep(2000);
			}
			catch (InterruptedException ie) {
				logger.warn(parent.getId() + " " + ie.getMessage(), ie);
			}
		}
	}
	
	@Override
	/** No action taken */
    public void terminate() {        
    }
}
