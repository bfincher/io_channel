package com.fincher.iochannel.tcp;

import com.fincher.iochannel.ChannelException;
import com.fincher.thread.MyRunnableIfc;

import java.net.Socket;

interface ReceiveRunnableFactory {

    MyRunnableIfc createReceiveRunnable(String id, Socket socket, StreamIo streamIo,
            TcpChannel parent) throws ChannelException;

}
