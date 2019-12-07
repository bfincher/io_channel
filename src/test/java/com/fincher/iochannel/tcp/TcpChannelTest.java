package com.fincher.iochannel.tcp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import com.fincher.iochannel.ChannelException;
import com.fincher.iochannel.ChannelState;
import com.fincher.iochannel.IoType;
import com.fincher.iochannel.MessageBuffer;
import com.fincher.thread.MyCallableIfc;

import java.io.IOException;
import java.net.Socket;

import org.junit.Test;
import org.mockito.Mockito;

public class TcpChannelTest {
    
    @Test
    public void testSetReceiveRunnable() throws Exception {
        TestImpl impl = new TestImpl();
        impl.setReceiveRunnableFactory(null);
        
        impl.setState(ChannelState.CONNECTED);
        try {
            impl.setReceiveRunnableFactory(null);
            fail("Should have got exception");
        } catch (IllegalStateException e) {
            // expected
        }
        
        impl.close();
    }
    
    @Test
    public void testSetSocketOptions() throws Exception {
        TestImpl impl = new TestImpl();
        TcpSocketOptions socketOptions = Mockito.mock(TcpSocketOptions.class);
        impl.setSocketOptions(socketOptions);
        assertEquals(socketOptions, impl.getSocketOptions());
        
        impl.setState(ChannelState.CONNECTED);
        try {
            impl.setSocketOptions(socketOptions);
            fail("Should have got exception");
        } catch (IllegalStateException e) {
            // expected
        }
        
        impl.close();
    }
    
    @Test
    public void testSendThrowsException() throws IOException {
        TestImpl impl = new TestImpl();
        Socket socket = Mockito.mock(Socket.class);
        Mockito.when(socket.getOutputStream()).thenThrow(IOException.class);
        impl.addSocket("testId", socket);
        
        try {
            impl.send(new MessageBuffer(new byte[0]));
            fail("Should have got exception");
        } catch (ChannelException e) {
            // expected
        }
        
        try {
            impl.send(new MessageBuffer(new byte[0]), "testId");
            fail("Should have got exception");
        } catch (ChannelException e) {
            // expected
        }
        
        impl.close();
    }
    
    class TestImpl extends TcpChannel {
        
        public TestImpl() {
            super("id", IoType.INPUT_AND_OUTPUT, null, new SimpleStreamIo());
        }
        
        @Override
        public void setState(ChannelState state) {
            super.setState(state);
        }
        
        @Override
        protected MyCallableIfc<Socket> getConnectRunnable() {
            return null;
        }
        
        public void addSocket(String id, Socket socket) {
            sockets.put(id, socket);
        }
        
    }

}
