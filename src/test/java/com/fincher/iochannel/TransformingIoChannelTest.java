package com.fincher.iochannel;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class TransformingIoChannelTest {
    
    public IoChannelIfc<MessageBuffer> getDelegate() {
        @SuppressWarnings("unchecked")
        IoChannelIfc<MessageBuffer> delegate = Mockito.mock(IoChannelIfc.class);
        return delegate;
    }
    
    
    public TestImplIfc getTestImpl(String id, IoChannelIfc<MessageBuffer> delegate) {
        return new TestImpl(id, delegate);
    }

    @Test
    public void test() throws Exception {
        IoChannelIfc<MessageBuffer> delegate = getDelegate();
        TestImplIfc channel = getTestImpl("id", delegate);

        AtomicReference<MessageBuffer> sentMb = new AtomicReference<>();

        Mockito.doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) {
                sentMb.set(invocation.getArgument(0));
                return null;
            }
        }).when(delegate).send(Mockito.any(MessageBuffer.class));

        AtomicReference<String> ref1 = new AtomicReference<>();
        AtomicReference<String> ref2 = new AtomicReference<>();
        Consumer<String> listener1 = (str) -> ref1.set(str);
        Consumer<String> listener2 = (str) -> ref2.set(str);

        channel.addTransformedMessageListener(listener1);
        channel.addTransformedMessageListener(listener2);

        channel.handleMessage(new MessageBuffer("testString1".getBytes()));
        assertEquals("testString1", ref1.get());
        assertEquals("testString1", ref2.get());

        channel.removeTransformedMessageListener(listener2);
        channel.handleMessage(new MessageBuffer("testString2".getBytes()));
        assertEquals("testString2", ref1.get());
        assertEquals("testString1", ref2.get());

        channel.send("testString3");

        assertArrayEquals("testString3".getBytes(), sentMb.get().getBytes());

        channel.setBlowUpOnDecode(true);
        channel.handleMessage(new MessageBuffer("testString4".getBytes()));
        assertEquals("testString2", ref1.get());

        channel.close();
    }
    
    
    public static interface TestImplIfc extends TransformingIoChannelIfc<MessageBuffer, String>{
        void setBlowUpOnDecode(boolean val);
        
        void handleMessage(MessageBuffer mb);
    }

    public static class TestImpl extends TransformingIoChannel<MessageBuffer, String> implements TestImplIfc {

        private boolean blowUpOnDecode = false;

        public TestImpl(String id, IoChannelIfc<MessageBuffer> delegate) {
            super(id, delegate);
        }


        @Override
        protected String decode(MessageBuffer mb) throws ChannelException {
            if (blowUpOnDecode) {
                throw new ChannelException("testExcetion");
            }
            return new String(mb.getBytes());
        }


        @Override
        protected MessageBuffer encode(String str) {
            return new MessageBuffer(str.getBytes());
        }


        @Override
        public void setBlowUpOnDecode(boolean val) {
            blowUpOnDecode = val;
        }
        
        
        @Override
        public void handleMessage(MessageBuffer mb) {
            super.handleMessage(mb);
        }

    }

}
