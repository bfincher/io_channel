package com.fincher.io_channel;

/** An enumeration of the different types of data that could be received by an IOChannel
 * 
 * @author Brian Fincher
 *
 */
public enum IOChannelDataTypeEnum {
	
	/** IOChannels that receive data in some native format */
	RAW_DATA,
	
	/** IOChannels that receive data in the form of Java Objects */
	POJO

}
