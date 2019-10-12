package com.fincher.io_channel.tcp;

/**
 * Uses a simple scheme of prepending a 4 byte integer containing the length of the message before
 * sending
 * 
 * @author s149450 Brian Fincher
 *
 */
public class SimpleStreamIO implements StreamIoIfc {

    @Override
    /**
     * Returns the length of the header. Hard coded to 4
     * 
     * @return 4
     */
    public int getHeaderLength() {
        return 4;
    }

    @Override
    /**
     * Gets the length of the message by decoding the 4 byte integer header
     * 
     * @return the length of the message
     */
    public int getMessageLength(byte[] header) {
        int length = 0;
        length |= (header[0] & 0xff) << 24;
        length |= (header[1] & 0xff) << 16;
        length |= (header[2] & 0xff) << 8;
        length |= (header[3] & 0xff);

        return length;
    }

    /**
     * Creates a 4 byte integer containing the length of the given bytes. This 4 bytes along with
     * the given bytes are concatenated and returned
     * 
     * @param bytes
     * @return the original bytes plus the 4 byte length pre-pended
     */
    public static byte[] prePendLength(byte[] bytes) {
        int origLength = bytes.length;
        byte[] toReturn = new byte[origLength + 4];
        System.arraycopy(bytes, 0, toReturn, 4, origLength);
        toReturn[0] = (byte) ((origLength >> 24) & 0xff);
        toReturn[1] = (byte) ((origLength >> 16) & 0xff);
        toReturn[2] = (byte) ((origLength >> 8) & 0xff);
        toReturn[3] = (byte) (origLength & 0xff);

        return toReturn;
    }

//    /**
//     * Creates a header that should be sent prior to the message bytes
//     * 
//     * @param message The message to be sent
//     * @return The message header
//     */
//    @Override
//    public byte[] getOutgoingHeader(byte[] message) {
//        byte[] dest = new byte[4];
//
//        dest[0] = (byte) ((message.length >> 24) & 0xff);
//        dest[1] = (byte) ((message.length >> 16) & 0xff);
//        dest[2] = (byte) ((message.length >> 8) & 0xff);
//        dest[3] = (byte) (message.length & 0xff);
//
//        return dest;
//    }

    /**
     * Should the header be included as a part of the message that is placed on the socket's queue?
     * 
     * @return false
     */
    @Override
    public boolean headerPartOfMessage() {
        return false;
    }

}
