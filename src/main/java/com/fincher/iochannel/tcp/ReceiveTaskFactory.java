package com.fincher.iochannel.tcp;

import java.net.Socket;

import com.fincher.iochannel.ChannelException;
import com.fincher.thread.RunnableTask;

interface ReceiveTaskFactory {

    RunnableTask createReceiveTask(String id, Socket socket, StreamIo streamIo,
            TcpChannel parent) throws ChannelException;

}
