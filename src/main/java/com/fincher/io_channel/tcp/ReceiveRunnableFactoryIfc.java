package com.fincher.io_channel.tcp;

import com.fincher.io_channel.ChannelException;
import com.fincher.thread.MyRunnableIfc;

import java.net.Socket;

public interface ReceiveRunnableFactoryIfc {

    public MyRunnableIfc createReceiveRunnable(String id, Socket socket, StreamIoIfc streamIo,
            TcpChannel parent) throws ChannelException;

}
