package com.fincher.iochannel.tcp;

import java.util.Collection;

import com.fincher.iochannel.ChannelException;
import com.fincher.iochannel.IoChannelIfc;
import com.fincher.iochannel.MessageBuffer;

public interface TcpChannelIfc extends IoChannelIfc<MessageBuffer> {

    /**
     * Send data via this IO Thread.
     * 
     * @param message The data to be sent
     * @throws ChannelException If an error occurs while sending
     */
    void send(byte[] message) throws ChannelException;

    /**
     * Sends a message on this channel.
     * 
     * @param message The message to send
     * @param channelId The ID of the channel on which to send this message. "*" if
     *        sending to all channels
     * @throws ChannelException If an exception occurs while sending or if the
     *         channelID does not exist
     */
    void send(MessageBuffer message, String channelId) throws ChannelException;

    /**
     * Get the IDs of the sockets that are associated with this channel
     * 
     * @return the IDs of the sockets that are associated with this channel
     */
    Collection<String> getSocketIds();

    /**
     * The the number of connections that have been opened by this channel
     * 
     * @return the number of connections that have been opened by this channel
     */
    int getNumConnections();

    /**
     * Add a listener to be notified when a connection is established
     * 
     * @param listener the listener
     */
    void addConnectionEstablishedListener(ConnectionEstablishedListener listener);

    /**
     * Remove a connection established listener
     * 
     * @param listener The listener to be removed
     */
    void removeConnectionEstablishedListener(ConnectionEstablishedListener listener);

    /**
     * Add a listener to be notified when a connection is lost
     * 
     * @param listener The listener
     */
    void addConnectionLostListener(ConnectionLostListener listener);

    /**
     * Remove a connection lost listener
     * 
     * @param listener The listener to be removed
     */
    void removeConnectionLostListener(ConnectionLostListener listener);

}
