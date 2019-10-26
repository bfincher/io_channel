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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A UDP Unicast representation of a Socket IO Thread
 * 
 * @author Brian Fincher
 * 
 */
public class UdpChannel extends SocketIoChannel {

    private static final Logger LOG = LoggerFactory.getLogger(UdpChannel.class);

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
                messageReceived(new MessageBuffer(buf, 0, packet.getLength()), LOG,
                        packet.getSocketAddress().toString());
            } catch (SocketTimeoutException ste) {
                // no action necessary
            } catch (IOException ioe) {
                LOG.error(ioe.getMessage(), ioe);
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
            // no action necessary
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
        if (getState() != StateEnum.INITIAL) {
            throw new IllegalStateException(getId() + " Illegal state for connect: " + getState());
        }

        boolean socketCreated = false;
        while (!socketCreated) {
            try {
                socket = createSocket();
                socketCreated = true;
            } catch (BindException be) {
                LOG.warn(getId() + " " + be.getMessage());
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

        LOG.info(getId() + " Connected to local address " + socket.getLocalAddress() + " "
                + socket.getLocalPort());

        if (remoteAddress == null) {
            LOG.info(getId() + " Remote address = null");
        } else {
            LOG.info(getId() + " Remote address = " + remoteAddress);
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
    protected DatagramSocket createSocket() throws IOException {
        DatagramSocket createdSocket = new DatagramSocket(getlocalAddress());
        socketOptions.applySocketOptions(getId(), createdSocket);
        return createdSocket;
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
