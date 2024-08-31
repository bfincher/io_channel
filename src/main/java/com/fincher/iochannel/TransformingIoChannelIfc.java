package com.fincher.iochannel;

import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * An IO Channel that transforms from some other type to the channel's required
 * type
 * 
 * @author bfincher
 *
 * @param <T> The type of data exchanged by the underlying channel
 * @param <S> The type of data sent by this channel
 * @param <R> The type of data received by this channel
 */
public interface TransformingIoChannelIfc<T extends Exchangeable, S, R> extends IoChannelIfc<T> {

    /**
     * Add a listener to be notified when a transformd message is received by this
     * channel
     * 
     * @param listener The listener
     */
    public void addTransformedMessageListener(Consumer<R> listener);

    /**
     * Add a listener to be notified when a transformed message is received by this
     * channel
     * 
     * @param listener The listener
     * @param predicate A filter for this listener
     */
    public void addTransformedMessageListener(Consumer<R> listener, Predicate<R> predicate);

    /**
     * Remove a transformed message listener
     * 
     * @param listener The listener
     * @return True if a listener was removed
     */
    public boolean removeTransformedMessageListener(Consumer<R> listener);

    /**
     * Send a message via this channel
     * 
     * @param data The message to send
     * @throws ChannelException If an exception occurs while sending
     */
    public void send(S data) throws ChannelException;

}
