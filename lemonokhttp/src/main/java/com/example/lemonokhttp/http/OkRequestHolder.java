package com.example.lemonokhttp.http;


import com.example.lemonokhttp.enums.RequestType;
import com.example.lemonokhttp.interfaces.IHttpListener;
import com.example.lemonokhttp.interfaces.IHttpService;

import okhttp3.RequestBody;

/**
 * Created by ShuWen on 2017/3/5.
 * 对网络请求参数的二次封装
 */

public class OkRequestHolder {
    private IHttpListener httpListener;
    private IHttpService httpService;
    private String url;
    private RequestBody requestBody;
    private RequestType requestType;

    public RequestType getRequestType() {
        return requestType;
    }

    public void setRequestType(RequestType requestType) {
        this.requestType = requestType;
    }

    public IHttpListener getHttpListener() {
        return httpListener;
    }

    public void setHttpListener(IHttpListener httpListener) {
        this.httpListener = httpListener;
    }

    public IHttpService getHttpService() {
        return httpService;
    }

    public void setHttpService(IHttpService httpService) {
        this.httpService = httpService;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public RequestBody getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(RequestBody requestBody) {
        this.requestBody = requestBody;
    }
}
