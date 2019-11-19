package com.fincher.io_channel.tcp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import com.fincher.io_channel.ChannelException;
import com.fincher.io_channel.IoChannelDataTypeEnum;
import com.fincher.io_channel.IoChannelTesterBase;
import com.fincher.io_channel.MessageBuffer;
import com.google.common.io.Closer;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import org.awaitility.Awaitility;
import org.junit.Test;

/** A JUnit tester for TCP sockets */
public class TcpTest extends IoChannelTesterBase<MessageBuffer> {

    @FunctionalInterface
    private interface ChannelFactory {
        public TcpChannel createChannel(String id) throws ChannelException;
    }

    private BlockingQueue<MessageBuffer> queue1 = new LinkedBlockingQueue<MessageBuffer>();
    private BlockingQueue<MessageBuffer> queue2 = new LinkedBlockingQueue<MessageBuffer>();

    private void test(TcpClientChannel client1, TcpClientChannel client2,
            ChannelFactory channelFactory) throws Exception {
        
        assertEquals(0, client1.getlocalAddress().getPort());

        SimpleStreamIO streamIo = new SimpleStreamIO();

        assertEquals(IoChannelDataTypeEnum.RAW_DATA, client1.getDataType());
        assertTrue(client1.isInput());
        assertTrue(client2.isInput());
        assertTrue(client1.isOutput());
        assertTrue(client2.isOutput());

        try (Closer closer = Closer.create()) {

            BlockingQueue<MessageBuffer> queue3 = new LinkedBlockingQueue<>();

            Consumer<MessageBuffer> msgListener = queue3::add;
            client2.addMessageListener(msgListener);

            AtomicBoolean client1Connected = new AtomicBoolean(false);
            AtomicBoolean client2Connected = new AtomicBoolean(true);
            
            ConnectionEstablishedListener client1ConnectionListener = id -> client1Connected.set(true);
            ConnectionEstablishedListener client2ConnectionListener = id -> client2Connected.set(true);
            
            client1.addConnectionEstablishedListener(client1ConnectionListener);
            client2.addConnectionEstablishedListener(client2ConnectionListener);

            closer.register(client1);
            closer.register(client2);
            client1.connect();
            TcpChannel server = closer.register(channelFactory.createChannel("sender"));
            server.connect();
            assertTrue(server.isOutput());
            assertFalse(server.isInput());
            
            // connect client1 first so that we can get it's id in the server connection established listener
            client2.connect();

            Awaitility.await().until(() -> client1.isConnected() && client2.isConnected());
            
            assertFalse(client1.getlocalAddress().getPort() == 0);

            assertTrue(client1Connected.get());
            assertTrue(client2Connected.get());

            for (int i = 0; i < 5; i++) {
                MessageBuffer mb = new MessageBuffer(
                        streamIo.prePendLength(new String("Hello World " + i).getBytes()));

                server.send(mb.getBytes());
            }

            Awaitility.await().until(() -> queue1.size() == 5);
            assertEquals(5, queue1.size());
            assertEquals(5, queue2.size());
            assertEquals(5, queue3.size());

            client2.removeMessageListener(msgListener);

            server.close();
            client1Connected.set(false);
            client2Connected.set(false);
            client2.removeConnectionEstablishedListener(client2ConnectionListener);
            server = closer.register(channelFactory.createChannel("sender"));
            server.connect();

            Awaitility.await().until(() -> client1.isConnected() && client2.isConnected());
            
            assertTrue(client1Connected.get());
            assertFalse(client2Connected.get()); // should be false since we removed the listener

            System.out.println(client1.isConnected() + " " + client2.isConnected());

            for (int i = 5; i < 10; i++) {
                server.send(new MessageBuffer(
                        streamIo.prePendLength(new String("Hello World " + i).getBytes())));
            }

            Awaitility.await().until(() -> queue1.size() == 10);

            assertEquals(10, queue1.size());
            assertEquals(10, queue2.size());
            assertEquals(5, queue3.size());

            server.send(new MessageBuffer(streamIo.prePendLength(new String("test").getBytes())), "*");
            Awaitility.await().until(() -> queue1.size() == 11);
            assertEquals(11, queue1.size());
            assertEquals(11, queue2.size());
            
            System.out.println("*****************" + server.getSocketIds());
            
            server.send(new MessageBuffer(streamIo.prePendLength(new String("test").getBytes())), 
                    server.getSocketIds().get(0));
            
            server.send(new MessageBuffer(streamIo.prePendLength(new String("test").getBytes())), 
                    server.getSocketIds().get(1));
            
            Awaitility.await().until(() -> queue1.size() == 12);
            assertEquals(12, queue1.size());
            assertEquals(12, queue2.size());
            
            try {
                server.send(new MessageBuffer(new byte[0]), "no channel id");
                fail();
            } catch (ChannelException e) {
                // expected
            }
        }
    }

    /**
     * Test TCP sockets
     * 
     */
    @Test // (timeout = 10000)
    public void test() throws Exception {
        final InetSocketAddress localAddress5000 = new InetSocketAddress(InetAddress.getLocalHost(),
                5000);

        InetSocketAddress remoteAddress = new InetSocketAddress(InetAddress.getLocalHost(), 5000);

        InetSocketAddress localAddress0 = new InetSocketAddress(InetAddress.getLocalHost(), 0);

        ChannelFactory createServer = (id) -> {
            return TcpServerChannel.createOutputOnly(id, new SimpleStreamIO(), localAddress5000);
        };

        TcpClientChannel client1 = TcpClientChannel.createChannel("client1", new SimpleStreamIO(),
                localAddress0, remoteAddress);

        client1.addMessageListener(queue1::add);

        TcpClientChannel client2 = TcpClientChannel.createChannel("client2", queue2::add,
                new SimpleStreamIO(), null, remoteAddress);

        test(client1, client2, createServer);
    }

    @Test
    public void testClientSendingToServer() throws Exception {
        SimpleStreamIO streamIo = new SimpleStreamIO(true);

        InetSocketAddress address5001 = new InetSocketAddress(InetAddress.getLocalHost(), 5001);
        InetSocketAddress localAddress0 = new InetSocketAddress(InetAddress.getLocalHost(), 0);

        LinkedBlockingQueue<MessageBuffer> queue = new LinkedBlockingQueue<>();

        TcpServerChannel server = TcpServerChannel.createChannel("server", queue::add, streamIo,
                address5001);
        TcpClientChannel client = TcpClientChannel.createOutputOnlyChannel("client", streamIo,
                localAddress0, address5001);
        
        client.connect();
        // try to send before connecting
        client.send(new byte[0]);
        server.connect();

        Awaitility.await().until(() -> client.isConnected() && server.isConnected());
        
        assertEquals(1, client.getNumConnections());

        for (byte i = 0; i < 5; i++) {
            byte[] bytes = { i };
            client.send(new MessageBuffer(streamIo.prePendLength(bytes)));
        }

        Awaitility.await().until(() -> queue.size() == 5);

        assertEquals(5, queue.size());

        for (byte i = 0; i < 5; i++) {
            assertEquals(i, queue.take().getBytes()[4]);
        }
    }

    @Test
    public void testOffNominal() throws Exception {
        InetSocketAddress address5001 = new InetSocketAddress(InetAddress.getLocalHost(), 5001);
        TcpServerChannel server = TcpServerChannel.createOutputOnly("server", new SimpleStreamIO(),
                address5001);
        server.connect();

        try {
            ReceiveRunnableFactoryIfc factory = mock(ReceiveRunnableFactoryIfc.class);
            server.setReceiveRunnableFactory(factory);
            fail("Should have got exception");
        } catch (IllegalStateException e) {
            // expected
        }

        try {
            TcpSocketOptions options = mock(TcpSocketOptions.class);
            server.setSocketOptions(options);
            fail("Should have got exception");
        } catch (IllegalStateException e) {
            // expected
        }

        try {
            server.connect();
            fail("Should have got exception");
        } catch (IllegalStateException e) {
            // expected
        }

        server.close();
    }
}
