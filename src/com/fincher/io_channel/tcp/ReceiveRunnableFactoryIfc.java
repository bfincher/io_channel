package com.fincher.io_channel.tcp;

import java.net.Socket;

import com.fincher.io_channel.ChannelException;
import com.fincher.io_channel.MyRunnableIfc;

public interface ReceiveRunnableFactoryIfc {
	
	public MyRunnableIfc createReceiveRunnable(String id,
			Socket socket,
			StreamIOIfc streamIo,
			TCPChannel parent) throws ChannelException;

}
