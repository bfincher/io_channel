package com.fincher.io_channel.udp;

import com.fincher.io_channel.IoChannelTesterBase;
import com.fincher.io_channel.MessageBuffer;
import com.fincher.io_channel.TestDataFactoryIfc;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test UDP sockets
 * 
 * @author Brian Fincher
 *
 */
public class UdpTest extends IoChannelTesterBase<MessageBuffer> {

    private static InetSocketAddress localAddress0;
    private static InetSocketAddress localAddress5000;
    private static InetSocketAddress multicastAddress;

    /**
     * Method name is self explainatory
     * 
     * @throws Exception
     */
    @BeforeClass
    public static void setUp() throws Exception {
        //      loadConfigMap(SOURCE_UNICAST_CONFIG, DEST_UNICAST_CONFIG, SCHEMA_FILE);
        localAddress0 = new InetSocketAddress(InetAddress.getByName("localhost"), 0);
        localAddress5000 = new InetSocketAddress(InetAddress.getByName("localhost"), 5000);
        multicastAddress = new InetSocketAddress(InetAddress.getByName("239.1.1.1"), 5000);
    }

    /** Method name is self explainatory */
    @AfterClass
    public static void tearDown() {
        //      DEST_UNICAST_CONFIG.delete();
    }

    private static class TestDataFactory implements TestDataFactoryIfc<MessageBuffer> {
        @Override
        public String toString(MessageBuffer data) {
            return new String(data.getBytes());
        }

        @Override
        public MessageBuffer getTestData(int iteration) {
            return new MessageBuffer(new String("Hello World " + iteration).getBytes());
        }
    }

    /**
     * Method name is self explainatory
     * 
     */
    @Test
    @Ignore
    public void testMulticast() throws UnknownHostException {
        UdpMulticastChannel output = new UdpMulticastChannel("output", localAddress0,
                multicastAddress);

        UdpMulticastChannel input = new UdpMulticastChannel("input",
                messageQueue::add, localAddress5000,
                InetAddress.getByName("239.1.1.1"));

        test(input, output, new TestDataFactory());
    }

    /**
     * Method name is self explanatory
     * 
     */
    @Test
    public void testUnicast() {
        UdpChannel output = new UdpChannel("output", localAddress0, localAddress5000);

        UdpChannel input = new UdpChannel("input",
                messageQueue::add, localAddress5000);
        test(input, output, new TestDataFactory());
    }
}