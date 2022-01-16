package com.smppgw.core.data;

import java.util.Date;

public class AsynchronousData {
    private String src;
    private String dest;
    private String msgId;
    private Date requestDate;
    private String content;
    private boolean longMsg;
    private int lang;
    public AsynchronousData(){

    }

    public AsynchronousData(String src, String dest, String msgId, Date requestDate, String content, boolean longMsg, int lang) {

        this.src = src;
        this.dest = dest;
        this.msgId = msgId;
        this.requestDate = requestDate;
        this.content = content;
        this.longMsg = longMsg;
        this.lang = lang;
    }


    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    public String getDest() {
        return dest;
    }

    public void setDest(String dest) {
        this.dest = dest;
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public Date getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(Date requestDate) {
        this.requestDate = requestDate;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isLongMsg() {
        return longMsg;
    }

    public void setLongMsg(boolean longMsg) {
        this.longMsg = longMsg;
    }

    public int getLang() {
        return lang;
    }

    public void setLang(int lang) {
        this.lang = lang;
    }

    @Override
    public String toString() {
        return msgId + " : " + content + "  from :  " + src + " to : " + dest + " at " + requestDate;
    }
}
