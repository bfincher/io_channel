package com.fincher.io_channel;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.function.Consumer;

import org.slf4j.Logger;

/**
 * An abstract class used to send / receive data
 * 
 * @author Brian Fincher
 * 
 * @param <T>
 *
 */

public abstract class IoChannel<T extends ExchangeableIfc> implements IoChannelIfc<T> {

    /** The ID of this IO Thread */
    private final String id;    

    /**
     * The message listeners used to notify clients of received data. Not applicable for output only channels
     */
    private final Collection<Consumer<T>> messageListeners = new ArrayList<>();

    /** Is this IO Thread input, output, or both */
    private final IoTypeEnum ioType;

    /** The state of this TCP socket */
    private StateEnum state = StateEnum.INITIAL;

    /**
     * Constructs a new IOThread that is capable of only sending data
     * 
     * @param id     The ID of this IO Thread
     * @param ioType Is this IO Thread input, output, or both
     */
    public IoChannel(String id, IoTypeEnum ioType) {
        this.id = id;
        this.ioType = ioType;
    }

    /**
     * Get the ID of this IO Thread
     * 
     * @return the ID of this IO Thread
     */
    @Override
    public final String getId() {
        return id;
    }

    /**
     * Gets the IO Type for this IO Thread
     * 
     * @return Is this IO Thread input, output, or both
     */
    @Override
    public final IoTypeEnum getIoType() {
        return ioType;
    }

    /**
     * Gets the state of this IO Thread
     * 
     * @return The state of this IO Thread
     */
    @Override
    public StateEnum getState() {
        return state;
    }

    /**
     * Sets the state of this IO Thread
     * 
     * @param state The state of this IO Thread
     */
    protected void setState(StateEnum state) {
        this.state = state;
    }

    /**
     * Handle a received message
     * 
     * @param mb        The message that was received
     * @param logger    The Log4j Logger
     * @param logString log information about the received message
     */
    protected void messageReceived(T mb, Logger logger, String logString) {
        logger.info("Message received on IO Thread {} {} {}", getId(), mb.getTransactionId(), logString);
        messageListeners.forEach(listener -> listener.accept(mb));
    }

    @Override
    public Collection<Consumer<T>> getMessageListeners() {
        return ImmutableList.copyOf(messageListeners);
    }
    
    
    @Override
    public void addMessageListener(Consumer<T> listener) {
        Preconditions.checkState(ioType.isInput(), "Cannot set a message listener on an output only channel");
        messageListeners.add(listener);
    }


    /**
     * Log the fact that this IoChannel is sending data
     * 
     * @param logger    The Log4jLogger
     * @param message   The message being sent
     * @param logString Additional Log info
     */
    protected void logSend(Logger logger, T message, String logString) {

        if (logger.isInfoEnabled()) {
            StringBuilder sb = new StringBuilder();
            sb.append("Sending TID " + message.getTransactionId() + " on IoChannel " + getId());
            sb.append(" PTID = [");

            for (Iterator<?> iterator = message.getParentTransactionIds().iterator(); iterator
                    .hasNext();) {
                sb.append(iterator.next());
                if (iterator.hasNext()) {
                    sb.append(", ");
                }
            }
            sb.append(']');

            if (logString != null && logString.length() > 0) {
                sb.append(" " + logString);
            }

            logger.info(sb.toString());
        }
    }
}
