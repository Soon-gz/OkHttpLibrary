package com.example.lemonokhttp.http;

import android.support.annotation.NonNull;
import android.util.Log;

import com.example.lemonokhttp.enums.HttpPriority;
import com.example.lemonokhttp.enums.RequestType;
import com.example.lemonokhttp.http.down.DownloadManager;
import com.example.lemonokhttp.interfaces.IDataListener;
import com.example.lemonokhttp.interfaces.IDownloadCallback;

import java.io.File;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.FutureTask;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

/**
 * Created by ShuWen on 2017/3/5.
 */

@SuppressWarnings("unchecked")
public class OkHttpLemon {

    private static LemonParams lemonParams;

    private final String TAG = OkHttpLemon.class.getName();

    //okhttp请求参数设置
    private OkHttpOptions options;

    public static OkHttpLemon init(){
        lemonParams = new LemonParams();
        return InnerClass.instance;
    }

    private OkHttpLemon(){
    }

    /**
     * 设置url
     * @param url
     * @return
     */
    public OkHttpLemon url(String url){
        lemonParams.setUrl(url);
        return this;
    }

    /**
     * get请求响应类
     * @param responseClazz
     * @param <T>
     * @return
     */
    public <T> OkHttpLemon get(Class<T> responseClazz){
        lemonParams.setRequestType(RequestType.GET);
        lemonParams.setResponseClazz(responseClazz);
        return this;
    }

    /**
     * 返回字符串
     * @param <T>
     * @return
     */
    public <T> OkHttpLemon get(){
        lemonParams.setRequestType(RequestType.GET);
        lemonParams.setResponseClazz(null);
        return this;
    }



    /**
     * post String
     * @param key
     * @param value
     * @return
     */
    public OkHttpLemon postString(String key,String value){
        lemonParams.getPostStr().put(key,value);
        lemonParams.setRequestType(RequestType.POST);
        return this;
    }


    /**
     * 上传图片、文件
     * @param name
     * @param fileName
     * @param imgFile
     * @return
     */
    public OkHttpLemon postFile(String name,String fileName,File imgFile){
        FileWrapper wrapper = new FileWrapper(name,fileName,imgFile);
        lemonParams.getUpdateFiles().add(wrapper);
        lemonParams.setRequestType(RequestType.POST);
        return this;
    }


    /**
     * 设置post响应类
     * @param responseClazz
     * @param <T>
     * @return
     */
    public <T> OkHttpLemon postResponseClazz(Class<T> responseClazz){
        lemonParams.setResponseClazz(responseClazz);
        return this;
    }


    /**
     * 返回字符串
     * @param dataListener
     */
    public void executes(@NonNull IDataListener<String> dataListener){
        execute(dataListener);
    }

    /**
     * 执行请求
     * @param dataListener
     * @param <T>
     * @return
     */
    public <T> void execute(@NonNull IDataListener<T> dataListener){

        if (lemonParams.getUrl() != null){

            if (lemonParams.getRequestType() != null){
                //get请求
                if (lemonParams.getRequestType().equals(RequestType.GET)){

                    sendRequest(lemonParams.getRequestType(),null,lemonParams.getUrl(),lemonParams.getResponseClazz(),dataListener,lemonParams.getHeaderMap());

                }else if (lemonParams.getRequestType().equals(RequestType.POST)){
                    //表单上传
                    if (lemonParams.getUpdateFiles().size() == 0){

                        FormBody.Builder builder = new FormBody.Builder();
                        addPostString(builder);
                        RequestBody requestBody = builder.build();
                        sendRequest(lemonParams.getRequestType(),requestBody,lemonParams.getUrl(),lemonParams.getResponseClazz(),dataListener,lemonParams.getHeaderMap());
                    }//上传文件
                    else {

                        MultipartBody.Builder builder = new MultipartBody.Builder();
                        addPostString(builder);
                        addFiles(builder);
                        RequestBody requestBody = builder.build();
                        sendRequest(lemonParams.getRequestType(),requestBody,lemonParams.getUrl(),lemonParams.getResponseClazz(),dataListener,lemonParams.getHeaderMap());
                    }
                }
            }else {
                Log.e(TAG,"未设置请求模式，请调用get或post方法！");
            }
        }else {
            Log.e(TAG,"未设置请求url，请调用url方法！");
        }


    }

    /**
     * 添加文件
     * @param builder
     */
    private void addFiles(MultipartBody.Builder builder) {
        for (int i = 0; i < lemonParams.getUpdateFiles().size(); i++) {
            FileWrapper wrapper = (FileWrapper) lemonParams.getUpdateFiles().get(i);
            builder.addFormDataPart(wrapper.getKey(),wrapper.getFileName(),RequestBody.create(guessMimeType(wrapper.getFileName()),wrapper.getFile()));
        }
    }

    /**
     * 添加上传字符串
     * @param builder
     */
    private void addPostString(FormBody.Builder builder) {
        Iterator<String> iterator = lemonParams.getPostStr().keySet().iterator();
        while (iterator.hasNext()){
            String key = iterator.next();
            String value = (String) lemonParams.getPostStr().get(key);
            builder.add(key,value);
        }
    }

    /**
     * 添加上传字符串
     * @param builder
     */
    private void addPostString(MultipartBody.Builder builder) {
        Iterator<String> iterator = lemonParams.getPostStr().keySet().iterator();
        while (iterator.hasNext()){
            String key = iterator.next();
            String value = (String) lemonParams.getPostStr().get(key);
            builder.addFormDataPart(key,value);
        }
    }


    /**
     * 内部类单利模式
     */
    private static class InnerClass{
        public static OkHttpLemon instance =  new OkHttpLemon();
    }

    /**
     * 发起网络请求
     * @param requestType
     * @param body
     * @param url
     * @param responseClazz
     * @param dataListener
     * @param headerMap
     * @param <T>
     */
    private  <T> void sendRequest(RequestType requestType, RequestBody body, String url, Class<T> responseClazz, IDataListener<T> dataListener,Map<String,String> headerMap){
       OkRequestHolder requestHolder = new OkRequestHolder();
        requestHolder.setUrl(url);
        requestHolder.setRequestBody(body);
        requestHolder.setRequestType(requestType);

        OkHttpService okHttpService = new OkHttpService();
        OkHttpListener<T> okHttpListener = new OkHttpListener<>(dataListener,responseClazz);
        if (headerMap != null){
            okHttpService.setHeaderMap(headerMap);
        }

        requestHolder.setHttpService(okHttpService);
        requestHolder.setHttpListener(okHttpListener);

        OkHttpTask httpTask = new OkHttpTask(requestHolder);

        FutureTask futureTask = new FutureTask(httpTask,null);

        try {
            ThreadPoolManager.getInstance().execute(futureTask);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }


    /**
     * 暴露给调用层的下载
     * @param dataCallback
     */
    public void executeDown(@NonNull IDownloadCallback dataCallback) {

        if (lemonParams.getUrl() != null) {

            if (lemonParams.getRequestType() != null) {

                if (lemonParams.getFilePath() != null) {

                    if (lemonParams.getHttpPriority() != null) {
                        DownloadManager.getInstance()
                                .addObservable(lemonParams.getUrl(), dataCallback)
                                .download(lemonParams.getUrl(),
                                        lemonParams.getRequestType(),
                                        lemonParams.getFilePath(),
                                        lemonParams.getHttpPriority());
                    } else {
                        DownloadManager.getInstance()
                                .addObservable(lemonParams.getUrl(), dataCallback)
                                .download(lemonParams.getUrl(),
                                        lemonParams.getRequestType(),
                                        lemonParams.getFilePath());
                    }

                } else {

                    if (lemonParams.getHttpPriority() != null) {
                        DownloadManager.getInstance()
                                .addObservable(lemonParams.getUrl(), dataCallback)
                                .download(lemonParams.getUrl(),
                                        lemonParams.getRequestType(),
                                        lemonParams.getHttpPriority());
                    } else {
                        DownloadManager.getInstance()
                                .addObservable(lemonParams.getUrl(), dataCallback)
                                .download(lemonParams.getUrl(),
                                        lemonParams.getRequestType());
                    }

                }

            } else {

                if (lemonParams.getFilePath() != null) {

                    if (lemonParams.getHttpPriority() != null) {
                        DownloadManager.getInstance()
                                .addObservable(lemonParams.getUrl(), dataCallback)
                                .download(lemonParams.getUrl(),
                                        RequestType.GET,
                                        lemonParams.getFilePath(),
                                        lemonParams.getHttpPriority());
                    } else {
                        DownloadManager.getInstance()
                                .addObservable(lemonParams.getUrl(), dataCallback)
                                .download(lemonParams.getUrl(),
                                        RequestType.GET,
                                        lemonParams.getFilePath());
                    }

                } else {

                    if (lemonParams.getHttpPriority() != null) {
                        DownloadManager.getInstance()
                                .addObservable(lemonParams.getUrl(), dataCallback)
                                .download(lemonParams.getUrl(),
                                        RequestType.GET,
                                        lemonParams.getHttpPriority());
                    } else {
                        DownloadManager.getInstance()
                                .addObservable(lemonParams.getUrl(), dataCallback)
                                .download(lemonParams.getUrl(),
                                        RequestType.GET);
                    }

                }

            }
        }else {

            Log.e(TAG, "请设置网络请求的url");

        }
    }


    /**
     * 设置下载优先级
     * @param priority
     * @return
     */
    public OkHttpLemon downPriority(HttpPriority priority){
        lemonParams.setHttpPriority(priority);
        return this;
    }

    /**
     * 设置具体文件下载地址
     * @param filePath  sdcard/0/wps.apk  包括全名
     * @return
     */
    public OkHttpLemon downFilePath(String filePath){
        lemonParams.setFilePath(filePath);
        return this;
    }


    /**
     * 暂停
     * @param url
     */
    public void pause(String url){
        DownloadManager.getInstance().pauseByUrl(url);
    }

    /**
     * 暂停
     * @param url
     * @param fileName
     */
    public void pause(String url,String fileName){
        DownloadManager.getInstance().pauseByUrlAndFilePath(url,fileName);
    }

    /**
     * 重启
     * @param url
     */
    public void start(String url){
        DownloadManager.getInstance().stratByUrl(url);
    }

    /**
     * 重启
     * @param url
     * @param filePath
     */
    public void start(String url,String filePath){
        DownloadManager.getInstance().stratByUrlAndFilePath(url,filePath);
    }

    /**
     * @return  初始化okhttp请求参数
     */
    public OkHttpOptions initOptions(){
        options = new OkHttpOptions();
        return options;
    }

    public OkHttpOptions getOptions(){
        return options;
    }


    /**
     * 参考AlertDialog源码,对请求参数做一个缓存
     */
    private static class LemonParams<T>{

        //请求类型封装
        private RequestType requestType;

        //请求的字符串
        private HashMap<String,String> postStr = new HashMap<>();


        //请求头文件封装
        private HashMap<String,String> headerMap = new HashMap<>();

        //请求地址
        private String url;

        //网络数据响应类型
        private Class<T> responseClazz;

        //网络上传文件缓存
        private List<FileWrapper> updateFiles = new ArrayList<>();

        //下载文件的优先级
        private HttpPriority httpPriority;

        //文件保存位置   eg:    sdcard/0/wps.apk
        private String filePath;

        public String getFilePath() {
            return filePath;
        }

        public void setFilePath(String filePath) {
            this.filePath = filePath;
        }

        public HttpPriority getHttpPriority() {
            return httpPriority;
        }

        public void setHttpPriority(HttpPriority httpPriority) {
            this.httpPriority = httpPriority;
        }

        public List<FileWrapper> getUpdateFiles() {
            return updateFiles;
        }

        public void setUpdateFiles(List<FileWrapper> updateFiles) {
            this.updateFiles = updateFiles;
        }


        public RequestType getRequestType() {
            return requestType;
        }

        public void setRequestType(RequestType requestType) {
            this.requestType = requestType;
        }

        public HashMap<String, String> getPostStr() {
            return postStr;
        }

        public void setPostStr(HashMap<String, String> postStr) {
            this.postStr = postStr;
        }

        public HashMap<String, String> getHeaderMap() {
            return headerMap;
        }

        public void setHeaderMap(HashMap<String, String> headerMap) {
            this.headerMap = headerMap;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public Class<T> getResponseClazz() {
            return responseClazz;
        }

        public void setResponseClazz(Class<T> responseClazz) {
            this.responseClazz = responseClazz;
        }
    }


    /**
     * 文件类型封装
     */
    private class FileWrapper{
        private String key;
        private String fileName;
        private File file;

        public FileWrapper(String key, String fileName, File file) {
            this.key = key;
            this.fileName = fileName;
            this.file = file;
        }

        public String getKey() {
            return key;
        }

        public String getFileName() {
            return fileName;
        }

        public File getFile() {
            return file;
        }
    }

    /**
     * 通过文件名获取文件MIME
     * @param path
     * @return
     */
    private MediaType guessMimeType(String path) {
        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        path = path.replace("#", "");   //解决文件名中含有#号异常的问题
        String contentType = fileNameMap.getContentTypeFor(path);
        if (contentType == null) {
            contentType = "application/octet-stream";
        }
        return MediaType.parse(contentType);
    }


    /**
     * okhttp请求参数封装
     */
    public class OkHttpOptions{
        private long CONNECT_TIME_OUT = 10;
        private long WRITE_TIME_OUT = 10;
        private long READ_TIME_OUT = 30;

        public OkHttpOptions(int CONNECT_TIME_OUT, int WRITE_TIME_OUT, int READ_TIME_OUT) {
            this.CONNECT_TIME_OUT = CONNECT_TIME_OUT;
            this.WRITE_TIME_OUT = WRITE_TIME_OUT;
            this.READ_TIME_OUT = READ_TIME_OUT;
        }

        public OkHttpOptions() {
        }

        public long getCONNECT_TIME_OUT() {
            return CONNECT_TIME_OUT;
        }

        public long getWRITE_TIME_OUT() {
            return WRITE_TIME_OUT;
        }

        public long getREAD_TIME_OUT() {
            return READ_TIME_OUT;
        }

        public OkHttpOptions setCONNECT_TIME_OUT(long CONNECT_TIME_OUT) {
            this.CONNECT_TIME_OUT = CONNECT_TIME_OUT;
            return  this;
        }

        public OkHttpOptions setWRITE_TIME_OUT(long WRITE_TIME_OUT) {
            this.WRITE_TIME_OUT = WRITE_TIME_OUT;
            return  this;
        }

        public OkHttpOptions setREAD_TIME_OUT(long READ_TIME_OUT) {
            this.READ_TIME_OUT = READ_TIME_OUT;
            return  this;
        }
    }

}
