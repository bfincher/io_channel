package com.fincher.iochannel.tcp;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class DelegatingTcpChannelTest {

    @Test
    public void test() throws Exception {
        TcpChannelIfc delegate = Mockito.mock(TcpChannelIfc.class);
        Mockito.when(delegate.getNumConnections()).thenReturn(5);
        Mockito.when(delegate.getSocketIds()).thenReturn(Arrays.asList("id1", "id2"));
        DelegatingTcpChannel channel = new TestImpl("id", delegate);

        ConnectionEstablishedListener cel = new ConnectionEstablishedListener() {

            @Override
            public void connectionEstablished(String channelID) {
            }
        };

        channel.addConnectionEstablishedListener(cel);
        Mockito.verify(delegate, Mockito.times(1)).addConnectionEstablishedListener(cel);

        channel.removeConnectionEstablishedListener(cel);
        Mockito.verify(delegate, Mockito.times(1)).removeConnectionEstablishedListener(cel);

        ConnectionLostListener cll = new ConnectionLostListener() {

            @Override
            public void connectionLost(String channelID) {
            }
        };

        channel.addConnectionLostListener(cll);
        Mockito.verify(delegate, Mockito.times(1)).addConnectionLostListener(cll);

        channel.removeConnectionLostListener(cll);
        Mockito.verify(delegate, Mockito.times(1)).removeConnectionLostListener(cll);

        assertEquals(5, channel.getNumConnections());
        assertEquals(Arrays.asList("id1", "id2"), channel.getSocketIds());

        byte[] bytes = { 1, 2, 3 };
        channel.send(bytes);
        Mockito.verify(delegate, Mockito.times(1)).send(bytes);
        
        channel.close();

    }

    private static class TestImpl implements DelegatingTcpChannel {

        private final TcpChannelIfc delegate;
        private final String id;

        TestImpl(String id, TcpChannelIfc delegate) {
            this.id = id;
            this.delegate = delegate;
        }


        @Override
        public TcpChannelIfc getDelegate() {
            return delegate;
        }


        @Override
        public String getId() {
            return id;
        }
    }

}
