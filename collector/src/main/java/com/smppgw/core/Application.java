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

    public static void main(String[] args)  {
        SpringApplication.run(Application.class, args);
    }
}
