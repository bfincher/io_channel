package com.fincher.io_channel.tcp;

import com.fincher.io_channel.ChannelException;
import com.fincher.thread.MyCallableIfc;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

import org.apache.log4j.Logger;

/**
 * Used to connect a TCP Server socket
 * 
 * @author Brian Fincher
 *
 */
class TcpServerConnectRunnable implements MyCallableIfc<Socket> {

    private static Logger logger = Logger.getLogger(TcpServerConnectRunnable.class);

    /** Should this thread continue to execute */
    private boolean continueExecution = true;

    /** The parent TCP Server object */
    private final TcpServerChannel tcpServer;

    /** The java.net.ServerSocket used to accept connections */
    protected final ServerSocket serverSocket;

    /** Is the server socket bound locally? */
    private boolean serverSocketConnected = false;

    /**
     * Construct a new TcpServerConnectRunnable
     * 
     * @param tcpServer The parent TCP Server object
     * @throws ChannelException
     */
    public TcpServerConnectRunnable(TcpServerChannel tcpServer) throws ChannelException {
        this.tcpServer = tcpServer;

        try {
            serverSocket = new ServerSocket();
            tcpServer.socketOptions.applySocketOptions(tcpServer.getId(), serverSocket);
        } catch (IOException ioe) {
            throw new ChannelException(tcpServer.getId(), ioe);
        }
    }

    @Override
    /**
     * Should this thread continue to execute
     * 
     * @return true if execution should continue
     */
    public boolean continueExecution() {
        return continueExecution;
    }

    @Override
    /** Called when the parent thread is terminating */
    public void terminate() {
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException ioe) {
                logger.error(tcpServer.getId() + " " + ioe.getMessage(), ioe);
            }
        }
    }

    @Override
    /** The body of the thread */
    public Socket call() throws InterruptedException, ChannelException {

        try {
            if (!serverSocketConnected) {
                try {
                    serverSocket.bind(tcpServer.getlocalAddress());
                    serverSocketConnected = true;
                } catch (BindException be) {
                    logger.warn(tcpServer.getId() + " " + be.getMessage());
                    synchronized (this) {
                        wait(2000);
                    }
                    throw be;
                }
            }

            Socket socket = serverSocket.accept();
            tcpServer.connectionEstablished(socket);
            return socket;
        } catch (SocketTimeoutException ste) {
            return null;
        } catch (IOException e) {
            logger.error(tcpServer.getId() + " " + e.getMessage(), e);
            synchronized (this) {
                wait(2000);
            }
            throw new ChannelException(e);
        }
    }

}
