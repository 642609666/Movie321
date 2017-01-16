package com.example.administrator.movie321.fragment;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.administrator.movie321.R;
import com.example.administrator.movie321.SystemMusicPlayerActivity;
import com.example.administrator.movie321.base.BaseFragment;
import com.example.administrator.movie321.bean.LocalVideoAdapter;
import com.example.administrator.movie321.bean.LocalVideoBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/1/6 0006.
 */

public class LocalMusicFragment extends BaseFragment {
    private TextView text;
    private List<LocalVideoBean> data;
    private ListView lv_local_video_list;
    private TextView tv_local_video_content;
    private LocalVideoAdapter adapter;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (data != null && data.size() > 0) {
                //文本隐藏
                tv_local_video_content.setVisibility(View.GONE);
                //有数据,设置适配器
                adapter = new LocalVideoAdapter(data, mContext, false);
                lv_local_video_list.setAdapter(adapter);
            } else {
                //没有数据显示文本
                tv_local_video_content.setVisibility(View.VISIBLE);
            }
        }
    };

    //初始化UI
    @Override
    public View initView() {
        View view = View.inflate(getContext(), R.layout.fragment_local_video, null);
        lv_local_video_list = (ListView) view.findViewById(R.id.lv_local_video_list);
        tv_local_video_content = (TextView) view.findViewById(R.id.tv_local_video_content);
        lv_local_video_list.setOnItemClickListener(new LocalMusicFragment.MyOnItemClickListener());
        return view;
    }

    class MyOnItemClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent intent = new Intent(mContext, SystemMusicPlayerActivity.class);

            //传递点击的位置
            intent.putExtra("position", position);
            startActivity(intent);
        }
    }

    //初始化数据
    @Override
    public void init() {
        super.init();
        getDataFromLocal();
    }

    /**
     * 子线程中得到视频数据
     */
    private void getDataFromLocal() {
        new Thread() {
            public void run() {
                //初始化数据
                data = new ArrayList<LocalVideoBean>();
                //访问内置的数据库
                ContentResolver resolver = mContext.getContentResolver();
                //获取SD卡的视频路径
                Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                String[] objs = {
                        MediaStore.Audio.Media.DISPLAY_NAME,//在sdcard显示的视频名称
                        MediaStore.Audio.Media.DURATION,//视频的时长,毫秒
                        MediaStore.Audio.Media.SIZE,//文件大小-byte
                        MediaStore.Audio.Media.DATA,//在sdcard的路径-播放地址
                        MediaStore.Audio.Media.ARTIST//艺术家
                };
                //设置游标,读取数据库
                Cursor cusor = resolver.query(uri, objs, null, null, null);
                if (cusor != null) {
                    while (cusor.moveToNext()) {
                        LocalVideoBean lvb = new LocalVideoBean(cusor.getString(0), cusor.getLong(1),
                                cusor.getLong(2), cusor.getString(3), cusor.getString(4));
                        data.add(lvb);
                    }
                    //关闭游标
                    cusor.close();
                }
                //发送消息到主线程
                handler.sendEmptyMessage(1);
            }
        }.start();
    }

    /**
     * 重写父类的刷新方法,当数据需要刷新时,回调该方法
     */
    @Override
    protected void onRefreshData() {
    }
}
