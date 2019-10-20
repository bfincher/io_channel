package com.fincher.io_channel;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.awaitility.Awaitility;

/**
 * Test IO Channels
 * 
 * @author Brian Fincher
 *
 * @param <T>
 */
public abstract class IoChannelTesterBase<T extends ExchangeableIfc> {

    /** The queue used to store retrieved messages */
    protected BlockingQueue<T> messageQueue = new LinkedBlockingQueue<T>();


    /**
     * Test IO Channels
     * 
     * @param input           The input IoChannel
     * @param output          The output IoChannel
     * @param testDataFactory Used to create test data
     */
    protected void test(IoChannelIfc<T> input, IoChannelIfc<T> output,
            TestDataFactoryIfc<T> testDataFactory) {

        try {
            output.connect();
            input.connect();

            Awaitility.await().until(() -> output.isConnected() && input.isConnected());

            for (int i = 0; i < 5; i++) {
                output.send(testDataFactory.getTestData(i));
            }
            
            Awaitility.await().atMost(1, TimeUnit.SECONDS).until(() -> messageQueue.size() == 5);

            System.out.println("messages: ");
            for (T data : messageQueue) {
                System.out.println("    " + testDataFactory.toString(data));
            }

            int size = messageQueue.size();
            assertEquals(5, size);

            System.out.println("messages: ");
            for (T data : messageQueue) {
                System.out.println("    " + testDataFactory.toString(data));
            }

            input.close();
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

}
