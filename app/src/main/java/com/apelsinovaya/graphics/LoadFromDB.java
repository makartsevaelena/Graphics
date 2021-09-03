package com.apelsinovaya.graphics;

public class LoadFromDB {
    String dataTime;
    int category, value;

    public LoadFromDB(String dataTime, int category, int value) {
        this.dataTime = dataTime;
        this.category = category;
        this.value = value;
    }

    public LoadFromDB() {

    }

    public void setDataTime(String dataTime) {
        this.dataTime = dataTime;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    public void setValue(int value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "LoadFromDB{" +
                "dataTime='" + dataTime + '\'' +
                ", category=" + category +
                ", value=" + value +
                '}';
    }
}
