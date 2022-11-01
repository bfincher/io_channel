package com.fincher.iochannel;

import java.util.concurrent.ConcurrentHashMap;

import org.mockito.Mockito;
import org.slf4j.Logger;

public class TestUtilities extends Utilities {

    private final ConcurrentHashMap<Class<?>, Logger> loggerMap = new ConcurrentHashMap<>();

    @Override
    public Logger getLogger(Class<?> cls) {
        return loggerMap.computeIfAbsent(cls, (clz) -> Mockito.mock(Logger.class));
    }

}
