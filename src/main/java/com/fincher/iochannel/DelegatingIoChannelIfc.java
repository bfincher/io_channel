package com.fincher.iochannel;

import java.util.function.Consumer;
import java.util.function.Predicate;

public interface DelegatingIoChannelIfc<T extends Exchangeable> extends IoChannelIfc<T> {

    IoChannelIfc<T> getDelegate();


    default void addMessageListener(Consumer<T> listener) {
        getDelegate().addMessageListener(listener);
    }
    
    
    default void addMessageListener(Consumer<T> listener, Predicate<T> predicate) {
        getDelegate().addMessageListener(listener, predicate);
    }


    default boolean removeMessageListener(Consumer<T> listener) {
        return getDelegate().removeMessageListener(listener);
    }


    default void connect() throws ChannelException, InterruptedException {
        getDelegate().connect();
    }


    default boolean isConnected() {
        return getDelegate().isConnected();
    }


    default void close() throws ChannelException {
        getDelegate().close();
    }


    default void send(T data) throws ChannelException {
        getDelegate().send(data);
    }


    default IoType getIoType() {
        return getDelegate().getIoType();
    }


    default ChannelState getState() {
        return getDelegate().getState();
    }


    default IoChannelDataType getDataType() {
        return getDelegate().getDataType();
    }


    default boolean isInput() {
        return getDelegate().isInput();
    }


    default boolean isOutput() {
        return getDelegate().isOutput();
    }

}
