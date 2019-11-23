package com.fincher.io_channel.tcp;

import com.fincher.io_channel.ChannelException;
import com.fincher.thread.MyRunnableIfc;

import java.net.Socket;

interface ReceiveRunnableFactory {

    MyRunnableIfc createReceiveRunnable(String id, Socket socket, StreamIo streamIo,
            TcpChannel parent) throws ChannelException;

}
