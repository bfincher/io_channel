package com.fincher.iochannel.udp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import com.fincher.iochannel.ChannelException;
import com.fincher.iochannel.TestAnswer;

public class UdpSocketOptionsTest {
    
    @Test
    public void test() throws Exception {
        DatagramSocket socket = mock(DatagramSocket.class);
        
        AtomicReference<Object[]> recBufSize = new AtomicReference<>();
        AtomicReference<Object[]> sendBufSize = new AtomicReference<>();
        AtomicReference<Object[]> timeout = new AtomicReference<>();
        doAnswer(new TestAnswer(o -> recBufSize.set(o))).when(socket).setReceiveBufferSize(anyInt());
        doAnswer(new TestAnswer(o -> sendBufSize.set(o))).when(socket).setSendBufferSize(anyInt());
        doAnswer(new TestAnswer(o -> timeout.set(o))).when(socket).setSoTimeout(anyInt());
        
        UdpSocketOptions so = new UdpSocketOptions();
        so.clearReceiveBufferSize();
        so.clearSendBufferSize();
        so.clearTimeout();
        so.applySocketOptions("id", socket);
        
        assertNull(recBufSize.get());
        assertNull(sendBufSize.get());
        assertNull(timeout.get());
        
        // test real values
        so.setReceiveBufferSize(5);
        so.setSendBufferSize(6);
        so.setTimeout(7);
        so.applySocketOptions("id", socket);
        
        testArray(recBufSize, 5);
        testArray(sendBufSize, 6);
        testArray(timeout, 7);
        
        // testException
        doAnswer(new TestAnswer(new SocketException())).when(socket).setSoTimeout(anyInt());
        try {
            so.applySocketOptions("id", socket);
            fail("Should have got exception");
        } catch (ChannelException e) {
            // expected
        }
    }
    
    private static void testArray(AtomicReference<Object[]> ref, int val) {
        Object[] array = ref.get();
        assertNotNull(array);
        assertEquals(1, array.length);
        assertEquals(val, ((Integer)array[0]).intValue());
        
    }

}
