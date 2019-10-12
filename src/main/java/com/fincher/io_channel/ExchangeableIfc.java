package com.fincher.io_channel;

/**
 * An interface representing messages exchanged between components. This can either be MessageIfc
 * (internal POJOs) or MessageBuffer (external encoded messages)
 * 
 * @author Brian Fincher
 *
 */
public interface ExchangeableIfc extends HasTransactionIdIfc, HasOriginationTimeIfc {

}
