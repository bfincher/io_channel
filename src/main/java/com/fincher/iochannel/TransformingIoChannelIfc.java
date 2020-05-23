package com.fincher.iochannel;

import java.util.function.Consumer;
import java.util.function.Predicate;

public interface TransformingIoChannelIfc<T extends Exchangeable, SendType, ReceiveType> extends IoChannelIfc<T> {

    public void addTransformedMessageListener(Consumer<ReceiveType> listener);
    
    public void addTransformedMessageListener(Consumer<ReceiveType> listener, Predicate<ReceiveType> predicate);

    public boolean removeTransformedMessageListener(Consumer<ReceiveType> listener);

    public void send(SendType data) throws ChannelException;

}
