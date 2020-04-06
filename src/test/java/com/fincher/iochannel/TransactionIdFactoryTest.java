package com.fincher.iochannel;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TransactionIdFactoryTest {
    
    @Test
    public void test() {
        TransactionIdFactory.init(5, 7);
        assertEquals(5, TransactionIdFactory.getNextTid());
        assertEquals(6, TransactionIdFactory.getNextTid());
        assertEquals(7, TransactionIdFactory.getNextTid());
        assertEquals(5, TransactionIdFactory.getNextTid());
    }

}
