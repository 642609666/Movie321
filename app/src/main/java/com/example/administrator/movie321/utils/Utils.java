package com.example.administrator.movie321.utils;

import android.app.Activity;
import android.net.TrafficStats;

import java.util.Formatter;
import java.util.Locale;

public class Utils {
    private StringBuilder mFormatBuilder;
    private Formatter mFormatter;

    public Utils() {
        // 转换成字符串的时间
        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());

    }

    /**
     * 把毫秒转换成：1:20:30这里形式
     *
     * @param timeMs
     * @return
     */
    public String stringForTime(int timeMs) {
        int totalSeconds = timeMs / 1000;
        int seconds = totalSeconds % 60;

        int minutes = (totalSeconds / 60) % 60;

        int hours = totalSeconds / 3600;

        mFormatBuilder.setLength(0);
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds)
                    .toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    /**
     * 判断是否为网络数据
     * @param url 网址
     * @return
     */
    public boolean isNetUrl(String url) {
        boolean result = false;
        if (url != null) {

            if (url.toLowerCase().startsWith("http")
                    || url.toLowerCase().startsWith("rtsp")
                    || url.toLowerCase().startsWith("mms")) {
                result = true;
            }
        }
        return result;
    }
    /**
     * 显示网络速度
     *
     * @param context 上下文
     */
    private long lastTotalRxBytes = 0;
    private long lastTimeStamp = 0;
    public String showNetSpeed(Activity context) {
        long nowTotalRxBytes = TrafficStats.getUidRxBytes(context.getApplicationInfo().uid) == TrafficStats.UNSUPPORTED ? 0 : (TrafficStats.getTotalRxBytes() / 1024);//转为KB;
        long nowTimeStamp = System.currentTimeMillis();

        long speed = ((nowTotalRxBytes - lastTotalRxBytes) * 1000 / (nowTimeStamp - lastTimeStamp));//毫秒转换

        lastTimeStamp = nowTimeStamp;
        lastTotalRxBytes = nowTotalRxBytes;

        String netSpeed = String.valueOf(speed) + " kb/s";
        return netSpeed;
    }


}
