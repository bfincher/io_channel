package com.fincher.io_channel;

/** Specifies the Input / Output type of an IO Thread
 * 
 * @author Brian Fincher
 *
 */
public enum IOTypeEnum {
	
	/** The IO Thread is only capable of receiving messages */
	INPUT_ONLY,
	
	/** The IO Thread is only capable of sending messages */
	OUTPUT_ONLY,
	
	/** The IO Thread is capable of both sending and receiving messages */
	INPUT_AND_OUTPUT

}
