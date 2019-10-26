package com.fincher.io_channel.udp;

import com.fincher.io_channel.ChannelException;
import com.fincher.io_channel.MessageBuffer;
import com.google.common.base.Preconditions;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A UDP Multicast representation of a Socket IO Thread
 * 
 * @author Brian Fincher
 *
 */
public class UdpMulticastChannel extends UdpChannel {

    private static final Logger LOG = LoggerFactory.getLogger(UdpMulticastChannel.class);

    private final InetAddress multicastAddress;

    /**
     * Constructs a new input only UDP MULTICAST IO Thread
     * 
     * @param id               The ID of this IO Thread
     * @param messageHandler   Used to notify clients of received data
     * @param localAddress     The local address to which this socket will be bound.
     * @param multicastAddress The multicast address to which this socket will join
     */
    protected UdpMulticastChannel(String id, Consumer<MessageBuffer> messageHandler,
            InetSocketAddress localAddress, InetAddress multicastAddress) {
        super(id, messageHandler, localAddress);

        Preconditions.checkArgument(localAddress != null && localAddress.getPort() != 0,
                id + " localAddress port must be non zero");
        
        Preconditions.checkArgument(multicastAddress.isMulticastAddress(),
                "The multicast address given is not a valid multicast address");

        this.multicastAddress = multicastAddress;

        socketOptions = new UdpMulticastSocketOptions();
    }

    /**
     * Constructs a new output only UDP IO Thread
     * 
     * @param id               The ID of this IO Thread
     * @param localAddress     The local address to which this socket will be bound.
     * @param multicastAddress The remote multicast address to which messages will be sent
     */
    protected UdpMulticastChannel(String id, InetSocketAddress localAddress,
            InetSocketAddress multicastAddress) {

        super(id, localAddress, multicastAddress);

        this.multicastAddress = multicastAddress.getAddress();

        socketOptions = new UdpMulticastSocketOptions();

        Preconditions.checkArgument(multicastAddress.getAddress().isMulticastAddress(),
                "The multicast address given is not a valid multicast address");
    }

    /**
     * Creates a new input only UDP MULTICAST IO Thread
     * 
     * @param id               The ID of this IO Thread
     * @param messageHandler   Used to notify clients of received data
     * @param localAddress     The local address to which this socket will be bound.
     * @param multicastAddress The multicast address to which this socket will join
     * @return a new input only UDP MULTICAST IO Thread
     */
    public static UdpMulticastChannel createInputChannel(String id,
            Consumer<MessageBuffer> messageHandler, InetSocketAddress localAddress,
            InetAddress multicastAddress) {
        return new UdpMulticastChannel(id, messageHandler, localAddress, multicastAddress);
    }

    /**
     * Creates a new input only UDP MULTICAST IO Thread
     * 
     * @param id               The ID of this IO Thread
     * @param localAddress     The local address to which this socket will be bound.
     * @param multicastAddress The multicast address to which this socket will join
     * @return a new input only UDP MULTICAST IO Thread
     */
    public static UdpMulticastChannel createInputChannel(String id, InetSocketAddress localAddress,
            InetAddress multicastAddress) {
        return new UdpMulticastChannel(id, null, localAddress, multicastAddress);
    }

    /**
     * Creates a new output only UDP IO Thread
     * 
     * @param id               The ID of this IO Thread
     * @param localAddress     The local address to which this socket will be bound.
     * @param multicastAddress The remote multicast address to which messages will be sent
     * @return Creates a new output only UDP IO Thread
     */
    public static UdpMulticastChannel createOutputChannel(String id, InetSocketAddress localAddress,
            InetSocketAddress multicastAddress) {
        return new UdpMulticastChannel(id, localAddress, multicastAddress);
    }

    @Override
    /** Connects this UDP Multicast IO thread */
    public void connect() throws ChannelException, InterruptedException {
        super.connect();

        switch (getIoType()) {
            case INPUT_ONLY:
            case INPUT_AND_OUTPUT:
                try {
                    ((MulticastSocket) socket).joinGroup(multicastAddress);
                    LOG.info(getId() + " joined multicast group "
                            + multicastAddress.getHostAddress());
                } catch (IOException se) {
                    throw new ChannelException(getId(), se);
                }
                break;

            case OUTPUT_ONLY:
                // no action necessary
        }
    }

    /**
     * Creates a new Multicast Socket bound to the local address and port
     * 
     * @return A MulticastSocket bound to the local address and port
     * @throws SocketException
     * @throws ChannelException
     */
    @Override
    protected DatagramSocket createSocket() throws IOException {
        MulticastSocket socket = new MulticastSocket(getlocalAddress());

        UdpMulticastSocketOptions socketOptions = (UdpMulticastSocketOptions) this.socketOptions;
        socketOptions.applySocketOptions(getId(), socket);

        return socket;
    }

}
