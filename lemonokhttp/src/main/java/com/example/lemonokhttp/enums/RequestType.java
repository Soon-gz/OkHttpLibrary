package com.example.lemonokhttp.enums;

/**
 * Created by ShuWen on 2017/3/5.
 */

public enum RequestType {
    ERROR(0),POST(1),GET(2),PUT(3);

    private int value;
    RequestType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
