package com.fincher.iochannel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public abstract class SocketOptionsTest {

    protected SocketOptions so;

    @BeforeEach
    public void before() {
        so = buildSocketOptions();
    }

    @Test
    public void test() {
        assertFalse(so.getSendBufferSize().isPresent());
        assertFalse(so.getReceiveBufferSize().isPresent());
        assertTrue(so.isReuseAddress());
        assertEquals(2000, so.getTimeout().getAsInt());

        init();

        assertEquals(1, so.getSendBufferSize().getAsInt());
        assertEquals(2, so.getReceiveBufferSize().getAsInt());
        assertEquals(3, so.getTimeout().getAsInt());
        assertTrue(so.isReuseAddress());

        so.clearSendBufferSize();
        so.clearReceiveBufferSize();
        so.clearTimeout();

        assertFalse(so.getSendBufferSize().isPresent());
        assertFalse(so.getReceiveBufferSize().isPresent());
        assertFalse(so.getTimeout().isPresent());
    }

    protected void init() {
        so.setSendBufferSize(1);
        so.setReceiveBufferSize(2);
        so.setReuseAddress(true);
        so.setTimeout(3);
    }

    protected abstract SocketOptions buildSocketOptions();

}
