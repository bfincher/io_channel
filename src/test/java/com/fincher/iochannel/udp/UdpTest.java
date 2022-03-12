package com.fincher.iochannel.udp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.BindException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;

import com.fincher.iochannel.ChannelException;
import com.fincher.iochannel.ChannelState;
import com.fincher.iochannel.IoChannelTesterBase;
import com.fincher.iochannel.IoType;
import com.fincher.iochannel.MessageBuffer;
import com.fincher.iochannel.TestDataFactoryIfc;
import com.fincher.iochannel.TestUtilities;
import com.fincher.iochannel.Utilities;

/**
 * Test UDP sockets
 * 
 * @author Brian Fincher
 *
 */
public class UdpTest extends IoChannelTesterBase<MessageBuffer> {

    /** Method name is self explainatory */
    @AfterAll
    public static void tearDown() {
        // DEST_UNICAST_CONFIG.delete();
    }

    static class TestDataFactory implements TestDataFactoryIfc<MessageBuffer> {
        @Override
        public String toString(MessageBuffer data) {
            return new String(data.getBytes());
        }

        @Override
        public MessageBuffer getTestData(int iteration) {
            return new MessageBuffer(new String("Hello World " + iteration).getBytes());
        }
    }

    @Test
    @Timeout(value = 10)
    public void testUnicast() throws Exception {
        InetSocketAddress localAddress0 = new InetSocketAddress(InetAddress.getByName("0.0.0.0"), 0);
        InetSocketAddress localAddress5000 = new InetSocketAddress(InetAddress.getByName("localhost"), 5000);

        UdpChannel output = UdpChannel.createOutputChannel("output", localAddress0, localAddress5000);

        UdpChannel input = UdpChannel.createInputChannel("input", messageQueue::add, localAddress5000);
        test(input, output, new TestDataFactory());
    }

    @Test
    @Timeout(value = 10)
    public void testConnectThrowsIOException() throws Exception {
        TestUdpChannel channel = new TestUdpChannel();
        channel.createSocketThrowsIOException = true;
        assertThrows(ChannelException.class, () -> channel.connect());
        channel.close();
    }

    @Test
    @Timeout(value = 10)
    public void testConnectThrowsBindException() throws Exception {
        TestUdpChannel channel = new TestUdpChannel();
        channel.createSocketThrowsBindException = true;

        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(channel.bindExceptionSleepTimeMillis * 4 + 100);
                channel.createSocketThrowsBindException = false;
            } catch (InterruptedException ie) {
                throw new RuntimeException(ie);
            }
        });

        Awaitility.await().atLeast(Duration.ofMillis(channel.bindExceptionSleepTimeMillis * 4))
                .atMost(Duration.ofMillis(channel.bindExceptionSleepTimeMillis * 4 + 1000)).until(() -> {
                    channel.connect();
                    return true;
                });

        channel.close();
    }

    @Test
    @Timeout(value = 10)
    public void testReadThrowsException() throws Exception {

        Utilities origUtilities = Utilities.getInstance();
        try {
            Utilities.setInstanceForTesting(new TestUtilities());
            Logger logger = Utilities.getInstance().getLogger(UdpChannel.class);
            when(logger.isInfoEnabled()).thenReturn(true);

            String testExceptionText = "!!TEST EXCEPTION TEXT!!";

            TestUdpChannel channel = new TestUdpChannel();
            DatagramSocket socket = channel.createSocket();
            doAnswer(new Answer<Void>() {
                @Override
                public Void answer(InvocationOnMock invocation) throws IOException {
                    throw new IOException(testExceptionText);
                }
            }).when(socket).receive(any());

            AtomicReference<IOException> loggedException = new AtomicReference<>();

            doAnswer(new Answer<Void>() {
                @Override
                public Void answer(InvocationOnMock invocation) {
                    loggedException.set(invocation.getArgument(1, IOException.class));
                    return null;
                }
            }).when(logger).error(anyString(), any(IOException.class));

            channel.connect();

            Awaitility.await().until(() -> loggedException.get() != null);

            assertTrue(loggedException.get().getMessage().contains(testExceptionText));

            channel.close();
        } finally {
            Utilities.setInstanceForTesting(origUtilities);
        }
    }

    @Test
    @Timeout(value = 10)
    public void testSendThrowsException() throws Exception {

        TestUdpChannel channel = new TestUdpChannel("id", IoType.OUTPUT_ONLY, new InetSocketAddress(1000),
                new InetSocketAddress(1001));

        DatagramSocket socket = channel.createSocket();
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws IOException {
                throw new IOException();
            }
        }).when(socket).send(any());

        channel.connect();
        assertThrows(IOException.class, () -> channel.send(new MessageBuffer(new byte[0])));
        channel.close();
    }

    @Test
    @Timeout(value = 10)
    public void testIllegalStates() throws ChannelException, InterruptedException {
        UdpChannel channel = new TestUdpChannel();
        assertFalse(channel.isConnected());

        // test send on a channel not connected
        try {
            channel.send(null);
            fail("Should have got exception");
        } catch (IllegalStateException e) {
            assertEquals("id Cannot send on a channel that is not connected", e.getMessage());
        }

        channel.connect();
        assertTrue(channel.isConnected());

        // test send on an input only channel
        try {
            channel.send(null);
            fail("Should have got exception");
        } catch (IllegalStateException e) {
            assertEquals("id Cannot send on an input only channel", e.getMessage());
        }
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

    @Test
    @Timeout(value = 10)
    public void testOffNominal() throws Exception {
        UdpChannel channel = UdpChannel.createInputChannel("id", null);
        channel.close();
        assertEquals(ChannelState.CLOSED, channel.getState());
    }

    private static class TestUdpChannel extends UdpChannel {

        final DatagramSocket socket;

        boolean createSocketThrowsIOException = false;
        boolean createSocketThrowsBindException = false;
        long bindExceptionSleepTimeMillis = 100;

        public TestUdpChannel(String id, IoType ioType, InetSocketAddress local, InetSocketAddress remote) {
            super(id, ioType, local, remote);
            socket = mock(DatagramSocket.class);
        }

        public TestUdpChannel() {
            this("id", IoType.INPUT_ONLY, null, null);
        }

        @Override
        protected DatagramSocket createSocket() throws IOException {
            if (createSocketThrowsIOException) {
                throw new IOException();
            }
            if (createSocketThrowsBindException) {
                throw new BindException();
            }
            return socket;
        }

        @Override
        protected long getBindExceptionSleepTimeMillis() {
            return bindExceptionSleepTimeMillis;
        }
    }
}
