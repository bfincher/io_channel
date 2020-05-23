package com.fincher.iochannel.tcp;

import com.fincher.iochannel.ChannelException;
import com.fincher.iochannel.MessageBuffer;
import com.fincher.iochannel.TransformingIoChannel;

/**
 * A Transforming IOChannel for TCP
 * 
 * @author bfincher
 *
 * @param <U> {@link TransformingIoChannel}
 */
public abstract class TransformingTcpChannel<SendType, ReceiveType>
        extends TransformingIoChannel<MessageBuffer, SendType, ReceiveType>
        implements DelegatingTcpChannel {

    private final TcpChannelIfc delegate;

    public TransformingTcpChannel(String id, TcpChannelIfc delegate) {
        super(id, delegate);
        this.delegate = delegate;
    }


    @Override
    public TcpChannelIfc getDelegate() {
        return delegate;
    }
    
    
    public void send(SendType msg, String channelId) throws ChannelException {
        delegate.send(encode(msg), channelId);
    }

}
