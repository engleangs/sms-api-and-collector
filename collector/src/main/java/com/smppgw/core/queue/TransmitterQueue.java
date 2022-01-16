package com.smppgw.core.queue;

import com.smppgw.core.data.AsynchronousData;
import ie.omk.smpp.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransmitterQueue extends BaseAsyncQueue {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransmitterQueue.class);

    public TransmitterQueue(QueueEvent queueEvent, String host, int port, String smscId, String accountName, String password) {
        super(queueEvent, host, port, smscId, accountName, password);
    }

    @Override
    public int bindType() {
        return Connection.TRANSMITTER;
    }

    @Override
    public void doAction(AsynchronousData item) {
      sendSmsFromQueue(item);
    }
}
