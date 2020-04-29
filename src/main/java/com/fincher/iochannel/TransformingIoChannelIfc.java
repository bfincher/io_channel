package com.fincher.iochannel;

import java.util.function.Consumer;

public interface TransformingIoChannelIfc<T extends Exchangeable, U> extends IoChannelIfc<T> {

    public void addTransformedMessageListener(Consumer<U> listener);

    public boolean removeTransformedMessageListener(Consumer<U> listener);

    public void send(U data) throws ChannelException;

}
