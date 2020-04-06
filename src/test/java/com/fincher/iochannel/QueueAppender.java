package com.fincher.iochannel;

import java.util.concurrent.BlockingQueue;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;

public class QueueAppender extends AbstractAppender {
    
    private final BlockingQueue<String> queue;
    
    public QueueAppender(BlockingQueue<String> queue) {
        super("QueueAppender", null, null, true, Property.EMPTY_ARRAY);
        this.queue = queue;
    }
    
    @Override
    public void append(LogEvent event) {
        queue.add(event.getMessage().getFormattedMessage());
    }

}
