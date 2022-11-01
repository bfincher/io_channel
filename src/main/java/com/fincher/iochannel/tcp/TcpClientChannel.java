package com.fincher.iochannel.tcp;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.function.Consumer;

import com.fincher.iochannel.ChannelException;
import com.fincher.iochannel.IoType;
import com.fincher.iochannel.MessageBuffer;

/**
 * A TCP client socket.
 * 
 * @author Brian Fincher
 *
 */
public class TcpClientChannel extends TcpChannel {

    /** The remote address to which this client is trying to connect. */
    private final InetSocketAddress remoteAddress;

    /**
     * Constructs a new TCP client socket.
     * 
     * @param id            The ID of this IO Thread
     * @param ioType        Specifies the input/output status of this channel
     * @param streamIo      Used to determine how many bytes should be read from the
     *                      socket for each message
     * @param localAddress  The local address to which this socket will be bound. If
     *                      null "localhost" will be used that the OS will choose an
     *                      available port
     * @param remoteAddress The remote address to which this client is trying to
     *                      connect
     */
    private TcpClientChannel(String id, IoType ioType, StreamIo streamIo,
            InetSocketAddress localAddress, InetSocketAddress remoteAddress) {
        super(id, ioType, localAddress, streamIo);
        this.remoteAddress = remoteAddress;
    }

    /**
     * Creates a new TCP client socket that is capable of both sending and receiving
     * data.
     * 
     * @param id             The ID of this IO Thread
     * @param messageHandler Used to notify clients of received data
     * @param streamIo       Used to determine how many bytes should be read from
     *                       the socket for each message
     * @param localAddress   The local address to which this socket will be bound.
     *                       If null "localhost" will be used that the OS will
     *                       choose an available port
     * @param remoteAddress  The remote address to which this client is trying to
     *                       connect
     * @return a new TCP client socket that is capable of both sending and receiving
     *         data
     */
    public static TcpClientChannel createChannel(String id, Consumer<MessageBuffer> messageHandler,
            StreamIo streamIo, InetSocketAddress localAddress, InetSocketAddress remoteAddress) {
        TcpClientChannel channel = new TcpClientChannel(id, IoType.INPUT_AND_OUTPUT, streamIo,
                localAddress, remoteAddress);
        channel.addMessageListener(messageHandler);
        return channel;
    }

    /**
     * Creates a new TCP client socket that is capable of both sending and receiving
     * data.
     * 
     * @param id            The ID of this IO Thread
     * @param streamIo      Used to determine how many bytes should be read from the
     *                      socket for each message
     * @param localAddress  The local address to which this socket will be bound. If
     *                      null "localhost" will be used that the OS will choose an
     *                      available port
     * @param remoteAddress The remote address to which this client is trying to
     *                      connect
     * @return a new TCP client socket that is capable of both sending and receiving
     *         data
     */
    public static TcpClientChannel createChannel(String id, StreamIo streamIo,
            InetSocketAddress localAddress, InetSocketAddress remoteAddress) {
        return new TcpClientChannel(id, IoType.INPUT_AND_OUTPUT, streamIo, localAddress, remoteAddress);
    }

    /**
     * Creates a new TCP client socket that is capable of only sending data.
     * 
     * @param id            The ID of this IO Thread
     * @param streamIo      Used to determine how many bytes should be read from the
     *                      socket for each message
     * @param localAddress  The local address to which this socket will be bound. If
     *                      null "localhost" will be used that the OS will choose an
     *                      available port
     * @param remoteAddress The remote address to which this client is trying to
     *                      connect
     * @return a new TCP client socket that is capable of only sending data
     */
    public static TcpClientChannel createOutputOnlyChannel(String id, StreamIo streamIo,
            InetSocketAddress localAddress, InetSocketAddress remoteAddress) {
        return new TcpClientChannel(id, IoType.OUTPUT_ONLY, streamIo, localAddress, remoteAddress);
    }

    /** Gets the Runnable used to create a connect thread. */
    @Override
    protected TcpClientConnectTask getConnectTask() {
        return new TcpClientConnectTask(this, remoteAddress);
    }

    /**
     * Called when the socket connection is lost.
     * 
     * @param socket The socket what was lost
     */
    @Override
    protected synchronized void connectionLost(Socket socket) throws ChannelException {
        super.connectionLost(socket);
        performConnect();
    }

    /**
     * Get the local address to which this socket is bound or the address to which
     * it will be bound if not already bound.
     */
    @Override
    public InetSocketAddress getlocalAddress() {
        if (isConnected()) {
            Socket socket = sockets.values().iterator().next();
            return new InetSocketAddress(socket.getLocalAddress(), socket.getLocalPort());
        } else {
            return super.getlocalAddress();
        }
    }
}
