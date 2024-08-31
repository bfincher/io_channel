package com.fincher.iochannel.udp;

import java.io.IOException;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.util.concurrent.Future;
import java.util.function.Consumer;

import org.slf4j.Logger;

import com.fincher.iochannel.ChannelException;
import com.fincher.iochannel.ChannelState;
import com.fincher.iochannel.IoType;
import com.fincher.iochannel.MessageBuffer;
import com.fincher.iochannel.SocketIoChannel;
import com.fincher.iochannel.Utilities;
import com.fincher.thread.LongLivedTask;
import com.fincher.thread.RunnableTask;
import com.google.common.base.Preconditions;

/**
 * A UDP Unicast representation of a Socket IO Channel.
 * 
 * @author Brian Fincher
 * 
 */
public class UdpChannel extends SocketIoChannel {

    /** Used by a task to receive messages. */
    private class ReceiveTask implements RunnableTask {

        /** The byte array used to store received messages. */
        private final byte[] buf;

        /** The Datagram Packet used to receive messages. */
        private final DatagramPacket packet;

        /** Constructs a new ReceiveTask. */
        public ReceiveTask() {
            buf = new byte[64 * 1024];
            packet = new DatagramPacket(buf, buf.length);
        }

        @Override
        public void run() {

            try {
                socket.receive(packet);
                messageReceived(new MessageBuffer(buf, 0, packet.getLength()), logger,
                        packet.getSocketAddress().toString());
            } catch (SocketTimeoutException ste) {
                // no action necessary
            } catch (IOException ioe) {
                logger.error(ioe.getMessage(), ioe);
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

    private final Logger logger = Utilities.getInstance().getLogger(UdpChannel.class);

    /** For output sockets, the address to which messages will be sent. */
    protected final InetSocketAddress remoteAddress;

    /** The underlying UDP socket. */
    protected DatagramSocket socket;

    private Future<?> receiveFuture;

    /** The UDP Socket Options. */
    protected UdpSocketOptions socketOptions = new UdpSocketOptions();

    /**
     * Constructs a new input only UDP IO Channel.
     * 
     * @param id The ID of this IO Channel
     * @param ioType The input/output status of this channel
     * @param localAddress The local address to which this socket will be bound. If
     *        null "localhost" will be used
     */
    protected UdpChannel(String id, IoType ioType, InetSocketAddress localAddress) {
        super(id, ioType, localAddress);
        remoteAddress = null;
        setSocketOptions(new UdpSocketOptions());
    }

    /**
     * Constructs a new output only UDP IO Channel.
     * 
     * @param id The ID of this IO Channel
     * @param ioType The input/output status of this channel
     * @param localAddress The local address to which this socket will be bound. If
     *        null "localhost" will be used
     * @param remoteAddress The remote address to which messages will be sent
     */
    protected UdpChannel(String id, IoType ioType, InetSocketAddress localAddress, InetSocketAddress remoteAddress) {

        super(id, ioType, localAddress);
        this.remoteAddress = remoteAddress;
        setSocketOptions(new UdpSocketOptions());
    }

    /**
     * Creates a new input only UDP IO Channel.
     * 
     * @param id The ID of this IO Channel
     * @param messageHandler Used to notify clients of received data
     * @param localAddress The local address to which this socket will be bound.
     *        If null "localhost" will be used
     * @return a new input only UDP IO Channel
     */
    public static UdpChannel createInputChannel(String id, Consumer<MessageBuffer> messageHandler,
            InetSocketAddress localAddress) {
        UdpChannel channel = new UdpChannel(id, IoType.INPUT_ONLY, localAddress);
        channel.addMessageListener(messageHandler);
        return channel;
    }

    /**
     * Creates a new input only UDP IO Channel.
     * 
     * @param id The ID of this IO Channel
     * @param localAddress The local address to which this socket will be bound. If
     *        null "localhost" will be used
     * @return a new input only UDP IO Channel
     */
    public static UdpChannel createInputChannel(String id, InetSocketAddress localAddress) {
        return new UdpChannel(id, IoType.INPUT_ONLY, localAddress);
    }

    /**
     * Constructs a new output only UDP IO Channel.
     * 
     * @param id The ID of this IO Channel
     * @param localAddress The local address to which this socket will be bound. If
     *        null "localhost" will be used
     * @param remoteAddress The remote address to which messages will be sent
     * @return The newly created channel
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
                logger.warn("{} {} ", getId(), be.getMessage());
                Thread.sleep(getBindExceptionSleepTimeMillis());
            } catch (IOException se) {
                throw new ChannelException(getId(), se);
            }

        }

        if (getIoType().isInput()) {
            receiveFuture = LongLivedTask.create(getId() + "ReceiveTask", new ReceiveTask()).start();
        }
        setState(ChannelState.CONNECTED);

        logger.info("{} Connected to local address {} {}", getId(), socket.getLocalAddress(), socket.getLocalPort());

        if (remoteAddress == null) {
            logger.info("{} Remote address = null", getId());
        } else {
            logger.info("{} Remote address = {}", getId(), remoteAddress);
        }
    }

    protected long getBindExceptionSleepTimeMillis() {
        return 2000;
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
        if (receiveFuture != null) {
            receiveFuture.cancel(true);
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

        Preconditions.checkState(getIoType().isOutput(), "%s Cannot send on an input only channel", getId());

        logSend(logger, message,
                "remote address = " + remoteAddress.toString() + " size = " + message.getBytes().length);
        byte[] bytes = message.getBytes();

        try {
            DatagramPacket packet = new DatagramPacket(bytes, bytes.length, remoteAddress);
            socket.send(packet);
        } catch (IOException ioe) {
            throw new ChannelException(getId(), ioe);
        }

    }
}
