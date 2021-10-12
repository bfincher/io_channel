package com.fincher.iochannel.tcp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.fincher.iochannel.ChannelException;
import com.fincher.iochannel.ChannelState;

public class TcpServerConnectRunnableTest {

    @Mock
    private TcpServerChannel server;

    @Mock
    private ServerSocket ss;

    @Mock
    private TcpSocketOptions socketOptions;

    @BeforeEach
    public void before() {
        MockitoAnnotations.initMocks(this);
        socketOptions = mock(TcpSocketOptions.class);
        when(server.getId()).thenReturn("id");
        when(server.getSocketOptions()).thenReturn(socketOptions);
        TcpServerConnectRunnable.serverSocketFactory = TcpServerConnectRunnable.DEFAULT_SERVER_SOCKET_FACTORY;
    }

    @Test
    public void testConstruct() throws ChannelException {
        new TcpServerConnectRunnable(server, ss);
        verify(socketOptions).applySocketOptions(eq("id"), eq(ss));

        // test with exception
        Mockito.doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock inv) throws ChannelException {
                throw new ChannelException("");
            }
        }).when(socketOptions).applySocketOptions(any(String.class), any(ServerSocket.class));

        assertThrows(ChannelException.class, () -> new TcpServerConnectRunnable(server, ss));
    }

    @Test
    public void testCreate() throws ChannelException {
        TcpServerConnectRunnable.create(server);
        verify(socketOptions).applySocketOptions(eq("id"), any(ServerSocket.class));
    }

    @Test
    public void testCreateWithChannelException() throws Exception {

        // test with Channel Exception
        Mockito.doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock inv) throws ChannelException {
                throw new ChannelException("");
            }
        }).when(socketOptions).applySocketOptions(any(String.class), any(ServerSocket.class));

        assertThrows(ChannelException.class, () -> TcpServerConnectRunnable.create(server));
    }

    @Test
    public void testCreateWithIoException() throws Exception {
        TcpServerConnectRunnable.serverSocketFactory = () -> { throw new IOException();};

        assertThrows(IOException.class, () -> TcpServerConnectRunnable.create(server));
    }

    @Test
    public void testConnectSocketWithException() throws IOException {
        Mockito.doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock inv) throws IOException {
                throw new BindException();
            }
        }).when(ss).bind(any());

        assertThrows(BindException.class, () -> new TcpServerConnectRunnable(server, ss).connectSocket());
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
    
    @Test
    public void testCheckedSupplier() {
        TcpServerConnectRunnable.CheckedSupplier supplier1 = () -> null;
        assertNull(supplier1.get());
                
        TcpServerConnectRunnable.CheckedSupplier supplier2 = () -> {throw new IOException();};
        assertThrows(RuntimeException.class, () -> supplier2.get());
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
