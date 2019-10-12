package com.fincher.io_channel;

import com.fincher.thread.DataHandlerIfc;

import java.util.concurrent.BlockingQueue;

/**
 * A message handler that simply places messages on a queue
 * 
 * @author Brian Fincher
 *
 * @param <T>
 */
public class QueueMessageHandler<T> implements DataHandlerIfc<T> {

    private final BlockingQueue<T> queue;

    /**
     * Constructs a new QueueMessageHandler
     * 
     * @param queue Used to store received messages
     */
    public QueueMessageHandler(BlockingQueue<T> queue) {
        this.queue = queue;
    }

    @Override
    public void handleMessage(T message) {
        queue.add(message);
    }

    /**
     * Get the queue backing this message handler
     * 
     * @return the queue backing this message handler
     */
    public BlockingQueue<T> getQueue() {
        return queue;
    }

}
