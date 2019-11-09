package com.fincher.io_channel.tcp;

import com.fincher.io_channel.ChannelException;
import com.fincher.io_channel.MessageBuffer;
import com.fincher.thread.MyRunnableIfc;

import java.net.Socket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class AbstractReceiveRunnable implements MyRunnableIfc {

    private static final Logger LOGGER = LogManager.getLogger();

    /** Should this thread continue to execute */
    private boolean continueExecution = true;

    private Socket socket;

    private volatile boolean terminate = false;

    private final String id;

    protected final TcpChannel parent;

    public AbstractReceiveRunnable(String id, Socket socket, TcpChannel parent) {
        this.id = id;
        this.socket = socket;
        this.parent = parent;
    }

    public String getId() {
        return id;
    }

    public Socket getSocket() {
        return socket;
    }

    public TcpChannel getParent() {
        return parent;
    }

    @Override
    /**
     * Should this thread continue to execute
     * 
     * @return true if this thread should continue to execute
     */
    public boolean continueExecution() {
        return continueExecution;
    }

    /** The connection was lost so notify the parent that the connection was lost */
    protected void closeSocket() throws ChannelException {
        continueExecution = false;
        terminate = true;
        parent.connectionLost(socket);
    }

    /** Called when this thread is terminating. No action is necessary */
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
