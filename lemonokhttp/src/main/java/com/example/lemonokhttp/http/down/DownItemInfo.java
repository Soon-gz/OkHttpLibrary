package com.example.lemonokhttp.http.down;

import com.example.lemonlibrary.db.annotion.DbField;
import com.example.lemonlibrary.db.annotion.DbPrimaryField;
import com.example.lemonlibrary.db.annotion.DbTable;
import com.example.lemonokhttp.http.OkHttpTask;

/**
 * Created by ShuWen on 2017/3/1.
 */

@DbTable("t_downloadInfo")
public class DownItemInfo extends BaseEntity<DownItemInfo> {
    public  transient  OkHttpTask httpTask;
    /**
     * 下载id
     */
    @DbPrimaryField(value = "id")
    public Integer id;

    /**
     * 下载url
     */
    @DbField(value = "url")
    public String url;

    /**
     * 下载存储的文件路径
     */
    @DbField(value = "filePath")
    public String filePath;

    /**
     * 下载文件显示名
     */
    @DbField(value = "displayName")
    public String displayName;
    /**
     * 下载文件总大小
     */
    @DbField(value = "totalLen")
    public Long totalLen;

    /**
     * 下载文件当前大小
     */
    @DbField(value = "currentLen")
    public Long currentLen;

    /**
     * 下载开始时间
     */
    @DbField(value = "startTime")
    public String startTime;

    /**
     * 下载结束时间
     */
    @DbField(value = "finishTime")
    public String finishTime;

    /**
     * 用户id
     */
    @DbField(value = "userId")
    public String userId;

    /**
     * 下载任务类型
     */
    @DbField(value = "httpTaskType")
    public Integer httpTaskType;

    /**
     * 下载优先级
     */
    @DbField(value = "priority")
    public Integer priority;

    /**
     * 下载停止模式
     */
    @DbField(value = "stopMode")
    public Integer stopMode;


    //下载的状态
    @DbField(value = "status")
    public Integer status;

    public DownItemInfo(String url, String filePath) {
        this.url = url;
        this.filePath = filePath;
    }

    public DownItemInfo() {
    }

    public OkHttpTask getHttpTask() {
        return httpTask;
    }

    public void setHttpTask(OkHttpTask httpTask) {
        this.httpTask = httpTask;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Long getTotalLen() {
        return totalLen;
    }

    public void setTotalLen(Long totalLen) {
        this.totalLen = totalLen;
    }

    public Long getCurrentLen() {
        return currentLen;
    }

    public void setCurrentLen(Long currentLen) {
        this.currentLen = currentLen;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(String finishTime) {
        this.finishTime = finishTime;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Integer getHttpTaskType() {
        return httpTaskType;
    }

    public void setHttpTaskType(Integer httpTaskType) {
        this.httpTaskType = httpTaskType;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Integer getStopMode() {
        return stopMode;
    }

    public void setStopMode(Integer stopMode) {
        this.stopMode = stopMode;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
