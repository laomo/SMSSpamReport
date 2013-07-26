package com.laomo.spamreport.bean;

public class SmsInfo {

    public String id;
    public long threadId;
    public String presonName;
    public long person;
    public String address;
    public String body;
    public String subject;
    public long date;
    //是否阅读 0未读， 1已读
    public int read;
    public int type;
    
    @Override
    public String toString() {
	return "SmsInfo [id=" + id + ", threadId=" + threadId + ", presonName=" + presonName + ", person=" + person
	    + ", address=" + address + ", body=" + body + ", subject=" + subject + ", date=" + date + ", read=" + read
	    + ", type=" + type + "]";
    }
}