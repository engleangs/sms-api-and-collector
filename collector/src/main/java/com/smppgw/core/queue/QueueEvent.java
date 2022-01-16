package com.smppgw.core.queue;

import com.smppgw.core.data.AsynchronousData;

public interface QueueEvent {
    void onResponse(AsynchronousData data, int commandStatus);
}
