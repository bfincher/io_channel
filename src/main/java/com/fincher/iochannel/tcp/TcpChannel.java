package com.fincher.iochannel.tcp;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import org.slf4j.Logger;

import com.fincher.iochannel.ChannelException;
import com.fincher.iochannel.ChannelState;
import com.fincher.iochannel.IoType;
import com.fincher.iochannel.Listeners;
import com.fincher.iochannel.MessageBuffer;
import com.fincher.iochannel.SocketIoChannel;
import com.fincher.iochannel.Utilities;
import com.fincher.thread.CallableTask;
import com.fincher.thread.LongLivedTask;
import com.fincher.thread.RunnableTask;
import com.google.common.base.Preconditions;

/** An IO Channel implementation of TCP sockets. */
public abstract class TcpChannel extends SocketIoChannel implements TcpChannelIfc {

    private static final Logger LOG = Utilities.getInstance().getLogger(TcpChannel.class);

    /** Used to determine how many bytes to read for each message. */
    private final StreamIo streamIo;

    /** A map of TCP Sockets that have been connected. */
    protected final Map<String, Socket> sockets = new HashMap<>();

    /** A map of receive task futures. */
    private final Map<String, Future<?>> receiveTasks = new HashMap<>();

    protected Future<Socket> connectTaskFuture;

    protected CallableTask<Socket> connectTask;

    /** The TCP Socket Options. */
    private TcpSocketOptions socketOptions = new TcpSocketOptions();

    private final Listeners<ConnectionEstablishedListener, String> connectionEstablishedListeners = new Listeners<>();

    private final Listeners<ConnectionLostListener, String> connectionLostListeners = new Listeners<>();

    private ReceiveTaskFactory receiveTaskFactory = new DefaultReceiveTaskFactory();

    private static final class DefaultReceiveTaskFactory implements ReceiveTaskFactory {
        @Override
        public RunnableTask createReceiveTask(String id, Socket socket, StreamIo streamIo, TcpChannel parent)
                throws ChannelException {
            return new ReceiveTask(id, socket, streamIo, parent);
        }
    }

    /**
     * Constructs a new TCP socket that is capable of both sending and receiving
     * data.
     * 
     * @param id           The ID of this IO Channel
     * @param ioType       Specifies the input/output status of this channel
     * @param streamIo     Used to determine how many bytes should be read from the
     *                     socket for each message
     * @param localAddress The local address to which this socket will be bound. If
     *                     null "localhost" will be used that the OS will choose an
     *                     available port
     */
    protected TcpChannel(String id, IoType ioType, InetSocketAddress localAddress, StreamIo streamIo) {
        super(id, ioType, localAddress);
        this.streamIo = streamIo;
    }

    protected void setReceiveTaskFactory(ReceiveTaskFactory factory) {
        Preconditions.checkState(getState() == ChannelState.INITIAL,
                "The state must be INITIAL for setReceiveTaskFactory");

        this.receiveTaskFactory = factory;
    }

    /**
     * Sets the TCP socket options for this socket.
     * 
     * @param socketOptions The TCP socket options
     */
    public void setSocketOptions(TcpSocketOptions socketOptions) {
        Preconditions.checkState(getState() == ChannelState.INITIAL, "The state must be INITIAL for setSocketOptions");

        this.socketOptions = socketOptions;
    }

    /**
     * Connect this socket.
     * 
     * @throws ChannelException     If an error occurs while connecting
     * @throws InterruptedException if the task is interrupted
     */
    @Override
    public final void connect() throws ChannelException, InterruptedException {
        Preconditions.checkState(getState() == ChannelState.INITIAL, "Cannot connect when state = " + getState());

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

        connectTask = getConnectTask();
        connectTaskFuture = LongLivedTask.create(getId() + "ConnectTask", connectTask).start();
    }

    /**
     * Gets the task object used to create a connect task.
     * 
     * @return the task object used to create a connect task
     * @throws ChannelException If an error occurs while building the connect task
     */
    protected abstract CallableTask<Socket> getConnectTask() throws ChannelException;

    /**
     * Is this socket connected.
     * 
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

        if (connectTaskFuture != null) {
            connectTaskFuture.cancel(true);
        }

        receiveTasks.values().forEach(future -> future.cancel(true));

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

    protected final void send(MessageBuffer message, Socket channel) throws IOException {
        byte[] bytes = message.getBytes();
        logSend(LOG, message, "message length = " + bytes.length);

        send(bytes, channel);
    }

    protected final synchronized void send(byte[] msgBytes, int offset, int length, Socket channel) throws IOException {
        OutputStream output = channel.getOutputStream();
        output.write(msgBytes, offset, length);
    }

    protected final synchronized void send(byte[] msgBytes, Socket channel) throws IOException {
        send(msgBytes, 0, msgBytes.length, channel);
    }

    /**
     * Send data via this IO Channel.
     * 
     * @param message The data to be sent
     * @throws ChannelException If an error occurs while sending
     */
    @Override
    public void send(byte[] message) throws ChannelException {
        send(new MessageBuffer(message));
    }

    /**
     * Send data via this IO Channel.
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
                LOG.warn("{} Cannot send due to no sockets connected", getId());
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
     * 
     * @param message   The message to send
     * @param channelId The ID of the channel on which to send this message. "*" if
     *                  sending to all channels
     * @throws ChannelException If an exception occurs while sending or if the
     *                          channelID does not exist
     */
    @Override
    public void send(MessageBuffer message, String channelId) throws ChannelException {
        if (channelId.equals("*")) {
            send(message);
        } else {
            Socket socket = sockets.get(channelId);
            if (socket == null) {
                throw new ChannelException(getId() + " no such channel ID " + channelId);
            } else {
                try {
                    send(message, socket);
                } catch (IOException ioe) {
                    throw new ChannelException(ioe);
                }
            }
        }
    }

    @Override
    public List<String> getSocketIds() {
        return new ArrayList<>(sockets.keySet());
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
     * Builds an ID for a receive task.
     * 
     * @param socketId The socketID for which a receive task ID should be built
     * @return The receive task ID
     */
    protected final String getReceiveTaskId(String socketId) {
        return getId() + socketId + "ReceiveTask";
    }

    /**
     * Called by connect tasks when a socket connection is established.
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
            String receiveTaskId = socketId;
            RunnableTask receiveTask = receiveTaskFactory.createReceiveTask(receiveTaskId, socket,
                    streamIo, this);
            Future<Void> receiveTaskFuture = LongLivedTask.create(receiveTaskId, receiveTask).start();
            receiveTasks.put(socketId, receiveTaskFuture);
        }

        LOG.debug("{} setting state to CONNECTED", getId());
        setState(ChannelState.CONNECTED);

        connectionEstablishedListeners.getListeners().forEach(listener -> listener.connectionEstablished(socketId));
    }

    /**
     * Called by a receive task when a socket connection is lost.
     * 
     * @param socket The socket that was lost
     * @throws ChannelException If an error occurs while handling the connection
     *                          loss event
     */
    protected synchronized void connectionLost(Socket socket) throws ChannelException {
        LOG.warn("{} {} connection lost", getId(), getSocketId(socket));

        String socketId = getSocketId(socket);
        connectionLostListeners.getListeners().forEach(listener -> listener.connectionLost(socketId));

        sockets.remove(socketId);

        if (sockets.isEmpty()) {
            LOG.debug("{} setting state to CONNECTING", getId());
            setState(ChannelState.CONNECTING);
        }

        Future<?> receiveTask = receiveTasks.remove(getReceiveTaskId(socketId));
        if (receiveTask != null) {
            receiveTask.cancel(true);
        }

        try {
            socket.close();
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        } finally {
            // wait to give tasks time to close
            try {
                Utilities.sleep(this, getSocketSleepTime().plus(Duration.ofSeconds(1)));
            } catch (InterruptedException ie) {
                LOG.warn(getId() + " " + ie.getMessage(), ie);
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public int getNumConnections() {
        return sockets.size();
    }

    @Override
    public void addConnectionEstablishedListener(ConnectionEstablishedListener listener) {
        connectionEstablishedListeners.addListener(listener);
    }

    @Override
    public void removeConnectionEstablishedListener(ConnectionEstablishedListener listener) {
        connectionEstablishedListeners.removeListener(listener);
    }

    @Override
    public void addConnectionLostListener(ConnectionLostListener listener) {
        connectionLostListeners.addListener(listener);
    }

    @Override
    public void removeConnectionLostListener(ConnectionLostListener listener) {
        connectionLostListeners.removeListener(listener);
    }

    @Override
    protected void messageReceived(MessageBuffer mb, Logger logger, String logString) {
        super.messageReceived(mb, logger, logString);
    }

    protected TcpSocketOptions getSocketOptions() {
        return socketOptions;
    }

    protected Duration getSocketSleepTime() {
        return Duration.ofMillis(socketOptions.getTimeout().orElse(0));
    }
}
