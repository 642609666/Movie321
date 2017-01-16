package com.example.administrator.movie321.bean;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.administrator.movie321.R;
import com.example.administrator.movie321.utils.Utils;

import org.xutils.common.util.DensityUtil;
import org.xutils.image.ImageOptions;

import java.util.List;

/**
 * Created by Administrator on 2017/1/8 0008.
 */

public class NetVideoAdapter extends BaseAdapter {
    private List<LocalVideoBean> data;
    private Context mContent;
    private Utils utils = new Utils();
    private ImageOptions imageOptions;

    public NetVideoAdapter(List<LocalVideoBean> data, Context mcontent) {
        this.data = data;
        this.mContent = mcontent;

        imageOptions = new ImageOptions.Builder()
                .setSize(DensityUtil.dip2px(120), DensityUtil.dip2px(120))
                .setRadius(DensityUtil.dip2px(5))
                // 如果ImageView的大小不是定义为wrap_content, 不要crop.
                .setCrop(true) // 很多时候设置了合适的scaleType也不需要它.
                // 加载中或错误图片的ScaleType
                //.setPlaceholderScaleType(ImageView.ScaleType.MATRIX)
                .setImageScaleType(ImageView.ScaleType.CENTER_CROP)
                .setLoadingDrawableId(R.drawable.video_default)//加载过程中的默认图片
                .setFailureDrawableId(R.drawable.video_default)//就挨着出错的图片
                .build();
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
            convertView = View.inflate(mContent, R.layout.activity_netviewlist, null);
            myHold = new MyHold();
            myHold.name = (TextView) convertView.findViewById(R.id.tv_name);
            myHold.duration = (TextView) convertView.findViewById(R.id.tv_recommend);
            myHold.size = (TextView) convertView.findViewById(R.id.tv_size);
            myHold.iv_icon = (ImageView) convertView.findViewById(R.id.iv_image);
            convertView.setTag(myHold);
        } else {
            myHold = (MyHold) convertView.getTag();
        }
        LocalVideoBean bean = data.get(position);
        myHold.name.setText(bean.getName());
        //设置电影多久
        myHold.size.setText(bean.getDuration() + "秒");
        //设置描述
        myHold.duration.setText(bean.getDesc());

        //使用Xutlis3请求图片
        //x.image().bind(myHold.iv_icon, bean.getImageUrl());


        //使用picasso请求图片
//        Picasso.with(mContent)
//                .load(bean.getImageUrl())
//                .placeholder(R.drawable.video_default)
//                .error(R.drawable.video_default)
//                .into(myHold.iv_icon);


        //使用Glide请求图片
        Glide.with(mContent)
                .load(bean.getImageUrl())
                .placeholder(R.drawable.video_default)
                .error(R.drawable.video_default)
                .into(myHold.iv_icon);

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
