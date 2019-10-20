package com.fincher.io_channel;

import com.fincher.thread.DataHandlerIfc;

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
     * Gets the message handler used to notify clients of received data. May be null in which case
     * the IO Thread will not attempt to receive data
     * 
     * @return the message handler used to notify clients of received data
     */
    public DataHandlerIfc<T> getMessageHandler();

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
