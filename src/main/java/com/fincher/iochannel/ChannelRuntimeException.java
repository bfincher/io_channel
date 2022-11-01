package com.fincher.iochannel;

/**
 * An exception related to IOChannels.
 * 
 * @author Brian Fincher
 *
 */
public class ChannelRuntimeException extends RuntimeException {

    private static final long serialVersionUID = 3072275181924642097L;

    /**
     * Construct a new ChannelException.
     * 
     * @param msg The Exception message
     */
    public ChannelRuntimeException(String msg) {
        super(msg);
    }

    /**
     * Construct a new ChannelException.
     * 
     * @param msg   The Exception message
     * @param cause The nested exception
     */
    public ChannelRuntimeException(String msg, Throwable cause) {
        super(msg, cause);
    }

    /**
     * Construct a new ChannelException.
     * 
     * @param cause The nested exception
     */
    public ChannelRuntimeException(Throwable cause) {
        super(cause);
    }

}
