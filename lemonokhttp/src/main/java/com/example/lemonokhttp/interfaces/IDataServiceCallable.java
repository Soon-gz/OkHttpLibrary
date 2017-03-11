package com.example.lemonokhttp.interfaces;

import com.example.lemonokhttp.enums.DownloadStatus;
import com.example.lemonokhttp.http.down.DownItemInfo;

/**
 * Created by ShuWen on 2017/3/1.
 */

public interface IDataServiceCallable {

    void onDownTotalLength(DownItemInfo itemInfo,long totalLength);

    void onDownStatusChanged(DownItemInfo itemInfo,DownloadStatus downloadStatus);

    void onDownSuccess(DownItemInfo downItemInfo);

    void onDownFail(DownItemInfo downItemInfo, int code, String ts);

    void onDownCurrentLength(DownItemInfo downItemInfo, double downLength, long speed);


}
