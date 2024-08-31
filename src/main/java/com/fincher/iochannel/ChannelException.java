package com.fincher.iochannel;

import java.io.IOException;

/**
 * An exception related to IOChannels.
 * 
 * @author Brian Fincher
 *
 */
public class ChannelException extends IOException {

    private static final long serialVersionUID = 3072275181924642097L;

    /**
     * Construct a new ChannelException.
     * 
     * @param msg The Exception message
     */
    public ChannelException(String msg) {
        super(msg);
    }

    /**
     * Construct a new ChannelException.
     * 
     * @param msg The Exception message
     * @param cause The nested exception
     */
    public ChannelException(String msg, Throwable cause) {
        super(msg, cause);
    }

    /**
     * Construct a new ChannelException.
     * 
     * @param cause The nested exception
     */
    public ChannelException(Throwable cause) {
        super(cause);
    }

}
