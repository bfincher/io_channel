package com.fincher.iochannel.tcp;

import java.util.Collection;

import com.fincher.iochannel.ChannelException;
import com.fincher.iochannel.DelegatingIoChannelIfc;
import com.fincher.iochannel.MessageBuffer;

public interface DelegatingTcpChannel extends TcpChannelIfc, DelegatingIoChannelIfc<MessageBuffer> {

    @Override
    TcpChannelIfc getDelegate();

    @Override
    default void addConnectionEstablishedListener(ConnectionEstablishedListener listener) {
        getDelegate().addConnectionEstablishedListener(listener);
    }

    @Override
    default void removeConnectionEstablishedListener(ConnectionEstablishedListener listener) {
        getDelegate().removeConnectionEstablishedListener(listener);
    }

    @Override
    default void addConnectionLostListener(ConnectionLostListener listener) {
        getDelegate().addConnectionLostListener(listener);
    }

    @Override
    default void removeConnectionLostListener(ConnectionLostListener listener) {
        getDelegate().removeConnectionLostListener(listener);
    }

    @Override
    default int getNumConnections() {
        return getDelegate().getNumConnections();
    }

    @Override
    default Collection<String> getSocketIds() {
        return getDelegate().getSocketIds();
    }

    @Override
    default void send(byte[] bytes) throws ChannelException {
        getDelegate().send(bytes);
    }

    @Override
    default void send(MessageBuffer mb, String channelId) throws ChannelException {
        getDelegate().send(mb, channelId);
    }

}
