package com.fincher.iochannel.udp;

import com.fincher.iochannel.IoChannelTesterBase;
import com.fincher.iochannel.MessageBuffer;
import com.fincher.iochannel.TestDataFactoryIfc;
import com.fincher.iochannel.udp.UdpChannel;
import com.fincher.iochannel.udp.UdpMulticastChannel;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import org.junit.AfterClass;
import org.junit.Test;

/**
 * Test UDP sockets
 * 
 * @author Brian Fincher
 *
 */
public class UdpTest extends IoChannelTesterBase<MessageBuffer> {

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
    public void testMulticast() throws UnknownHostException {
        InetSocketAddress localAddress0 = new InetSocketAddress(InetAddress.getByName("0.0.0.0"), 0);
        InetSocketAddress localAddress5000 = new InetSocketAddress(InetAddress.getByName("0.0.0.0"), 5000);
        InetSocketAddress multicastAddress = new InetSocketAddress(InetAddress.getByName("239.1.1.1"), 5000);
        
        UdpMulticastChannel output = UdpMulticastChannel.createOutputChannel("output", localAddress0,
                multicastAddress);

        UdpMulticastChannel input = UdpMulticastChannel.createInputChannel("input",
                messageQueue::add, localAddress5000,
                multicastAddress.getAddress());

        test(input, output, new TestDataFactory());
    }

    /**
     * Method name is self explanatory
     * 
     */
    @Test
    public void testUnicast() throws Exception {
        InetSocketAddress localAddress0 = new InetSocketAddress(InetAddress.getByName("0.0.0.0"), 0);
        InetSocketAddress localAddress5000 = new InetSocketAddress(InetAddress.getByName("localhost"), 5000);
        
        UdpChannel output = UdpChannel.createOutputChannel("output", localAddress0, localAddress5000);

        UdpChannel input = UdpChannel.createInputChannel("input",
                messageQueue::add, localAddress5000);
        test(input, output, new TestDataFactory());
    }
}
