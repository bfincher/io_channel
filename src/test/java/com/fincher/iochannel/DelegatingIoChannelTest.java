package com.fincher.iochannel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class DelegatingIoChannelTest {

    @SuppressWarnings("unchecked")
    @Test
    public void test() throws Exception {
        IoChannelIfc<MessageBuffer> delegate = Mockito.mock(IoChannelIfc.class);
        Mockito.when(delegate.removeMessageListener(Mockito.any())).thenReturn(true);
        Mockito.when(delegate.isConnected()).thenReturn(true);
        Mockito.when(delegate.getState()).thenReturn(ChannelState.CONNECTED);
        Mockito.when(delegate.getDataType()).thenReturn(IoChannelDataType.RAW_DATA);
        Mockito.when(delegate.isInput()).thenReturn(true);
        Mockito.when(delegate.isOutput()).thenReturn(true);
        Mockito.when(delegate.getIoType()).thenReturn(IoType.INPUT_AND_OUTPUT);

        DelegatingIoChannelIfc<MessageBuffer> channel = new TestImpl("testId", delegate);
        assertEquals("testId", channel.getId());

        channel.connect();
        Mockito.verify(delegate, Mockito.times(1)).connect();

        Consumer<MessageBuffer> listener = (mb) -> System.out.println("testListener");
        channel.addMessageListener(listener);
        Mockito.verify(delegate, Mockito.times(1)).addMessageListener(listener);

        assertTrue(channel.removeMessageListener(listener));
        Mockito.verify(delegate, Mockito.times(1)).removeMessageListener(listener);

        assertTrue(channel.isConnected());
        Mockito.verify(delegate, Mockito.times(1)).isConnected();

        byte[] bytes = { 1, 2, 3 };
        MessageBuffer mb = new MessageBuffer(bytes);
        channel.send(mb);
        Mockito.verify(delegate, Mockito.times(1)).send(mb);

        assertEquals(ChannelState.CONNECTED, channel.getState());
        assertEquals(IoChannelDataType.RAW_DATA, channel.getDataType());
        assertTrue(channel.isInput());
        assertTrue(channel.isOutput());

        assertEquals(IoType.INPUT_AND_OUTPUT, channel.getIoType());

        channel.close();
        Mockito.verify(delegate, Mockito.times(1)).close();
    }

    private static class TestImpl implements DelegatingIoChannelIfc<MessageBuffer> {

        private final IoChannelIfc<MessageBuffer> delegate;
        private final String id;

        TestImpl(String id, IoChannelIfc<MessageBuffer> delegate) {
            this.id = id;
            this.delegate = delegate;
        }

        @Override
        public IoChannelIfc<MessageBuffer> getDelegate() {
            return delegate;
        }

        @Override
        public String getId() {
            return id;
        }
    }

}
