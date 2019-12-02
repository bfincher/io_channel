package com.fincher.iochannel.udp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import com.fincher.iochannel.ChannelException;

import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class UdpSocketOptionsTest {
    
    @Test
    public void test() throws Exception {
        DatagramSocket socket = mock(DatagramSocket.class);
        
        AtomicReference<Object[]> recBufSize = new AtomicReference<>();
        AtomicReference<Object[]> sendBufSize = new AtomicReference<>();
        AtomicReference<Object[]> timeout = new AtomicReference<>();
        doAnswer(new MyAnswer(o -> recBufSize.set(o))).when(socket).setReceiveBufferSize(anyInt());
        doAnswer(new MyAnswer(o -> sendBufSize.set(o))).when(socket).setSendBufferSize(anyInt());
        doAnswer(new MyAnswer(o -> timeout.set(o))).when(socket).setSoTimeout(anyInt());
        
        UdpSocketOptions so = new UdpSocketOptions();
        so.clearReceiveBufferSize();
        so.clearSendBufferSize();
        so.clearTimeout();
        so.applySocketOptions("id", socket);
        
        assertNull(recBufSize.get());
        assertNull(sendBufSize.get());
        assertNull(timeout.get());
        
        
        Logger logger = (Logger)LogManager.getLogger(UdpSocketOptions.class);
        logger.setLevel(Level.OFF);
        
        // test real values
        so.setReceiveBufferSize(5);
        so.setSendBufferSize(6);
        so.setTimeout(7);
        so.applySocketOptions("id", socket);
        
        testArray(recBufSize, 5);
        testArray(sendBufSize, 6);
        testArray(timeout, 7);
        
        // testException
        doAnswer(new MyAnswer(new SocketException())).when(socket).setSoTimeout(anyInt());
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
    
    
    private static class MyAnswer implements Answer<Void> {
        
        private final Consumer<Object[]> consumer;
        private final Exception exception;
        
        MyAnswer(Consumer<Object[]> consumer) {
            this.consumer = consumer;
            exception = null;
        }
        
        MyAnswer(Exception exception) {
            this.exception = exception;
            consumer = null;
        }
        
        @Override
        public Void answer(InvocationOnMock invocation) throws Exception {
            if (exception == null) {
                consumer.accept(invocation.getArguments());
                return null;
            } else {
                throw exception;
            }
        }
    }

}
