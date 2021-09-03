package com.apelsinovaya.graphics;

import java.util.Date;

public class Srk {
    private String uid;
    private int category, value;
    private Date dateTime;

    public Srk() {
    }

    public Srk(String uid, int category, int value, Date dateTime) {
        this.uid = uid;
        this.category = category;
        this.value = value;
        this.dateTime = dateTime;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Date getDateTime() {
        return dateTime;
    }

    public void setDateTime(Date dateTime) {
        this.dateTime = dateTime;
    }

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "Srk{" +
                "uid='" + uid + '\'' +
                ", dateTime=" + dateTime +
                ", category=" + category +
                ", value=" + value +
                '}';
    }
}
