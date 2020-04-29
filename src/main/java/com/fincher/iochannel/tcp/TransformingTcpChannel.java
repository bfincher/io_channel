package com.fincher.iochannel.tcp;

import com.fincher.iochannel.MessageBuffer;
import com.fincher.iochannel.TransformingIoChannel;

/** A Transforming IOChannel for TCP
 * 
 * @author bfincher
 *
 * @param <U> {@link TransformingIoChannel}
 */
public abstract class TransformingTcpChannel<U> extends TransformingIoChannel<MessageBuffer, U> implements DelegatingTcpChannel {
    
    private final TcpChannelIfc delegate;
    
    public TransformingTcpChannel(String id, TcpChannelIfc delegate) {
        super(id, delegate);
        this.delegate = delegate;
    }
    
    
    @Override
    public TcpChannelIfc getDelegate() {
        return delegate;
    }

}
