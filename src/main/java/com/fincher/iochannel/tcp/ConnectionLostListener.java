package com.fincher.iochannel.tcp;

/** A listener to be notified when a connection is lost */
@FunctionalInterface
public interface ConnectionLostListener {

    /**
     * Called when a connection is list
     * 
     * @param channelID The ID of the channel that had the connection lost
     */
    public void connectionLost(String channelID);

}
