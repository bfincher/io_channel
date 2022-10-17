package com.fincher.iochannel;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

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
        assertTrue(delta <= 100, "time delta is " + delta);
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
        assertNotEquals(0, mb.getTransactionId());
        assertEquals(0, mb.getParentTransactionIds().size());
        
        String expectedHexDump = mb.getTransactionId() + ", " + mb.getOriginationTime() + ", hex dump: 0a 0b 0c 0d 0e 14";
        assertEquals(expectedHexDump, mb.toHexString());
        
        MessageBuffer mb2 = MessageBuffer.fromHexString(mb.toHexString());
        assertArrayEquals(mb.getBytes(), mb2.getBytes());
        
        assertEquals("0a 0b 0c 0d 0e 14", MessageBuffer.toHexString(bytes));
        mb2 = MessageBuffer.fromHexString(MessageBuffer.toHexString(bytes));
        assertArrayEquals(bytes, mb2.getBytes());   
    }
    
    
    @Test
    public void testConstructMultipleArrays() {
        byte[] bytes1 = {1, 2, 3};
        byte[] bytes2 = {4, 5, 6};
        MessageBuffer mb = new MessageBuffer(bytes1, bytes2);
        
        byte[] bytes = mb.getBytes();
        assertEquals(6, bytes.length);
        
        byte[] expected = {1, 2, 3, 4, 5, 6};
        assertArrayEquals(expected, bytes);
    }

}
