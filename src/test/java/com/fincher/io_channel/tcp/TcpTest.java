package com.fincher.io_channel.tcp;

import static org.junit.Assert.assertEquals;

import com.fincher.io_channel.ChannelException;
import com.fincher.io_channel.IoChannelTesterBase;
import com.fincher.io_channel.MessageBuffer;
import com.fincher.io_channel.QueueMessageHandler;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.awaitility.Awaitility;
import org.junit.Test;

/** A JUnit tester for TCP sockets */
public class TcpTest extends IoChannelTesterBase<MessageBuffer> {

    private abstract class TcpServerFactory {
        public abstract TcpServerChannel createTcpServer() throws ChannelException;
    }

    private BlockingQueue<MessageBuffer> queue1 = new LinkedBlockingQueue<MessageBuffer>();
    private BlockingQueue<MessageBuffer> queue2 = new LinkedBlockingQueue<MessageBuffer>();

    private void test(TcpClientChannel client1, TcpClientChannel client2,
            TcpServerFactory tcpServerFactory) throws ChannelException, InterruptedException {
        TcpServerChannel server = tcpServerFactory.createTcpServer();
        server.connect();
        client1.connect();
        client2.connect();

        Awaitility.await().until(() -> client1.isConnected() && client2.isConnected());

        for (int i = 0; i < 5; i++) {
            server.send(new MessageBuffer(
                    SimpleStreamIO.prePendLength(new String("Hello World " + i).getBytes())));
        }
        
        Awaitility.await().until(() -> queue1.size() == 5);

        server.close();
        server = tcpServerFactory.createTcpServer();
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

        Logger.getRootLogger().addAppender(new ConsoleAppender(new SimpleLayout()));

        final InetSocketAddress localAddress5000 = new InetSocketAddress(
                InetAddress.getLocalHost(), 5000);

        InetSocketAddress remoteAddress = new InetSocketAddress(InetAddress.getLocalHost(),
                5000);

        InetSocketAddress localAddress0 = new InetSocketAddress(InetAddress.getLocalHost(), 0);
        TcpClientChannel client1 = new TcpClientChannel("client1",
                new QueueMessageHandler<MessageBuffer>(queue1), new SimpleStreamIO(),
                localAddress0, remoteAddress);

        TcpClientChannel client2 = new TcpClientChannel("client2",
                new QueueMessageHandler<MessageBuffer>(queue2), new SimpleStreamIO(),
                localAddress0, remoteAddress);

        test(client1, client2, new TcpServerFactory() {

            @Override
            public TcpServerChannel createTcpServer() throws ChannelException {
                return new TcpServerChannel("server", new SimpleStreamIO(), localAddress5000);
            }
        });
    }
}
