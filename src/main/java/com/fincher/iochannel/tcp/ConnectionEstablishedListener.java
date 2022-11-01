package com.fincher.iochannel.tcp;

/** A listener to be notified when a connection is established */
@FunctionalInterface
public interface ConnectionEstablishedListener {

    /**
     * Called when a connection is established
     * 
     * @param channelID The ID of the channel that had the connection established
     */
    public void connectionEstablished(String channelID);

}
