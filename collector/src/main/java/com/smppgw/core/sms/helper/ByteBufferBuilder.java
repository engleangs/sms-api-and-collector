package com.smppgw.core.sms.helper;

public class ByteBufferBuilder {
    private ByteBuffer ed;
    private int dec;
    private int seqResp;
    private int totalSegments;
    private int seqNumber;

    public ByteBufferBuilder(){
        ed = new ByteBuffer();
    }
    public ByteBufferBuilder seqNumber(int seqNumber){
        this.seqNumber = seqNumber;
        return this;
    }
    public ByteBufferBuilder totalSegment(int totalSegments){
        this.totalSegments = totalSegments;
        return this;
    }
    public ByteBufferBuilder dec(int dec){
        this.dec = dec;
        return this;
    }
    public ByteBufferBuilder seqResp(int seqResp){
        this.seqResp = seqResp;
        return this;
    }
    public ByteBuffer build(){
        if( dec!=8){
            //Consider Ascii
            ed.appendByte((byte) 5); // UDH Length
            ed.appendByte((byte) 0x00); // IE Identifier
            ed.appendByte((byte) 3); // IE Data Length
            ed.appendByte((byte) seqResp); // Reference Number
            ed.appendByte((byte) totalSegments); // Number of pieces
            ed.appendByte((byte) (seqNumber + 1)); // Sequence number

        }else{

            //Only 8 is consider Unicode.
            ed.appendByte((byte) 6); // UDH Length
            ed.appendByte((byte) 0x08); // IE Identifier
            ed.appendByte((byte) 4); // IE Data Length
            ed.appendByte((byte) seqResp) ; //Reference Number 1st Octet
            ed.appendByte((byte) seqResp) ; //Reference Number 2nd Octet
            ed.appendByte((byte) totalSegments) ; //Number of pieces
            ed.appendByte((byte) (seqNumber+1)) ; //Sequence number
        }
        return ed;
    }

}
