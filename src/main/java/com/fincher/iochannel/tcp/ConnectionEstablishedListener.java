package com.fincher.iochannel.tcp;

@FunctionalInterface
public interface ConnectionEstablishedListener {

    public void connectionEstablished(String channelID);

}
