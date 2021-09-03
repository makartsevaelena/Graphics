package com.apelsinovaya.graphics;

import java.util.Date;

public class Voltage {
    private int value;
    private Date dateTime;


    public Voltage() {
    }

    public Voltage(int value, Date dateTime) {
        this.value = value;
        this.dateTime = dateTime;
    }

    public Date getDateTime() {
        return dateTime;
    }

    public int getValue() {
        return value;
    }

    public void setDateTime(Date dateTime) {
        this.dateTime = dateTime;
    }

    public void setValue(int value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "Voltage{" +
                "value=" + value +
                ", dateTime=" + dateTime +
                '}';
    }
}
