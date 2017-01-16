package com.example.administrator.movie321.app;

import android.app.Application;

import org.xutils.x;

/**
 * Created by Administrator on 2017/1/10 0010.
 */

/**
 *  <uses-permission android:name="android.permission.READ_PHONE_STATE" />

 <application
 android:allowBackup="true"
 android:icon="@mipmap/ic_launcher"
 android:label="@string/app_name"

 android:name=".app.MyApplication"

 android:supportsRtl="true"
 android:theme="@style/AppTheme">
 */
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        //把类放入清单文件中  最先初始化
        //初始化xutils
        x.Ext.init(this);
        x.Ext.setDebug(true);//是否输出debug日志
    }
}
