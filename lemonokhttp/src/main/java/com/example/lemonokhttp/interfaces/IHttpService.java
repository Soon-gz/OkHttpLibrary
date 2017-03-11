package com.example.lemonokhttp.interfaces;


import com.example.lemonokhttp.enums.RequestType;

import java.util.Map;

import okhttp3.RequestBody;


/**
 * Created by ShuWen on 2017/3/5.
 */

public interface IHttpService {
    /**
     * @param httpListener when network is success callback
     */
    void setIHttpListener(IHttpListener httpListener);
    /**
     * @param url  server address
     */
    void setUrl(String url);
    /**
     * set network params
     * @param requestBody the params
     */
    void setRequestBody(RequestBody requestBody);
    /**
     * execute runnable
     */
    void execute();

    /**
     * @param requestType  请求类型
     */
    void setRequestType(RequestType requestType);

    /**
     * @return 设置网络请求头
     */
    Map<String,String> getHeaderMap();

    /**
     * @return  用于取消网络请求
     */
    void cancel();

    /**
     * @return  是否取消该请求
     */
    boolean isCancel();

    /**
     * 暂停
     */
    void pause();
    /**
     * @return 是否暂停该请求
     */
    boolean isPause();

}
