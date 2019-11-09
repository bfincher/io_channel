package com.fincher.io_channel.udp;

import com.fincher.io_channel.SocketOptions;

import java.net.DatagramSocket;
import java.net.SocketException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Options for UDP Unicast sockets */
public class UdpSocketOptions extends SocketOptions {

    private static final Logger LOG = LogManager.getLogger();

    /** Constructs a new UdpSocketOptions object */
    public UdpSocketOptions() {
        setReceiveBufferSize(64 * 1024);
        setSendBufferSize(64 * 1024);
    }

    /**
     * Sets the options represented by this object to the given socket
     * 
     * @param socketId The ID of the socket
     * @param socket   The socket for which options will be set
     * @throws SocketException
     */
    public void applySocketOptions(String socketId, DatagramSocket socket) throws SocketException {
        if (getReceiveBufferSize().isPresent()) {
            socket.setReceiveBufferSize(getReceiveBufferSize().getAsInt());
        }

        if (getSendBufferSize().isPresent()) {
            socket.setSendBufferSize(getSendBufferSize().getAsInt());
        }

        socket.setReuseAddress(isReuseAddress());

        if (getTimeout().isPresent()) {
            socket.setSoTimeout(getTimeout().getAsInt());
        }

        if (LOG.isInfoEnabled()) {
            StringBuilder logString = new StringBuilder();
            logString.append(socketId);
            logString.append(
                    " actual socket options: receiveBufferSize = " + socket.getReceiveBufferSize());

            logString.append(", sendBufferSize = " + socket.getSendBufferSize());
            logString.append(", reuseAddress = " + socket.getReuseAddress());
            logString.append(", timeout = " + socket.getSoTimeout());

            LOG.info(logString.toString());
        }
    }

}
