package com.example.administrator.okhttplemon;

import android.Manifest;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.example.lemonlibrary.db.util.PermissionUtils;
import com.example.lemonokhttp.enums.DownloadStatus;
import com.example.lemonokhttp.http.OkHttpLemon;
import com.example.lemonokhttp.interfaces.IDataListener;
import com.example.lemonokhttp.interfaces.IDownloadCallback;

public class MainActivity extends AppCompatActivity {
    public  static  final String url="http://v.juhe.cn/toutiao/index?type=top&key=29da5e8be9ba88b932394b7261092f71";

    public String tabaoUrl = "http://download.apk8.com/soft/2015/%E6%B7%98%E5%AE%9D.apk";
    public String wpsUrl = "http://gdown.baidu.com/data/wisegame/8be18d2c0dc8a9c9/WPSOffice_177.apk";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PermissionUtils.getInstance().requestPermission(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionUtils.getInstance().onRequestPermissionsResult(requestCode,permissions,grantResults,this);
    }

    public void click(View view){
        switch (view.getId()){
            case R.id.btn1:
                Log.i("tag00","下载wps：");
                OkHttpLemon.init()
                        .url(wpsUrl)
                        .downFilePath(Environment.getExternalStorageDirectory() + "/wps.apk")
                        .executeDown(new IDownloadCallback() {
                            @Override
                            public void onDownTotalLength(long totalLen) {

                            }

                            @Override
                            public void onDownCurrentLenChange(long alreadyDownLen, double downPercent, long speed) {
                                Log.i("tag00","-----已下载  "+ alreadyDownLen/1024/1024+"M  下载长度  "+downPercent*100 +"%   "+"下载速度："+ speed/1000 +"k/s");
                            }

                            @Override
                            public void onFinish(int downId) {
                                Log.i("tag00","下载成功。");
                            }

                            @Override
                            public void onEorror(int errorCode, String ts) {
                                Log.i("tag00","下载异常："+"  errorCode：  " + errorCode +" errorMsg " +ts);

                            }

                            @Override
                            public void onDownStatusChange(DownloadStatus downStatus) {
                                Log.i("tag00","状态变更："+downStatus);
                            }
                        });

                break;
            case R.id.btn2:
                Log.i("tag00","下载game：");
                OkHttpLemon.init()
                        .url(tabaoUrl)
                        .downFilePath(Environment.getExternalStorageDirectory() + "/taobao.apk")
                        .executeDown(new IDownloadCallback() {
                            @Override
                            public void onDownTotalLength(long totalLen) {

                            }

                            @Override
                            public void onDownCurrentLenChange(long alreadyDownLen, double downPercent, long speed) {
                                Log.i("tag00","-----已下载  "+ alreadyDownLen/1024/1024+"M  下载长度  "+downPercent*100 +"%   "+"下载速度："+ speed/1000 +"k/s");
                            }

                            @Override
                            public void onFinish(int downId) {
                                Log.i("tag00","下载成功。");
                            }

                            @Override
                            public void onEorror(int errorCode, String ts) {
                                Log.i("tag00","下载异常："+"  errorCode：  " + errorCode +" errorMsg " +ts);

                            }

                            @Override
                            public void onDownStatusChange(DownloadStatus downStatus) {
                                Log.i("tag00","状态变更："+downStatus);
                            }
                        });
                break;
            case R.id.btn3:
                OkHttpLemon.init().pause(wpsUrl);
                break;
            case R.id.btn5:
                OkHttpLemon.init().start(wpsUrl);
                break;
            case R.id.btn4:
                OkHttpLemon.init().pause(tabaoUrl);
                break;
            case R.id.btn6:
                OkHttpLemon.init().start(tabaoUrl);
                break;
            case R.id.btn7:
                OkHttpLemon.init().url("http://v3.wufazhuce.com:8000/api/hp/idlist/0")
                        .get(MainPageBean.class)
                        .execute(new IDataListener<MainPageBean>() {
                            @Override
                            public void onSuccess(MainPageBean s) {
                                Log.i("tag00",s.getRes()+"");
                                for (String s1:s.getData()) {
                                    Log.i("tag00",s1);
                                }
                            }

                            @Override
                            public void onError(int code,String ts) {

                            }
                        });
                break;
            case R.id.btn8:
                OkHttpLemon.init().url("http://v3.wufazhuce.com:8000/api/praise/add")
                        .postString("itemid","1644")
                        .postString("type","hpcontent")
                        .postString("deviceid","ffffffff-b821-e83f-45c3-423b5c7ea996")
                        .postString("version","3.5.0")
                        .postString("devicetype","android")
                        .postString("platform","android")
                        .executes(new IDataListener<String>() {
                            @Override
                            public void onSuccess(String s) {
                                Log.i("tag00",s);
                            }

                            @Override
                            public void onError(int code, String ts) {
                                Log.i("tag00","code:"+code + "提示："+ts);
                            }
                        });
                break;
        }

    }
}
