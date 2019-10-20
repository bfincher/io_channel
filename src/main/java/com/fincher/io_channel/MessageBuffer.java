package com.fincher.io_channel;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/** An message in an encoded form */
public class MessageBuffer implements ExchangeableIfc {

    private final byte[] bytes;

    private long transactionId;

    private List<Long> parentTransactionIds = new ArrayList<>();

    private final long originationTime;

    private String receivedFromIoChannel;

    /**
     * Constructs a new MessageBuffer
     * 
     * @param bytes The encoded message bytes
     */
    public MessageBuffer(byte[] bytes) {
        this(bytes, 0, bytes.length);
    }

    /**
     * Constructs a new MessageBuffer
     * 
     * @param bytes  The encoded message bytes
     * @param offset The offset into the bytes array
     * @param length The length of the bytes array from offset which should be copied into this
     *               MessageBuffer
     */
    public MessageBuffer(byte[] bytes, int offset, int length) {
        transactionId = TransactionIdFactory.getNextTid();
        originationTime = System.currentTimeMillis();
        this.bytes = new byte[length];
        System.arraycopy(bytes, offset, this.bytes, 0, length);
    }

    /**
     * Construct a new MessageBuffer from an ASCII hex dump
     * 
     * @param asciiHexDump  The ASCII hex dump
     * @param messageFormat The message format of this MessageBuffer
     */
    public MessageBuffer(String asciiHexDump) {
        transactionId = TransactionIdFactory.getNextTid();
        originationTime = System.currentTimeMillis();

        StringTokenizer tokenizer = new StringTokenizer(asciiHexDump, " ");
        bytes = new byte[tokenizer.countTokens()];

        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(tokenizer.nextToken(), 16);
        }
    }

    /**
     * Get the encoded bytes
     * 
     * @return the encoded bytes
     */
    public byte[] getBytes() {
        return bytes;
    }

    @Override
    public long getTransactionId() {
        return transactionId;
    }

    @Override
    public void setTransactionId(long transactionId) {
        this.transactionId = transactionId;
    }

    @Override
    public List<Long> getParentTransactionIds() {
        return parentTransactionIds;
    }

    @Override
    public void addParentTransactionId(long parentTransactionId) {
        parentTransactionIds.add(parentTransactionId);
    }

    @Override
    public long getOriginationTime() {
        return originationTime;
    }

    /**
     * Convert the array of bytes into a hexadecimal formatted string
     * 
     * @return The bytes in a hex string
     */
    public String toHexString() {
        StringBuilder sb = new StringBuilder();
        for (Long tid : getParentTransactionIds()) {
            sb.append(tid + ", ");
        }
        sb.append(getTransactionId());
        sb.append(", " + getOriginationTime());
        sb.append(", hex dump: ");
        sb.append(toHexString(bytes));
        return sb.toString();
    }
    
    /**
     * Convert the array of bytes into a hexadecimal formatted string
     * 
     * @param bytes The input array
     * @return The bytes in a hex string
     */
    public static String toHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder(200);
        for (int i = 0; i < bytes.length; i++) {
            String hexStr = Integer.toHexString(bytes[i]);
            int len = hexStr.length();
            if (len == 1) {
                sb.append('0');
            } else if (len > 2) {
                hexStr = hexStr.substring(len - 2, len);
            }
            sb.append(hexStr + ' ');
        }

        return sb.toString();
    }

    
    
    /** Sets the ID of the channel from which this message was received
     * 
     * @param channelId the ID of the channel from which this message was received
     */
    public void setReceivedFromIoChannelId(String channelId) {
        receivedFromIoChannel = channelId;
    }
    
    /** Gets the ID of the IO channel from which this message was received
     * 
     * @return the ID of the IO channel from which this message was received
     */
    public String getReceivedFromChannelId() {
        return receivedFromIoChannel;
    }

  
    /**
     * Convert the hex string into a MessageBuffer
     * 
     * @param str The hex string
     * @return A new MessageBuffer
     */
    public static MessageBuffer fromHexString(String str) {
        int idx = str.indexOf("hex string: ");
        if (idx != -1) {
            str = str.substring(idx + 12);
        }

        StringTokenizer tokenizer = new StringTokenizer(str, " ");
        byte[] bytes = new byte[tokenizer.countTokens()];

        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = Byte.valueOf(tokenizer.nextToken(), 16);
        }

        return new MessageBuffer(bytes);
    }
}
