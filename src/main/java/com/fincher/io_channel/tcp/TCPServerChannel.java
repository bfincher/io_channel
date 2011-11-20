package com.fincher.io_channel.tcp;

import java.net.InetSocketAddress;
import java.net.ServerSocket;

import com.fincher.io_channel.ChannelException;
import com.fincher.io_channel.MessageBuffer;
import com.fincher.thread.DataHandlerIfc;
import com.fincher.thread.MyRunnableIfc;

/** A TCPIOThread implementation for TCP Server sockets
 * 
 * @author Brian Fincher
 *
 */
public class TCPServerChannel extends TCPChannel {	
	
	/** Creates a new TCPServerChannel from JAXB XML configuration
	 * 
	 * @param config The JAXB XML configuration
	 * @param messageFormat The message format for this IOChannel
	 * @param messageHandler Used to notify clients of received data.  May be null if this is an output only channel
	 * @param streamIo Used to determine how many bytes should be read from the socket for each message
	 * @return A new TCPServerChannel
	 * @throws ChannelException
	 */
//	public static TCPServerChannel createTCPServerChannel(TCPIOChannelType config, 
//			MessageFormatEnum messageFormat,
//			MessageHandlerIfc<MessageBuffer> messageHandler,
//			StreamIOIfc streamIo) throws ChannelException{
//		
//		if (config.getLocalAddress() == null)
//			throw new ChannelException(config.getId() + " local-address must be set");
//		
//		InetSocketAddress localAddress = getLocalAddress(config);		
//		
//		TCPServerChannel channel;
//		if (messageHandler == null)
//			channel = new TCPServerChannel(config.getId(), messageFormat, streamIo, localAddress);
//		else
//			channel = new TCPServerChannel(config.getId(), messageFormat, messageHandler, streamIo, localAddress);
//		
//		if(config.getSocketOptions() != null) {
//			TCPSocketOptions socketOptions = TCPSocketOptions.getSocketOptions(config.getSocketOptions());
//			channel.setSocketOptions(socketOptions);
//		}
//		
//		loadFromConfig(channel, config);
//		
//		return channel;
//	}
	
	/** Constructs a new TCP server socket that is capable of both sending and receiving data 
	 * @param id The ID of this IO Thread
	 * @param messageHandler Used to notify clients of received data
	 * @param streamIo Used to determine how many bytes should be read from the socket for each message
	 * @param localAddress The local address to which this socket will be bound.  If null "localhost" will be used
	 */
	public TCPServerChannel(String id,
			DataHandlerIfc<MessageBuffer> messageHandler,
			StreamIOIfc streamIo,
			InetSocketAddress localAddress) {
		super(id, localAddress, messageHandler, streamIo);
	}	
	
	/** Constructs a new TCP server socket that is capable of only sending data 
	 * @param id The ID of this IO Thread
	 * @param streamIo Used to determine how many bytes should be read from the socket for each message
	 * @param localAddress The local address to which this socket will be bound.  If null "localhost" will be used
	 */
	public TCPServerChannel(String id,
			StreamIOIfc streamIo,
			InetSocketAddress localAddress) {
		super(id, localAddress, streamIo);
	}
	
	/** Gets the Runnable object used to create a connect thread
	 * @return the Runnable object used to create a connect thread
	 * @throws ChannelException
	 */
	@Override
	protected MyRunnableIfc getConnectRunnable() throws ChannelException {
		return new TCPServerConnectRunnable(this);
	}
	
	/** Get the local address to which this socket is bound or the address to which it will be bound if not already bound */
	@Override
	public InetSocketAddress getlocalAddress() {
		ServerSocket socket = ((TCPServerConnectRunnable)connectThread.getRunnable()).serverSocket;
		if (socket.isBound())
			return new InetSocketAddress(socket.getInetAddress(), socket.getLocalPort());
		else
			return super.getlocalAddress();				
	}						
}
