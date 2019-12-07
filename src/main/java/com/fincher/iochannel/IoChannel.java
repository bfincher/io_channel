package com.fincher.iochannel;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.function.Consumer;

import org.apache.logging.log4j.Logger;

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

    /**
     * The message listeners used to notify clients of received data. Not applicable for output only channels
     */
    private final EventBus eventBus;
    
    private final Collection<ListenerEntry> listeners;

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
            eventBus = null;
            listeners = null;
        } else {
            eventBus = new EventBus();
            listeners = new LinkedList<>();
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
        eventBus.post(mb);
    }
    
    
    @Override
    public void addMessageListener(final Consumer<T> listener) {
        Preconditions.checkState(ioType.isInput(), "Cannot set a message listener on an output only channel");
        Preconditions.checkNotNull(listener, "Listener cannot be null");
        
        Object subscriber = new Object() {
            
            @Subscribe
            public void handleMessage(T msg) {
                listener.accept(msg);
            }
        };
        
        eventBus.register(subscriber);
        listeners.add(new ListenerEntry(listener, subscriber));
    }
    
    
    @Override
    public boolean removeMessageListener(Consumer<T> listener) {
        if (listeners != null) {
            for (Iterator<ListenerEntry> it = listeners.iterator(); it.hasNext(); ) {
                ListenerEntry entry = it.next();
                if (entry.listener == listener) {
                    it.remove();
                    eventBus.unregister(entry.subscriber);
                    return true;
                }
            }
        }
        return false;
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
    
    private class ListenerEntry {
        final Consumer<T> listener;
        final Object subscriber;
        
        ListenerEntry(Consumer<T> listener, Object subscriber) {
            this.listener = listener;
            this.subscriber = subscriber;
        }
    }
}
