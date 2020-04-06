package com.fincher.iochannel;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class MessageBufferTest {
    
    @Test
    public void test() {
        byte[] bytes = {10, 11, 12, 13, 14, 20};
        
        long time = System.currentTimeMillis();
        MessageBuffer mb = new MessageBuffer(bytes, 1, 2);
        assertEquals(2, mb.getBytes().length);
        assertEquals(11, mb.getBytes()[0]);
        assertEquals(12, mb.getBytes()[1]);
        mb.setTransactionId(10);
        assertEquals(10, mb.getTransactionId());
        assertEquals(0, mb.getParentTransactionIds().size());
        long delta = mb.getOriginationTime() - time;
        assertTrue("time delta is " + delta, delta <= 100);
        assertNull(mb.getReceivedFromChannelId());
        mb.setReceivedFromIoChannelId("bla");
        assertEquals("bla", mb.getReceivedFromChannelId());
        mb.addParentTransactionId(5);
        mb.addParentTransactionId(7);
        assertEquals(2, mb.getParentTransactionIds().size());
        assertEquals(5, mb.getParentTransactionIds().get(0).longValue());
        assertEquals(7, mb.getParentTransactionIds().get(1).longValue());
        
        mb = new MessageBuffer(bytes);
        assertArrayEquals(bytes, mb.getBytes());
        assertEquals(1, mb.getTransactionId());
        assertEquals(0, mb.getParentTransactionIds().size());
        
        String expectedHexDump = "1, " + mb.getOriginationTime() + ", hex dump: 0a 0b 0c 0d 0e 14";
        assertEquals(expectedHexDump, mb.toHexString());
        
        MessageBuffer mb2 = MessageBuffer.fromHexString(mb.toHexString());
        assertArrayEquals(mb.getBytes(), mb2.getBytes());
        
        assertEquals("0a 0b 0c 0d 0e 14", MessageBuffer.toHexString(bytes));
        mb2 = MessageBuffer.fromHexString(MessageBuffer.toHexString(bytes));
        assertArrayEquals(bytes, mb2.getBytes());
        
    }

}
