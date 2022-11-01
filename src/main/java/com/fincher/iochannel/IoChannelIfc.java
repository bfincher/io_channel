package com.fincher.iochannel;

import java.io.Closeable;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * A representation of a component used to send / receive data.
 * 
 * @author Brian Fincher
 *
 * @param <T> The type of data exchanged with this channel
 */
public interface IoChannelIfc<T extends Exchangeable> extends Closeable {

    /**
     * Get the ID of this IO Thread.
     * 
     * @return the ID of this IO Thread
     */
    public String getId();

    /**
     * Adds a listener to be notified of received messages. Not applicable for
     * output only channels
     * 
     * @param listener The listener
     */
    public void addMessageListener(Consumer<T> listener);

    /**
     * Adds a listener to be notified of received messages. Not applicable for
     * output only channels
     * 
     * @param listener  The message listener
     * @param predicate A predicate to be evaluated upon receipt of a message to
     *                  determine if this listener will be notified
     */
    public void addMessageListener(Consumer<T> listener, Predicate<T> predicate);

    /**
     * Remove a previously registered message listener.
     * 
     * @param listener The listener to remove
     * @return True if a listener was removed
     */
    public boolean removeMessageListener(Consumer<T> listener);

    /**
     * Connects this IOChannel.
     * 
     * @throws ChannelException     If an exception occurs while connecting
     * @throws InterruptedException If the thread is interrupted
     */
    public void connect() throws ChannelException, InterruptedException;

    /**
     * Is this IO Thread connected to a peer?.
     * 
     * @return true if connected
     */
    public boolean isConnected();

    /**
     * Close this IO Channel.
     * 
     * @throws ChannelException If an error occurs while closing
     */
    @Override
    public void close() throws ChannelException;

    /**
     * Send data via this IO Channel.
     * 
     * @param data The data to be sent
     * @throws ChannelException If an error occurs while sending
     */
    public void send(T data) throws ChannelException;

    /**
     * Gets the IO Type for this IO Channel.
     * 
     * @return Is this IO Channel input, output, or both
     */
    public IoType getIoType();

    /**
     * Gets the state of this IO Channel.
     * 
     * @return The state of this IO Channel
     */
    public ChannelState getState();

    /**
     * Get the type of data processed by this IoChannel.
     * 
     * @return the type of data processed by this IoChannel
     */
    public IoChannelDataType getDataType();

    /**
     * Determine if this channel is capable of receiving messages.
     * 
     * @return true if this channel is capable of receiving messages
     */
    public boolean isInput();

    /**
     * Determine if this channel is capable of sending messages.
     * 
     * @return true if this channel is capable of sending messages
     */
    public boolean isOutput();
}
