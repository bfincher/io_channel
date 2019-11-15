package com.fincher.io_channel.tcp;

import static org.junit.Assert.assertEquals;

import com.fincher.io_channel.ChannelException;
import com.fincher.io_channel.IoChannelTesterBase;
import com.fincher.io_channel.MessageBuffer;
import com.google.common.io.Closer;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

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
            ChannelFactory channelFactory) throws ChannelException, InterruptedException {
        Closer closer = Closer.create();

        TcpChannel server = closer.register(channelFactory.createChannel("sender"));

        closer.register(client1);
        closer.register(client2);
        client1.connect();
        client2.connect();
        server.connect();

        Awaitility.await().until(() -> client1.isConnected() && client2.isConnected());

        for (int i = 0; i < 5; i++) {
            server.send(new MessageBuffer(
                    SimpleStreamIO.prePendLength(new String("Hello World " + i).getBytes())));
        }

        Awaitility.await().until(() -> queue1.size() == 5);

        server.close();
        server = closer.register(channelFactory.createChannel("sender"));
        server.connect();

        Awaitility.await().until(() -> client1.isConnected() && client2.isConnected());

        System.out.println(client1.isConnected() + " " + client2.isConnected());

        for (int i = 5; i < 10; i++) {
            server.send(new MessageBuffer(
                    SimpleStreamIO.prePendLength(new String("Hello World " + i).getBytes())));
        }

        Awaitility.await().until(() -> queue1.size() == 10);

        assertEquals(10, queue1.size());
        assertEquals(10, queue2.size());

        System.out.println("client1's messages: ");
        for (MessageBuffer mb : queue1) {
            System.out.println("    " + new String(mb.getBytes()));
        }

        System.out.println("client2's messages: ");
        for (MessageBuffer mb : queue2) {
            System.out.println("    " + new String(mb.getBytes()));
        }
    }

    /**
     * Test TCP sockets
     * 
     */
    @Test(timeout = 10000)
    public void test() throws ChannelException, InterruptedException, UnknownHostException {
        final InetSocketAddress localAddress5000 = new InetSocketAddress(InetAddress.getLocalHost(),
                5000);

        InetSocketAddress remoteAddress = new InetSocketAddress(InetAddress.getLocalHost(), 5000);

        InetSocketAddress localAddress0 = new InetSocketAddress(InetAddress.getLocalHost(), 0);

        ChannelFactory createServer = (id) -> {
            return new TcpServerChannel(id, new SimpleStreamIO(), localAddress5000);
        };

        TcpClientChannel client1 = new TcpClientChannel("client1", queue1::add,
                new SimpleStreamIO(), localAddress0, remoteAddress);

        TcpClientChannel client2 = new TcpClientChannel("client2", queue2::add,
                new SimpleStreamIO(), localAddress0, remoteAddress);

        test(client1, client2, createServer);
    }

    public void testClientSendingToServer() throws Exception {
        final InetSocketAddress localAddress5000 = new InetSocketAddress(InetAddress.getLocalHost(),
                5000);
        InetSocketAddress address5001 = new InetSocketAddress(InetAddress.getLocalHost(), 5001);
        InetSocketAddress localAddress0 = new InetSocketAddress(InetAddress.getLocalHost(), 0);
        
        LinkedBlockingQueue<MessageBuffer> queue = new LinkedBlockingQueue<>();
        
        TcpServerChannel server = TcpServerChannel.createChannel("server", queue::add, new SimpleStreamIO(), address5001);
        TcpClientChannel client = TcpClientChannel.createOutputOnlyChannel("client", new SimpleStreamIO(), localAddress0, address5001);
    }
}
