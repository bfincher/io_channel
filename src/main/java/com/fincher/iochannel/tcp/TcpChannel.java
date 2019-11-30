package com.fincher.iochannel.tcp;

import com.fincher.iochannel.ChannelException;
import com.fincher.iochannel.ChannelState;
import com.fincher.iochannel.IoType;
import com.fincher.iochannel.MessageBuffer;
import com.fincher.iochannel.SocketIoChannel;
import com.fincher.thread.MyCallableIfc;
import com.fincher.thread.MyRunnableIfc;
import com.fincher.thread.MyThread;
import com.google.common.base.Preconditions;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** An IO Thread implementation of TCP sockets. */
public abstract class TcpChannel extends SocketIoChannel {

    private static final Logger LOG = LogManager.getLogger();

    /** Used to determine how many bytes to read for each message. */
    private final StreamIo streamIo;

    /** A map of TCP Sockets that have been connected. */
    protected final Map<String, Socket> sockets = new HashMap<>();

    /** A map of receive threads. */
    private final Map<String, MyThread> receiveThreads = new HashMap<>();

    /** The thread used to connect the socket. */
    protected MyThread connectThread;

    /** The number of socket connections. */
    private final AtomicInteger connectionCount = new AtomicInteger(0);

    /** The TCP Socket Options. */
    protected TcpSocketOptions socketOptions = new TcpSocketOptions();

    /**
     * The minimum amount of time between warning messages regarding sending when no sockets are
     * connected.
     */
    private double noSocketsSendErrorWarningInterval = Double.parseDouble(
            System.getProperty("no.sockets.send.error.warning.interval.seconds", "30.0"));

    /**
     * The last time at which a warning message regarding sending when no sockets are connected was
     * issued.
     */
    private long lastNoSocketsSendError = 0;

    private final List<ConnectionEstablishedListener> connectionEstablishedListeners = Collections
            .synchronizedList(new LinkedList<ConnectionEstablishedListener>());

    private ReceiveRunnableFactory receiveRunnableFactory = new DefaultReceiveRunnableFactory();

    private static final class DefaultReceiveRunnableFactory implements ReceiveRunnableFactory {
        @Override
        public MyRunnableIfc createReceiveRunnable(String id, Socket socket, StreamIo streamIo,
                TcpChannel parent) throws ChannelException {
            return new ReceiveRunnable(id, socket, streamIo, parent);
        }
    }

    /**
     * Constructs a new TCP socket that is capable of both sending and receiving data.
     * 
     * @param id             The ID of this IO Thread
     * @param ioType         Specifies the input/output status of this channel
     * @param streamIo       Used to determine how many bytes should be read from the socket for
     *                       each message
     * @param localAddress   The local address to which this socket will be bound. If null
     *                       "localhost" will be used that the OS will choose an available port
     */
    protected TcpChannel(String id, IoType ioType, InetSocketAddress localAddress, StreamIo streamIo) {
        super(id, ioType, localAddress);
        this.streamIo = streamIo;
    }


    protected void setReceiveRunnableFactory(ReceiveRunnableFactory factory) {
        Preconditions.checkState(getState() == ChannelState.INITIAL,
                "The state must be INITIAL for setReceiveRunnableFactory");

        this.receiveRunnableFactory = factory;
    }

    /**
     * Sets the TCP socket options for this socket.
     * 
     * @param socketOptions The TCP socket options
     */
    public void setSocketOptions(TcpSocketOptions socketOptions) {
        Preconditions.checkState(getState() == ChannelState.INITIAL,
                "The state must be INITIAL for setSocketOptions");

        this.socketOptions = socketOptions;
    }

    /** Connect this socket.
     * @throws ChannelException If an error occurs while connecting
     * @throws InterruptedException if the thread is interrupted
     */
    @Override
    public final void connect() throws ChannelException, InterruptedException {
        Preconditions.checkState(getState() == ChannelState.INITIAL,
            "Cannot connect when state = " + getState());

        performConnect();
    }

    /**
     * Performs the actual socket connection logic.
     * 
     * @throws ChannelException If an exception is thrown while connecting
     */
    protected void performConnect() throws ChannelException {
        LOG.debug("{} Setting state to CONNECTING", getId());
        setState(ChannelState.CONNECTING);
        connectThread = new MyThread(getId() + "ConnectThread", getConnectRunnable());
        connectThread.start();
    }

    /**
     * Gets the Runnable object used to create a connect thread.
     * 
     * @return the Runnable object used to create a connect thread
     * @throws ChannelException If an error occurs while building the connect runnable
     */
    protected abstract MyCallableIfc<Socket> getConnectRunnable() throws ChannelException;

    /** 
     * Is this socket connected.
     * @return true if connected 
     */
    @Override
    public boolean isConnected() {
        LOG.debug("{} isConnected() returning {}", getId(), (getState() == ChannelState.CONNECTED));
        return getState() == ChannelState.CONNECTED;
    }

    /** Close this socket. */
    @Override
    public void close() throws ChannelException {
        LOG.debug("{} setting state to CLOSED", getId());
        setState(ChannelState.CLOSED);

        if (connectThread != null) {
            connectThread.terminate();
        }

        for (MyThread receiveThread : receiveThreads.values()) {
            receiveThread.terminate();
        }

        synchronized (sockets) {
            for (Socket socket : sockets.values()) {
                try {
                    socket.close();
                } catch (IOException ioe) {
                    throw new ChannelException(getId(), ioe);
                }
            }
        }
    }

    protected final void send(MessageBuffer message, Socket channel, boolean logSend)
            throws IOException {
        byte[] bytes = message.getBytes();

        if (logSend) {
            logSend(LOG, message, "message length = " + bytes.length);
        }

        send(bytes, channel);
    }

    protected final synchronized void send(byte[] msgBytes, int offset, int length, Socket channel)
            throws IOException {
        OutputStream output = channel.getOutputStream();
        output.write(msgBytes, offset, length);
    }

    protected final synchronized void send(byte[] msgBytes, Socket channel) throws IOException {
        send(msgBytes, 0, msgBytes.length, channel);
    }

    /**
     * Send data via this IO Thread.
     * 
     * @param message The data to be sent
     * @throws ChannelException If an error occurs while sending
     */
    public void send(byte[] message) throws ChannelException {
        send(new MessageBuffer(message));
    }

    /**
     * Send data via this IO Thread.
     * 
     * @param message The data to be sent
     * @throws ChannelException If an error occurs while sending
     */
    @Override
    public void send(MessageBuffer message) throws ChannelException {

        byte[] bytes = message.getBytes();

        logSend(LOG, message, "message length = " + bytes.length);

        synchronized (sockets) {
            if (sockets.values().isEmpty()) {
                logNoSocketsSendError();
            }
            for (Socket socket : sockets.values()) {
                try {
                    send(bytes, socket);
                } catch (IOException ioe) {
                    throw new ChannelException(getId(), ioe);
                }
            }
        }
    }

    /**
     * Sends a message on this channel.
     * @param message The message to send
     * @param channelId The ID of the channel on which to send this message.  "*" if sending to all channels
     * @throws ChannelException If an exception occurs while sending or if the channelID does not exist
     */
    public void send(MessageBuffer message, String channelId) throws ChannelException {
        if (channelId.equals("*")) {
            send(message);
        } else {
            Socket socket = sockets.get(channelId);
            if (socket == null) {
                throw new ChannelException(getId() + " no such channel ID " + channelId);
            } else {
                try {
                    send(message, socket, true);
                } catch (IOException ioe) {
                    throw new ChannelException(ioe);
                }
            }
        }
    }
    
    public List<String> getSocketIds() {
        return new ArrayList<>(sockets.keySet());
    }

    private final void logNoSocketsSendError() {
        long currentTime = System.currentTimeMillis();
        double durationSecs = (currentTime - lastNoSocketsSendError) / 1000.0;

        String logStr = getId() + " Cannot send due to no sockets connected";

        if (durationSecs > noSocketsSendErrorWarningInterval) {
            LOG.warn(logStr);
            lastNoSocketsSendError = currentTime;
        } else {
            LOG.info(logStr);
        }
    }

    /**
     * Builds an ID for a socket.
     * 
     * @param socket The socket for which the ID should be built
     * @return The socket ID
     */
    protected static final String getSocketId(Socket socket) {
        return socket.getInetAddress().getHostName() + ":" + socket.getPort();
    }

    /**
     * Builds an ID for a receive thread.
     * 
     * @param socketId The socketID for which a receive thread ID should be built
     * @return The receive thread ID
     */
    protected final String getReceiveThreadId(String socketId) {
        return getId() + socketId + "ReceiveThread";
    }

    /**
     * Called by connect threads when a socket connection is established.
     * 
     * @param socket The newly established socket connection
     * @throws ChannelException If an exception occurs while processing the event
     */
    protected synchronized void connectionEstablished(Socket socket) throws ChannelException {
        String socketId = getSocketId(socket);

        synchronized (sockets) {
            sockets.put(socketId, socket);
        }

        socketOptions.applySocketOptions(getId(), socket);

        if (getIoType().isInput()) {
            String receiveThreadId = socketId;
            MyRunnableIfc receiveRunnable = receiveRunnableFactory
                    .createReceiveRunnable(receiveThreadId, socket, streamIo, this);
            MyThread receiveThread = new MyThread(receiveThreadId, receiveRunnable);
            receiveThreads.put(socketId, receiveThread);
            receiveThread.start();
        }

        connectionCount.incrementAndGet();

        LOG.debug("{} setting state to CONNECTED", getId());
        setState(ChannelState.CONNECTED);

        for (ConnectionEstablishedListener listener : connectionEstablishedListeners) {
            listener.connectionEstablished(socketId);
        }
    }

    /**
     * Called by a receive thread when a socket connection is lost.
     * 
     * @param socket The socket that was lost
     * @throws ChannelException If an error occurs while handling the connection loss event
     */
    protected synchronized void connectionLost(Socket socket) throws ChannelException {
        LOG.warn("{} {} connection lost", getId(), getSocketId(socket));

        int count = connectionCount.decrementAndGet();

        if (count <= 0) {
            LOG.debug("{} setting state to CONNECTING", getId());
            setState(ChannelState.CONNECTING);
        }

        String socketId = getSocketId(socket);

        MyThread receiveThread = receiveThreads.remove(getReceiveThreadId(socketId));
        if (receiveThread != null) {
            receiveThread.terminate();
        }

        sockets.remove(socketId);

        try {
            socket.close();
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        } finally {
            // wait 3 seconds to give threads time to close
            try {
                wait(3000);
            } catch (InterruptedException ie) {
                LOG.warn(getId() + " " + ie.getMessage(), ie);
                Thread.currentThread().interrupt();
            }
        }
    }

    public int getNumConnections() {
        return sockets.size();
    }

    public void addConnectionEstablishedListener(ConnectionEstablishedListener listener) {
        connectionEstablishedListeners.add(listener);
    }

    public void removeConnectionEstablishedListener(ConnectionEstablishedListener listener) {
        connectionEstablishedListeners.remove(listener);
    }

    @Override
    protected void messageReceived(MessageBuffer mb, Logger logger, String logString) {
        super.messageReceived(mb, logger, logString);
    }
}
