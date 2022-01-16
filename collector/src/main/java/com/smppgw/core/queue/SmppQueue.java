package com.smppgw.core.queue;

import com.smppgw.core.data.AsynchronousData;

import java.io.IOException;

public interface SmppQueue {
    void open() throws IOException;

    boolean isActiveLinked();

    void close();

    boolean isUnbind();

    int bindType();

    String bindTypeName();

    void enqueue(AsynchronousData item);

    void startQueue();

    void stopQueue();

    void doAction(AsynchronousData item);

}
