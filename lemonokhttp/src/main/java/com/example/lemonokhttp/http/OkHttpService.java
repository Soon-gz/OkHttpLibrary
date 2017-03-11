package com.example.lemonokhttp.http;


import com.example.lemonokhttp.enums.DownloadErrorCode;
import com.example.lemonokhttp.enums.RequestType;
import com.example.lemonokhttp.interfaces.IHttpListener;
import com.example.lemonokhttp.interfaces.IHttpService;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by ShuWen on 2017/3/5.
 */

public class OkHttpService implements IHttpService {
    private String url;
    private IHttpListener httpListener;
    private long connectTimeout = OkHttpLemon.init().getOptions().getCONNECT_TIME_OUT();
    private long writeTimeout = OkHttpLemon.init().getOptions().getWRITE_TIME_OUT();
    private long readTimeout = OkHttpLemon.init().getOptions().getREAD_TIME_OUT();
    private OkHttpClient httpClient;
    private RequestBody requestBody;
    private RequestType requestType;
    private AtomicBoolean isCancel = new AtomicBoolean(false);
    private AtomicBoolean isPause = new AtomicBoolean(false);

    private Map<String,String> headerMap = Collections.synchronizedMap(new HashMap<String, String>());

    public OkHttpService() {
        checkLong();
        httpClient = new OkHttpClient.Builder()
                .connectTimeout(connectTimeout, TimeUnit.SECONDS)
                .writeTimeout(writeTimeout,TimeUnit.SECONDS)
                .readTimeout(readTimeout,TimeUnit.SECONDS)
                .build();
    }

    //防止有使用者未调用okhttp参数初始化函数
    private void checkLong() {
        if (connectTimeout == 0){
            connectTimeout = 10L;
        }
        if (writeTimeout == 0){
            writeTimeout = 10L;
        }
        if (readTimeout == 0){
            readTimeout = 30L;
        }
    }

    @Override
    public void setIHttpListener(IHttpListener httpListener) {
        this.httpListener = httpListener;
    }

    @Override
    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public void setRequestBody(RequestBody requestBody) {
        this.requestBody = requestBody;
    }

    @Override
    public void execute() {
        switch (requestType) {
            case GET:
                Request.Builder builderGet = new Request.Builder();
                initHeaderMap(builderGet);
                Request requestGet = builderGet.url(url).build();
                try {
                    Call callGet = httpClient.newCall(requestGet);
                    Response responseGet = callGet.execute();
                    httpListener.onSuccess(responseGet);
                } catch (IOException e) {
                    httpListener.onError(DownloadErrorCode.IOException.getValue(),"网络流读取异常，IOException");
                    e.printStackTrace();
                }
                break;
            case POST:
                Request.Builder builderPost = new Request.Builder();
                initHeaderMap(builderPost);
                Request requestPost = builderPost.url(url).post(requestBody).build();
                try {
                    Call callPost = httpClient.newCall(requestPost);
                    Response responsePost = callPost.execute();
                    httpListener.onSuccess(responsePost);
                } catch (IOException e) {
                    httpListener.onError(DownloadErrorCode.IOException.getValue(),"网络流读取异常，IOException");
                    e.printStackTrace();
                }
                break;
            case PUT:
                break;
        }
    }

    private void initHeaderMap( Request.Builder builder) {
        if (headerMap.size() > 0){
            Iterator<String> iterator = headerMap.keySet().iterator();
            while (iterator.hasNext()){
                String key = iterator.next();
                String value = headerMap.get(key);
                builder.addHeader(key,value);
            }
        }
    }


    public void setHeaderMap(Map<String, String> headerMap) {
        this.headerMap = headerMap;
    }

    @Override
    public void setRequestType(RequestType requestType) {
        this.requestType = requestType;
    }

    @Override
    public Map<String, String> getHeaderMap() {
        return headerMap;
    }

    @Override
    public void cancel() {
        isCancel.compareAndSet(false,true);
    }

    @Override
    public boolean isCancel() {
        return isCancel.get();
    }

    @Override
    public void pause() {
        isPause.compareAndSet(false,true);
    }

    @Override
    public boolean isPause() {
        return isPause.get();
    }

}
