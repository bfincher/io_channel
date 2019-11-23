package com.fincher.io_channel.tcp;

import com.fincher.io_channel.ChannelException;
import com.fincher.io_channel.ChannelState;
import com.fincher.thread.MyCallableIfc;
import com.fincher.thread.MyThread;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Used to connect a TCP Server socket
 * 
 * @author Brian Fincher
 *
 */
class TcpServerConnectRunnable implements MyCallableIfc<Socket> {

    private static final Logger LOG = LogManager.getLogger();

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
    TcpServerConnectRunnable(TcpServerChannel tcpServer) throws ChannelException {
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
                LOG.error(tcpServer.getId() + " " + ioe.getMessage(), ioe);
            }
        }
    }

    @Override
    /** The body of the thread */
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
                MyThread.wait(2, TimeUnit.SECONDS, this);
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
            MyThread.wait(2, TimeUnit.SECONDS, this);
            throw be;
        }
    }

}
