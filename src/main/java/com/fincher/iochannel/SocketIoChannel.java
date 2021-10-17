package com.fincher.iochannel;

import java.net.InetSocketAddress;

import org.slf4j.Logger;

/**
 * An IO Thread that is implemented for network socket communication.
 * 
 * @author Brian Fincher
 *
 */
public abstract class SocketIoChannel extends IoChannel<MessageBuffer> {

    /** The local network address and port to which this socket will be bound. */
    private final InetSocketAddress localAddress;

    /**
     * Constructs a new SocketIoChannel.
     * 
     * @param id           The ID of this IO Thread
     * @param ioType       Is this IO Thread input, output, or both
     * @param localAddress The local address to which this socket will be bound. If null "localhost"
     *                     will be used that the OS will choose an available port
     */
    protected SocketIoChannel(String id, IoType ioType, InetSocketAddress localAddress) {
        super(id, ioType);

        if (localAddress == null) {
            this.localAddress = new InetSocketAddress(0);
        } else {
            this.localAddress = localAddress;
        }
    }

    /**
     * Get the local address to which this socket is bound or the address to which it will be bound
     * if not already bound.
     * 
     * @return the local address
     */
    public InetSocketAddress getlocalAddress() {
        return localAddress;
    }

    /**
     * Get the type of data processed by this IoChannel.
     * 
     * @return the type of data processed by this IoChannel
     */
    @Override
    public IoChannelDataType getDataType() {
        return IoChannelDataType.RAW_DATA;
    }

    /**
     * Handle a received message.
     * 
     * @param mb        The message that was received
     * @param logger    The Log4j Logger
     * @param logString log information about the received message
     */
    @Override
    protected void messageReceived(MessageBuffer mb, Logger logger, String logString) {
        mb.setReceivedFromIoChannelId(getId());
        super.messageReceived(mb, logger, logString + " size = " + mb.getBytes().length);
    }
}
