package com.supinfo.supsms;

/**
 * Created by loicbillaud on 06/02/15.
 */
public class Sms {
    private String number;
    private String content;

    public Sms() {

    }

    public Sms(String content, String number) {
        this.content = content;
        this.number = number;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

}
