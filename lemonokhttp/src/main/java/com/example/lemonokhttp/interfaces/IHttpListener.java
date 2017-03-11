package com.example.lemonokhttp.interfaces;


import okhttp3.Response;

/**
 * Created by ShuWen on 2017/3/5.
 */

public interface IHttpListener {
    void onSuccess(Response response);
    void onError(int code,String ts);
}
