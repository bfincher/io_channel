package com.fincher.iochannel;

/**
 * The state of the TCP socket.
 * 
 * @author Brian Fincher
 *
 */
public enum ChannelState {
    /** The initial state when the object is constructed. */
    INITIAL,

    /** The socket is attempting to connect. */
    CONNECTING,

    /**
     * The socket has a connected peer. In the case of a TCP Server, the socket is
     * still trying to accept additional peers.
     */
    CONNECTED,

    /** The socket has been closed. */
    CLOSED
}
