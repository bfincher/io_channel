package com.fincher.iochannel.udp;

import com.fincher.iochannel.ChannelException;
import com.fincher.iochannel.ChannelState;
import com.fincher.iochannel.IoType;
import com.fincher.iochannel.MessageBuffer;
import com.fincher.iochannel.SocketIoChannel;
import com.fincher.thread.MyRunnableIfc;
import com.fincher.thread.MyThread;
import com.google.common.base.Preconditions;

import java.io.IOException;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A UDP Unicast representation of a Socket IO Thread.
 * 
 * @author Brian Fincher
 * 
 */
public class UdpChannel extends SocketIoChannel {

    private static final Logger LOG = LogManager.getLogger();

    /** Used by a thread to receive messages. */
    private class ReceiveRunnable implements MyRunnableIfc {

        /** The byte array used to store received messages. */
        private final byte[] buf;

        /** The Datagram Packet used to receive messages. */
        private final DatagramPacket packet;

        /** Constructs a new ReceiveRunnable. */
        public ReceiveRunnable() {
            buf = new byte[64 * 1024];
            packet = new DatagramPacket(buf, buf.length);
        }

        @Override
        public void run() {

            try {
                socket.receive(packet);
                messageReceived(new MessageBuffer(buf, 0, packet.getLength()), LOG,
                        packet.getSocketAddress().toString());
            } catch (SocketTimeoutException ste) {
                // no action necessary
            } catch (IOException ioe) {
                LOG.error(ioe.getMessage(), ioe);
            }
        }

        @Override
        public boolean continueExecution() {
            return true;
        }

        @Override
        public void terminate() {
            // no action necessary
        }
    }

    /** For output sockets, the address to which messages will be sent. */
    protected final InetSocketAddress remoteAddress;

    /** The underlying UDP socket. */
    protected DatagramSocket socket;

    /** The thread used to receive messages. */
    private MyThread receiveThread;

    /** The UDP Socket Options. */
    protected UdpSocketOptions socketOptions = new UdpSocketOptions();

    /**
     * Constructs a new input only UDP IO Channel.
     * 
     * @param id           The ID of this IO Thread
     * @param ioType       The input/output status of this channel
     * @param localAddress The local address to which this socket will be bound. If null "localhost"
     *                     will be used
     */
    protected UdpChannel(String id, IoType ioType, InetSocketAddress localAddress) {
        super(id, ioType, localAddress);
        remoteAddress = null;
        setSocketOptions(new UdpSocketOptions());
    }

    /**
     * Constructs a new output only UDP IO Channel.
     * 
     * @param id            The ID of this IO Thread
     * @param ioType        The input/output status of this channel
     * @param localAddress  The local address to which this socket will be bound. If null
     *                      "localhost" will be used
     * @param remoteAddress The remote address to which messages will be sent
     */
    protected UdpChannel(String id, IoType ioType, InetSocketAddress localAddress,
            InetSocketAddress remoteAddress) {

        super(id, ioType, localAddress);
        this.remoteAddress = remoteAddress;
        setSocketOptions(new UdpSocketOptions());
    }

    /**
     * Creates a new input only UDP IO Channel.
     * 
     * @param id             The ID of this IO Thread
     * @param messageHandler Used to notify clients of received data
     * @param localAddress   The local address to which this socket will be bound. If null
     *                       "localhost" will be used
     * @return a new input only UDP IO Thread
     */
    public static UdpChannel createInputChannel(String id, Consumer<MessageBuffer> messageHandler,
            InetSocketAddress localAddress) {
        UdpChannel channel = new UdpChannel(id, IoType.INPUT_ONLY, localAddress);
        channel.addMessageListener(messageHandler);
        return channel;
    }

    /**
     * Creates a new input only UDP IO Thread.
     * 
     * @param id           The ID of this IO Thread
     * @param localAddress The local address to which this socket will be bound. If null "localhost"
     *                     will be used
     * @return a new input only UDP IO Thread
     */
    public static UdpChannel createInputChannel(String id, InetSocketAddress localAddress) {
        return new UdpChannel(id, IoType.INPUT_ONLY, localAddress);
    }

    /**
     * Constructs a new output only UDP IO Thread.
     * 
     * @param id            The ID of this IO Thread
     * @param localAddress  The local address to which this socket will be bound. If null
     *                      "localhost" will be used
     * @param remoteAddress The remote address to which messages will be sent
     */
    public static UdpChannel createOutputChannel(String id, InetSocketAddress localAddress,
            InetSocketAddress remoteAddress) {
        return new UdpChannel(id, IoType.OUTPUT_ONLY, localAddress, remoteAddress);
    }

    /**
     * Sets the UDP socket options for this socket.
     * 
     * @param socketOptions The TCP socket options
     */
    public void setSocketOptions(UdpSocketOptions socketOptions) {
        Preconditions.checkState(getState() == ChannelState.INITIAL,
                getId() + " The state must be INITIAL for setSocketOptions");
        this.socketOptions = socketOptions;
    }

    @Override
    public void connect() throws ChannelException, InterruptedException {
        Preconditions.checkState(getState() == ChannelState.INITIAL,
                getId() + " Illegal state for connect: " + getState());

        boolean socketCreated = false;
        while (!socketCreated) {
            try {
                socket = createSocket();
                socketCreated = true;
            } catch (BindException be) {
                LOG.warn("{} {} ", getId(), be.getMessage());
                Thread.sleep(2000);
            } catch (IOException se) {
                throw new ChannelException(getId(), se);
            }

        }

        switch (getIoType()) {
            case INPUT_ONLY:
            case INPUT_AND_OUTPUT:
                receiveThread = new MyThread(getId() + "ReceiveThread", new ReceiveRunnable());
                receiveThread.start();
                break;

            case OUTPUT_ONLY:
                // no action necessary
                break;
        }
        setState(ChannelState.CONNECTED);

        LOG.info(getId() + " Connected to local address " + socket.getLocalAddress() + " "
                + socket.getLocalPort());

        if (remoteAddress == null) {
            LOG.info("{} Remote address = null", getId());
        } else {
            LOG.info("{} Remote address = {}", getId(), remoteAddress);
        }
    }

    /**
     * Allows children classes to create specific DatagramSockets.
     * 
     * @return A DatagramSocket bound to the local address and port
     * @throws IOException If an error occurs while creating the socket
     */
    protected DatagramSocket createSocket() throws IOException {
        DatagramSocket createdSocket = new DatagramSocket(getlocalAddress());
        socketOptions.applySocketOptions(getId(), createdSocket);
        return createdSocket;
    }

    @Override
    public boolean isConnected() {
        return socket != null;
    }

    @Override
    public void close() throws ChannelException {
        if (receiveThread != null) {
            receiveThread.terminate();
        }

        if (socket != null) {
            socket.close();
            socket = null;
        }

        setState(ChannelState.CLOSED);
    }

    @Override
    public void send(MessageBuffer message) throws ChannelException {
        Preconditions.checkState(getState() == ChannelState.CONNECTED,
                "%s Cannot send on a channel that is not connected", getId());
        
        Preconditions.checkState(getIoType().isOutput(),
                "%s Cannot send on an input only channel", getId());

        logSend(LOG, message, "remote address = " + remoteAddress.toString() + " size = "
                + message.getBytes().length);
        byte[] bytes = message.getBytes();

        try {
            DatagramPacket packet = new DatagramPacket(bytes, bytes.length, remoteAddress);
            socket.send(packet);
        } catch (IOException ioe) {
            throw new ChannelException(getId(), ioe);
        }

    }
}
