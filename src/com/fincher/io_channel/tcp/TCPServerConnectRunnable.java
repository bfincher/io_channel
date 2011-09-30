package com.fincher.io_channel.tcp;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

import org.apache.log4j.Logger;

import com.fincher.io_channel.ChannelException;
import com.fincher.thread.MyRunnableIfc;

/** Used to connect a TCP Server socket 
 * 
 * @author Brian Fincher
 *
 */
class TCPServerConnectRunnable implements MyRunnableIfc {
	
	private static Logger logger = Logger.getLogger(TCPServerConnectRunnable.class);
	
	/** Should this thread continue to execute */
	private boolean continueExecution = true;
	
	/** The parent TCP Server object */
	private final TCPServerChannel tcpServer;
	
	/** The java.net.ServerSocket used to accept connections */
	protected final ServerSocket serverSocket;	
	
	/** Is the server socket bound locally? */
	private boolean serverSocketConnected = false;
	
	/** Construct a new TCPServerConnectRunnable
	 * @param tcpServer The parent TCP Server object
	 * @throws ChannelException
	 */
	public TCPServerConnectRunnable(TCPServerChannel tcpServer) throws ChannelException {
		this.tcpServer = tcpServer;			
		
		try {
			serverSocket = new ServerSocket();
			tcpServer.socketOptions.applySocketOptions(tcpServer.getId(), serverSocket);
		}
		catch (IOException ioe) {
			throw new ChannelException(tcpServer.getId(), ioe);
		}
	}
	
	@Override
	/** Should this thread continue to execute
	 * @return true if execution should continue
	 */
	public boolean continueExecution() {
		return continueExecution;
	}
	
	@Override
	/** Called when the parent thread is terminating */
	public void terminate() {
		if (serverSocket != null) {
			try {
				serverSocket.close();
			}
			catch (IOException ioe) {
				logger.error(tcpServer.getId() + " " + ioe.getMessage(), ioe);
			}
		}
	}
	
	@Override
	/** The body of the thread */
	public void run() {
		
		try {
			if (!serverSocketConnected) {
				try {
					serverSocket.bind(tcpServer.getlocalAddress());					
					serverSocketConnected = true;
				}
				catch (BindException be) {
					try {
						logger.warn(tcpServer.getId() + " " + be.getMessage());
						Thread.sleep(2000);
						return;
					}
					catch (InterruptedException ie) {
						logger.warn(tcpServer.getId() + " " + ie.getMessage(), ie);
					}
				}
			}
		
			Socket socket = serverSocket.accept();
			tcpServer.connectionEstablished(socket);
		}
		catch (SocketTimeoutException ste) {
			
		}
		catch (Exception e) {
			logger.error(tcpServer.getId() + " " + e.getMessage(), e);
			
			try {
				Thread.sleep(2000);
			}
			catch (InterruptedException ie) {
				logger.warn(tcpServer.getId() + " " + ie.getMessage(), ie);
			}
		}
	}

}
