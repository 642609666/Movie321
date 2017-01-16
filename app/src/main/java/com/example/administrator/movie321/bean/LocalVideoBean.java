package com.example.administrator.movie321.bean;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/1/8 0008.
 */

public class LocalVideoBean implements Serializable {
    private String name; //电影名字
    private long duration; //电影时间
    private long size;   //电影大小
    private String data;  //电影路径
    private String artist; //电影艺术家
    private String imageUrl; //图片路径
    private String hiIimageUrl; //高清图片路径
    private String desc; //描述信息

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getHiIimageUrl() {
        return hiIimageUrl;
    }

    public void setHiIimageUrl(String hiIimageUrl) {
        this.hiIimageUrl = hiIimageUrl;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public LocalVideoBean(String name, long duration, long size, String data, String artist) {
        this.name = name;
        this.duration = duration;
        this.size = size;
        this.data = data;
        this.artist = artist;
    }

    @Override
    public String toString() {
        return "LocalVideoBean{" +
                "name='" + name + '\'' +
                ", duration=" + duration +
                ", size=" + size +
                ", data='" + data + '\'' +
                ", artist='" + artist + '\'' +
                '}';
    }

    public LocalVideoBean() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }
}
