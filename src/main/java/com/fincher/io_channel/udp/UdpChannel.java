package com.fincher.io_channel.udp;

import com.fincher.io_channel.ChannelException;
import com.fincher.io_channel.IoTypeEnum;
import com.fincher.io_channel.MessageBuffer;
import com.fincher.io_channel.SocketIoChannel;
import com.fincher.io_channel.StateEnum;
import com.fincher.thread.DataHandlerIfc;
import com.fincher.thread.MyRunnableIfc;
import com.fincher.thread.MyThread;

import java.io.IOException;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import org.apache.log4j.Logger;

/**
 * A UDP Unicast representation of a Socket IO Thread
 * 
 * @author Brian Fincher
 * 
 */
public class UdpChannel extends SocketIoChannel {

    private static Logger logger = Logger.getLogger(UdpChannel.class);

    /** Used by a thread to receive messages */
    private class ReceiveRunnable implements MyRunnableIfc {

        /** The byte array used to store received messages */
        private final byte[] buf;

        /** The Datagram Packet used to receive messages */
        private final DatagramPacket packet;

        /** Constructs a new ReceiveRunnable */
        public ReceiveRunnable() {
            buf = new byte[64 * 1024];
            packet = new DatagramPacket(buf, buf.length);
        }

        @Override
        /** The thread body */
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
        /**
         * Should this thread continue to execute.
         * 
         * @return true
         */
        public boolean continueExecution() {
            return true;
        }

        @Override
        /** No action taken */
        public void terminate() {

        }
    }

    /** For output sockets, the address to which messages will be sent */
    protected final InetSocketAddress remoteAddress;

    /** The underlying UDP socket */
    protected DatagramSocket socket;

    /** The thread used to receive messages */
    private MyThread receiveThread;

    /** The UDP Socket Options */
    protected UdpSocketOptions socketOptions = new UdpSocketOptions();

    /**
     * Creates a new output only UDPIOChannel from JAXB XML configuration
     * 
     * @param config The JAXB XML configuration
     * @return A new UDPIOChannel
     * @throws ChannelException
     */
//    public static UdpChannel createUDPChannel(OutputUDPIOChannelType config)
//            throws ChannelException {
//
//        if (config.getRemoteAddress() == null) {
//            throw new ChannelException(config.getId() + " remote-address must be set");
//        }
//
//        InetSocketAddress remoteAddress;
//        try {
//            remoteAddress = new InetSocketAddress(
//                    InetAddress.getByName(config.getRemoteAddress().getHost()),
//                    config.getRemoteAddress().getPort());
//        } catch (UnknownHostException uhe) {
//            throw new ChannelException(uhe);
//        }
//
//        UdpChannel channel = new UdpChannel(config.getId(), messageFormat,
//                SocketIoChannel.getLocalAddress(config), remoteAddress);
//
//        if (config.getSocketOptions() != null) {
//            UdpSocketOptions socketOptions = new UdpSocketOptions();
//            SocketOptions.getSocketOptions(socketOptions, config.getSocketOptions());
//            channel.setSocketOptions(socketOptions);
//        }
//
//        loadFromConfig(channel, config);
//
//        return channel;
//
//    }

    /**
     * Creates a new input only UDPIOChannel from XML configuration
     * 
     * @param config         The XML configuration
     * @param messageFormat  The message format for this IoChannel
     * @param messageHandler Used to notify clients of received data. May be null if this is an
     *                       output only channel
     * @return A new UDPIOChannel
     * @throws ChannelException
     */
//    public static UdpChannel createUDPChannel(InputUDPIOChannelType config,
//            MessageFormatEnum messageFormat, MessageHandlerIfc<MessageBuffer> messageHandler)
//            throws ChannelException {
//
//        if (config.getLocalAddress() == null) {
//            throw new ChannelException(config.getId() + " local-address must be set");
//        }
//
//        UdpChannel channel = new UdpChannel(config.getId(), messageFormat, messageHandler,
//                getLocalAddress(config));
//
//        if (config.getSocketOptions() != null) {
//            UdpSocketOptions socketOptions = new UdpSocketOptions();
//            SocketOptions.getSocketOptions(socketOptions, config.getSocketOptions());
//            channel.setSocketOptions(socketOptions);
//        }
//
//        loadFromConfig(channel, config);
//
//        return channel;
//    }

    /**
     * Constructs a new input only UDP IO Thread
     * 
     * @param id             The ID of this IO Thread
     * @param messageHandler Used to notify clients of received data
     * @param localAddress   The local address to which this socket will be bound. If null
     *                       "localhost" will be used
     */
    public UdpChannel(String id, DataHandlerIfc<MessageBuffer> messageHandler,
            InetSocketAddress localAddress) {
        super(id, IoTypeEnum.INPUT_ONLY, messageHandler, localAddress);

        remoteAddress = null;
    }

    /**
     * Constructs a new output only UDP IO Thread
     * 
     * @param id            The ID of this IO Thread
     * @param localAddress  The local address to which this socket will be bound. If null
     *                      "localhost" will be used
     * @param remoteAddress The remote address to which messages will be sent
     */
    public UdpChannel(String id, InetSocketAddress localAddress, InetSocketAddress remoteAddress) {

        super(id, IoTypeEnum.OUTPUT_ONLY, localAddress);

        this.remoteAddress = remoteAddress;
    }

    /**
     * Sets the UDP socket options for this socket
     * 
     * @param socketOptions The TCP socket options
     */
    public void setSocketOptions(UdpSocketOptions socketOptions) {
        if (getState() != StateEnum.INITIAL) {
            throw new IllegalStateException(
                    getId() + " The state must be INITIAL for setSocketOptions");
        }
        this.socketOptions = socketOptions;
    }

    @Override
    /** Connects this UDP IO thread */
    public void connect() throws ChannelException, InterruptedException {

        switch (getState()) {
            case INITIAL:
                break;

            default:
                throw new IllegalStateException(
                        getId() + " Illegal state for connect: " + getState());
        }

        boolean socketCreated = false;
        while (!socketCreated) {
            try {
                socket = createSocket();
                socketCreated = true;
            } catch (BindException be) {
                logger.warn(getId() + " " + be.getMessage());
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
        setState(StateEnum.CONNECTED);

        logger.info(getId() + " Connected to local address " + socket.getLocalAddress() + " "
                + socket.getLocalPort());

        if (remoteAddress == null) {
            logger.info(getId() + " Remote address = null");
        } else {
            logger.info(getId() + " Remote address = " + remoteAddress);
        }
    }

    /**
     * Allows children classes to create specific DatagramSockets
     * 
     * @return A DatagramSocket bound to the local address and port
     * @throws SocketException
     * @throws IOException
     * @throws ChannelException
     */
    protected DatagramSocket createSocket() throws IOException, ChannelException {
        DatagramSocket socket = new DatagramSocket(getlocalAddress());
        socketOptions.applySocketOptions(getId(), socket);
        return socket;
    }

    @Override
    /**
     * Is this IO Thread connected?
     * 
     * @return true if connected
     */
    public boolean isConnected() {
        return socket != null;
    }

    @Override
    /** Closes this IO Thread */
    public void close() throws InterruptedException {
        if (receiveThread != null) {
            receiveThread.terminate();
        }

        if (receiveThread != null) {
            receiveThread.join();
        }

        if (socket != null) {
            socket.close();
            socket = null;
        }

        setState(StateEnum.CLOSED);
    }

    @Override
    /**
     * Sends data via this IO Thread
     * 
     * @param message The message to be sent
     */
    public void send(MessageBuffer message) throws ChannelException {
        if (getState() != StateEnum.CONNECTED || getIoType() == IoTypeEnum.INPUT_ONLY) {
            throw new IllegalStateException(
                    getId() + " Socket state = " + getState() + ", IO Type = " + getIoType());
        }

        logSend(logger, message, "remote address = " + remoteAddress.toString() + " size = "
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
