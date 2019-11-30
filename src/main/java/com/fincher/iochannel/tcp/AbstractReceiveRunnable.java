package com.fincher.iochannel.tcp;

import com.fincher.iochannel.ChannelException;
import com.fincher.iochannel.MessageBuffer;
import com.fincher.thread.MyRunnableIfc;

import java.net.Socket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

abstract class AbstractReceiveRunnable implements MyRunnableIfc {

    private static final Logger LOGGER = LogManager.getLogger();

    /** Should this thread continue to execute. */
    private boolean continueExecution = true;

    private Socket socket;

    private volatile boolean terminate = false;

    private final String id;

    protected final TcpChannel parent;

    /** Constructs a new AbstractReceiveRunnable.
     * 
     * @param id The ID of this ReceiveRunnable
     * @param socket The TCP socket
     * @param parent The parent TCPChannel
     */
    public AbstractReceiveRunnable(String id, Socket socket, TcpChannel parent) {
        this.id = id;
        this.socket = socket;
        this.parent = parent;
    }

    public String getId() {
        return id;
    }


    public TcpChannel getParent() {
        return parent;
    }

    @Override
    public boolean continueExecution() {
        return continueExecution;
    }

    /** The connection was lost so notify the parent that the connection was lost.*/
    protected void closeSocket() throws ChannelException {
        continueExecution = false;
        terminate = true;
        parent.connectionLost(socket);
    }

    @Override
    public void terminate() {
        terminate = true;
    }

    public boolean isTerminated() {
        return terminate;
    }

    protected void messageReceived(byte[] buf, int offset, int length) {
        if (!isTerminated()) {
            MessageBuffer mb = new MessageBuffer(buf, offset, length);
            mb.setReceivedFromIoChannelId(getId());
            getParent().messageReceived(mb, LOGGER, "");
        }
    }

}
