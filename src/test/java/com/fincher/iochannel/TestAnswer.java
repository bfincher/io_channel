package com.fincher.iochannel;

import java.util.Optional;
import java.util.function.Consumer;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class TestAnswer implements Answer<Void>{
    
    private final Optional<Consumer<Object[]>> consumer;
    private final Optional<Exception> exception;
    
    public TestAnswer() {
        consumer = Optional.empty();
        exception = Optional.empty();
    }
    
    public TestAnswer(Consumer<Object[]> consumer) {
        this.consumer = Optional.of(consumer);
        exception = Optional.empty();
    }
    
    public TestAnswer(Exception exception) {
        this.exception = Optional.of(exception);
        consumer = Optional.empty();
    }
    
    @Override
    public Void answer(InvocationOnMock invocation) throws Exception {
        if (exception.isPresent()) {
            throw exception.get();
        }
        
        if (consumer.isPresent()) {
            consumer.get().accept(invocation.getArguments());
        }
        
        return null;
    }

}
