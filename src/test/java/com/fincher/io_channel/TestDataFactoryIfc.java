package com.fincher.io_channel;

/**
 * Generate test data
 * 
 * @author Brian Fincher
 *
 * @param <T>
 */
public interface TestDataFactoryIfc<T> {

    /**
     * generate test data
     * 
     * @param iteration the iteration number
     * @return The test data
     */
    public T getTestData(int iteration);

    /**
     * Convert the given test data to a String
     * 
     * @param data the data to be converted
     * @return A string representation of the test data
     */
    public String toString(T data);

}
