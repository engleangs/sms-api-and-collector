package com.smppgw.core;

import com.smppgw.core.service.SmsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application implements CommandLineRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);
    @Autowired
    private SmsService smsService;

    @Override
    public void run(String... args) throws Exception {
        LOGGER.info("running");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            smsService.stop();
        }));
        smsService.start();
        Thread.currentThread().join();

    }

    public static void main(String[] args) throws InterruptedException {
        SpringApplication.run(Application.class, args);
//        QueueEvent queueEvent = (data, type) -> {
//            LOGGER.info("data " + data + " type : " + type);
//        };
//        TransmitterQueue transmitterQueue = new TransmitterQueue(queueEvent, "127.0.0.1", 2775, "SMPPSim", "SMPPSim", "password");
//        transmitterQueue.startQueue();
//        transmitterQueue.enqueue(new AsynchronousData( "964772119995", "110", UUID.randomUUID().toString(), new Date(), "سساينينين", false, 8));
//        Thread.sleep(20000);
//        transmitterQueue.stopQueue();
////        ReceiverQueue reciverQueue = new ReceiverQueue(queueEvent, "127.0.0.1", 2775, "SMPPSim", "SMPPSim", "password");
////        reciverQueue.startQueue();
////        Thread.sleep(200000);
////        reciverQueue.stopQueue();
//        TransceiverQueue transceiverQueue = new TransceiverQueue(queueEvent, "127.0.0.1", 2775, "SMPPSim", "SMPPSim", "password");
//        transceiverQueue.startQueue();
//        transceiverQueue.enqueue(new AsynchronousData( "964772119995", "110", UUID.randomUUID().toString(), new Date(), "Hello", false, 8));
//        Thread.sleep(10000);
//        transceiverQueue.stopQueue();
//        LOGGER.info("done...");
    }
}
