package com.example.myapplication;

import android.app.Application;

import org.xutils.x;

/**
 * Created by Administrator on 2017/1/10 0010.
 */

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        x.Ext.init(this);
        x.Ext.setDebug(true);//是否输出debug日志
    }
}
