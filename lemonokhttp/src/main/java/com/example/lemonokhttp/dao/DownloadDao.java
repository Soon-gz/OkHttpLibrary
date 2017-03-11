package com.example.lemonokhttp.dao;

import com.example.lemonlibrary.db.DefaultBaseDao;
import com.example.lemonokhttp.enums.DownloadStatus;
import com.example.lemonokhttp.enums.DownloadStopMode;
import com.example.lemonokhttp.enums.RequestType;
import com.example.lemonokhttp.http.down.DownItemInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by ShuWen on 2017/3/9.
 */

public class DownloadDao extends DefaultBaseDao<DownItemInfo> {
    //二级缓存
    protected List<DownItemInfo> downItemInfoList = Collections.synchronizedList(new ArrayList<DownItemInfo>());

    private  final  byte[] lock = new byte[0];
    /**
     * 查询下载记录
     * @param url
     * @param filePath
     */
    public DownItemInfo findRecord(String url,String filePath){
        synchronized (lock){
            DownItemInfo downItemInfo = findFromMemry(url,filePath);
            if (downItemInfo != null){
                return downItemInfo;
            }else {
                DownItemInfo where = new DownItemInfo();
                where.setUrl(url);
                where.setFilePath(filePath);
                List<DownItemInfo> downItemInfos = query(where);
                if (downItemInfos.size() > 0){
                    return downItemInfos.get(0);
                }
            }
            return  null;
        }
    }

    /**
     * 通过地址查询记录
     * @param filePath
     * @return
     */
    public List<DownItemInfo> findRecords(String filePath){
        synchronized (lock){
            DownItemInfo where = new DownItemInfo();
            where.setFilePath(filePath);
            List<DownItemInfo> results = super.query(where);
            return results;
        }
    }

    /**
     * 通过地址查找准确下载记录
     * @param filePath
     * @return
     */
    public DownItemInfo findSingleRecord(String filePath){
        synchronized (lock){
            List<DownItemInfo> results = findRecords(filePath);
            if (results.isEmpty()){
                return null;
            }
            return results.get(0);
        }
    }
    /**
     * 根据id查找下载记录对象
     *
     * @param recordId
     * @return
     */
    public DownItemInfo findRecordById(int recordId)
    {
        synchronized (lock)
        {

            DownItemInfo where = new DownItemInfo();
            where.setId(recordId);
            List<DownItemInfo > resultList = super.query(where);
            if (resultList.size() > 0)
            {
                return resultList.get(0);
            }
            return null;
        }

    }

    /**
     * 添加一条下载记录
     * @param url
     * @param filePath
     * @param displayName
     * @param priority
     */
    public void addRecord(String url,String filePath,String displayName,int priority,RequestType requestType){
        synchronized (lock){
            DownItemInfo downItemInfo = new DownItemInfo(url,filePath);
            downItemInfo.setDisplayName(displayName);
            downItemInfo.setPriority(priority);
            downItemInfo.setCurrentLen(0L);
            downItemInfo.setTotalLen(0L);
            downItemInfo.setFinishTime("0");
            java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            downItemInfo.setStartTime(dateFormat.format(new Date()));
            downItemInfo.setStatus(DownloadStatus.waitting.getValue());
            downItemInfo.setHttpTaskType(requestType.getValue());
            downItemInfo.setStopMode(DownloadStopMode.auto.getValue());
            insert(downItemInfo);
            //二级缓存池
            downItemInfoList.add(downItemInfo);
        }
    }

    /**
     * 移除缓存
     * @param url
     * @param filePath
     */
    public void removeFromMemery(String url,String filePath){
        synchronized (lock){
            DownItemInfo itemInfo = findFromMemry(url,filePath);
            if (itemInfo != null){
                downItemInfoList.remove(itemInfo);
            }
        }
    }

    /**
     * 更新缓存数据和数据库记录
     * @param itemInfo
     */
    public void updateRecord(DownItemInfo itemInfo){
        synchronized (lock){
            DownItemInfo where = new DownItemInfo(itemInfo.getUrl(),itemInfo.getFilePath());
            int result = update(itemInfo,where);
            if (result > 0){
                for (int i = 0; i < downItemInfoList.size(); i++)
                {
                    if (downItemInfoList.get(i).getUrl().equals(itemInfo.getUrl()) && downItemInfoList.get(i).getFilePath().equals(itemInfo.getFilePath()))
                    {
                        downItemInfoList.set(i, itemInfo);
                        break;
                    }
                }
            }
        }
    }


    /**
     * 检查二级缓存
     * @param url
     * @param filePath
     * @return
     */
    private DownItemInfo findFromMemry(String url,String filePath){
        for (DownItemInfo downItemInfo:downItemInfoList) {
            if (url.equals(downItemInfo.getUrl()) && filePath.equals(downItemInfo.getFilePath())){
                return downItemInfo;
            }
        }
        return null;
    }


}
