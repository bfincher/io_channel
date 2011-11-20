package com.fincher.io_channel;

import java.util.Iterator;

import org.apache.log4j.Logger;

import com.fincher.thread.DataHandlerIfc;


/** An abstract class used to send / receive data
 * 
 * @author Brian Fincher
 * 
 * @param <T>
 *
 */
public abstract class IOChannel <T extends ExchangeableIfc> implements IOChannelIfc<T> {
	
	/** The ID of this IO Thread */
	private final String id;	
	
	/** The message handler used to notify clients of received data.  
	 * May be null in which case the IO Thread will not attempt to receive data */
	private final DataHandlerIfc<? super T> messageHandler;
	
	/** Is this IO Thread input, output, or both */
	private final IOTypeEnum ioType;
	
	/** The state of this TCP socket */
	private StateEnum state = StateEnum.INITIAL;		
	
	/** Constructs a new IOThread that is capable of both sending and receiving data 
	 * @param id The ID of this IO Thread
	 * @param ioType Is this IO Thread input, output, or both
	 * @param messageHandler The message handler used to notify clients of received data
	 */
	public IOChannel(String id, 
			IOTypeEnum ioType,
			DataHandlerIfc<? super T> messageHandler) {
		this.id = id;
		this.messageHandler = messageHandler;
		this.ioType = ioType;
	}
	
	/** Constructs a new IOThread that is capable of only sending data 
	 * @param id The ID of this IO Thread
	 * @param ioType Is this IO Thread input, output, or both
	 */
	public IOChannel(String id,
			IOTypeEnum ioType) {
		this.id = id;
		this.ioType = ioType;
		this.messageHandler = null;
	}
	
	/** Get the ID of this IO Thread
	 * @return the ID of this IO Thread
	 */
	@Override
	public final String getId() {
		return id;
	}		
	
	/** Gets the IO Type for this IO Thread 
	 * 
	 * @return Is this IO Thread input, output, or both
	 */
	@Override
	public final IOTypeEnum getIOType() {
		return ioType;
	}
	
	/** Gets the state of this IO Thread
	 * @return The state of this IO Thread
	 */
	@Override
	public StateEnum getState() {
		return state;
	}
	/** Sets the state of this IO Thread
	 * 
	 * @param state The state of this IO Thread
	 */
	protected void setState(StateEnum state) {
		this.state = state;
	}
	
	/** Handle a received message
	 * 
	 * @param mb The message that was received
	 * @param logger The Log4j Logger
	 * @param logString log information about the received message
	 */
	protected void messageReceived(T mb, Logger logger, String logString) {
		logger.info("Message received on IO Thread " + getId() + " " + mb.getTransactionId() + " " + logString);
		messageHandler.handleMessage(mb);
	}
	
	@Override
	public DataHandlerIfc<? super T> getMessageHandler() {
		return messageHandler;
	}		
		
	
	/** Initialize this IOChannel from configuration
	 * 
	 * @param ioChannel The IOChannel to load
	 * @param config The config
	 */
//	protected static void loadFromConfig(IOChannelIfc<?> ioChannel, IOChannelType config) {
//		ioChannel.addTags(config.getTag());
//		
//		for (String str: config.getAssociatedMessageType()) {
//			ioChannel.addAssociatedMessageType(MessageTypeEnum.valueOf(str));
//		}
//	}		
	
	/** Log the fact that this IOChannel is sending data
	 * 
	 * @param logger The Log4jLogger
	 * @param message The message being sent
	 * @param logString Additional Log info
	 */
	protected void logSend(Logger logger, 
			T message,
			String logString) {
		
		if (logger.isInfoEnabled()) {
			StringBuilder sb = new StringBuilder();
			sb.append("Sending TID " + message.getTransactionId() + " on IOChannel " + getId());
			sb.append(" PTID = [");
			
			for (Iterator<?> iterator = message.getParentTransactionIds().iterator(); iterator.hasNext() ;) {
				sb.append(iterator.next());
				if (iterator.hasNext())
					sb.append(", ");
			}
			sb.append(']');
			
			if (logString != null && logString.length() > 0)
				sb.append(" " + logString);
			
			logger.info(sb.toString());
		}
	}
}
