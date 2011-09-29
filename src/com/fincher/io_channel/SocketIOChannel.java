package com.fincher.io_channel;

import java.net.InetSocketAddress;

import org.apache.log4j.Logger;

/** An IO Thread that is implemented for network socket communication
 * 
 * @author Brian Fincher
 *
 */
public abstract class SocketIOChannel extends IOChannel<MessageBuffer> {
	
	/** The local network address and port to which this socket will be bound */
	private final InetSocketAddress localAddress;	
	
	/** Constructs a new SocketIOThread
	 * @param id The ID of this IO Thread
	 * @param ioType Is this IO Thread input, output, or both
	 * @param messageHandler Used to notify clients of received data
	 * @param localAddress The local address to which this socket will be bound.  If null "localhost" will be used
	 * that the OS will choose an available port
	 */
	public SocketIOChannel(String id,
			IOTypeEnum ioType,
			MessageHandlerIfc<MessageBuffer> messageHandler,
			InetSocketAddress localAddress) {
		super(id, ioType, messageHandler);
		
		if (localAddress == null)
			this.localAddress = new InetSocketAddress(0);
		else
			this.localAddress = localAddress;		
	}
	
	/** Constructs a new SocketIOThread
	 * @param id The ID of this IO Thread
	 * @param ioType Is this IO Thread input, output, or both
	 * @param localAddress The local address to which this socket will be bound.  If null "localhost" will be used
	 * that the OS will choose an available port
	 */
	public SocketIOChannel(String id,
			IOTypeEnum ioType,
			InetSocketAddress localAddress) {
		this(id, ioType, null, localAddress);		
	}
	
	/** Get the local address to which this socket is bound or the address to which it will be bound if not already bound
	 * 
	 * @return the local address
	 */
	public InetSocketAddress getlocalAddress() {
		return localAddress;
	}		
	
//	/** Builds a local address from JAXB XML configuration
//	 * 
//	 * @param config The JAXB XML configuration
//	 * @return The local address as retrieved from the configuration
//	 */
//	protected static InetSocketAddress getLocalAddress(SocketIOChannelType config) {
//		InetSocketAddress localAddress = null;
//		if (config.getLocalAddress() != null) {
//			localAddress = new InetSocketAddress(config.getLocalAddress().getHost(), config.getLocalAddress().getPort());
//		}
//		
//		return localAddress;
//	}
	
	/** Get the type of data processed by this IOChannel
	 * 
	 * @return the type of data processed by this IOChannel
	 */
	@Override
	public IOChannelDataTypeEnum getDataType() {
		return IOChannelDataTypeEnum.RAW_DATA;
	}
	
	/** Handle a received message
	 * 
	 * @param mb The message that was received
	 * @param logger The Log4j Logger
	 * @param logString log information about the received message
	 */
	@Override
	protected void messageReceived(MessageBuffer mb, Logger logger, String logString) {
//		mb.receivedFromIOChannel = getId();
		super.messageReceived(mb, logger, logString + " size = " + mb.getBytes().length);		
	}
}
