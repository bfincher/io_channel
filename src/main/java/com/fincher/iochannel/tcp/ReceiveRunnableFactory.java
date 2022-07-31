package com.fincher.iochannel.tcp;

import java.net.Socket;

import com.fincher.iochannel.ChannelException;
import com.fincher.thread.RunnableTask;

interface ReceiveRunnableFactory {

    RunnableTask createReceiveRunnable(String id, Socket socket, StreamIo streamIo,
            TcpChannel parent) throws ChannelException;

}
