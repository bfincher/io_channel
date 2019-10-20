package com.fincher.io_channel.tcp;

import com.fincher.io_channel.ChannelException;
import com.fincher.io_channel.SocketOptions;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import org.apache.log4j.Logger;

/** Socket options for TCP Sockets */
public class TcpSocketOptions extends SocketOptions {

    private static Logger logger = Logger.getLogger(TcpSocketOptions.class);

    /** The SO_KEEPALIVE socket setting. Defaults to true */
    private boolean keepAlive = true;

    /** The TCP_NODELAY socket setting. Defaults to true */
    private boolean tcpNoDelay = true;

    /**
     * Get a new TcpSocketOptions object from XML configuration
     * 
     * @param config The XML configuration
     * @return The newly created TcpSocketOptions type
     */
//    public static TcpSocketOptions getSocketOptions(TCPSocketOptionsType config) {
//        TcpSocketOptions socketOptions = new TcpSocketOptions();
//        SocketOptions.getSocketOptions(socketOptions, config);
//
//        socketOptions.keepAlive = config.isKeepAlive();
//        socketOptions.tcpNoDelay = config.isNoDelay();
//
//        return socketOptions;
//
//    }

    /**
     * Sets the options represented by this object to the given TCP Socket
     * 
     * @param socketId The ID of the socket
     * @param socket   The socket for which options will be set
     * @throws ChannelException
     */
    public void applySocketOptions(String socketId, Socket socket) throws ChannelException {
        try {
            if (getReceiveBufferSize().isPresent()) {
                socket.setReceiveBufferSize(getReceiveBufferSize().getAsInt());
            }

            if (getSendBufferSize().isPresent()) {
                socket.setSendBufferSize(getSendBufferSize().getAsInt());
            }

            socket.setKeepAlive(keepAlive);
            socket.setReuseAddress(isReuseAddress());
            
            if (getTimeout().isPresent()) {
                socket.setSoTimeout(getTimeout().getAsInt());
            }
            
            socket.setTcpNoDelay(tcpNoDelay);

            StringBuilder logString = new StringBuilder();
            logString.append(socketId);
            logString.append(
                    " actual socket options: receiveBufferSize = " + socket.getReceiveBufferSize());
            ;
            logString.append(", sendBufferSize = " + socket.getSendBufferSize());
            logString.append(", keepAlive = " + socket.getKeepAlive());
            logString.append(", reuseAddress = " + socket.getReuseAddress());
            logString.append(", timeout = " + socket.getSoTimeout());
            logString.append(", tcpNoDelay = " + socket.getTcpNoDelay());

            logger.info(logString.toString());
        } catch (SocketException se) {
            throw new ChannelException(socketId, se);
        }

    }

    /**
     * Sets the options represented by this object to the given TCP Server Socket
     * 
     * @param socketId The ID of the socket
     * @param socket   The socket for which options will be set
     * @throws SocketException
     * @throws IOException
     */
    public void applySocketOptions(String socketId, ServerSocket socket) throws IOException {
        if (getReceiveBufferSize().isPresent()) {
            socket.setReceiveBufferSize(getReceiveBufferSize().getAsInt());
        }

        socket.setReuseAddress(isReuseAddress());
        
        if (getTimeout().isPresent()) {
            socket.setSoTimeout(getTimeout().getAsInt());
        }

        StringBuilder logString = new StringBuilder();
        logString.append(socketId);
        logString.append(
                " actual socket options: receiveBufferSize = " + socket.getReceiveBufferSize());
        ;
        logString.append(", reuseAddress = " + socket.getReuseAddress());
        logString.append(", timeout = " + socket.getSoTimeout());

        logger.info(logString.toString());
    }
    
    /** The SO_KEEPALIVE socket setting.
     * 
     * @param val The SO_KEEPALIVE socket setting.
     */
    public void setKeepAlive(boolean val) {
        keepAlive = val;
    }
    
    /** The SO_KEEPALIVE socket setting.
     * 
     * @return The SO_KEEPALIVE socket setting.n 
     */
    public boolean getKeepAlive() {
        return keepAlive;
    }
    
    /** The TCP_NODELAY socket setting.
     * 
     * @return The TCP_NODELAY socket setting
     */
    public void setTcpNoDelay(boolean val) {
        tcpNoDelay = val;
    }

    /** The TCP_NODELAY socket setting.
     * 
     * @return The TCP_NODELAY socket setting
     */
    public boolean getTcpNoDelay() {
        return tcpNoDelay;
    }

}
