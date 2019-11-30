package com.fincher.iochannel.tcp;

import com.fincher.iochannel.ChannelException;
import com.fincher.iochannel.ChannelState;
import com.fincher.thread.MyCallableIfc;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Used to connect a TCP Server socket.
 * 
 * @author Brian Fincher
 *
 */
class TcpServerConnectRunnable implements MyCallableIfc<Socket> {

    private static final Logger LOG = LogManager.getLogger();

    /** Should this thread continue to execute. */
    private boolean continueExecution = true;

    /** The parent TCP Server object. */
    private final TcpServerChannel tcpServer;

    /** The java.net.ServerSocket used to accept connections */
    protected final ServerSocket serverSocket;

    /** Is the server socket bound locally?. */
    private boolean serverSocketConnected = false;

    /**
     * Construct a new TcpServerConnectRunnable.
     * 
     * @param tcpServer The parent TCP Server object
     * @throws ChannelException If an error occurs while creating the socket
     */
    TcpServerConnectRunnable(TcpServerChannel tcpServer) throws ChannelException {
        this.tcpServer = tcpServer;

        try {
            serverSocket = new ServerSocket();
            tcpServer.socketOptions.applySocketOptions(tcpServer.getId(), serverSocket);
        } catch (IOException ioe) {
            throw new ChannelException(tcpServer.getId(), ioe);
        }
    }

    /**
     * Should this thread continue to execute.
     * 
     * @return true if execution should continue
     */
    @Override
    public boolean continueExecution() {
        return continueExecution;
    }

    /** Called when the parent thread is terminating. */
    @Override
    public void terminate() {
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException ioe) {
                LOG.error(tcpServer.getId() + " " + ioe.getMessage(), ioe);
            }
        }
    }

    @Override
    public Socket call() throws InterruptedException, ChannelException {

        try {
            if (!serverSocketConnected) {
                serverSocketConnected = connectSocket();
            }

            Socket socket = serverSocket.accept();
            tcpServer.connectionEstablished(socket);
            return socket;
        } catch (SocketTimeoutException ste) {
            return null;
        } catch (IOException e) {
            if (tcpServer.getState() == ChannelState.CLOSED) {
                return null;
            } else {
                LOG.error(tcpServer.getId() + " " + e.getMessage(), e);
                synchronized (this) {
                    wait(2000);
                }
                throw new ChannelException(e);
            }
        }
    }

    private boolean connectSocket() throws IOException, InterruptedException {
        try {
            serverSocket.bind(tcpServer.getlocalAddress());
            return true;
        } catch (BindException be) {
            LOG.warn("{} {} ", tcpServer.getId(), be.getMessage());
            synchronized (this) {
                wait(2000);
            }
            throw be;
        }
    }

}
