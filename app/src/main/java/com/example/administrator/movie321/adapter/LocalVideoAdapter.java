package com.example.administrator.movie321.adapter;

import android.content.Context;
import android.text.format.Formatter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.administrator.movie321.R;
import com.example.administrator.movie321.bean.LocalVideoBean;
import com.example.administrator.movie321.utils.Utils;

import java.util.List;

/**
 * Created by Administrator on 2017/1/8 0008.
 */

public class LocalVideoAdapter extends BaseAdapter {
    private final boolean isVideo;
    private List<LocalVideoBean> data;
    private Context mContent;
    private Utils utils = new Utils();

    public LocalVideoAdapter(List<LocalVideoBean> data, Context mcontent, boolean b) {
        this.data = data;
        this.mContent = mcontent;
        this.isVideo = b;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MyHold myHold;
        if (convertView == null) {
            convertView = View.inflate(mContent, R.layout.activity_localviewlist, null);
            myHold = new MyHold();
            myHold.name = (TextView) convertView.findViewById(R.id.tv_name);
            myHold.duration = (TextView) convertView.findViewById(R.id.tv_time);
            myHold.size = (TextView) convertView.findViewById(R.id.tv_size);
            myHold.iv_icon = (ImageView) convertView.findViewById(R.id.iv_image);
            convertView.setTag(myHold);
        } else {
            myHold = (MyHold) convertView.getTag();
        }
        LocalVideoBean bean = data.get(position);
        myHold.name.setText(bean.getName());
        //设置文件的大小
        myHold.size.setText((Formatter.formatFileSize(mContent, bean.getSize())));
        //设置时间
        myHold.duration.setText(utils.stringForTime((int) bean.getDuration()));
        if (!isVideo) {
            myHold.iv_icon.setImageResource(R.drawable.music_default_bg);
        }

        return convertView;
    }

    public Context getmContent() {
        return mContent;
    }

    class MyHold {
        TextView name; //电影名字
        TextView duration; //电影时间
        TextView size;   //电影大小
        TextView data;  //电影路径
        ImageView iv_icon;//电影图片
    }
}
