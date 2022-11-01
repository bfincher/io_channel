package com.fincher.iochannel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;

public class IOChannelTest {

    private IoChannel<MessageBuffer> channel;

    @BeforeEach
    public void before() {
        channel = new TestImpl("testId", IoType.INPUT_AND_OUTPUT);
    }

    @Test
    public void testConstruct() {
        assertEquals("testId", channel.getId());
        assertEquals(IoType.INPUT_AND_OUTPUT, channel.getIoType());
        assertEquals(ChannelState.INITIAL, channel.getState());
    }

    @Test
    public void testAddMessageListener() {
        try {
            channel.addMessageListener(null);
            // should have thrown null pointer
        } catch (NullPointerException e) {
            // expected
        }

        AtomicReference<MessageBuffer> ref1 = new AtomicReference<MessageBuffer>();
        Consumer<MessageBuffer> listener1 = mb -> ref1.set(mb);
        channel.addMessageListener(listener1);

        AtomicReference<MessageBuffer> ref2 = new AtomicReference<MessageBuffer>();
        Consumer<MessageBuffer> listener2 = mb -> ref2.set(mb);
        channel.addMessageListener(listener2);

        MessageBuffer mb1 = new MessageBuffer(new byte[1]);
        channel.messageReceived(mb1, Mockito.mock(Logger.class), "");
        assertEquals(mb1, ref1.get());
        assertEquals(mb1, ref2.get());

        // test removing a message listener
        channel.removeMessageListener(listener2);

        MessageBuffer mb2 = new MessageBuffer(new byte[2]);
        channel.messageReceived(mb2, Mockito.mock(Logger.class), "");
        assertEquals(mb2, ref1.get());
        assertEquals(mb1, ref2.get());

        channel = new TestImpl("id", IoType.OUTPUT_ONLY);
        try {
            channel.addMessageListener(o -> ref1.set(o));
            fail("expected exception");
        } catch (IllegalStateException e) {
            // expected
        }
    }

    @Test
    public void testAddMessageListenerWithPredicate() {
        try {
            channel.addMessageListener(null, o -> true);
            // should have thrown null pointer
        } catch (NullPointerException e) {
            // expected
        }

        try {
            channel.addMessageListener(mb -> System.out.println("hello"), null);
            // should have thrown null pointer
        } catch (NullPointerException e) {
            // expected
        }

        AtomicReference<MessageBuffer> ref1 = new AtomicReference<MessageBuffer>();
        AtomicReference<MessageBuffer> ref2 = new AtomicReference<MessageBuffer>();
        channel.addMessageListener(mb -> ref1.set(mb), mb -> mb.getBytes().length == 1);
        channel.addMessageListener(mb -> ref2.set(mb), mb -> mb.getBytes().length == 2);

        MessageBuffer mb1 = new MessageBuffer(new byte[1]);
        MessageBuffer mb2 = new MessageBuffer(new byte[2]);
        channel.messageReceived(mb1, Mockito.mock(Logger.class), "");
        assertEquals(mb1, ref1.get());
        assertNull(ref2.get());

        channel.messageReceived(mb2, Mockito.mock(Logger.class), "");
        assertEquals(mb1, ref1.get());
        assertEquals(mb2, ref2.get());

        channel = new TestImpl("id", IoType.OUTPUT_ONLY);
        try {
            channel.addMessageListener(o -> ref1.set(o));
            fail("expected exception");
        } catch (IllegalStateException e) {
            // expected
        }
    }

    @Test
    public void removeMessageListener() {
        // test removing when there are no listeners. normal removing has already been
        // tested
        channel = new TestImpl("id", IoType.INPUT_ONLY);
        Consumer<MessageBuffer> listener1 = mb -> System.out.println("Hello from listener1");
        Consumer<MessageBuffer> listener2 = mb -> System.out.println("Hello from listener2");
        channel.addMessageListener(listener1);
        assertFalse(channel.removeMessageListener(listener2));
    }

    @Test
    public void testIsInputAndIsOutput() {
        channel = new TestImpl("id", IoType.OUTPUT_ONLY);
        assertFalse(channel.isInput());
        assertTrue(channel.isOutput());

        channel = new TestImpl("id", IoType.INPUT_ONLY);
        assertTrue(channel.isInput());
        assertFalse(channel.isOutput());

        channel = new TestImpl("id", IoType.INPUT_AND_OUTPUT);
        assertTrue(channel.isInput());
        assertTrue(channel.isOutput());
    }

    private static class TestImpl extends IoChannel<MessageBuffer> {
        public TestImpl(String id, IoType ioType) {
            super(id, ioType);
        }

        @Override
        public void close() {
        }

        @Override
        public void connect() {

        }

        @Override
        public IoChannelDataType getDataType() {
            return IoChannelDataType.RAW_DATA;
        }

        @Override
        public boolean isConnected() {
            return true;
        }

        @Override
        public void send(MessageBuffer mb) {

        }
    }

}
