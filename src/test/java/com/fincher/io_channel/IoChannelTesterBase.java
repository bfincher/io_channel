package com.fincher.io_channel;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

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

    /** The loaded io channel configs */
//    protected Map<String, IOChannelType> configMap;
//
//    /**
//     * Load configuration from the config file into a map of configurations. The source config
//     * file's contents will be copied to the destConfigFile's contents
//     * 
//     * @param sourceConfigFile
//     * @param destConfigFile
//     * @param schemaFile
//     * @throws Exception
//     */
//    public void loadConfigMap(File sourceConfigFile, File destConfigFile, File schemaFile)
//            throws Exception {
//
//        FileCopy.copyFile(sourceConfigFile, destConfigFile);
//        IoChannels config = null;
//        try {
//            config = JAXBLoader.load(destConfigFile, schemaFile, IoChannels.class,
//                    Logger.getLogger(IOChannelTester.class));
//        } catch (Exception e) {
//            e.printStackTrace();
//            fail();
//        }
//
//        configMap = new HashMap<String, IOChannelType>();
//
//        for (IOChannelType channelConfig : config.getInputChannels().getInputChannelGroup()) {
//            configMap.put(channelConfig.getId(), channelConfig);
//        }
//
//        for (IOChannelType channelConfig : config.getOutputChannels().getOutputChannelGroup()) {
//            configMap.put(channelConfig.getId(), channelConfig);
//        }
//    }

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

            Thread.sleep(1000);

            while (!output.isConnected() || !input.isConnected()) {
                Thread.sleep(100);
            }

            for (int i = 0; i < 5; i++) {
                output.send(testDataFactory.getTestData(i));
                Thread.sleep(100);
            }

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
