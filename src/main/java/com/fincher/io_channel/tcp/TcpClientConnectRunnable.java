package com.fincher.io_channel.tcp;

import com.fincher.thread.MyCallableIfc;
import com.fincher.thread.MyThread;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Used to connect a TCP Client socket
 * 
 * @author Brian Fincher
 *
 */
class TcpClientConnectRunnable implements MyCallableIfc<Socket> {

    private static final Logger LOG = LoggerFactory.getLogger(TcpClientConnectRunnable.class);

    /** Should this thread continue to execute */
    private boolean continueExecution = true;

    /** The parent object */
    private final TcpClientChannel parent;

    /** The remote address to which this client is trying to connect */
    private final InetSocketAddress remoteAddress;

    /**
     * Constructs a new TcpClientConnectRunnable
     * 
     * @param parent        The parent object
     * @param remoteAddress The remote address to which this client is trying to connect
     */
    public TcpClientConnectRunnable(TcpClientChannel parent, InetSocketAddress remoteAddress) {
        this.parent = parent;
        this.remoteAddress = remoteAddress;
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
    /** The body of the thread */
    public Socket call() throws InterruptedException, IOException {
        try {
            Socket socket = new Socket(remoteAddress.getAddress(), remoteAddress.getPort());

            socket.setSoTimeout(2000);

            LOG.info("{} connection established.  Local port = {}", parent.getId(), socket.getLocalPort());
            parent.connectionEstablished(socket);
            continueExecution = false;
            return socket;
        } catch (IOException ioe) {
            LOG.error(parent.getId() + " " + ioe.getMessage(), ioe);
            MyThread.wait(2, TimeUnit.SECONDS, this);
            throw ioe;
        }
    }

    @Override
    /** No action taken */
    public void terminate() {
        // no action necessary
    }
}
