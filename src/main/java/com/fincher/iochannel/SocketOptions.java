package com.fincher.iochannel;

import java.util.OptionalInt;

/**
 * IP Socket Options.
 * 
 * @author Brian Fincher
 *
 */
public class SocketOptions {

    /** The SO_SNDBUF socket setting. */
    private OptionalInt sendBufferSize = OptionalInt.empty();

    /** The SO_RCVBUF socket setting. */
    private OptionalInt receiveBufferSize = OptionalInt.empty();

    /** The SO_REUSEADDR socket setting. Defaults to true */
    private boolean reuseAddress = true;

    /**
     * The SO_TIMEOUT socket setting (in milliseconds). Defaults to 2000 (2 seconds)
     */
    private OptionalInt timeout = OptionalInt.of(2000);

    /**
     * Gets the SO_SNDBUF socket setting.
     * 
     * @return the SO_SNDBUF socket setting
     */
    public OptionalInt getSendBufferSize() {
        return sendBufferSize;
    }

    /**
     * Sets the SO_SNDBUF socket setting.
     * 
     * @param sendBufferSize the SO_SNDBUF socket setting
     */
    public void setSendBufferSize(int sendBufferSize) {
        this.sendBufferSize = OptionalInt.of(sendBufferSize);
    }

    /** Clears the SO_SNDBUF socket setting */
    public void clearSendBufferSize() {
        sendBufferSize = OptionalInt.empty();
    }

    /**
     * Gets the SO_RCVBUF socket setting.
     * 
     * @return the SO_RCVBUF socket setting
     */
    public OptionalInt getReceiveBufferSize() {
        return receiveBufferSize;
    }

    /**
     * Sets the SO_RCVBUF socket setting.
     * 
     * @param receiveBufferSize the SO_RCVBUF socket setting
     */
    public void setReceiveBufferSize(int receiveBufferSize) {
        this.receiveBufferSize = OptionalInt.of(receiveBufferSize);
    }

    /** Clears the SO_RCVBUF socket setting */
    public void clearReceiveBufferSize() {
        receiveBufferSize = OptionalInt.empty();
    }

    /**
     * Gets the SO_REUSEADDR socket setting.
     * 
     * @return the SO_REUSEADDR socket setting
     */
    public boolean isReuseAddress() {
        return reuseAddress;
    }

    /**
     * Sets the SO_REUSEADDR socket setting.
     * 
     * @param reuseAddress the SO_REUSEADDR socket setting
     */
    public void setReuseAddress(boolean reuseAddress) {
        this.reuseAddress = reuseAddress;
    }

    /**
     * Gets the SO_TIMEOUT socket setting (in milliseconds).
     * 
     * @return the SO_TIMEOUT socket setting (in milliseconds)
     */
    public OptionalInt getTimeout() {
        return timeout;
    }

    /**
     * Sets the SO_TIMEOUT socket setting (in milliseconds).
     * 
     * @param timeout the SO_TIMEOUT socket setting (in milliseconds)
     */
    public void setTimeout(int timeout) {
        this.timeout = OptionalInt.of(timeout);
    }

    /** Clears the SO_TIMEOUT socket setting */
    public void clearTimeout() {
        timeout = OptionalInt.empty();
    }

}
