package com.fincher.iochannel.udp;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.net.MulticastSocket;
import java.net.StandardSocketOptions;

import org.junit.jupiter.api.Test;

public class UdpMulticastSocketOptionsTest {

    @Test
    public void test() throws IOException {
        MulticastSocket socket = mock(MulticastSocket.class);
        UdpMulticastSocketOptions options = new UdpMulticastSocketOptions();
        options.applySocketOptions("", socket);
        verifyOptions(socket, options);
        
        
        socket = mock(MulticastSocket.class);
        options.clearReceiveBufferSize();
        options.clearSendBufferSize();
        options.clearTimeout();
        options.applySocketOptions("", socket);
        verifyOptions(socket, options);
    }
    
    
    private void verifyOptions(MulticastSocket socket, UdpMulticastSocketOptions options) throws IOException {
        verify(socket).setTimeToLive(eq(options.getTimeToLive()));
        verify(socket).setOption(StandardSocketOptions.IP_MULTICAST_LOOP, options.isLoopbackDisabled());
        verify(socket).setReuseAddress(options.isReuseAddress());
        
        if (options.getSendBufferSize().isPresent()) {
            verify(socket).setSendBufferSize(eq(options.getSendBufferSize().getAsInt()));    
        } else {
            verify(socket, never()).setSendBufferSize(anyInt());    
        }
        
        if (options.getReceiveBufferSize().isPresent()) {
            verify(socket).setReceiveBufferSize(eq(options.getReceiveBufferSize().getAsInt()));
        } else {
            verify(socket, never()).setReceiveBufferSize(anyInt());
        }
        
        if (options.getTimeout().isPresent()) {
            verify(socket).setSoTimeout(eq(options.getTimeout().getAsInt()));
        } else {
            verify(socket, never()).setSoTimeout(anyInt());
        }
        
    }

}
