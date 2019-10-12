package com.fincher.io_channel.tcp;

import com.fincher.io_channel.ChannelException;
import com.fincher.io_channel.MessageBuffer;
import com.fincher.thread.DataHandlerIfc;

import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * A TCP client socket
 * 
 * @author Brian Fincher
 *
 */
public class TcpClientChannel extends TcpChannel {

    /** The remote address to which this client is trying to connect */
    private final InetSocketAddress remoteAddress;

    /**
     * Creates a new TcpClientChannel from JAXB XML configuration
     * 
     * @param config         The JAXB XML configuration
     * @param messageFormat  The message format for this IoChannel
     * @param messageHandler Used to notify clients of received data. May be null if this is an
     *                       output only channel
     * @param streamIo       Used to determine how many bytes should be read from the socket for
     *                       each message
     * @return A new TcpClientChannel
     */
//    public static TcpClientChannel createTCPClientChannel(TCPClientIOChannelType config,
//            MessageFormatEnum messageFormat, MessageHandlerIfc<MessageBuffer> messageHandler,
//            StreamIoIfc streamIo) {
//        InetSocketAddress remoteAddress = new InetSocketAddress(config.getRemoteAddress().getHost(),
//                config.getRemoteAddress().getPort());
//
//        InetSocketAddress localAddress = getLocalAddress(config);
//
//        TcpClientChannel channel;
//        if (messageHandler == null) {
//            channel = new TcpClientChannel(config.getId(), messageFormat, streamIo, localAddress,
//                    remoteAddress);
//        } else {
//            channel = new TcpClientChannel(config.getId(), messageFormat, messageHandler, streamIo,
//                    localAddress, remoteAddress);
//        }
//
//        if (config.getSocketOptions() != null) {
//            TcpSocketOptions socketOptions = TcpSocketOptions
//                    .getSocketOptions(config.getSocketOptions());
//            channel.setSocketOptions(socketOptions);
//        }
//
//        loadFromConfig(channel, config);
//
//        return channel;
//    }

    /**
     * Constructs a new TCP client socket that is capable of both sending and receiving data
     * 
     * @param id             The ID of this IO Thread
     * @param messageHandler Used to notify clients of received data
     * @param streamIo       Used to determine how many bytes should be read from the socket for
     *                       each message
     * @param localAddress   The local address to which this socket will be bound. If null
     *                       "localhost" will be used that the OS will choose an available port
     * @param remoteAddress  The remote address to which this client is trying to connect
     */
    public TcpClientChannel(String id, DataHandlerIfc<MessageBuffer> messageHandler,
            StreamIoIfc streamIo, InetSocketAddress localAddress, InetSocketAddress remoteAddress) {
        super(id, localAddress, messageHandler, streamIo);
        this.remoteAddress = remoteAddress;
    }

    /**
     * Constructs a new TCP client socket that is capable of only sending data
     * 
     * @param id            The ID of this IO Thread
     * @param streamIo      Used to determine how many bytes should be read from the socket for each
     *                      message
     * @param localAddress  The local address to which this socket will be bound. If null
     *                      "localhost" will be used that the OS will choose an available port
     * @param remoteAddress The remote address to which this client is trying to connect
     */
    public TcpClientChannel(String id, StreamIoIfc streamIo, InetSocketAddress localAddress,
            InetSocketAddress remoteAddress) {
        super(id, localAddress, streamIo);
        this.remoteAddress = remoteAddress;
    }

    @Override
    /** Gets the Runnable used to create a connect thread */
    protected TcpClientConnectRunnable getConnectRunnable() {
        return new TcpClientConnectRunnable(this, remoteAddress);
    }

    @Override
    /**
     * Called when the socket connection is lost
     * 
     * @param The socket what was lost
     */
    protected synchronized void connectionLost(Socket socket) throws ChannelException {
        super.connectionLost(socket);
        performConnect();
    }

    /**
     * Get the local address to which this socket is bound or the address to which it will be bound
     * if not already bound
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
