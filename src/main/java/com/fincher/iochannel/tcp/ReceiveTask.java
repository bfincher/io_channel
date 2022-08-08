package com.fincher.iochannel.tcp;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;

import org.slf4j.Logger;

import com.fincher.iochannel.ChannelException;
import com.fincher.iochannel.Utilities;

class ReceiveTask extends AbstractReceiveTask {

    private static final Logger LOG = Utilities.getInstance().getLogger(ReceiveTask.class);

    /** The byte array used to receive data. */
    private byte[] buf = new byte[4096];

    /** The length of headers that are used to determine the length of messages. */
    private final int headerLength;

    private final StreamIo streamIo;

    private final InputStream inputStream;

    /**
     * Creates a new ReceiveTask object.
     * 
     * @param id     The ID of this ReceiveTask
     * @param socket The TCP Socket
     * @throws ChannelException If an error occurs while creating the socket connection
     */
    ReceiveTask(String id, Socket socket, StreamIo streamIo, TcpChannel parent)
            throws ChannelException {
        super(id, socket, parent);
        this.streamIo = streamIo;
        headerLength = streamIo.getHeaderLength();

        try {
            this.inputStream = socket.getInputStream();
        } catch (IOException e) {
            throw new ChannelException(e);
        }
    }
    
    private void growBufIfNecessary(int offset, int length) {
        int bytesRemaining = buf.length - offset;
        int delta = length - bytesRemaining;
        if (delta > 0) {
            byte[] newBuf = new byte[buf.length + delta + 100];
            System.arraycopy(buf, 0, newBuf, 0, buf.length);
            buf = newBuf;
        }
    }

    /**
     * Read data from the socket.
     * 
     * @param buf    The byte array used to store the result of the read
     * @param offset The index into buf used to store the result of the read
     * @param length The number of bytes that should be read
     * @throws IOException If an exception occurs while reading from the socket
     */
    private void read(int offset, int length) throws IOException {

        // increase size of buf if necessary
        growBufIfNecessary(offset, length);

        int remaining = length;
        while (remaining > 0) {
            try {
                int bytesRead = inputStream.read(buf, offset, remaining);

                if (bytesRead == -1) {
                    throw new EOFException();
                }

                offset += bytesRead;
                remaining -= bytesRead;
            } catch (SocketTimeoutException e) {
                // no action necessary
            }
        }
    }

    /**
     * The body of the task. Reads data from the socket and places the received messages on a
     * queue
     */
    @Override
    public void run() {
        try {

            if (LOG.isTraceEnabled()) {
                LOG.trace("{} Reading header (length = {})", getId(), headerLength);
            }
            read(0, headerLength);

            final int messageLength = streamIo.getMessageLength(buf);

            if (LOG.isTraceEnabled()) {
                LOG.trace("{} message length = {}", getId(), messageLength);
            }

            int bytesToRead;
            int offset;
            if (streamIo.headerPartOfMessage()) {
                bytesToRead = messageLength - headerLength;
                offset = headerLength;
            } else {
                bytesToRead = messageLength;
                offset = 0;
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("reading length {}", bytesToRead);
            }

            read(offset, bytesToRead);

            messageReceived(buf, 0, bytesToRead + offset);
        } catch (EOFException eofe) {
            LOG.warn("end of stream reached");

            try {
                closeSocket();
            } catch (ChannelException ce) {
                LOG.error(ce.getMessage(), ce);
            }
        } catch (IOException ioe) {
            LOG.error(ioe.getMessage(), ioe);

            try {
                closeSocket();
            } catch (ChannelException ce) {
                LOG.error(ce.getMessage(), ce);
            }
        }
    }
}
