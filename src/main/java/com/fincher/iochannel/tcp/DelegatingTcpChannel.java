package com.fincher.iochannel.tcp;

import com.fincher.iochannel.ChannelException;
import com.fincher.iochannel.DelegatingIoChannelIfc;
import com.fincher.iochannel.MessageBuffer;

import java.util.List;

public interface DelegatingTcpChannel extends TcpChannelIfc, DelegatingIoChannelIfc<MessageBuffer> {

    
    @Override
    TcpChannelIfc getDelegate();

    default void addConnectionEstablishedListener(ConnectionEstablishedListener listener) {
        getDelegate().addConnectionEstablishedListener(listener);
    }
    
    
    default void removeConnectionEstablishedListener(ConnectionEstablishedListener listener) {
        getDelegate().removeConnectionEstablishedListener(listener);
    }


    default void addConnectionLostListener(ConnectionLostListener listener) {
        getDelegate().addConnectionLostListener(listener);
    }
    
    
    default void removeConnectionLostListener(ConnectionLostListener listener) {
        getDelegate().removeConnectionLostListener(listener);
    }
    
    
    default int getNumConnections() {
        return getDelegate().getNumConnections();
    }
    
    
    default List<String> getSocketIds() {
        return getDelegate().getSocketIds();
    }
    
    
    default void send(byte[] bytes) throws ChannelException {
        getDelegate().send(bytes);
    }
    
    
    default void send(MessageBuffer mb, String channelId) throws ChannelException {
        getDelegate().send(mb, channelId);
    }

}
