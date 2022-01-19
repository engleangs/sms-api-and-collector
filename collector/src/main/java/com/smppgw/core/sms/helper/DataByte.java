package com.smppgw.core.sms.helper;

import ie.omk.smpp.util.AlphabetEncoding;

public class DataByte {
    private int protocolId;
    private int dataCoding;
    private AlphabetEncoding encoding;

    public DataByte(int protocolId, int dataCoding, AlphabetEncoding encoding, byte[] bytes) {
        this.protocolId = protocolId;
        this.dataCoding = dataCoding;
        this.encoding = encoding;
        this.bytes = bytes;
    }

    private byte [] bytes;

    public int getProtocolId() {
        return protocolId;
    }

    public void setProtocolId(int protocolId) {
        this.protocolId = protocolId;
    }

    public int getDataCoding() {
        return dataCoding;
    }

    public void setDataCoding(int dataCoding) {
        this.dataCoding = dataCoding;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public AlphabetEncoding getEncoding() {
        return encoding;
    }
}
