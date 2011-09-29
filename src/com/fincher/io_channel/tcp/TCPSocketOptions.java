package com.fincher.io_channel.tcp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import org.apache.log4j.Logger;

import com.fincher.io_channel.ChannelException;
import com.fincher.io_channel.SocketOptions;

/** Socket options for TCP Sockets */
public class TCPSocketOptions extends SocketOptions {
	
	private static Logger logger = Logger.getLogger(TCPSocketOptions.class);
	
	/** The SO_KEEPALIVE socket setting.  Defaults to true */
	public boolean keepAlive = true;
	
	/** The TCP_NODELAY socket setting.  Defaults to true */
	public boolean tcpNoDelay = true;
	
	/** Get a new TCPSocketOptions object from XML configuration
	 * 
	 * @param config The XML configuration
	 * @return The newly created TCPSocketOptions type
	 */
//	public static TCPSocketOptions getSocketOptions(TCPSocketOptionsType config) {
//		TCPSocketOptions socketOptions = new TCPSocketOptions();
//		SocketOptions.getSocketOptions(socketOptions, config);
//		
//		socketOptions.keepAlive = config.isKeepAlive();
//		socketOptions.tcpNoDelay = config.isNoDelay();
//		
//		return socketOptions;
//		
//	}
	
	/** Sets the options represented by this object to the given TCP Socket
	 * @param socketId The ID of the socket
	 * @param socket The socket for which options will be set
	 * @throws ChannelException
	 */
	public void applySocketOptions(String socketId, Socket socket) throws ChannelException {
		try {
			if (receiveBufferSize != -1)
				socket.setReceiveBufferSize(receiveBufferSize);
		
			if (sendBufferSize != -1)
				socket.setSendBufferSize(sendBufferSize);
		
			socket.setKeepAlive(keepAlive);		
			socket.setReuseAddress(reuseAddress);		
			socket.setSoTimeout(timeout);
			socket.setTcpNoDelay(tcpNoDelay);
		
			StringBuilder logString = new StringBuilder();
			logString.append(socketId);
			logString.append(" actual socket options: receiveBufferSize = " + socket.getReceiveBufferSize());;		
			logString.append(", sendBufferSize = " + socket.getSendBufferSize());		
			logString.append(", keepAlive = " + socket.getKeepAlive());
			logString.append(", reuseAddress = " + socket.getReuseAddress());
			logString.append(", timeout = " + socket.getSoTimeout());
			logString.append(", tcpNoDelay = " + socket.getTcpNoDelay());
		
			logger.info(logString.toString());
		}
		catch (SocketException se) {
			throw new ChannelException(socketId, se);
		}
		
	}
	
	/** Sets the options represented by this object to the given TCP Server Socket
	 * @param socketId The ID of the socket
	 * @param socket The socket for which options will be set
	 * @throws SocketException
	 * @throws IOException
	 */
	public void applySocketOptions(String socketId, ServerSocket socket) throws IOException {
		if (receiveBufferSize != -1)
			socket.setReceiveBufferSize(receiveBufferSize);		
				
		socket.setReuseAddress(reuseAddress);		
		socket.setSoTimeout(timeout);
		
		StringBuilder logString = new StringBuilder();
		logString.append(socketId);
		logString.append(" actual socket options: receiveBufferSize = " + socket.getReceiveBufferSize());;				
		logString.append(", reuseAddress = " + socket.getReuseAddress());
		logString.append(", timeout = " + socket.getSoTimeout());
	
		
		logger.info(logString.toString());
	}

}
