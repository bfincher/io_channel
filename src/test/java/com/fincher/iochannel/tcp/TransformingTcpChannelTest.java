package com.fincher.iochannel.tcp;

import com.fincher.iochannel.ChannelException;
import com.fincher.iochannel.IoChannelIfc;
import com.fincher.iochannel.MessageBuffer;
import com.fincher.iochannel.TransformingIoChannelTest;

import org.mockito.Mockito;

public class TransformingTcpChannelTest extends TransformingIoChannelTest {
    
    public IoChannelIfc<MessageBuffer> getDelegate() {
        TcpChannelIfc delegate = Mockito.mock(TcpChannelIfc.class);
        return delegate;
    }
    
    
    public TestImplIfc getTestImpl(String id, IoChannelIfc<MessageBuffer> delegate) {
        return new TestTcpImpl(id, (TcpChannelIfc)delegate);
    }
    
    
    private static class TestTcpImpl extends TransformingTcpChannel<String> implements DelegatingTcpChannel, TestImplIfc {
        
        private final TcpChannelIfc delegate;
        private boolean blowUpOnDecode;
        
        TestTcpImpl(String id, TcpChannelIfc delegate) {
            super(id, delegate);
            this.delegate = delegate;
        }
        
        
        @Override
        public TcpChannelIfc getDelegate() {
            return delegate;
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
