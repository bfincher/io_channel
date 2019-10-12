package com.fincher.io_channel;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Used to generate unique transaction IDs
 * 
 * @author Brian Fincher
 *
 */
public class TransactionIdFactory {

    /** The minimum value for a transaction ID */
    private static long minTID = 0;

    /** The maximum value for a transaction ID */
    private static long maxTID = Long.MAX_VALUE;

    /** The value of the next TID to be assigned */
    private static AtomicLong nextTID = new AtomicLong(minTID);

    /**
     * Initialize the TransactionIdFactory
     * 
     * @param minTransactionId The minimum value for a transaction ID
     * @param maxTransactionId The maximum value for a transaction ID
     */
    public static void init(long minTransactionId, long maxTransactionId) {
        minTID = minTransactionId;
        maxTID = maxTransactionId;
        nextTID.set(minTID);
    }

    /**
     * Get the next Transaction ID
     * 
     * @return the next Transaction ID
     */
    public static long getNextTid() {
        long returnValue = nextTID.get();
        if (returnValue == maxTID) {
            nextTID.set(minTID);
        } else {
            nextTID.incrementAndGet();
        }

        return returnValue;
    }

}
