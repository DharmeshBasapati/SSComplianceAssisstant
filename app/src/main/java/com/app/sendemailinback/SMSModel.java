package com.app.sendemailinback;

public class SMSModel {

    String smsDate;

    String smsFromNumber;

    String smsBody;

    String smsType;

    boolean isEmailed;

    public SMSModel(String smsDate, String smsFromNumber, String smsBody, String smsType, boolean isEmailed) {
        this.smsDate = smsDate;
        this.smsFromNumber = smsFromNumber;
        this.smsBody = smsBody;
        this.smsType = smsType;
        this.isEmailed = isEmailed;
    }

    public String getSmsDate() {
        return smsDate;
    }

    public void setSmsDate(String smsDate) {
        this.smsDate = smsDate;
    }

    public String getSmsFromNumber() {
        return smsFromNumber;
    }

    public void setSmsFromNumber(String smsFromNumber) {
        this.smsFromNumber = smsFromNumber;
    }

    public String getSmsBody() {
        return smsBody;
    }

    public void setSmsBody(String smsBody) {
        this.smsBody = smsBody;
    }

    public String getSmsType() {
        return smsType;
    }

    public void setSmsType(String smsType) {
        this.smsType = smsType;
    }

    public boolean isEmailed() {
        return isEmailed;
    }

    public void setEmailed(boolean emailed) {
        isEmailed = emailed;
    }
}
