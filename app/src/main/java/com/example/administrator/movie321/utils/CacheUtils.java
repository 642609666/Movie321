package com.example.administrator.movie321.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.administrator.movie321.service.MusicPlayerService;

/**
 * Created by Administrator on 2017/1/11 0011.
 * 缓存工具类
 */

public class CacheUtils {
    /**
     * 得到缓存的文本数据
     *
     * @param mContext
     * @param key
     * @return
     */
    public static String getString(Context mContext, String key) {
        SharedPreferences sp = mContext.getSharedPreferences("ly", Context.MODE_PRIVATE);

        return sp.getString(key, "");
    }

    /**
     * 保存数据
     *
     * @param mContext
     * @param key
     * @param value
     */
    public static void putString(Context mContext, String key, String value) {
        SharedPreferences sp = mContext.getSharedPreferences("ly", Context.MODE_PRIVATE);
        sp.edit().putString(key, value).commit();
    }

    /**
     * 保存播放模式
     *
     * @param key
     * @param playmode
     */
    public static void setPlaymode(Context content, String key, int playmode) {
        SharedPreferences sp = content.getSharedPreferences("playmode", Context.MODE_PRIVATE);
        sp.edit().putInt(key, playmode).commit();
    }

    public static int getPlaymode(Context content, String key) {
        SharedPreferences sp = content.getSharedPreferences("playmode", Context.MODE_PRIVATE);
        return sp.getInt(key, MusicPlayerService.REPEATE_NOMAL);
    }
}
