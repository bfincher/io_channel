package com.fincher.iochannel.tcp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

import org.junit.Before;
import org.junit.Test;

import com.fincher.iochannel.ChannelException;
import com.fincher.iochannel.ChannelState;

public class TcpServerConnectRunnableTest {

    private TcpServerChannel server;
    private ServerSocket ss;
    private TcpSocketOptions socketOptions;

    @Before
    public void before() {
        server = mock(TcpServerChannel.class);
        ss = mock(ServerSocket.class);
        socketOptions = mock(TcpSocketOptions.class);
        when(server.getId()).thenReturn("id");
        when(server.getSocketOptions()).thenReturn(socketOptions);
    }


    @Test
    public void testTerminate() throws Exception {
        // test nominal
        TcpServerConnectRunnable tcr = new MyImpl(server, ss);
        tcr.terminate();
        verify(ss).close();
    }
    
    @Test
    public void testTerminateServerSocketNull() throws Exception {
        MyImpl tcr = new MyImpl(server, null);
        tcr.terminate();
    }
    
    @Test
    public void testCloseThrowsException() throws Exception {
        doThrow(new IOException()).when(ss).close();
        TcpServerConnectRunnable tcr = new MyImpl(server, ss);
        tcr.terminate();
    }

    @Test
    public void testCall() throws Exception {
        Socket socket = mock(Socket.class);
        when(ss.accept()).thenReturn(socket);
        MyImpl tcr = new MyImpl(server, ss);
        assertEquals(socket, tcr.call());

        // test socket timeout exception
        when(ss.accept()).thenThrow(SocketTimeoutException.class).thenReturn(null);
        assertNull(tcr.call());

        // test exception
        when(ss.accept()).thenThrow(IOException.class).thenReturn(null);
        try {
            tcr.call();
            fail("Should have got exception");
        } catch (IOException e) {
            // expected
        }

        // test exception when channel closed
        when(ss.accept()).thenThrow(IOException.class);
        when(server.getState()).thenReturn(ChannelState.CLOSED);
        assertNull(tcr.call());
    }
    
    @Test
    public void testConnectSocket() throws Exception {
        when(server.getlocalAddress()).thenReturn(InetSocketAddress.createUnresolved("localhost", 0));
        MyImpl tcr = new MyImpl(server, ss);
        tcr.setCallSuperConnectSocket(true);
        assertTrue(tcr.connectSocket());
    }

    private static class MyImpl extends TcpServerConnectRunnable {

        ServerSocket ss;
        boolean callSuperConnectSocket = false;

        public MyImpl(TcpServerChannel channel, ServerSocket ss) throws ChannelException {
            super(channel, ss);
            this.ss = ss;
        }

        @Override
        protected ServerSocket getServerSocket() {
            return ss;
        }
        
        public void setCallSuperConnectSocket(boolean val) {
            callSuperConnectSocket = val;
        }

        @Override
        protected boolean connectSocket() throws IOException, InterruptedException {
            if (callSuperConnectSocket) {
                return super.connectSocket();
            }
            return true;
        }
    }

}
