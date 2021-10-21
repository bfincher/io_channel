package com.fincher.iochannel.udp;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

import org.junit.jupiter.api.Test;

import com.fincher.iochannel.ChannelException;
import com.fincher.iochannel.IoChannelTesterBase;
import com.fincher.iochannel.MessageBuffer;
import com.fincher.iochannel.TestAnswer;
import com.fincher.iochannel.udp.UdpTest.TestDataFactory;

public class MulticastTest extends IoChannelTesterBase<MessageBuffer> {

    @Test
    public void test() throws UnknownHostException {
        InetSocketAddress localAddress0 = new InetSocketAddress(InetAddress.getByName("0.0.0.0"), 0);
        InetSocketAddress localAddress5000 = new InetSocketAddress(InetAddress.getByName("0.0.0.0"), 5000);
        InetSocketAddress multicastAddress = new InetSocketAddress(InetAddress.getByName("239.1.1.1"), 5000);

        UdpMulticastChannel output = UdpMulticastChannel.createOutputChannel("output", localAddress0, multicastAddress);

        UdpMulticastChannel input = UdpMulticastChannel.createInputChannel("input", messageQueue::add, localAddress5000,
                multicastAddress.getAddress());

        test(input, output, new TestDataFactory());
    }

    @Test
    public void testConnectThrowsIOException() throws Exception {
        TestChannel channel = new TestChannel();
        doAnswer(new TestAnswer(new IOException())).when(channel.socket).joinGroup(any());
        assertThrows(ChannelException.class, () -> channel.connect());
        channel.close();
    }

    @Test
    public void testConstructors() throws UnknownHostException {
        InetAddress mcAddr = InetAddress.getByName("230.0.0.0");
        InetAddress localhost = InetAddress.getByName("127.0.0.1");
        InetSocketAddress localAddr0 = InetSocketAddress.createUnresolved("0.0.0.0", 0);
        InetSocketAddress localAddr1234 = InetSocketAddress.createUnresolved("0.0.0.0", 1234);

        assertThrows(IllegalArgumentException.class, () -> new UdpMulticastChannel("", null, mcAddr));

        assertThrows(IllegalArgumentException.class, () -> new UdpMulticastChannel("", localAddr0, mcAddr));

        assertThrows(IllegalArgumentException.class, () -> new UdpMulticastChannel("", localAddr1234, localhost));
    }

    private static class TestChannel extends UdpMulticastChannel {

        MulticastSocket socket = mock(MulticastSocket.class);

        TestChannel() throws UnknownHostException {
            super("", InetSocketAddress.createUnresolved("0.0.0.0", 4589), InetAddress.getByName("227.0.0.1"));
        }

        @Override
        public DatagramSocket createSocket() {
            return socket;
        }

    }

}
