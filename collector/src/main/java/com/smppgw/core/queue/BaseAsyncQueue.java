package com.smppgw.core.queue;

import com.smppgw.core.data.AsynchronousData;
import com.smppgw.core.sms.helper.ByteBuffer;
import com.smppgw.core.sms.helper.ByteBufferBuilder;
import com.smppgw.core.sms.helper.DataByte;
import com.smppgw.core.sms.helper.DataByteBuilder;
import ie.omk.smpp.Address;
import ie.omk.smpp.Connection;
import ie.omk.smpp.event.ConnectionObserver;
import ie.omk.smpp.event.ReceiverExitEvent;
import ie.omk.smpp.event.SMPPEvent;
import ie.omk.smpp.message.*;
import ie.omk.smpp.util.EncodingFactory;
import ie.omk.smpp.version.SMPPVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

public abstract class BaseAsyncQueue implements SmppQueue, ConnectionObserver {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseAsyncQueue.class);
    private QueueEvent queueEvent;
    protected final ConcurrentHashMap<String, AsynchronousData> queueItems = new ConcurrentHashMap<>();
    protected final ConcurrentHashMap<String, AsynchronousData> sendingItems = new ConcurrentHashMap<>();
    protected String host;
    protected int port;
    protected boolean isSmpp34;
    private boolean bound;
    protected SubmitSM submitSM;
    protected SubmitMulti submitMulti;
    protected String smscId;
    protected Connection connection;
    private Thread thread;
    private boolean running = false;
    private String accountName;
    private String password;
    private String type;


    public BaseAsyncQueue(QueueEvent queueEvent, String host, int port, String smscId, String accountName, String password) {
        this.queueEvent = queueEvent;
        this.host = host;
        this.port = port;
        this.smscId = smscId;
        this.isSmpp34 = true;
        this.accountName = accountName;
        this.password = password;
        this.type = smscId;
        bound = true;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setSmpp34(boolean smpp34) {
        isSmpp34 = smpp34;
    }

    public Connection getConnection() {
        return connection;
    }

    @Override
    public void packetReceived(Connection connection, SMPPPacket smppPacket) {
        try {
            switch (smppPacket.getCommandId()) {
                case SMPPPacket.BIND_TRANSCEIVER_RESP:
                case SMPPPacket.BIND_RECEIVER_RESP:
                case SMPPPacket.BIND_TRANSMITTER_RESP:
                    if (smppPacket.getCommandStatus() != 0) {
                        LOGGER.error("Error binding to SMSC " + smppPacket.getCommandStatus());
                        bound = false;
                    } else {
                        bound = true;
                        connection.newInstance(SMPPPacket.SUBMIT_SM);
                        submitSM = (SubmitSM) connection.newInstance(SMPPPacket.SUBMIT_SM);
                        submitMulti = (SubmitMulti) connection.newInstance(SMPPPacket.SUBMIT_MULTI);
                    }
                    break;
                case SMPPPacket.DELIVER_SM:
                    if (smppPacket.getCommandStatus() == 0) {
                        DeliverSM deliverSM = (DeliverSM) smppPacket;
                        String src = deliverSM.getSource().getAddress();
                        String dest = deliverSM.getDestination().getAddress();
                        String msgId = deliverSM.getMessageId();
                        String content = deliverSM.getMessageText();
                        int npi = deliverSM.getDestination().getNPI();
                        int ton = deliverSM.getDestination().getTON();
                        int srcNpi = deliverSM.getSource().getNPI();
                        int srcTon = deliverSM.getSource().getTON();
                        int dataCoding = deliverSM.getDataCoding();
                        int emsClass = deliverSM.getEsmClass();
                        String messageContent = "";
                        try {
                            if (deliverSM.getMessageText() != null && deliverSM.getMessageText().length() > 0) {
                                messageContent = EncodingFactory.getInstance().getDefaultAlphabet().decodeString(deliverSM.getMessage());
                            }
                            LOGGER.info("message : " + messageContent);

                        } catch (Exception e) {
                            LOGGER.error("parse message error", e);
                        }

                        queueEvent.onResponse(new AsynchronousData(src, dest, msgId, new Date(), content, false, 0), smppPacket.getCommandStatus());
                    } else {
                        LOGGER.info("receiving data error ", smppPacket.getCommandStatus() + " " + smppPacket.getErrorCode());
                    }
                    break;

                case SMPPPacket.SUBMIT_SM_RESP:
                    SubmitSMResp resp = (SubmitSMResp) smppPacket;
                    String msgId = resp.getMessageId();
                    AsynchronousData asynchronousData = sendingItems.remove(msgId);
                    queueEvent.onResponse(asynchronousData, smppPacket.getCommandStatus());//call back
                    break;
            }
        } catch (Exception e) {
            LOGGER.error("error receive package", e);
        }
    }

    @Override
    public void update(Connection connection, SMPPEvent smppEvent) {
        if (smppEvent.getType() == SMPPEvent.RECEIVER_EXIT) {
            ReceiverExitEvent ev = (ReceiverExitEvent) smppEvent;
            if (ev.getReason() != ReceiverExitEvent.EXCEPTION) {
                if (ev.getReason() == ReceiverExitEvent.BIND_TIMEOUT) {
                    LOGGER.info("Bind timed out waiting for response.");
                }
            } else {
                Throwable t = ev.getException();
                LOGGER.error("Bind problem ", t);
            }
        }
    }

    @Override
    public void open() throws IOException {
        if (isUnbind()) {
            return;
        }
        connection = new Connection(host, port, true);
        connection.addObserver(this);
        connection.autoAckLink(true);
        if (isSmpp34) {
            connection.setVersion(SMPPVersion.V34);
        } else {
            connection.setVersion(SMPPVersion.V33);
        }
        connection.bind(this.bindType(), accountName, password, type);
        LOGGER.info("bind connection successfully  as " + bindTypeName() + " ID : " + smscId);

    }

    @Override
    public boolean isActiveLinked() {
        if (isUnbind()) {
            return true;
        }
        try {
            connection.enquireLink();
            return true;
        } catch (IOException e) {
            bound = false;
            LOGGER.error("Error enquire link", e);
        }
        return false;
    }

    @Override
    public void close() {
        try {
            if (connection.isBound()) {
                connection.unbind();
                bound = false;
            }
        } catch (Exception e) {
            LOGGER.error("Error unbinding", e);
        }
    }

    @Override
    public boolean isUnbind() {
        return !bound;
    }

    public SubmitSM getSubmitSM() {
        return submitSM;
    }

    public SubmitMulti getSubmitMulti() {
        return submitMulti;
    }

    @Override
    public void enqueue(AsynchronousData item) {
        queueItems.put(item.getMsgId(), item);
    }

    private void running() {
        while (running) {
            if (queueItems.size() > 0) {
                for (String msgId : queueItems.keySet()) {
                    AsynchronousData asynchronousData = queueItems.remove(msgId);
                    sendingItems.put(msgId, asynchronousData);
                    doAction(asynchronousData);//doing action according to subclass
                }
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {

            }
        }
    }

    @Override
    public void startQueue() {
        running = true;
        thread = new Thread(this::running);
        thread.start();
        try {
            open();
        } catch (Exception e) {
            LOGGER.error("open connection error", e);
        }
    }

    @Override
    public void stopQueue() {
        running = false;
        try {
            close();
            thread.join();
        } catch (InterruptedException e) {

        }
    }

    @Override
    public String bindTypeName() {
        switch (bindType()) {
            case Connection.TRANSMITTER:
                return "TRANSMITTER";
            case Connection.RECEIVER:
                return "RECEIVER";
            case Connection.TRANSCEIVER:
                return "TRANSCEIVER";
        }
        return "Unknown";
    }

    protected void sendSmsFromQueue(AsynchronousData item) {
        if (submitSM == null) {
            LOGGER.warn("not yet ready to send sms");
            sendingItems.remove(item.getMsgId());
            enqueue(item);

            return;
        }
        sendSms(item);
    }

    protected void sendSms(AsynchronousData item) {
        LOGGER.info("begin to send the sms " + item);
        final SubmitSM sm = getSubmitSM();
        sm.setSource(new Address(0, 0, item.getSrc()));//default ton =0, npi =0
        sm.setDestination(new Address(0, 0, item.getDest()));
        sm.setMessageText(item.getContent());
        try {
            ByteBufferBuilder builder = new ByteBufferBuilder().dec(item.getLang()).seqNumber(0).seqResp(0).totalSegment(1);
            DataByte dataByte = new DataByteBuilder(item.getLang()).content(item.getContent()).build();
            ByteBuffer buffer = builder.build();
            buffer.appendBytes(dataByte.getBytes());
            sm.setEsmClass((byte) 0x40); // User Data Header indicator set);
            sm.setMessage(buffer.getBuffer());
            sm.setProtocolID(dataByte.getProtocolId());
            sm.setDataCoding(dataByte.getDataCoding());
            buffer.appendBytes(dataByte.getBytes());
            sm.setEsmClass(0); // User Data Header indicator set);
            sm.setMessage(buffer.getBuffer());
            sm.setProtocolID(dataByte.getProtocolId());
            sm.setDataCoding(dataByte.getDataCoding());
            sm.setMessage(dataByte.getBytes(), dataByte.getEncoding());
            sm.setPriority(0);
            sm.setSequenceNum(0);
            connection.sendRequest(sm);
        } catch (Exception e) {
            LOGGER.error(" error sending sms",e);
        }
    }


}
