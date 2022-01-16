package com.smppgw.core.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smppgw.core.queue.ReceiverQueue;
import com.smppgw.core.queue.TransceiverQueue;
import com.smppgw.core.queue.TransmitterQueue;
import com.smppgw.core.data.AsynchronousData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

@Service
public class SmsServiceImpl implements SmsService {
    ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger LOGGER = LoggerFactory.getLogger(SmsServiceImpl.class);
    private TransmitterQueue transmitterQueue;
    private ReceiverQueue receiverQueue;
    private TransceiverQueue transceiverQueue;
    @Value("${sms.transmitter.active}")
    private boolean transmitterEnabled;
    @Value("${sms.transmitter.username}")
    private String transmitterUsername;
    @Value("${sms.transmitter.password}")
    private String transmitterPassword;
    @Value("${sms.receiver.username}")
    private String receiverUsername;
    @Value("${sms.receiver.password}")
    private String receiverPassword;
    @Value("${sms.receiver.active}")
    private boolean receiverEnabled;
    @Value("${sms.transceiver.active}")
    private boolean transceiverEnabled;
    @Value("${sms.transceiver.username}")
    private String transceiverUsername;
    @Value("${sms.transceiver.password}")
    private String transceiverPassword;
    @Value("${sms.host}")
    private String hostname;
    @Value("${sms.port}")
    private int port;
    @Value("${sms.smscId}")
    private String smscId;
    @Value("${sms.api_send_topic}")
    private String smsApiTriggerTopic;
    @Value("${sms.receiving_topic}")
    private String smsReceivingTopic;
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Override
    public void start() {
        LOGGER.info("begin to start sms queue");
        if (receiverEnabled) {
            this.receiverQueue = new ReceiverQueue((data, commandStatus) -> {
                if (commandStatus == 0) {
                    //success case
                    try {
                        submitSmsReceivedData(objectMapper.writeValueAsString(data));//forward receiving sms data
                    } catch (JsonProcessingException e) {
                        LOGGER.error("error processing json", e);
                    }
                }

            }, hostname, port, smscId, receiverUsername, receiverPassword);
            receiverQueue.startQueue();
            LOGGER.info("done starting receiver queue");
        }
        if (transmitterEnabled) {
            transmitterQueue = new TransmitterQueue((data, commandStatus) -> {

            }, hostname, port, smscId, transceiverUsername, transceiverPassword);
            transmitterQueue.startQueue();
            LOGGER.info("done starting transmitter queue");
        }
        if (transceiverEnabled) {
            transceiverQueue = new TransceiverQueue((data, commandStatus) -> {

            }, hostname, port, smscId, transceiverUsername, transceiverPassword);
            transceiverQueue.startQueue();
            LOGGER.info("done starting transceiver queue");
        }
    }

    @Override
    @KafkaListener(topics = "sms_api_send", groupId = "smpp_collector")
    public void onApiCall(String apiBody) throws JsonProcessingException {
        try {
            AsynchronousData data = objectMapper.readValue(apiBody, AsynchronousData.class);
            //preparing data
            if (data.getMsgId() == null) {
                data.setMsgId(UUID.randomUUID().toString());
            }
            if(data.getRequestDate() == null){
                data.setRequestDate(new Date());
            }
            LOGGER.info("receiving data  from kafka " + apiBody);
            if (transceiverQueue != null) {
                transceiverQueue.enqueue(data);
            } else if (transmitterQueue != null) {
                transmitterQueue.enqueue(data);
            } else {
                LOGGER.warn("no interface available either transceiver or transmitter");
            }
        } catch (Exception e) {
            LOGGER.error("error receiving data", e);
        }
    }

    @Override
    public void submitSmsReceivedData(String json) {
        kafkaTemplate.send(smsReceivingTopic, UUID.randomUUID().toString(), json);

    }

    @Override
    public void stop() {
        if (receiverQueue != null) {
            receiverQueue.stopQueue();
        }
        if (transceiverQueue != null) {
            transceiverQueue.startQueue();
        }
        if (transmitterQueue != null) {
            transmitterQueue.stopQueue();
        }
        LOGGER.info("done stop sms queue");
    }
}
