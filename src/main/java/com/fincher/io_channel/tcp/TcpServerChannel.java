package com.fincher.io_channel.tcp;

import com.fincher.io_channel.ChannelException;
import com.fincher.io_channel.IoTypeEnum;
import com.fincher.io_channel.MessageBuffer;
import com.fincher.thread.MyCallableIfc;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.Consumer;

/**
 * A TCPIOThread implementation for TCP Server sockets
 * 
 * @author Brian Fincher
 *
 */
public class TcpServerChannel extends TcpChannel {

    /**
     * Constructs a new TCP server socket that is capable of both sending and receiving data
     * 
     * @param id             The ID of this IO Thread
     * @param ioType         Specifies the input/output status of this channel
     * @param streamIo       Used to determine how many bytes should be read from the socket for
     *                       each message
     * @param localAddress   The local address to which this socket will be bound. If null
     *                       "localhost" will be used
     */
    protected TcpServerChannel(String id, IoTypeEnum ioType,
            StreamIoIfc streamIo, InetSocketAddress localAddress) {
        super(id, ioType, localAddress, streamIo);
    }

    
    /**
     * Creates a new TCP server socket that is capable of both sending and receiving data
     * 
     * @param id             The ID of this IO Thread
     * @param streamIo       Used to determine how many bytes should be read from the socket for
     *                       each message
     * @param localAddress   The local address to which this socket will be bound. If null
     *                       "localhost" will be used
     * @return a new TCP server socket that is capable of both sending and receiving data
     */
    public static TcpServerChannel createChannel(String id,
            StreamIoIfc streamIo, InetSocketAddress localAddress) {
        return new TcpServerChannel(id, IoTypeEnum.INPUT_AND_OUTPUT, streamIo, localAddress);
    }
    
    /**
     * Creates a new TCP server socket that is capable of both sending and receiving data
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
            StreamIoIfc streamIo, InetSocketAddress localAddress) {
        TcpServerChannel channel = new TcpServerChannel(id, IoTypeEnum.INPUT_AND_OUTPUT, streamIo, localAddress);
        channel.addMessageListener(messageHandler);
        return channel;
    }

    /**
     * Creates a new TCP server socket that is capable of only sending data
     * 
     * @param id           The ID of this IO Thread
     * @param streamIo     Used to determine how many bytes should be read from the socket for each
     *                     message
     * @param localAddress The local address to which this socket will be bound. If null "localhost"
     *                     will be used
     * @return a new TCP server socket that is capable of only sending data
     */
    public static TcpServerChannel createOutputOnly(String id, StreamIoIfc streamIo, InetSocketAddress localAddress) {
        return new TcpServerChannel(id, IoTypeEnum.OUTPUT_ONLY, streamIo, localAddress);
    }

    /**
     * Gets the Runnable object used to create a connect thread
     * 
     * @return the Runnable object used to create a connect thread
     * @throws ChannelException
     */
    @Override
    protected MyCallableIfc<Socket> getConnectRunnable() throws ChannelException {
        return new TcpServerConnectRunnable(this);
    }

    /**
     * Get the local address to which this socket is bound or the address to which it will be bound
     * if not already bound
     */
    @Override
    public InetSocketAddress getlocalAddress() {
        TcpServerConnectRunnable connectRunnable;
        if (connectThread.getCallable().isPresent()) {
            connectRunnable = (TcpServerConnectRunnable)connectThread.getCallable().get();
        } else {
            connectRunnable = (TcpServerConnectRunnable)connectThread.getRunnable().get();
        }
        
        
        ServerSocket socket = connectRunnable.serverSocket;
        if (socket.isBound()) {
            return new InetSocketAddress(socket.getInetAddress(), socket.getLocalPort());
        } else {
            return super.getlocalAddress();
        }
    }
}
