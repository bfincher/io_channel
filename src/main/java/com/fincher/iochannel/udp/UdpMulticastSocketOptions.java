package com.fincher.iochannel.udp;

import java.io.IOException;
import java.net.MulticastSocket;
import java.net.StandardSocketOptions;
import java.util.Optional;

import org.slf4j.Logger;

import com.fincher.iochannel.Utilities;

/**
 * A set of socket options for UDP Multicast sockets.
 * 
 * @author Brian Fincher
 *
 */
public class UdpMulticastSocketOptions extends UdpSocketOptions {

    private static final Logger LOG = Utilities.getInstance().getLogger(UdpMulticastSocketOptions.class);

    /** The multicast TTL option. */
    private int timeToLive = 16;

    /** The multicast Loopback Disabled option. */
    private Optional<Boolean> loopbackDisabled = Optional.empty();

    /**
     * Gets the multicast TTL option.
     * 
     * @return the multicast TTL option
     */
    public int getTimeToLive() {
        return timeToLive;
    }

    /**
     * Sets the multicast TTL option.
     * 
     * @param timeToLive the multicast TTL option
     */
    public void setTimeToLive(int timeToLive) {
        this.timeToLive = timeToLive;
    }

    /**
     * Gets the multicast loopback disabled option.
     * 
     * @return the multicast loopback disabled option
     */
    public Optional<Boolean> getLoopbackDisabled() {
        return loopbackDisabled;
    }

    /**
     * Sets the multicast loopback disabled option.
     * 
     * @param loopbackDisabled the multicast loopback disabled option
     */
    public void setLoopbackDisabled(boolean loopbackDisabled) {
        this.loopbackDisabled = Optional.of(loopbackDisabled);
    }

    /**
     * Sets the options represented by this object to the given socket.
     * 
     * @param socketId The ID of the socket
     * @param socket   The socket for which options will be set
     * @throws IOException If an exception occurs while applying socket options
     */
    public void applySocketOptions(String socketId, MulticastSocket socket) throws IOException {
        super.applySocketOptions(socketId, socket);
        socket.setTimeToLive(timeToLive);

        if (loopbackDisabled.isPresent()) {
            socket.setOption(StandardSocketOptions.IP_MULTICAST_LOOP, false);
        }

        LOG.info("{} timeToLive = {}.  loopbackDisabled = {}", socketId, socket.getTimeToLive(), loopbackDisabled);
    }
}
