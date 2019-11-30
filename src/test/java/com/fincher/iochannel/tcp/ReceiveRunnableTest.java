package com.fincher.iochannel.tcp;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fincher.iochannel.tcp.ReceiveRunnable;
import com.fincher.iochannel.tcp.SimpleStreamIo;
import com.fincher.iochannel.tcp.StreamIo;
import com.fincher.iochannel.tcp.TcpChannel;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.Socket;

import org.junit.Test;

public class ReceiveRunnableTest {
    
    @Test(expected = IOException.class)
    public void testConstructorThrowsException() throws IOException {
        Socket socket = mock(Socket.class);
        when(socket.getInputStream()).thenThrow(IOException.class);
        
        new ReceiveRunnable("id", socket, new SimpleStreamIo(), null);
        try {
            socket.getInputStream();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
