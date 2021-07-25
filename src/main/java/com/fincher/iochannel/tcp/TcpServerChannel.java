package com.fincher.iochannel.tcp;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.Consumer;

import com.fincher.iochannel.ChannelException;
import com.fincher.iochannel.IoType;
import com.fincher.iochannel.MessageBuffer;
import com.fincher.thread.MyCallableIfc;

/**
 * A TCPIOThread implementation for TCP Server sockets.
 * 
 * @author Brian Fincher
 *
 */
public class TcpServerChannel extends TcpChannel {

    /**
     * Constructs a new TCP server socket that is capable of both sending and receiving data.
     * 
     * @param id             The ID of this IO Thread
     * @param ioType         Specifies the input/output status of this channel
     * @param streamIo       Used to determine how many bytes should be read from the socket for
     *                       each message
     * @param localAddress   The local address to which this socket will be bound. If null
     *                       "localhost" will be used
     */
    protected TcpServerChannel(String id, IoType ioType,
            StreamIo streamIo, InetSocketAddress localAddress) {
        super(id, ioType, localAddress, streamIo);
    }

    
    /**
     * Creates a new TCP server socket that is capable of both sending and receiving data.
     * 
     * @param id             The ID of this IO Thread
     * @param streamIo       Used to determine how many bytes should be read from the socket for
     *                       each message
     * @param localAddress   The local address to which this socket will be bound. If null
     *                       "localhost" will be used
     * @return a new TCP server socket that is capable of both sending and receiving data
     */
    public static TcpServerChannel createChannel(String id,
            StreamIo streamIo, InetSocketAddress localAddress) {
        return new TcpServerChannel(id, IoType.INPUT_AND_OUTPUT, streamIo, localAddress);
    }
    
    /**
     * Creates a new TCP server socket that is capable of both sending and receiving data.
     * 
     * @param id             The ID of this IO Thread
     * @param messageHandler Used to notify clients of received data
     * @param streamIo       Used to determine how many bytes should be read from the socket for
     *                       each message
     * @param localAddress   The local address to which this socket will be bound. If null
     *                       "localhost" will be used
     * @return a new TCP server socket that is capable of both sending and receiving data
     */
    public static TcpServerChannel createChannel(String id, Consumer<MessageBuffer> messageHandler,
            StreamIo streamIo, InetSocketAddress localAddress) {
        TcpServerChannel channel = new TcpServerChannel(id, IoType.INPUT_AND_OUTPUT, streamIo, localAddress);
        channel.addMessageListener(messageHandler);
        return channel;
    }

    /**
     * Creates a new TCP server socket that is capable of only sending data.
     * 
     * @param id           The ID of this IO Thread
     * @param streamIo     Used to determine how many bytes should be read from the socket for each
     *                     message
     * @param localAddress The local address to which this socket will be bound. If null "localhost"
     *                     will be used
     * @return a new TCP server socket that is capable of only sending data
     */
    public static TcpServerChannel createOutputOnly(String id, StreamIo streamIo, InetSocketAddress localAddress) {
        return new TcpServerChannel(id, IoType.OUTPUT_ONLY, streamIo, localAddress);
    }

    @Override
    protected MyCallableIfc<Socket> getConnectRunnable() throws ChannelException {
        return TcpServerConnectRunnable.create(this);
    }

    @Override
    public InetSocketAddress getlocalAddress() {
        TcpServerConnectRunnable connectRunnable = (TcpServerConnectRunnable)connectThread.getCallable().orElseThrow();
        
        ServerSocket socket = connectRunnable.getServerSocket();
        if (socket.isBound()) {
            return new InetSocketAddress(socket.getInetAddress(), socket.getLocalPort());
        } else {
            return super.getlocalAddress();
        }
    }
}
