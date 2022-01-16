package com.smppgw.core.queue;

import com.smppgw.core.data.AsynchronousData;
import ie.omk.smpp.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReceiverQueue extends BaseAsyncQueue {
    private static final Logger LOGGER = LoggerFactory.getLogger( ReceiverQueue.class);
    public ReceiverQueue(QueueEvent queueEvent, String host, int port, String smscId, String accountName, String password) {
        super(queueEvent, host, port, smscId, accountName, password);
    }

    @Override
    public int bindType() {
         return Connection.RECEIVER;
    }

    @Override
    public void enqueue(AsynchronousData item) {
        throw new RuntimeException("Not supported for receiver");
    }

    @Override
    public void doAction(AsynchronousData item) {
        LOGGER.info("receiving data"+item);
    }
}
