package com.example.lemonokhttp.http;


import com.example.lemonokhttp.interfaces.IHttpService;

import java.util.concurrent.FutureTask;

import okhttp3.MediaType;
import okhttp3.RequestBody;

/**
 * Created by ShuWen on 2017/3/5.
 */

@SuppressWarnings("unchecked")
public class OkHttpTask implements Runnable {
    private IHttpService httpService;
    private FutureTask futureTask;

    public OkHttpTask(OkRequestHolder okRequestHolder) {
        httpService = okRequestHolder.getHttpService();
        httpService.setUrl(okRequestHolder.getUrl());
        httpService.setIHttpListener(okRequestHolder.getHttpListener());
        httpService.setRequestType(okRequestHolder.getRequestType());
        httpService.setRequestBody(okRequestHolder.getRequestBody());
    }

    @Override
    public void run() {
        httpService.execute();
    }

    /**
     * 暂停
     */
    public void pause(){
        httpService.pause();
        if (futureTask != null){
            ThreadPoolManager.getInstance().removeTask(futureTask);
        }
    }

    public void start(){
        futureTask = new FutureTask(this,null);
        try {
            ThreadPoolManager.getInstance().execute(futureTask);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
