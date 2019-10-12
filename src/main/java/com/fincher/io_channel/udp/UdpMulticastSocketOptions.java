package com.fincher.io_channel.udp;

import java.io.IOException;
import java.net.MulticastSocket;

import org.apache.log4j.Logger;

/**
 * A set of socket options for UDP Multicast sockets
 * 
 * @author Brian Fincher
 *
 */
public class UdpMulticastSocketOptions extends UdpSocketOptions {

    private static Logger logger = Logger.getLogger(UdpMulticastSocketOptions.class);

    /** The multicast TTL option */
    public int timeToLive = 16;

    /** The multicast Loopback Disabled option */
    public boolean loopbackDisabled = false;

    /**
     * Sets the options represented by this object to the given socket
     * 
     * @param socketId The ID of the socket
     * @param socket   The socket for which options will be set
     * @throws IOException
     */
    public void applySocketOptions(String socketId, MulticastSocket socket) throws IOException {
        super.applySocketOptions(socketId, socket);
        socket.setTimeToLive(timeToLive);
        socket.setLoopbackMode(loopbackDisabled);

        logger.info(socketId + " timeToLive = " + socket.getTimeToLive());
    }

    /**
     * Build a UdpMulticastSocketOptions object from XML configuration
     * 
     * @param config the XML configuration
     * @return The newly created UdpMulticastSocketOptions
     */
//    public static UdpMulticastSocketOptions getSocketOptions(UDPMulticastSocketOptionsType config) {
//        UdpMulticastSocketOptions socketOptions = new UdpMulticastSocketOptions();
//        SocketOptions.getSocketOptions(socketOptions, config);
//
//        socketOptions.timeToLive = config.getTimeToLive();
//        socketOptions.loopbackDisabled = config.isLoopbackDisabled();
//
//        return socketOptions;
//    }

}
