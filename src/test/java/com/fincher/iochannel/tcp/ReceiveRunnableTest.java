package com.fincher.iochannel.tcp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.Socket;

import org.junit.jupiter.api.Test;

public class ReceiveRunnableTest {
    
    @Test
    public void testConstructorThrowsException() throws IOException {
        Socket socket = mock(Socket.class);
        when(socket.getInputStream()).thenThrow(IOException.class);
        
        assertThrows(IOException.class, () -> new ReceiveRunnable("id", socket, new SimpleStreamIo(), null));
    }
    
    @Test
    public void testGrowBuff() throws IOException, ReflectiveOperationException {
        Socket socket = mock(Socket.class);
        StreamIo streamIo = mock(StreamIo.class);
        TcpChannel parent = mock(TcpChannel.class);
        InputStream inputStream = mock(InputStream.class);
        
        when(streamIo.getHeaderLength()).thenReturn(4097);
        when(inputStream.read(any(byte[].class), anyInt(), anyInt())).thenReturn(4097);
        when(socket.getInputStream()).thenReturn(inputStream);
        
        ReceiveRunnable rr = new ReceiveRunnable("id", socket, streamIo, parent);
        rr.run();
        
        Field f = ReceiveRunnable.class.getDeclaredField("buf");
        f.setAccessible(true);
        byte[] buf = (byte[])f.get(rr);
        assertEquals(4197, buf.length);
    }

}
