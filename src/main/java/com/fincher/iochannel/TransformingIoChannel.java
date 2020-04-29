package com.fincher.iochannel;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** An IO Channel that transforms data by encoding U typed data to T before sending and transforming
 * T data to U when receiving
 * @author bfincher
 *
 * @param <T> The type of data exchanged on the wire.
 * @param <U> The type of data exchanged via this channel
 */
public abstract class TransformingIoChannel<T extends Exchangeable, U>
        implements DelegatingIoChannelIfc<T>, TransformingIoChannelIfc<T, U> {

    private static final Logger LOG = LogManager.getLogger();

    private final List<Consumer<U>> listeners = new LinkedList<>();
    private final IoChannelIfc<T> delegate;
    private final String id;

    /** Constructs a new TransformingIOChannel
     * 
     * @param id The ID of the channel
     * @param delegate The delegate to actually send/receive data
     */
    public TransformingIoChannel(String id, IoChannelIfc<T> delegate) {
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
    public void addTransformedMessageListener(Consumer<U> listener) {
        listeners.add(listener);
    }


    @Override
    public boolean removeTransformedMessageListener(Consumer<U> listener) {
        return listeners.remove(listener);
    }


    @Override
    public void send(U data) throws ChannelException {
        delegate.send(encode(data));
    }


    protected abstract U decode(T msg) throws ChannelException;

    protected abstract T encode(U msg) throws ChannelException;


    protected void handleMessage(T msg) {
        try {
            U decoded = decode(msg);
            listeners.forEach(listener -> listener.accept(decoded));
        } catch (ChannelException e) {
            LOG.error(e.getMessage(), e);
        }
    }

}
