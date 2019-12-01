package com.fincher.iochannel.udp;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.fincher.iochannel.ChannelException;
import com.fincher.iochannel.IoChannelTesterBase;
import com.fincher.iochannel.IoType;
import com.fincher.iochannel.MessageBuffer;
import com.fincher.iochannel.TestDataFactoryIfc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.appender.OutputStreamAppender;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.junit.AfterClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

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
        // DEST_UNICAST_CONFIG.delete();
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
        InetSocketAddress localAddress0 = new InetSocketAddress(InetAddress.getByName("0.0.0.0"),
                0);
        InetSocketAddress localAddress5000 = new InetSocketAddress(InetAddress.getByName("0.0.0.0"),
                5000);
        InetSocketAddress multicastAddress = new InetSocketAddress(
                InetAddress.getByName("239.1.1.1"), 5000);

        UdpMulticastChannel output = UdpMulticastChannel.createOutputChannel("output",
                localAddress0, multicastAddress);

        UdpMulticastChannel input = UdpMulticastChannel.createInputChannel("input",
                messageQueue::add, localAddress5000, multicastAddress.getAddress());

        test(input, output, new TestDataFactory());
    }

    /**
     * Method name is self explanatory
     * 
     */
    @Test
    public void testUnicast() throws Exception {
        InetSocketAddress localAddress0 = new InetSocketAddress(InetAddress.getByName("0.0.0.0"),
                0);
        InetSocketAddress localAddress5000 = new InetSocketAddress(
                InetAddress.getByName("localhost"), 5000);

        UdpChannel output = UdpChannel.createOutputChannel("output", localAddress0,
                localAddress5000);

        UdpChannel input = UdpChannel.createInputChannel("input", messageQueue::add,
                localAddress5000);
        test(input, output, new TestDataFactory());
    }

    @Test
    public void testReadThrowsException() throws Exception {

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        OutputStreamAppender appender = OutputStreamAppender.createAppender(
                PatternLayout.createDefaultLayout(), null, // filter
                bos, "appender", true, // follow
                false); // ignore

        appender.start();

        Logger logger = (Logger) LogManager.getLogger(UdpChannel.class);
        logger.addAppender(appender);

        String testExceptionText = "!!TEST EXCEPTION TEXT!!";

        TestUdpChannel channel = new TestUdpChannel();
        DatagramSocket socket = channel.createSocket();
        Mockito.doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws IOException {
                throw new IOException(testExceptionText);
            }
        }).when(socket).receive(Mockito.any());

        channel.connect();
        Thread.sleep(3000);
        channel.close();

        assertTrue(new String(bos.toByteArray()).contains(testExceptionText));
    }

    @Test(expected = IOException.class)
    public void testSendThrowsException() throws Exception {

        TestUdpChannel channel = new TestUdpChannel("id", IoType.OUTPUT_ONLY,
                new InetSocketAddress(1000), new InetSocketAddress(1001));
        
        DatagramSocket socket = channel.createSocket();
        Mockito.doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws IOException {
                throw new IOException();
            }
        }).when(socket).send(Mockito.any());

        channel.connect();
        channel.send(new MessageBuffer(new byte[0]));
        channel.close();
    }

    @Test
    public void testIllegalStates() throws ChannelException, InterruptedException {
        UdpChannel channel = new TestUdpChannel();
        assertFalse(channel.isConnected());

        try {
            channel.send(null);
            fail("Should have got exception");
        } catch (IllegalStateException e) {
            // expected
        }

        channel.connect();
        assertTrue(channel.isConnected());

        try {
            channel.setSocketOptions(new UdpSocketOptions());
            fail("Should have got exception");
        } catch (IllegalStateException e) {
            // expected
        }

        try {
            channel.connect();
            fail("Should have got exception");
        } catch (IllegalStateException e) {
            // expected
        }

        channel.close();
    }

    private static class TestUdpChannel extends UdpChannel {

        final DatagramSocket socket;

        public TestUdpChannel(String id, IoType ioType, InetSocketAddress local,
                InetSocketAddress remote) {
            super(id, ioType, local, remote);
            socket = Mockito.mock(DatagramSocket.class);
        }

        public TestUdpChannel() {
            this("id", IoType.INPUT_ONLY, null, null);
        }

        @Override
        protected DatagramSocket createSocket() {
            return socket;
        }
    }
}
