package com.fincher.io_channel.udp;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.SocketException;

import org.apache.log4j.Logger;

import com.fincher.io_channel.ChannelException;
import com.fincher.io_channel.MessageBuffer;
import com.fincher.thread.DataHandlerIfc;

/** A UDP Multicast representation of a Socket IO Thread
 * 
 * @author Brian Fincher
 *
 */
public class UDPMulticastChannel extends UDPChannel {
	
	private static Logger logger = Logger.getLogger(UDPMulticastChannel.class);
	
	private final InetAddress multicastAddress;
	
	/** Creates a new output only UDPMulticastIOChannel from XML configuration
	 * 
	 * @param config The XML configuration
	 * @param messageFormat The message format for this IOChannel
	 * @return The newly created UDPMulticastIOChannel
	 * @throws ChannelException
	 */
//	public static UDPMulticastChannel createOutputUDPMulticastChannel(UDPMulticastIOChannelType config,
//			MessageFormatEnum messageFormat) 
//	throws ChannelException {
//		try {
//			InetSocketAddress localAddress = getLocalAddress(config);
//			InetSocketAddress multicastAddress = new InetSocketAddress(InetAddress.getByName(config.getMulticastAddress()), config.getMulticastPort());
//			int multicastPort = config.getMulticastPort();
//		
//			if (multicastPort == 0)
//				throw new ChannelException(config.getId() + " multicast-port must not be zero");						
//		
//			UDPMulticastChannel channel = new UDPMulticastChannel(config.getId(), messageFormat, localAddress, multicastAddress);
//		
//			if (config.getSocketOptions() != null) {
//				UDPMulticastSocketOptions socketOptions = UDPMulticastSocketOptions.getSocketOptions(config.getSocketOptions());
//				channel.setSocketOptions(socketOptions);
//			}
//			
//			loadFromConfig(channel, config);
//			
//			return channel;
//		}
//		catch (IOException ioe) {
//			throw new ChannelException(ioe);
//		}
//				
//	}
//	
//	/** Creates a new input only UDPMulticastIOChannel from XML configuration
//	 * 
//	 * @param config The XML configuration
//	 * @param messageFormat The message format for this IOChannel
//	 * @param messageHandler Used to notify clients of received data
//	 * @return The newly created UDPMulticastIOChannel
//	 * @throws ChannelException
//	 */
//	public static UDPMulticastChannel createInputUDPMulticastIOChannel(UDPMulticastIOChannelType config,
//			MessageFormatEnum messageFormat,
//			MessageHandlerIfc<MessageBuffer> messageHandler) throws ChannelException {
//		
//		if (config.getLocalAddress() == null) {
//			config.setLocalAddress(new SocketAddressType());
//			config.getLocalAddress().setHost("localhost");
//		}
//		
//		if (config.getLocalAddress().getPort() == null ||
//				config.getLocalAddress().getPort() == 0) {
//			config.getLocalAddress().setPort(config.getMulticastPort());
//		}
//		else {
//			if (config.getLocalAddress().getPort().intValue() != config.getMulticastPort()) {
//				throw new IllegalArgumentException(config.getId() + " local port must be equal to multicast port");
//			}
//		}
//		
//		InetSocketAddress localAddress = getLocalAddress(config);		
//		
//		try {
//			UDPMulticastChannel channel = new UDPMulticastChannel(config.getId(), 
//					messageFormat, 
//					messageHandler, 
//					localAddress, 
//					InetAddress.getByName(config.getMulticastAddress()));
//		
//			if (config.getSocketOptions() != null) {
//				UDPMulticastSocketOptions socketOptions = UDPMulticastSocketOptions.getSocketOptions(config.getSocketOptions());
//				channel.setSocketOptions(socketOptions);
//			}
//			
//			loadFromConfig(channel, config);
//		
//			return channel;
//		}
//		catch (IOException ioe) {
//			throw new ChannelException(ioe);
//		}
//	}
	
	/** Constructs a new input only UDP MULTICAST IO Thread
	 * @param id The ID of this IO Thread
	 * @param messageHandler Used to notify clients of received data
	 * @param localAddress The local address to which this socket will be bound.
	 * @param multicastAddress The multicast address to which this socket will join
	 */
	public UDPMulticastChannel(String id,
			DataHandlerIfc<MessageBuffer> messageHandler,
			InetSocketAddress localAddress,
			InetAddress multicastAddress) {
		super(id, messageHandler, localAddress);
		
		if (localAddress == null || localAddress.getPort() == 0)
			throw new IllegalArgumentException(id + " localAddress port must be non zero");
		
		this.multicastAddress = multicastAddress;
		
		socketOptions = new UDPMulticastSocketOptions();
		
		if (!multicastAddress.isMulticastAddress()) {
			throw new IllegalArgumentException("The multicast address given is not a valid multicast address");
		}
	}
	
	/** Constructs a new output only UDP IO Thread
	 * @param id The ID of this IO Thread
	 * @param localAddress The local address to which this socket will be bound.
	 * @param multicastAddress The remote multicast address to which messages will be sent
	 */
	public UDPMulticastChannel(String id,
			InetSocketAddress localAddress,
			InetSocketAddress multicastAddress) {
		
		super(id, localAddress, multicastAddress);
		
		this.multicastAddress = multicastAddress.getAddress();
		
		socketOptions = new UDPMulticastSocketOptions();
		
		if (!multicastAddress.getAddress().isMulticastAddress()) {
			throw new IllegalArgumentException("The multicast address given is not a valid multicast address");
		}
	}
	
	@Override
	/** Connects this UDP Multicast IO thread */
	public void connect() throws ChannelException, InterruptedException {
		super.connect();
		
		switch (getIOType()) {
		case INPUT_ONLY:
		case INPUT_AND_OUTPUT:
			try {
				((MulticastSocket)socket).joinGroup(multicastAddress);
				logger.info(getId() + " joined multicast group " + multicastAddress.getHostAddress());
			}
			catch (IOException se) {
				throw new ChannelException(getId(), se);
			}
			break;
		}		
	}
	
	/** Creates a new Multicast Socket bound to the local address and port
	 * @return A MulticastSocket bound to the local address and port
	 * @throws SocketException
	 * @throws ChannelException
	 */
	@Override
	protected DatagramSocket createSocket() throws IOException, ChannelException {
		MulticastSocket socket = new MulticastSocket(getlocalAddress());
		
		UDPMulticastSocketOptions socketOptions = (UDPMulticastSocketOptions)this.socketOptions;
		socketOptions.applySocketOptions(getId(), socket);
		
		return socket;
	}

}
