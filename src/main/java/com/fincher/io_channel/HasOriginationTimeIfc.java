package com.fincher.io_channel;

/**
 * An object that has a time attribute specifing the time at which the message was instantiated
 * 
 * @author Brian Fincher
 *
 */
public interface HasOriginationTimeIfc {

    /**
     * Gets the time at which this message was instantiated
     * 
     * @return the time at which this message was instantiated
     */
    public long getOriginationTime();

}
