package com.fincher.iochannel.tcp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.slf4j.Logger;

import com.fincher.iochannel.Utilities;
import com.fincher.thread.CallableTask;

/**
 * Used to connect a TCP Client socket.
 * 
 * @author Brian Fincher
 *
 */
class TcpClientConnectTask implements CallableTask<Socket> {

    private static final Logger LOG = Utilities.getInstance().getLogger(TcpClientConnectTask.class);

    /** Should this task continue to execute. */
    private boolean continueExecution = true;

    /** The parent object. */
    private final TcpClientChannel parent;

    /** The remote address to which this client is trying to connect. */
    private final InetSocketAddress remoteAddress;

    /**
     * Constructs a new TcpClientConnectTask.
     * 
     * @param parent        The parent object
     * @param remoteAddress The remote address to which this client is trying to connect
     */
    TcpClientConnectTask(TcpClientChannel parent, InetSocketAddress remoteAddress) {
        this.parent = parent;
        this.remoteAddress = remoteAddress;
    }

    @Override
    public boolean continueExecution() {
        return continueExecution;
    }

    @Override
    public Socket call() throws InterruptedException, IOException {
        try {
            Socket socket = new Socket(remoteAddress.getAddress(), remoteAddress.getPort());

            socket.setSoTimeout(2000);

            LOG.info("{} connection established.  Local port = {}", parent.getId(),
                    socket.getLocalPort());
            parent.connectionEstablished(socket);
            continueExecution = false;
            return socket;
        } catch (IOException ioe) {
            LOG.error(parent.getId() + " " + ioe.getMessage(), ioe);
            Utilities.sleep(this, parent.getSocketSleepTime());
            throw ioe;
        }
    }

    @Override
    public void terminate() {
        // no action necessary
    }
}
