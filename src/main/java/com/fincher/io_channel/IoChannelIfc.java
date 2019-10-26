package com.fincher.io_channel;

import java.util.Collection;
import java.util.function.Consumer;

/**
 * A representation of a component used to send / receive data
 * 
 * @author Brian Fincher
 *
 * @param <T>
 */
public interface IoChannelIfc<T extends ExchangeableIfc> {

    /**
     * Get the ID of this IO Thread
     * 
     * @return the ID of this IO Thread
     */
    public String getId();

    /**
     * Gets the a cop of the message listeners used to notify clients of received data. 
     * 
     * @return a copy of the message listeners used to notify clients of received data
     */
    public Collection<Consumer<T>> getMessageListeners();
    
    /** Adds a listener to be notified of received messages.  Not applicable for output only channels */
    public void addMessageListener(Consumer<T> listener);

    /**
     * Connects this IOThread
     * 
     * @throws ChannelException
     * @throws InterruptedException
     */
    public void connect() throws ChannelException, InterruptedException;

    /**
     * Is this IO Thread connected to a peer?
     * 
     * @return true if connected
     */
    public boolean isConnected();

    /**
     * Close this IO Thread
     * 
     * @throws ChannelException
     * @throws InterruptedException
     */
    public void close() throws ChannelException, InterruptedException;

    /**
     * Send data via this IO Thread
     * 
     * @param data The data to be sent
     * @throws ChannelException
     */
    public void send(T data) throws ChannelException;

    /**
     * Gets the IO Type for this IO Thread
     * 
     * @return Is this IO Thread input, output, or both
     */
    public IoTypeEnum getIoType();

    /**
     * Gets the state of this IO Thread
     * 
     * @return The state of this IO Thread
     */
    public StateEnum getState();

    /**
     * Get the type of data processed by this IoChannel
     * 
     * @return the type of data processed by this IoChannel
     */
    public IoChannelDataTypeEnum getDataType();
}
