package com.fincher.io_channel.tcp;

@FunctionalInterface
public interface ConnectionEstablishedListener {

    public void connectionEstablished(String channelID);

}
