package com.fincher.iochannel.tcp;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import org.junit.Test;
import org.mockito.Mockito;

import com.fincher.iochannel.ChannelException;
import com.fincher.iochannel.SocketOptionsTest;

public class TcpSocketOptionsTest extends SocketOptionsTest {

    @Override
    protected TcpSocketOptions buildSocketOptions() {
        return new TcpSocketOptions();
    }


    @Override
    protected void init() {
        super.init();
        TcpSocketOptions tcpSo = (TcpSocketOptions) so;
        tcpSo.setKeepAlive(true);
        tcpSo.setTcpNoDelay(true);
    }


    @Test
    public void testConstruct() {
        TcpSocketOptions tcpSo = (TcpSocketOptions) so;
        assertTrue(tcpSo.getKeepAlive());
        assertTrue(tcpSo.getTcpNoDelay());

        tcpSo.setKeepAlive(false);
        tcpSo.setTcpNoDelay(false);

        assertFalse(tcpSo.getKeepAlive());
        assertFalse(tcpSo.getTcpNoDelay());
    }


    @Test
    public void setApplySocketOptionsServer() throws Exception {
        TcpSocketOptions tcpSo = (TcpSocketOptions) so;
        tcpSo.clearTimeout();
        ServerSocket ss = Mockito.mock(ServerSocket.class);

        tcpSo.applySocketOptions("id", ss);
        Mockito.verify(ss, Mockito.never()).setReceiveBufferSize(Mockito.anyInt());
        Mockito.verify(ss, Mockito.times(1)).setReuseAddress(true);
        Mockito.verify(ss, Mockito.never()).setSoTimeout(Mockito.anyInt());

        // test with receive buf size set
        tcpSo.setReceiveBufferSize(1);
        tcpSo.applySocketOptions("id", ss);
        Mockito.verify(ss, Mockito.times(1)).setReceiveBufferSize(1);

        // test with exception
        Mockito.doAnswer(inv -> {
            throw new ChannelException("test");
        }).when(ss).setReceiveBufferSize(Mockito.anyInt());

        try {
            tcpSo.applySocketOptions("id", ss);
            fail("Expected exception");
        } catch (ChannelException e) {
            // expected
        }
    }


    @Test
    public void setApplySocketOptionsClient() throws Exception {
        TcpSocketOptions tcpSo = (TcpSocketOptions) so;
        tcpSo.clearReceiveBufferSize();
        tcpSo.clearSendBufferSize();
        tcpSo.clearTimeout();
        Socket s = Mockito.mock(Socket.class);
        tcpSo.applySocketOptions("id", s);

        Mockito.verify(s, Mockito.never()).setReceiveBufferSize(Mockito.anyInt());
        Mockito.verify(s, Mockito.never()).setSendBufferSize(Mockito.anyInt());
        Mockito.verify(s, Mockito.times(1)).setReuseAddress(true);
        Mockito.verify(s, Mockito.never()).setSoTimeout(Mockito.anyInt());
        Mockito.verify(s, Mockito.times(1)).setTcpNoDelay(true);
        Mockito.verify(s, Mockito.times(1)).setKeepAlive(true);

        init();
        tcpSo.applySocketOptions("id", s);
        Mockito.verify(s, Mockito.times(1)).setReceiveBufferSize(2);
        Mockito.verify(s, Mockito.times(1)).setSendBufferSize(1);
        Mockito.verify(s, Mockito.times(1)).setSoTimeout(3);

        // test with exception
        Mockito.doAnswer(inv -> {
            throw new SocketException("test");
        }).when(s).setReceiveBufferSize(Mockito.anyInt());

        try {
            tcpSo.applySocketOptions("id", s);
            fail("Expected exception");
        } catch (ChannelException e) {
            // expected
        }
    }

}
