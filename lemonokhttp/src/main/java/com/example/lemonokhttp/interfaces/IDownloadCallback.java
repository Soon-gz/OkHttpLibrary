package com.example.lemonokhttp.interfaces;

import com.example.lemonokhttp.enums.DownloadStatus;

/**
 * Created by ShuWen on 2017/3/5.
 */

public interface IDownloadCallback {
    void onDownTotalLength(long totalLen);
    void onDownCurrentLenChange(long alreadyDownLen, double downPercent, long speed);
    void onFinish(int downId);
    void onEorror(int errorCode, String ts);
    void onDownStatusChange(DownloadStatus downStatus);
}
