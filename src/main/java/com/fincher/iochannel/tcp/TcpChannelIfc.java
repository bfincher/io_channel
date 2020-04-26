package com.fincher.iochannel.tcp;

import com.fincher.iochannel.ChannelException;
import com.fincher.iochannel.IoChannelIfc;
import com.fincher.iochannel.MessageBuffer;

import java.util.List;

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
     * @param message The message to send
     * @param channelId The ID of the channel on which to send this message.  "*" if sending to all channels
     * @throws ChannelException If an exception occurs while sending or if the channelID does not exist
     */
    void send(MessageBuffer message, String channelId) throws ChannelException;
    
    List<String> getSocketIds();
    
    int getNumConnections();

    void addConnectionEstablishedListener(ConnectionEstablishedListener listener);

    void removeConnectionEstablishedListener(ConnectionEstablishedListener listener);
    
    void addConnectionLostListener(ConnectionLostListener listener);

    void removeConnectionLostListener(ConnectionLostListener listener);

}
