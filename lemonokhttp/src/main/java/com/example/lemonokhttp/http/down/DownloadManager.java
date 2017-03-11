package com.example.lemonokhttp.http.down;

import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.lemonlibrary.db.BaseDaoFactory;
import com.example.lemonokhttp.dao.DownloadDao;
import com.example.lemonokhttp.enums.DownloadErrorCode;
import com.example.lemonokhttp.enums.DownloadStatus;
import com.example.lemonokhttp.enums.DownloadStopMode;
import com.example.lemonokhttp.enums.HttpPriority;
import com.example.lemonokhttp.enums.RequestType;
import com.example.lemonokhttp.http.OkHttpService;
import com.example.lemonokhttp.http.OkHttpTask;
import com.example.lemonokhttp.http.OkRequestHolder;
import com.example.lemonokhttp.interfaces.IDataServiceCallable;
import com.example.lemonokhttp.interfaces.IDownloadCallback;
import com.example.lemonokhttp.interfaces.IHttpService;

import java.io.File;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * Created by ShuWen on 2017/3/9.
 */

public class DownloadManager implements IDataServiceCallable {

    private static DownloadManager instance;

    //网络请求缓存池
    private List<DownItemInfo> downItemInfoList = new CopyOnWriteArrayList<>();

    //观察者集合
    private Map<String,IDownloadCallback> callBackMap = Collections.synchronizedMap(new HashMap<String, IDownloadCallback>());

    //建立数据库链接
    private DownloadDao downloadDao = BaseDaoFactory.getInstance().getFileComponent(DownloadDao.class,DownItemInfo.class,Environment.getExternalStorageDirectory()+File.separator+"okLemonDown" + File.separator + "recordDb" ,"downFileRecord.db");

    //回调主线程
    private Handler handler = new Handler(Looper.getMainLooper());

    private final byte[]lock = new byte[0];

    private static final byte[]lock1 = new byte[0];


    private DownloadManager(){}

    public static DownloadManager getInstance(){
        if (instance == null){
            synchronized (lock1){
                if (instance == null){
                    instance = new DownloadManager();
                }
            }
        }
        return instance;
    }


    public int download(String url,  RequestType requestType){
        String[]prefixes = url.split("/");
        String afterFix = prefixes[prefixes.length - 1];
        File file = new File(Environment.getExternalStorageDirectory()+File.separator + "okLemonDown" ,"downFiles");
        if (!file.exists()){
            file.mkdirs();
        }
        String filePath = file.getAbsolutePath()+File.separator + afterFix;
        return download(url, requestType, filePath);
    }

    public int download(String url,  RequestType requestType,HttpPriority priority){
        String[]prefixes = url.split("/");
        String afterFix = prefixes[prefixes.length - 1];
        File file = new File(Environment.getExternalStorageDirectory(),"okLemonDown");
        if (!file.exists()){
            file.mkdirs();
        }
        String filePath = file.getAbsolutePath()+File.separator + afterFix;
        return download(url, requestType, filePath,prefixes[prefixes.length - 1],priority);
    }


    public int download(String url, RequestType requestType,String filePath ){
        String[]prefixes = url.split("/");
        String afterFix = prefixes[prefixes.length - 1];
        return download(url,requestType, filePath, afterFix);
    }

    public int download(String url, RequestType requestType,String filePath,HttpPriority priority ){
        String[]prefixes = url.split("/");
        String afterFix = prefixes[prefixes.length - 1];
        return download(url,requestType, filePath, afterFix,priority);
    }

    public int download(String url , RequestType requestType,String filePath , String displayName){
        return download(url,requestType,filePath , displayName,HttpPriority.middle);
    }

    public int download(final String url, RequestType requestType, String filePath, String displayName, HttpPriority priority){

        File file = new File(filePath);

        if (priority == null){
            priority = HttpPriority.low;
        }

        DownItemInfo downItemInfo = downloadDao.findRecord(url,filePath);
        //先检查数据库是否存在，如果不存在
        if (downItemInfo == null){
            DownItemInfo sameDown = downloadDao.findSingleRecord(filePath);
            //再次检查
            if (sameDown != null){
                if (sameDown.getCurrentLen().longValue() == sameDown.getTotalLen().longValue() && file.exists()){
                    //出现这种现象，是url和file不统一
                    sameDown.setUrl(url);
                    downloadDao.updateRecord(sameDown);

                    synchronized (lock){
                        try {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (callBackMap.get(url) != null){
                                        callBackMap.get(url).onEorror(DownloadErrorCode.FileExsits.getValue(),"下载的文件已存在。");
                                    }
                                }
                            });
                            return -1;
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }
            }

            //确定该下载不存在，那么添加数据库
            downloadDao.addRecord(url,filePath,displayName,priority.getValue(),requestType);
            //更新当前downItemInfo
            downItemInfo = downloadDao.findRecord(url,filePath);
        }

        //新加入任务是否正在下载缓存中
        if (isDownLoading(downItemInfo)){
            synchronized (lock){
                try {
                    if (callBackMap.get(url) != null){
                        callBackMap.get(url).onEorror(DownloadErrorCode.downloading.getValue(),"该任务已在下载，请勿重复添加。");
                    }
                    return -1;
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }

        //确保downloadItemInfo不能null
        if (downItemInfo != null){

            downItemInfo.setPriority(priority.getValue());
            //添加----------------------------------------------------
            downItemInfo.setStopMode(DownloadStopMode.auto.getValue());

            //查看数据库中存放记录的下载状态,避免下载成功而状态未及时更新
            if (downItemInfo.getStatus() != DownloadStatus.finish.getValue()){

                //判断数据库中 总长度是否等于文件长度
                if(file.exists()&&downItemInfo.getTotalLen()==file.length()&&downItemInfo.getTotalLen()!=0)
                {
                    downItemInfo.setStatus(DownloadStatus.finish.getValue());
                    //更新数据库
                    downloadDao.updateRecord(downItemInfo);

                    synchronized (lock)
                    {
                        try {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (callBackMap.get(url) != null){
                                        callBackMap.get(url).onEorror(DownloadErrorCode.FileExsits.getValue(),"文件已下载，数据库记录未更新。");
                                    }
                                }
                            });
                            return -1;
                        }catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }else {
                //数据库显示下载成功
                if (!file.exists() || downItemInfo.getCurrentLen().longValue() != downItemInfo.getTotalLen().longValue() || file.length() != downItemInfo.getTotalLen()){
                    //用户手动删除文件，更改下载失败
                    downItemInfo.setStatus(DownloadStatus.failed.getValue());
                }else {
                    synchronized (lock)
                    {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (callBackMap.get(url) != null){
                                    callBackMap.get(url).onEorror(DownloadErrorCode.FileExsits.getValue(),"该文件已存在，不用再次下载。");
                                }
                            }
                        });
                    }
                    return -1;
                }
            }

            /**
             * 更新
             */
            downloadDao.updateRecord(downItemInfo);

            List<DownItemInfo> downLoadings = downItemInfoList;
            //判断权限,不为最高级，检查缓存是否有最高级
            if (!priority.equals(HttpPriority.high)){
                for (DownItemInfo itemInfo:downLoadings) {

                    if (itemInfo != null && itemInfo.getPriority().longValue() == HttpPriority.high.getValue()){

                        /**
                         *     当前下载级别不是最高级 传进来的是middle    但是在数据库中查到路径一模一样 的记录   所以他也是最高级------------------------------
                         *     比如 第一次下载是用最高级下载，app闪退后，没有下载完成，第二次传的是默认级别，这样就应该是最高级别下载
                         */
                        if (itemInfo.getUrl().equals(downItemInfo.getUrl()) && itemInfo.getFilePath().equals(downItemInfo.getFilePath())){
                            break;
                        }else {
                            //暂时缓存
                            downItemInfo.setStatus(DownloadStatus.pause.getValue());
                            downItemInfo.setStopMode(DownloadStopMode.auto.getValue());
                            downItemInfoList.add(downItemInfo);
                            synchronized (lock)
                            {
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (callBackMap.get(url) != null){
                                            callBackMap.get(url).onEorror(DownloadErrorCode.hasHigherPriority.getValue(),"缓存有级别更高请求在执行，请稍等");
                                        }
                                    }
                                });
                            }
                            return -1;
                        }
                    }

                }
            }
            //下载逻辑
            reallyDown(downItemInfo,requestType);

            if(priority.equals(HttpPriority.high))
            {

                synchronized (lock)
                {
                    for (DownItemInfo downloadItemInfo1:downLoadings)
                    {
                        if(!downItemInfo.getFilePath().equals(downloadItemInfo1.getFilePath()))
                        {
                            DownItemInfo downingInfo=downloadDao.findSingleRecord(downloadItemInfo1.getFilePath());
                            if(downingInfo!=null)
                            {
                                pause(downingInfo.getUrl(),downingInfo.getFilePath(),DownloadStopMode.auto);
                            }
                        }
                    }
                }
                return -1;
            }

        }


        return -1;
    }


    /**
     * 手动暂停
     * @param url
     * @param filePath
     */
    public void pauseByUrlAndFilePath(String url,String filePath){
        for (DownItemInfo itemInfo:downItemInfoList) {
            if (url.equals(itemInfo.getUrl()) && filePath.equals(itemInfo.getFilePath())){
                itemInfo.setStopMode(DownloadStopMode.hand.getValue());
                itemInfo.setStatus(DownloadStatus.pause.getValue());
                pause(itemInfo.getUrl(),itemInfo.getFilePath(),DownloadStopMode.hand);
            }
        }
    }
    /**
     * 手动暂停
     * @param url
     */
    public void pauseByUrl(String url){
        for (DownItemInfo itemInfo:downItemInfoList) {
            if (url.equals(itemInfo.getUrl())){
                itemInfo.setStopMode(DownloadStopMode.hand.getValue());
                itemInfo.setStatus(DownloadStatus.pause.getValue());
                pause(itemInfo.getUrl(),itemInfo.getFilePath(),DownloadStopMode.hand);
            }
        }
    }

    /**
     * 手动启动
     * @param url
     * @param filePath
     */
    public void stratByUrlAndFilePath(String url,String filePath){
        for (DownItemInfo itemInfo:downItemInfoList) {
            if (url.equals(itemInfo.getUrl()) && filePath.equals(itemInfo.getFilePath())){
                if (itemInfo.getStopMode() == DownloadStopMode.hand.getValue() && itemInfo.getStatus() == DownloadStatus.pause.getValue()){
                    downItemInfoList.remove(itemInfo);
                    download(url,RequestType.values()[itemInfo.getHttpTaskType()]);
                }
            }
        }
    }

    /**
     * 手动启动
     * @param url
     */
    public void stratByUrl(String url){
        for (DownItemInfo itemInfo:downItemInfoList) {
            if (url.equals(itemInfo.getUrl())){
                if (itemInfo.getStopMode() == DownloadStopMode.hand.getValue() && itemInfo.getStatus() == DownloadStatus.pause.getValue()){
                    downItemInfoList.remove(itemInfo);
                    download(url,RequestType.values()[itemInfo.getHttpTaskType()]);
                }
            }
        }
    }



    /**
     * 停止
     * @param mode
     */
    public void pause(String url,String filePath, DownloadStopMode mode)
    {
        if (mode == null)
        {
            mode = DownloadStopMode.auto;
        }
        final DownItemInfo downloadInfo =downloadDao.findRecord(url,filePath);
        if (downloadInfo != null)
        {
            // 更新停止状态
            if (downloadInfo != null)
            {
                downloadInfo.setStopMode(mode.getValue());
                downloadInfo.setStatus(DownloadStatus.pause.getValue());
                downloadDao.updateRecord(downloadInfo);
            }
            for (DownItemInfo downing:downItemInfoList)
            {
                if(downing.getUrl().equals(downloadInfo.getUrl()) && downing.getFilePath().equals(downloadInfo.getFilePath()))
                {
                    downing.getHttpTask().pause();
                    break;
                }
            }
        }
    }

    /**
     * 下载
     */
    public DownItemInfo reallyDown(DownItemInfo downloadItemInfo,RequestType requestType)
    {
        synchronized (lock)
        {

            //实例化DownloadItem
            OkRequestHolder requestHodler=new OkRequestHolder();
            //设置请求下载的策略
            IHttpService httpService=new OkHttpService();
            //得到请求头的参数 map
            Map<String,String> map=httpService.getHeaderMap();
            /**
             * 处理结果的策略
             */
            OkDownListener httpListener=new OkDownListener(httpService,this,downloadItemInfo);

            httpListener.addHttpHeader(map);

            requestHodler.setHttpListener(httpListener);
            requestHodler.setHttpService(httpService);
            requestHodler.setUrl(downloadItemInfo.getUrl());
            requestHodler.setRequestType(requestType);
            requestHodler.setRequestBody(null);

            OkHttpTask httpTask=new OkHttpTask(requestHodler);
            downloadItemInfo.setHttpTask(httpTask);

            /**
             * 添加
             */

            downItemInfoList.add(downloadItemInfo);
            httpTask.start();
        }

        return downloadItemInfo;
    }

    /**
     * 是否正在下载
     * @param downItemInfo
     * @return
     */
    private boolean isDownLoading(DownItemInfo downItemInfo) {
        for (DownItemInfo itemInfo:downItemInfoList) {
            if (itemInfo.getUrl().equals(downItemInfo.getUrl()) && itemInfo.getFilePath().equals(downItemInfo.getFilePath())){
                return true;
            }
        }
        return false;
    }

    /**
     * 添加观察者
     * @param url
     * @param downloadCallback
     */
    public DownloadManager addObservable(String url,IDownloadCallback downloadCallback){
        if (!callBackMap.containsKey(url)){
            callBackMap.put(url,downloadCallback);
        }
        return this;
    }

    /**
     * 下载成功后，恢复自动暂停任务
     */
    private void resumeDownload() {
        for (DownItemInfo itemInfo:downItemInfoList) {
            if (DownloadStatus.pause.getValue() == itemInfo.getStatus() && itemInfo.getStopMode() == DownloadStopMode.auto.getValue()){
                downItemInfoList.remove(itemInfo);
                download(itemInfo.getUrl(),RequestType.values()[itemInfo.getHttpTaskType()]);
            }
        }
    }

    //更新数据库
    private void updateSqlite(DownItemInfo itemInfo){
        DownItemInfo where = new DownItemInfo();
        where.setUrl(itemInfo.getUrl());
        where.setFilePath(itemInfo.getFilePath());
        downloadDao.update(itemInfo,where);
    }

    @Override
    public void onDownTotalLength(DownItemInfo itemInfo,long totalLength) {
        callBackMap.get(itemInfo.getUrl()).onDownTotalLength(totalLength);
    }

    @Override
    public void onDownStatusChanged(DownItemInfo itemInfo,DownloadStatus downloadStatus) {
        callBackMap.get(itemInfo.getUrl()).onDownStatusChange(downloadStatus);
    }

    @Override
    public void onDownSuccess(DownItemInfo downItemInfo) {
        java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        downItemInfo.setFinishTime(dateFormat.format(new Date()));
        updateSqlite(downItemInfo);
        removeDownItem(downItemInfo);
        resumeDownload();
    }

    public void removeDownItem(DownItemInfo itemInfo){
        for (DownItemInfo downItemInfo:downItemInfoList) {
            if (itemInfo.getUrl().equals(downItemInfo.getUrl()) && itemInfo.getFilePath().equals(downItemInfo.getFilePath())){
                downItemInfoList.remove(downItemInfo);
                break;
            }
        }
    }

    @Override
    public void onDownFail(DownItemInfo downItemInfo, int code, String ts) {
        java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        downItemInfo.setFinishTime(dateFormat.format(new Date()));
        updateSqlite(downItemInfo);
        removeDownItem(downItemInfo);
        resumeDownload();
        callBackMap.get(downItemInfo.getUrl()).onEorror(code,ts);
    }

    @Override
    public void onDownCurrentLength(DownItemInfo downItemInfo, double downLength, long speed) {
        callBackMap.get(downItemInfo.getUrl()).onDownCurrentLenChange(downItemInfo.getCurrentLen(),downLength,speed);
    }

}
