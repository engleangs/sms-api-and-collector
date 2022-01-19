package com.smppgw.core.sms.helper;

import ie.omk.smpp.util.AlphabetEncoding;
import ie.omk.smpp.util.DefaultAlphabetEncoding;
import ie.omk.smpp.util.UCS2Encoding;

import java.io.UnsupportedEncodingException;

public class DataByteBuilder {
    private int lang = 0;
    private String content;

    public DataByteBuilder(int lang) {
        this.lang = lang;
    }

    public DataByteBuilder content(String content) {
        this.content = content;
        return this;
    }

    public DataByte build() throws UnsupportedEncodingException {
        int protocolID = 0;
        int dataCoding = 0;
        byte[] bytes = new byte[0];
        AlphabetEncoding de;
        if (lang == 0 || lang == 1) {
            de = new DefaultAlphabetEncoding();
            bytes = de.encodeString(content);
            // Using default SMSC Alphabet. As some special character
            // mapping in current SMSC with DSC=1 is not work properly.
            protocolID = 0;
            dataCoding = 0;

        } else if (lang == 16 || lang == 192) {
            //May not use as it will use only for short message.
            de = new DefaultAlphabetEncoding();
            bytes = de.encodeString(content);
            // Silent or Ghost SMS
            protocolID = 64;
            dataCoding = lang;

        } else {
            de = new UCS2Encoding();
            bytes = de.encodeString(content);
            protocolID = 0;
            dataCoding = lang;

        }
        return new DataByte(protocolID, dataCoding, de, bytes);
    }
}
