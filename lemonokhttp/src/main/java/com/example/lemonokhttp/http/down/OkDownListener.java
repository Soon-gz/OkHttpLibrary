package com.example.lemonokhttp.http.down;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;


import com.example.lemonokhttp.enums.DownloadErrorCode;
import com.example.lemonokhttp.enums.DownloadStatus;
import com.example.lemonokhttp.interfaces.IDataServiceCallable;
import com.example.lemonokhttp.interfaces.IHttpListener;
import com.example.lemonokhttp.interfaces.IHttpService;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import okhttp3.Response;

/**
 * Created by ShuWen on 2017/3/5.
 */

public class OkDownListener implements IHttpListener {
    private IHttpService httpService;
    private IDataServiceCallable dataCallback;
    private File file;
    private long breakPoint = 0L;
    private DownItemInfo downItenInfo;
    private Handler handler = new Handler(Looper.getMainLooper());

    public OkDownListener(IHttpService httpService, IDataServiceCallable dataCallback, DownItemInfo downItenInfo) {
        this.httpService = httpService;
        this.dataCallback = dataCallback;
        this.downItenInfo = downItenInfo;
        file = new File(downItenInfo.getFilePath());
        breakPoint = file.length();
    }

    @Override
    public void onSuccess(Response response) {
        if (response.isSuccessful()){
            InputStream inputStream = response.body().byteStream();

            if (!file.getParentFile().exists()){
                file.getParentFile().mkdirs();
            }

            FileOutputStream fos = null;
            BufferedOutputStream bos = null;
            try {
                fos = new FileOutputStream(file,true);
                bos = new BufferedOutputStream(fos);

                long speed = 0L;
                long calcSpeed = 0L;
                long receiveLen = 0L;
                long getLen = 0L;
                long dataLen = response.body().contentLength();
                long totalLen = breakPoint + dataLen;

                long stratTime = System.currentTimeMillis();
                long useTime = 0L;
                long currentTime = 0L;

                byte[]buffers = new byte[1024];
                int count = 0;
                int length = 0;

                this.onDownStatusChange(DownloadStatus.startting);
                this.onTotalLen(totalLen);

                this.onDownStatusChange(DownloadStatus.downloading);

                while ((length = inputStream.read(buffers)) != -1){

                    if (httpService.isCancel()){
                        this.onDownStatusChange(DownloadStatus.failed);
                        this.onDownCurrentLenChange(getLen+breakPoint,totalLen,speed);
                        this.onDownError(DownloadErrorCode.cancel.getValue(),"用户取消了网络请求");
                        break;
                    }

                    if (httpService.isPause()){
                        this.onDownStatusChange(DownloadStatus.pause);
                        this.onDownCurrentLenChange(getLen+breakPoint,totalLen,speed);
                        this.onDownError(DownloadErrorCode.pause.getValue(),"用户暂停了网络请求");
                        break;
                    }

                    bos.write(buffers,0,length);
                    calcSpeed +=(long)length;
                    getLen += (long)length;
                    receiveLen += (long)length;
                    count++;

                    if (receiveLen * 10L /totalLen >= 1L || count >= 4000){
                        currentTime = System.currentTimeMillis();
                        useTime = currentTime - stratTime;
                        speed = calcSpeed * 1000L / useTime;

                        stratTime = currentTime;
                        calcSpeed= 0L;
                        count = 0;
                        receiveLen = 0;

                        this.onDownCurrentLenChange(getLen+breakPoint,totalLen,speed);
                    }
                }

                bos.close();
                fos.close();
                if (httpService.isCancel()){
                    return;
                }

                if (httpService.isPause()){
                    return;
                }

                if (dataLen != getLen){
                    onDownError(DownloadErrorCode.dataunfinish.getValue(),"未完全下载数据");
                    onDownCurrentLenChange(getLen+breakPoint,totalLen,speed);
                    onDownStatusChange(DownloadStatus.failed);
                }else {
                    onDownCurrentLenChange(getLen+breakPoint,totalLen,speed);
                    onDownStatusChange(DownloadStatus.finish);
                    onDownFinish();
                }

            } catch (FileNotFoundException e) {
                onDownError(DownloadErrorCode.fileNotFind.getValue(),"下载文件位置未找到！");
                e.printStackTrace();
            } catch (IOException e) {
                onDownError(DownloadErrorCode.IOException.getValue(),"下载文件读取流异常！");
                e.printStackTrace();
            }
        }else {
            onError(response.code(),"服务器响应异常，请查看响应码。");
        }

    }

    private void onDownError(final int value, final String ts) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                dataCallback.onDownFail(downItenInfo.copy(),value,ts);
            }
        });
    }

    private void onDownFinish() {
        downItenInfo.setStatus(DownloadStatus.finish.getValue());
        handler.post(new Runnable() {
            @Override
            public void run() {
                dataCallback.onDownSuccess(downItenInfo);
            }
        });
    }

    private void onTotalLen(final long totalLen) {
        downItenInfo.setTotalLen(totalLen);
        handler.post(new Runnable() {
            @Override
            public void run() {
                dataCallback.onDownTotalLength(downItenInfo.copy(),totalLen);
            }
        });
    }

    private void onDownStatusChange(final DownloadStatus downStatus) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                dataCallback.onDownStatusChanged(downItenInfo.copy(),downStatus);
            }
        });
    }

    private void onDownCurrentLenChange(final long getLen, final long totalLen, final long speed) {
        downItenInfo.setCurrentLen(getLen);
        handler.post(new Runnable() {
            @Override
            public void run() {
                dataCallback.onDownCurrentLength(downItenInfo.copy(),(double)getLen/totalLen,speed);
            }
        });
    }

    /**
     * 2
     * @param headerMap
     */
    public void addHttpHeader(Map<String,String> headerMap)
    {
        long length=file.length();
        Log.e("okHttpLemon","将要下载文件已存在："+length/1024 + "K ,继续下载。");
        if(length>0L)
        {
            headerMap.put("RANGE","bytes="+length+"-");
        }

    }

    @Override
    public void onError(final int code, final String ts) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                onDownError(code,ts);
            }
        });
    }
}
