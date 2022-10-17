package com.fincher.iochannel;

import java.util.function.Consumer;
import java.util.function.Predicate;

import org.slf4j.Logger;

/** An IO Channel that transforms data by encoding U typed data to T before sending and transforming
 * T data to U when receiving
 * @author bfincher
 *
 * @param <T> The type of data exchanged on the wire.
 * @param <S> The type of data sent via this channel.
 * @param <R> The type of data received via this channel.
 */
public abstract class TransformingIoChannel<T extends Exchangeable, S, R>
        implements DelegatingIoChannelIfc<T>, TransformingIoChannelIfc<T, S, R> {

    private static final Logger LOG = Utilities.getInstance().getLogger(TransformingIoChannel.class);

    private final Listeners<Consumer<R>, R> listeners = new Listeners<>();
    private final IoChannelIfc<T> delegate;
    private final String id;

    /** Constructs a new TransformingIOChannel
     * 
     * @param id The ID of the channel
     * @param delegate The delegate to actually send/receive data
     */
    protected TransformingIoChannel(String id, IoChannelIfc<T> delegate) {
        this.delegate = delegate;
        this.id = id;
        delegate.addMessageListener(this::handleMessage);
    }
    
    
    @Override
    public String getId() {
        return id;
    }
    
    
    @Override
    public IoChannelIfc<T> getDelegate() {
        return delegate;
    }


    @Override
    public void addTransformedMessageListener(Consumer<R> listener) {
        listeners.addListener(listener);
    }
    
    
    @Override
    public void addTransformedMessageListener(Consumer<R> listener, Predicate<R> predicate) {
        listeners.addListener(listener, predicate);
    }


    @Override
    public boolean removeTransformedMessageListener(Consumer<R> listener) {
        return listeners.removeListener(listener);
    }


    @Override
    public void send(S data) throws ChannelException {
        delegate.send(encode(data));
    }


    protected abstract R decode(T msg) throws ChannelException;

    protected abstract T encode(S msg) throws ChannelException;


    protected void handleMessage(T msg) {
        try {
            R decoded = decode(msg);
            listeners.getListenersThatMatch(decoded).forEach(listener -> listener.accept(decoded));
        } catch (ChannelException e) {
            LOG.error(e.getMessage(), e);
        }
    }

}
