package com.fincher.iochannel;

import java.util.function.Consumer;
import java.util.function.Predicate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** An IO Channel that transforms data by encoding U typed data to T before sending and transforming
 * T data to U when receiving
 * @author bfincher
 *
 * @param <T> The type of data exchanged on the wire.
 * @param <U> The type of data exchanged via this channel
 */
public abstract class TransformingIoChannel<T extends Exchangeable, SendType, ReceiveType>
        implements DelegatingIoChannelIfc<T>, TransformingIoChannelIfc<T, SendType, ReceiveType> {

    private static final Logger LOG = LogManager.getLogger();

    private final Listeners<Consumer<ReceiveType>, ReceiveType> listeners = new Listeners<>();
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
    public void addTransformedMessageListener(Consumer<ReceiveType> listener) {
        listeners.addListener(listener);
    }
    
    
    @Override
    public void addTransformedMessageListener(Consumer<ReceiveType> listener, Predicate<ReceiveType> predicate) {
        listeners.addListener(listener, predicate);
    }


    @Override
    public boolean removeTransformedMessageListener(Consumer<ReceiveType> listener) {
        return listeners.removeListener(listener);
    }


    @Override
    public void send(SendType data) throws ChannelException {
        delegate.send(encode(data));
    }


    protected abstract ReceiveType decode(T msg) throws ChannelException;

    protected abstract T encode(SendType msg) throws ChannelException;


    protected void handleMessage(T msg) {
        try {
            ReceiveType decoded = decode(msg);
            listeners.getListenersThatMatch(decoded).forEach(listener -> listener.accept(decoded));
        } catch (ChannelException e) {
            LOG.error(e.getMessage(), e);
        }
    }

}
