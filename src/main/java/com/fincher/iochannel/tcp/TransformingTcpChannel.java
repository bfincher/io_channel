package com.fincher.iochannel.tcp;

import com.fincher.iochannel.ChannelException;
import com.fincher.iochannel.MessageBuffer;
import com.fincher.iochannel.TransformingIoChannel;

/**
 * A Transforming IOChannel for TCP
 * 
 * @author bfincher
 *
 */
public abstract class TransformingTcpChannel<S, R>
        extends TransformingIoChannel<MessageBuffer, S, R> implements DelegatingTcpChannel {

    private final TcpChannelIfc delegate;

    /**
     * Construct a new TransformingTcpChannel
     * 
     * @param id The ID of this channel
     * @param delegate The delegate channel which will execute the channel
     *        operations
     */
    protected TransformingTcpChannel(String id, TcpChannelIfc delegate) {
        super(id, delegate);
        this.delegate = delegate;
    }

    @Override
    public TcpChannelIfc getDelegate() {
        return delegate;
    }

    /**
     * Send the message on the channel with the specified ID by first transforming
     * the message
     * 
     * @param msg The message to be transformed and sent
     * @param channelId The channel on which the message should be sent
     * @throws ChannelException If an exception occurs while transforming or sending
     */
    public void send(S msg, String channelId) throws ChannelException {
        delegate.send(encode(msg), channelId);
    }

}
