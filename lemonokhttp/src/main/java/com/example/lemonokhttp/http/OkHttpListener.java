package com.example.lemonokhttp.http;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.lemonokhttp.interfaces.IDataListener;
import com.example.lemonokhttp.interfaces.IHttpListener;
import com.example.lemonokhttp.interfaces.IHttpService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;

import okhttp3.Response;
import okhttp3.ResponseBody;


/**
 * Created by ShuWen on 2017/3/5.
 */

@SuppressWarnings("unchecked")
public class OkHttpListener<T> implements IHttpListener {
    private IDataListener<T> dataListener;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Class<T> responseClazz;

    public OkHttpListener(IDataListener<T> dataListener, Class<T> responseClazz) {
        this.dataListener = dataListener;
        this.responseClazz = responseClazz;
    }


    @SuppressLint("NewApi")
    @Override
    public void onSuccess(Response response) {
        if (response.isSuccessful()){
            try( ResponseBody requestBody = response.body()) {
                if (responseClazz != null && responseClazz != String.class){
                    final T t = new Gson().fromJson(requestBody.charStream(),responseClazz);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            dataListener.onSuccess(t);
                        }
                    });
                }else {
                   final String result = new String(requestBody.bytes());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            dataListener.onSuccess((T) result);
                        }
                    });
                }

            } catch (Exception e) {
                dataListener.onError(response.code(),"网络响应成功，解析数据异常。");
                e.printStackTrace();
            }
        }else {
            dataListener.onError(response.code(),"网络响应异常，请查看错误码。");
        }

    }



    @Override
    public void onError(int code,String ts) {
        dataListener.onError(code,ts);
    }
}
