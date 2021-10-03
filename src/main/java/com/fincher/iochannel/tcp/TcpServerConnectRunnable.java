package com.fincher.iochannel.tcp;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fincher.iochannel.ChannelException;
import com.fincher.iochannel.ChannelState;
import com.fincher.thread.MyCallableIfc;

/**
 * Used to connect a TCP Server socket.
 * 
 * @author Brian Fincher
 *
 */
class TcpServerConnectRunnable implements MyCallableIfc<Socket> {

    private static final Logger LOG = LogManager.getLogger();
    
    @FunctionalInterface
    interface CheckedSupplier extends Supplier<ServerSocket> {
        
        ServerSocket checkedGet() throws IOException;
        
        default ServerSocket get() {
            try {
                return checkedGet();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    
    static final CheckedSupplier DEFAULT_SERVER_SOCKET_FACTORY = () -> new ServerSocket();
    
    // This factory is only used for unit testing purposes
    static CheckedSupplier serverSocketFactory = DEFAULT_SERVER_SOCKET_FACTORY;

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
    TcpServerConnectRunnable(TcpServerChannel tcpServer, ServerSocket serverSocket) throws ChannelException {
        this.tcpServer = tcpServer;
        this.serverSocket = serverSocket;

        try {
            tcpServer.getSocketOptions().applySocketOptions(tcpServer.getId(), serverSocket);
        } catch (IOException ioe) {
            throw new ChannelException(tcpServer.getId(), ioe);
        }
    }

    /**
     * Construct a new TcpServerConnectRunnable.
     * 
     * @param tcpServer The parent TCP Server object
     * @throws ChannelException If an error occurs while creating the socket
     */
    static TcpServerConnectRunnable create(TcpServerChannel tcpServer) throws ChannelException {
        try {
            return new TcpServerConnectRunnable(tcpServer, serverSocketFactory.checkedGet());
        } catch (ChannelException ce) {
            throw ce;
        } catch (IOException e) {
            throw new ChannelException(tcpServer.getId(), e);
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
    
    // This method is here for unit testing purposes
    protected ServerSocket getServerSocket() {
        return serverSocket;
    }

    // this method is protected for testing purposes
    protected boolean connectSocket() throws IOException, InterruptedException {
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
