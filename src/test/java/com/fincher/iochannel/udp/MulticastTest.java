package com.fincher.iochannel.udp;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.fincher.iochannel.ChannelException;
import com.fincher.iochannel.IoChannelTesterBase;
import com.fincher.iochannel.MessageBuffer;
import com.fincher.iochannel.TestAnswer;
import com.fincher.iochannel.udp.UdpTest.TestDataFactory;
import com.google.common.base.Preconditions;

public class MulticastTest extends IoChannelTesterBase<MessageBuffer> {

    private static NetworkInterface networkInterface;
    private static InetAddress address;

    @BeforeAll
    public static void beforeAll() throws Exception {
        System.setProperty("java.net.preferIPv4Stack", "true");
        System.setProperty("java.net.preferIPv4Addresses", "true");

        networkInterface = null;

        List<NetworkInterface> ifcs = NetworkInterface.networkInterfaces().collect(Collectors.toList());
        for (NetworkInterface ifc : ifcs) {
            if (ifc.isUp() && ifc.supportsMulticast()) {
                networkInterface = ifc;
            }
        }

        Preconditions.checkNotNull(networkInterface);

        for (InterfaceAddress addr : networkInterface.getInterfaceAddresses()) {
            if (addr.getAddress() instanceof Inet4Address) {
                address = addr.getAddress();
            }
        }

        Preconditions.checkNotNull(address);

        System.out.println("Using network interface " + networkInterface + " " + address);
    }

    @AfterAll
    public static void afterAll() {
        System.clearProperty("java.net.preferIPv4Stack");
    }

    @Test
    public void test() throws UnknownHostException {
        InetSocketAddress localAddress0 = new InetSocketAddress(address, 0);
        InetSocketAddress localAddress5000 = new InetSocketAddress(address, 5000);
        InetSocketAddress multicastAddress = new InetSocketAddress(Inet4Address.getByName("239.1.1.1"), 5000);

        UdpMulticastChannel output = UdpMulticastChannel.createOutputChannel("output", localAddress0, multicastAddress);

        UdpMulticastChannel input = UdpMulticastChannel.createInputChannel("input", messageQueue::add, localAddress5000,
                multicastAddress.getAddress(), networkInterface);

        test(input, output, new TestDataFactory());
    }

    @Test
    public void testConnectThrowsIOException() throws Exception {
        TestChannel channel = new TestChannel();
        doAnswer(new TestAnswer(new IOException())).when(channel.socket).joinGroup(any(), any());
        assertThrows(ChannelException.class, () -> channel.connect());
        channel.close();
    }

    @Test
    public void testConstructors() throws UnknownHostException {
        InetAddress mcAddr = Inet4Address.getByName("230.0.0.0");
        InetAddress localhost = Inet4Address.getByName("127.0.0.1");
        InetSocketAddress localAddr0 = InetSocketAddress.createUnresolved("0.0.0.0", 0);
        InetSocketAddress localAddr1234 = InetSocketAddress.createUnresolved("0.0.0.0", 1234);

        assertThrows(IllegalArgumentException.class, () -> new UdpMulticastChannel("", null, mcAddr, networkInterface));

        assertThrows(IllegalArgumentException.class,
                () -> new UdpMulticastChannel("", localAddr0, mcAddr, networkInterface));

        assertThrows(IllegalArgumentException.class,
                () -> new UdpMulticastChannel("", localAddr1234, localhost, networkInterface));
    }

    private class TestChannel extends UdpMulticastChannel {

        MulticastSocket socket = mock(MulticastSocket.class);

        TestChannel() throws UnknownHostException {
            super("", InetSocketAddress.createUnresolved("0.0.0.0", 4589), InetAddress.getByName("227.0.0.1"),
                    networkInterface);
        }

        @Override
        public DatagramSocket createSocket() {
            return socket;
        }

    }

}
