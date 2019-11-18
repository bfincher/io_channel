package com.fincher.io_channel.udp;

import java.io.IOException;
import java.net.MulticastSocket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A set of socket options for UDP Multicast sockets
 * 
 * @author Brian Fincher
 *
 */
public class UdpMulticastSocketOptions extends UdpSocketOptions {

    private static final Logger LOG = LogManager.getLogger();

    /** The multicast TTL option */
    private int timeToLive = 16;

    /** The multicast Loopback Disabled option */
    private boolean loopbackDisabled = false;

    /** Gets the multicast TTL option
     * 
     * @return the multicast TTL option
     */
    public int getTimeToLive() {
        return timeToLive;
    }

    /** Sets the multicast TTL option
     * 
     * @param timeToLive the multicast TTL option
     */
    public void setTimeToLive(int timeToLive) {
        this.timeToLive = timeToLive;
    }

    /** Gets the multicast loopback disabled option
     * 
     * @return the multicast loopback disabled option
     */
    public boolean isLoopbackDisabled() {
        return loopbackDisabled;
    }

    /** Sets the multicast loopback disabled option
     * 
     * @param loopbackDisabled the multicast loopback disabled option
     */
    public void setLoopbackDisabled(boolean loopbackDisabled) {
        this.loopbackDisabled = loopbackDisabled;
    }

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

        LOG.info("{} timeToLive = {}.  loopbackDisabled = {}", socketId, socket.getTimeToLive(), loopbackDisabled);
    }
}
