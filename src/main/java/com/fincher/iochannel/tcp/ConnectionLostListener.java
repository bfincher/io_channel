package com.fincher.iochannel.tcp;

@FunctionalInterface
public interface ConnectionLostListener {

    public void connectionLost(String channelID);

}
