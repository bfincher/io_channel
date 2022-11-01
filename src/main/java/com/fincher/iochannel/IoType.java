package com.fincher.iochannel;

/**
 * Specifies the Input / Output type of an IO Thread.
 * 
 * @author Brian Fincher
 *
 */
public enum IoType {

    /** The IO Thread is only capable of receiving messages. */
    INPUT_ONLY,

    /** The IO Thread is only capable of sending messages. */
    OUTPUT_ONLY,

    /** The IO Thread is capable of both sending and receiving messages. */
    INPUT_AND_OUTPUT;

    /**
     * Is this IoType capable of receiving messages.
     * 
     * @return true if this IoType capable of receiving messages
     */
    public boolean isInput() {
        return this == INPUT_ONLY || this == INPUT_AND_OUTPUT;
    }

    /**
     * Is this IoType capable of sending messages.
     * 
     * @return true if this IoType capable of sending messages
     */
    public boolean isOutput() {
        return this == OUTPUT_ONLY || this == INPUT_AND_OUTPUT;
    }

}
