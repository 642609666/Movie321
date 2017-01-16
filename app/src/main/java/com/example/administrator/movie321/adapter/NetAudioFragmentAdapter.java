package com.example.administrator.movie321.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.administrator.movie321.R;
import com.example.administrator.movie321.bean.NetAudioBean;

import java.util.List;

/**
 * Created by Administrator on 2017/1/16 0016.
 */
public class NetAudioFragmentAdapter extends BaseAdapter {
    private final Context mContext;
    private final List<NetAudioBean.ListBean> datas;

    public NetAudioFragmentAdapter(Context mContext, List<NetAudioBean.ListBean> datas) {
        this.mContext = mContext;
        this.datas = datas;
    }

    /**
     * 视频
     */
    private static final int TYPE_VIDEO = 0;
    /**
     * 图片
     */
    private static final int TYPE_IMAGE = 1;
    /**
     * 文字
     */
    private static final int TYPE_TEXT = 2;
    /**
     * GIF图片
     */
    private static final int TYPE_GIF = 3;
    /**
     * 软件推广
     */
    private static final int TYPE_AD = 4;
    private int currentType;

    /**
     * 返回总数量
     *
     * @return
     */
    //返回总类型数据
    @Override
    public int getViewTypeCount() {
        return 5;
    }

    /**
     * 当前item是什么类型
     *
     * @param position
     * @return
     */
    @Override
    public int getItemViewType(int position) {
        //根据位置，从列表中得到一个数据对象
        NetAudioBean.ListBean listBean = datas.get(position);
        String type = listBean.getType();//得到类型
        if ("video".equals(type)) {
            currentType = TYPE_VIDEO;
        } else if ("image".equals(type)) {
            currentType = TYPE_IMAGE;
        } else if ("text".equals(type)) {
            currentType = TYPE_TEXT;
        } else if ("gif".equals(type)) {
            currentType = TYPE_GIF;
        } else {
            currentType = TYPE_AD;//广播
        }
        return currentType;
    }

    @Override
    public int getCount() {
        return datas.size();
    }

    @Override
    public Object getItem(int position) {
        return datas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = initView(convertView, getItemViewType(position), datas.get(position));
        return convertView;
    }

    /**
     *
     * @param convertView
     * @param itemViewType 类型
     * @param mediaItem  数据
     * @return
     */
    private View initView(View convertView, int itemViewType, NetAudioBean.ListBean mediaItem) {
        switch (itemViewType) {
            case TYPE_VIDEO://视频

                VideoHoder videoHoder;
                if (convertView == null) {
                    convertView = View.inflate(mContext, R.layout.all_video_item, null);
                    videoHoder = new VideoHoder(convertView);
                    convertView.setTag(videoHoder);
                } else {
                    videoHoder = (VideoHoder) convertView.getTag();
                }

                //设置数据
                videoHoder.setData(mediaItem);

                break;
            case TYPE_IMAGE://图片
                ImageHolder imageHolder;
                if (convertView == null) {
                    convertView = View.inflate(mContext, R.layout.all_image_item, null);
                    imageHolder = new ImageHolder(convertView);
                    convertView.setTag(imageHolder);
                } else {
                    imageHolder = (ImageHolder) convertView.getTag();
                }
                //设置数据
                imageHolder.setData(mediaItem);
                break;
            case TYPE_TEXT://文字

                TextHolder textHolder;
                if (convertView == null) {
                    convertView = View.inflate(mContext, R.layout.all_text_item, null);
                    textHolder = new TextHolder(convertView);

                    convertView.setTag(textHolder);
                } else {
                    textHolder = (TextHolder) convertView.getTag();
                }

                textHolder.setData(mediaItem);

                break;
            case TYPE_GIF://gif

                GifHolder gifHolder;
                if (convertView == null) {
                    convertView = View.inflate(mContext, R.layout.all_gif_item, null);
                    gifHolder = new GifHolder(convertView);

                    convertView.setTag(gifHolder);
                } else {
                    gifHolder = (GifHolder) convertView.getTag();
                }

                gifHolder.setData(mediaItem);

                break;
            case TYPE_AD://软件广告

                ADHolder adHolder;
                if (convertView == null) {
                    convertView = View.inflate(mContext, R.layout.all_ad_item, null);
                    adHolder = new ADHolder(convertView);
                    convertView.setTag(adHolder);
                } else {
                    adHolder = (ADHolder) convertView.getTag();
                }

                break;
        }
        return convertView;
    }

    class VideoHoder {
        private TextView textView;

        public VideoHoder(View convertView) {
            textView = (TextView) convertView.findViewById(R.id.textView);
        }

        public void setData(NetAudioBean.ListBean mediaItem) {
            textView.setText("我是视频的内容");
        }
    }

    class ImageHolder {
        private TextView textView;

        public ImageHolder(View convertView) {
            textView = (TextView) convertView.findViewById(R.id.textView);
        }

        public void setData(NetAudioBean.ListBean mediaItem) {
            textView.setText("我是图片的内容");
        }
    }

    class TextHolder {
        private TextView textView;

        public TextHolder(View convertView) {
            textView = (TextView) convertView.findViewById(R.id.textView);
        }

        public void setData(NetAudioBean.ListBean mediaItem) {
//            textView.setText("我是文本的内容");
        }
    }

    class GifHolder {
        private TextView textView;

        public GifHolder(View convertView) {
            textView = (TextView) convertView.findViewById(R.id.textView);
        }

        public void setData(NetAudioBean.ListBean mediaItem) {
            textView.setText("我是GIF的内容");
        }
    }

    class ADHolder {
        private TextView textView;

        public ADHolder(View convertView) {
            textView = (TextView) convertView.findViewById(R.id.textView);
        }

        public void setData(NetAudioBean.ListBean mediaItem) {
            textView.setText("我是广告的内容");
        }
    }
}
