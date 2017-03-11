package com.example.lemonokhttp.enums;

/**
 * Created by ShuWen on 2017/3/9.
 */

public enum  DownloadErrorCode {
    cancel(0),pause(1),dataunfinish(2),fileNotFind(3),IOException(4),FileExsits(5),downloading(6),hasHigherPriority(7);

    private int value;
    DownloadErrorCode(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
