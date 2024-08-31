package com.fincher.iochannel.tcp;

import java.io.IOException;

/**
 * Used to determine how to read from a TCP socket.
 * 
 * @author Brian Fincher
 *
 */
public interface StreamIo {

    /**
     * Gets the number of bytes that should be read to determine the length of the
     * message.
     * 
     * @return the number of bytes that should be read to determine the length of
     *         the message
     */
    public int getHeaderLength();

    /**
     * Gets the length of the message minus the header bytes.
     * 
     * @param header The message header
     * @return the length of the message minus the header bytes
     * @throws IOException If an exception occurs while determining the message
     *         length
     */
    public int getMessageLength(byte[] header) throws IOException;

    /**
     * Should the header be included as a part of the message that is placed on the
     * socket's queue?.
     * 
     * @return True if the header be included as a part of the message that is
     *         placed on the socket's queue
     */
    public boolean headerPartOfMessage();

}
