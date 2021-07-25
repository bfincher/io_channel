package com.fincher.iochannel;

import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.apache.logging.log4j.Logger;

import com.google.common.base.Preconditions;

/**
 * An abstract class used to send / receive data.
 * 
 * @author Brian Fincher
 * 
 * @param <T>
 *
 */

public abstract class IoChannel<T extends Exchangeable> implements IoChannelIfc<T> {

    /** The ID of this IO Thread. */
    private final String id;

    private final Listeners<Consumer<T>, T> listeners;

    /** Is this IO Thread input, output, or both. */
    private final IoType ioType;

    /** The state of this TCP socket. */
    private ChannelState state = ChannelState.INITIAL;

    /**
     * Constructs a new IOChannel.
     * 
     * @param id     The ID of this IO Thread
     * @param ioType Is this IO Thread input, output, or both
     */
    public IoChannel(String id, IoType ioType) {
        this.id = id;
        this.ioType = ioType;

        if (ioType == IoType.OUTPUT_ONLY) {
            listeners = null;
        } else {
            listeners = new Listeners<>();
        }
    }


    @Override
    public String getId() {
        return id;
    }


    @Override
    public final IoType getIoType() {
        return ioType;
    }


    @Override
    public ChannelState getState() {
        return state;
    }


    /**
     * Sets the state of this IO Channel.
     * 
     * @param state The state of this IO Channel
     */
    protected void setState(ChannelState state) {
        this.state = state;
    }


    /**
     * Handle a received message.
     * 
     * @param mb        The message that was received
     * @param logger    The Log4j Logger
     * @param logString log information about the received message
     */
    protected void messageReceived(T mb, Logger logger, String logString) {
        logger.info("Message received on IO Thread {} {} {}", getId(), mb.getTransactionId(), logString);
        listeners.getListenersThatMatch(mb).forEach(l -> l.accept(mb));
    }


    @Override
    public void addMessageListener(Consumer<T> listener) {
        Preconditions.checkState(ioType.isInput(), "Cannot set a message listener on an output only channel");
        Preconditions.checkNotNull(listener, "Listener cannot be null");
        
        listeners.addListener(listener);
    }
    
    
    @Override
    public void addMessageListener(Consumer<T> listener, Predicate<T> predicate) {
        Preconditions.checkState(ioType.isInput(), "Cannot set a message listener on an output only channel");
        Preconditions.checkNotNull(listener, "Listener cannot be null");
        Preconditions.checkNotNull(predicate, "predicate cannot be null");
        
        listeners.addListener(listener, predicate);
    }


    @Override
    public boolean removeMessageListener(Consumer<T> listener) {
        return listeners != null && listeners.removeListener(listener);
    }


    @Override
    public boolean isInput() {
        return ioType.isInput();
    }


    @Override
    public boolean isOutput() {
        return ioType.isOutput();
    }


    /**
     * Log the fact that this IoChannel is sending data.
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
