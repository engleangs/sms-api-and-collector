package com.smppgw.core.queue;

import com.smppgw.core.data.AsynchronousData;
import ie.omk.smpp.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransceiverQueue extends BaseAsyncQueue {
    private static final Logger LOGGER = LoggerFactory.getLogger( TransceiverQueue.class);
    public TransceiverQueue(QueueEvent queueEvent, String host, int port, String smscId, String accountName, String password) {
        super(queueEvent, host, port, smscId, accountName, password);
    }

    @Override
    public int bindType() {
         return Connection.TRANSCEIVER;
    }

    @Override
    public void doAction(AsynchronousData item) {
       sendSmsFromQueue(item);
    }
}
