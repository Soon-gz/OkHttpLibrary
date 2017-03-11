package com.example.lemonokhttp.interfaces;

/**
 * Created by ShuWen on 2017/3/5.
 */

public interface IDataListener<T> {
    void onSuccess(T t);
    void onError(int code,String ts);
}
