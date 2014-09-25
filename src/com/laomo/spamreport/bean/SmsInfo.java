
package com.laomo.spamreport.bean;

public class SmsInfo {

    public String id;
    public String threadId;
    public long person;
    public String address;
    public String body;
    public long date;

    @Override
    public String toString() {
        return "SmsInfo [id=" + id + ", threadId=" + threadId + ", person="
                + person + ", address=" + address + ", body=" + body
                + ", date=" + date + "]";
    }
}
