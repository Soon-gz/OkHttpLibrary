package com.example.lemonokhttp.http;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by ShuWen on 2017/2/26.
 */

public class ThreadPoolManager {

    private static final ThreadPoolManager instance = new ThreadPoolManager();
    private LinkedBlockingQueue<Future<?>> taskQueue = new LinkedBlockingQueue<>();
    private ThreadPoolExecutor poolExecutor;


    public static ThreadPoolManager getInstance(){
        return instance;
    }

    private ThreadPoolManager() {
        /*
      拒绝策略
     */
        RejectedExecutionHandler rejectedHandler = new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                try {
                    taskQueue.put(new FutureTask<Object>(r, null));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        poolExecutor = new ThreadPoolExecutor(4,10,10, TimeUnit.SECONDS,new ArrayBlockingQueue<Runnable>(4), rejectedHandler);
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                while (true) {
                    FutureTask futureTask = null;
                    try {
                        futureTask = (FutureTask) taskQueue.take();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    if (futureTask != null) {
                        poolExecutor.execute(futureTask);
                    }
                }
            }
        };
        poolExecutor.execute(runnable);
    }

    public <T> boolean removeTask(FutureTask futureTask)
    {
        boolean result=false;
        /**
         * 阻塞式队列是否含有线程
         */
        if(taskQueue.contains(futureTask))
        {
            taskQueue.remove(futureTask);
        }else
        {
            result=poolExecutor.remove(futureTask);
        }
        return  result;
    }

    public void execute(FutureTask futureTask) throws InterruptedException {
        taskQueue.put(futureTask);
    }

}
