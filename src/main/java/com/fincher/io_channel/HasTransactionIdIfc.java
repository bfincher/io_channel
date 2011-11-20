package com.fincher.io_channel;

import java.util.List;

/** An object that has transaction IDs and parent transaction IDs
 * 
 * @author Brian Fincher
 *
 */
public interface HasTransactionIdIfc {
	
	/**
	 * get the transaction ID for this exchangeable
	 * 
	 * @return the transaction ID for this exchangeable
	 */
	public long getTransactionId();

	/**
	 * Sets the transaction ID for this exchangeable
	 * 
	 * @param transactionId
	 *            the transaction ID for this exchangeable
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
	 * @param parentTransactionId
	 *            A parent transaction ID for this exchangeable
	 */
	public void addParentTransactionId(long parentTransactionId);

}
