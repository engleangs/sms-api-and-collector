package com.smppgw.core.service;

import com.fasterxml.jackson.core.JsonProcessingException;

public interface SmsService {
    void start();
    void onApiCall(String apiBody) throws JsonProcessingException;
    void submitSmsReceivedData(String json);
    void stop();


}
