package com.fincher.iochannel.tcp;

/**
 * Uses a simple scheme of prepending a 4 byte integer containing the length of the message before
 * sending.
 *
 * @author Brian Fincher
 *
 */
public class SimpleStreamIo implements StreamIo {

    private final boolean headerPartOfMessage;

    /** Constructs a new SimpleStreamIo with headerPartOfMessage = false. */
    public SimpleStreamIo() {
        headerPartOfMessage = false;
    }

    /**
     * Constructs a new SimpleStreamIo.
     *
     * @param headerPartOfMessage Is the header considered a part of the message
     */
    public SimpleStreamIo(boolean headerPartOfMessage) {
        this.headerPartOfMessage = headerPartOfMessage;
    }

    /**
     * Returns the length of the header. Hard coded to 4
     * 
     * @return 4
     */
    @Override
    public int getHeaderLength() {
        return 4;
    }

    /**
     * Gets the length of the message by decoding the 4 byte integer header.
     * 
     * @return the length of the message
     */
    @Override
    public int getMessageLength(byte[] header) {
        int length = 0;
        length |= (header[0] & 0xff) << 24;
        length |= (header[1] & 0xff) << 16;
        length |= (header[2] & 0xff) << 8;
        length |= (header[3] & 0xff);

        return length;
    }

    /**
     * Creates a 4 byte integer containing the length of the given bytes. This 4 bytes along with the
     * given bytes are concatenated and returned
     * 
     * @param bytes message bytes before pre-pending the length
     * @return the original bytes plus the 4 byte length pre-pended
     */
    public byte[] prePendLength(byte[] bytes) {
        int origLength = bytes.length;
        int length;
        if (headerPartOfMessage) {
            length = origLength + 4;
        } else {
            length = origLength;
        }

        byte[] toReturn = new byte[origLength + 4];
        System.arraycopy(bytes, 0, toReturn, 4, origLength);
        populateByteArrayWithLength(toReturn, 0, length);
        return toReturn;
    }

    /**
     * Create a byte array containing the bytes of the given integer
     * 
     * @param length The integer to encode into a byte array
     * @return The encoded byte array
     */
    public static byte[] createLengthByteArray(int length) {
        byte[] toReturn = new byte[4];
        populateByteArrayWithLength(toReturn, 0, length);
        return toReturn;
    }

    /**
     * Should the header be included as a part of the message that is placed on the socket's queue?.
     * 
     * @return false
     */
    @Override
    public boolean headerPartOfMessage() {
        return headerPartOfMessage;
    }

    private static void populateByteArrayWithLength(byte[] bytes, int startPos, int length) {
        bytes[startPos] = (byte) ((length >> 24) & 0xff);
        bytes[startPos + 1] = (byte) ((length >> 16) & 0xff);
        bytes[startPos + 2] = (byte) ((length >> 8) & 0xff);
        bytes[startPos + 3] = (byte) (length & 0xff);
    }

}
