package com.apelsinovaya.graphics;

import java.util.Date;

public class Temperature {
    private int value;
    private Date dateTime;


    public Temperature() {
    }

    public Temperature(int value, Date dateTime) {
        this.value = value;
        this.dateTime = dateTime;
    }

    public Date getDateTime() {
        return dateTime;
    }

    public void setDateTime(Date dateTime) {
        this.dateTime = dateTime;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "Temperature{" +
                "value=" + value +
                ", dateTime=" + dateTime +
                '}';
    }
}
