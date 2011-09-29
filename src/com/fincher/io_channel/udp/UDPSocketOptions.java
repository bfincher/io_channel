package com.fincher.io_channel.udp;

import java.net.DatagramSocket;
import java.net.SocketException;

import org.apache.log4j.Logger;

import com.fincher.io_channel.SocketOptions;

/** Options for UDP Unicast sockets */
public class UDPSocketOptions extends SocketOptions {
	
	private static Logger logger = Logger.getLogger(UDPSocketOptions.class);
	
	/** Constructs a new UDPSocketOptions object */
	public UDPSocketOptions() {	
		receiveBufferSize = 64 * 1024;
		sendBufferSize = 64 * 1024;
	}
	
	/** Sets the options represented by this object to the given socket
	 * 
	 * @param socketId The ID of the socket
	 * @param socket The socket for which options will be set
	 * @throws SocketException
	 */
	public void applySocketOptions(String socketId, DatagramSocket socket) throws SocketException {
		if (receiveBufferSize != -1)
			socket.setReceiveBufferSize(receiveBufferSize);
		
		if (sendBufferSize != -1)
			socket.setSendBufferSize(sendBufferSize);
				
		socket.setReuseAddress(reuseAddress);		
		socket.setSoTimeout(timeout);
		
		StringBuilder logString = new StringBuilder();
		logString.append(socketId);
		logString.append(" actual socket options: receiveBufferSize = " + socket.getReceiveBufferSize());;		
		logString.append(", sendBufferSize = " + socket.getSendBufferSize());		
		logString.append(", reuseAddress = " + socket.getReuseAddress());
		logString.append(", timeout = " + socket.getSoTimeout());
		
		logger.info(logString.toString());
	}

}
