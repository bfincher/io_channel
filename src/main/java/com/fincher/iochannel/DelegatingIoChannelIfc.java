package com.fincher.iochannel;

import java.util.function.Consumer;
import java.util.function.Predicate;

/** A channel that forwards all calls to a delegate
 * 
 * @author bfincher
 *
 * @param <T> The type of data to be exchanged by this channel
 */
public interface DelegatingIoChannelIfc<T extends Exchangeable> extends IoChannelIfc<T> {

    /** Get the delegate channel
     * 
     * @return The delegate channel
     */
    IoChannelIfc<T> getDelegate();

    @Override
    default void addMessageListener(Consumer<T> listener) {
        getDelegate().addMessageListener(listener);
    }
    
    
    @Override
    default void addMessageListener(Consumer<T> listener, Predicate<T> predicate) {
        getDelegate().addMessageListener(listener, predicate);
    }


    @Override
    default boolean removeMessageListener(Consumer<T> listener) {
        return getDelegate().removeMessageListener(listener);
    }


    @Override
    default void connect() throws ChannelException, InterruptedException {
        getDelegate().connect();
    }


    @Override
    default boolean isConnected() {
        return getDelegate().isConnected();
    }


    @Override
    default void close() throws ChannelException {
        getDelegate().close();
    }


    @Override
    default void send(T data) throws ChannelException {
        getDelegate().send(data);
    }


    @Override
    default IoType getIoType() {
        return getDelegate().getIoType();
    }


    @Override
    default ChannelState getState() {
        return getDelegate().getState();
    }


    @Override
    default IoChannelDataType getDataType() {
        return getDelegate().getDataType();
    }


    @Override
    default boolean isInput() {
        return getDelegate().isInput();
    }


    @Override
    default boolean isOutput() {
        return getDelegate().isOutput();
    }

}
