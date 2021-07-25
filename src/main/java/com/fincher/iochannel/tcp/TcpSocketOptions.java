package com.fincher.iochannel.tcp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fincher.iochannel.ChannelException;
import com.fincher.iochannel.SocketOptions;

/** Socket options for TCP Sockets. */
public class TcpSocketOptions extends SocketOptions {

    private static final Logger LOG = LogManager.getLogger();

    /** The SO_KEEPALIVE socket setting. Defaults to true */
    private boolean keepAlive = true;

    /** The TCP_NODELAY socket setting. Defaults to true */
    private boolean tcpNoDelay = true;

    /**
     * Sets the options represented by this object to the given TCP Socket.
     * 
     * @param socketId The ID of the socket
     * @param socket   The socket for which options will be set
     * @throws ChannelException If an exception occurs while applying socket options
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

            if (LOG.isInfoEnabled()) {
                StringBuilder logString = new StringBuilder();
                logString.append(socketId);
                logString.append(" actual socket options: receiveBufferSize = "
                        + socket.getReceiveBufferSize());

                logString.append(", sendBufferSize = " + socket.getSendBufferSize());
                logString.append(", keepAlive = " + socket.getKeepAlive());
                logString.append(", reuseAddress = " + socket.getReuseAddress());
                logString.append(", timeout = " + socket.getSoTimeout());
                logString.append(", tcpNoDelay = " + socket.getTcpNoDelay());

                LOG.info(logString.toString());
            }
        } catch (SocketException se) {
            throw new ChannelException(socketId, se);
        }

    }

    /**
     * Sets the options represented by this object to the given TCP Server Socket.
     * 
     * @param socketId The ID of the socket
     * @param socket   The socket for which options will be set
     * @throws ChannelException If an exception occurs while setting socket options
     */
    public void applySocketOptions(String socketId, ServerSocket socket) throws ChannelException {
        try {
            if (getReceiveBufferSize().isPresent()) {
                socket.setReceiveBufferSize(getReceiveBufferSize().getAsInt());
            }

            socket.setReuseAddress(isReuseAddress());

            if (getTimeout().isPresent()) {
                socket.setSoTimeout(getTimeout().getAsInt());
            }

            if (LOG.isInfoEnabled()) {
                StringBuilder logString = new StringBuilder();
                logString.append(socketId);
                logString.append(" actual socket options: receiveBufferSize = "
                        + socket.getReceiveBufferSize());

                logString.append(", reuseAddress = " + socket.getReuseAddress());
                logString.append(", timeout = " + socket.getSoTimeout());

                LOG.info(logString.toString());
            }
        } catch (IOException e) {
            throw new ChannelException(e);
        }
    }

    /**
     * The SO_KEEPALIVE socket setting.
     * 
     * @param val The SO_KEEPALIVE socket setting.
     */
    public void setKeepAlive(boolean val) {
        keepAlive = val;
    }

    /**
     * The SO_KEEPALIVE socket setting.
     * 
     * @return The SO_KEEPALIVE socket setting.n
     */
    public boolean getKeepAlive() {
        return keepAlive;
    }

    /**
     * Sets the TCP_NODELAY socket setting.
     * 
     */
    public void setTcpNoDelay(boolean val) {
        tcpNoDelay = val;
    }

    /**
     * The TCP_NODELAY socket setting.
     * 
     * @return The TCP_NODELAY socket setting
     */
    public boolean getTcpNoDelay() {
        return tcpNoDelay;
    }

}
