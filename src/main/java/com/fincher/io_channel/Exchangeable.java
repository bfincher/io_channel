package com.fincher.io_channel;

import java.util.List;

/**
 * An interface representing messages exchanged between components. This can either be MessageIfc
 * (internal POJOs) or MessageBuffer (external encoded messages)
 * 
 * @author Brian Fincher
 *
 */
public interface Exchangeable {
    
    /**
     * Gets the time at which this message was instantiated
     * 
     * @return the time at which this message was instantiated
     */
    public long getOriginationTime();
    
    
    /**
     * get the transaction ID for this exchangeable
     * 
     * @return the transaction ID for this exchangeable
     */
    public long getTransactionId();

    /**
     * Sets the transaction ID for this exchangeable
     * 
     * @param transactionId the transaction ID for this exchangeable
     */
    public void setTransactionId(long transactionId);

    /**
     * Gets the parent transaction ID for this exchangeable
     * 
     * @return the parent transaction ID for this exchangeable
     */
    public List<Long> getParentTransactionIds();

    /**
     * Adds a parent transaction ID for this exchangeable
     * 
     * @param parentTransactionId A parent transaction ID for this exchangeable
     */
    public void addParentTransactionId(long parentTransactionId);

}
