package com.example.administrator.movie321.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cjj.MaterialRefreshLayout;
import com.cjj.MaterialRefreshListener;
import com.example.administrator.movie321.R;
import com.example.administrator.movie321.SystemVideoPlayerActivity;
import com.example.administrator.movie321.base.BaseFragment;
import com.example.administrator.movie321.bean.LocalVideoBean;
import com.example.administrator.movie321.bean.NetVideoAdapter;
import com.example.administrator.movie321.utils.CacheUtils;
import com.example.administrator.movie321.utils.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/1/6 0006.
 */

public class NetVideoFragment extends BaseFragment {
    @ViewInject(R.id.listview)
    private ListView listview;
    @ViewInject(R.id.tv_nonet)
    private TextView tv_nonet;
    private List<LocalVideoBean> data;
    private NetVideoAdapter adapter;

    @ViewInject(R.id.refresh)
    MaterialRefreshLayout refreshLayout;

    @Override
    public View initView() {
        View view = View.inflate(mContext, R.layout.fragment_net_video, null);
        x.view().inject(NetVideoFragment.this, view);//绑定布局
        //初始化
        listview.setOnItemClickListener(new MyOnItemClickListener());
        //监听上啦和下拉刷新
        refreshLayout.setMaterialRefreshListener(new MyMaterialRefreshListener());
        return view;
    }

    private boolean isLoadMore = false;

    class MyMaterialRefreshListener extends MaterialRefreshListener {

        @Override
        public void onRefresh(MaterialRefreshLayout materialRefreshLayout) {
            // Toast.makeText(mContext, "下拉", Toast.LENGTH_SHORT).show();
            isLoadMore = false;
            getDataFromNet();
        }

        @Override
        public void onRefreshLoadMore(MaterialRefreshLayout materialRefreshLayout) {
            super.onRefreshLoadMore(materialRefreshLayout);
            Toast.makeText(mContext, "上拉", Toast.LENGTH_SHORT).show();
            isLoadMore = true;
            getDataFromNet();
        }
    }

    class MyOnItemClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent intent = new Intent(mContext, SystemVideoPlayerActivity.class);

            Bundle bundle = new Bundle();
            //列表数据
            bundle.putSerializable("videolist", (Serializable) data);
            intent.putExtras(bundle);
            //传递点击的位置
            intent.putExtra("position", position);
            startActivity(intent);
        }
    }

    // private ProgressDialog pd;

    @Override
    public void init() {
        super.init();
//        pd = new ProgressDialog(mContext);
//        pd.setTitle("请稍后....正在加载");
//        pd.show();
       String json = CacheUtils.getString(mContext, Constants.NET_URL);
       //判断json是不是为空数据
        if(!TextUtils.isEmpty(json)) {
           processData(json);
       }
        getDataFromNet();
    }

    /**
     * XUTLIS3联网请求数据
     */
    private void getDataFromNet() {
        //网络的路径
        RequestParams params = new RequestParams(Constants.NET_URL);
        x.http().get(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                //Log.e("TAG", "result === " + result);
                CacheUtils.putString(mContext, Constants.NET_URL,result);

                processData(result);

                if (!isLoadMore) {
                    //完成刷新
                    refreshLayout.finishRefresh();
                } else {
                    //把上拉的隐藏
                    refreshLayout.finishRefreshLoadMore();
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                Log.e("TAG", "ex === " + ex);
            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {

            }
        });
    }

    /**
     * 解析json数据 :gson解析 fastjson解析和手动解析(原声的api)
     * 显示数据 --- 设置适配器
     *
     * @param result
     */
    private void processData(String result) {
        if (!isLoadMore) {
            data = parsedJson(result);
            if (data != null && data.size() > 0) {
                tv_nonet.setVisibility(View.GONE);
                adapter = new NetVideoAdapter(data, mContext);
                listview.setAdapter(adapter);
            } else {
                tv_nonet.setVisibility(View.VISIBLE);
            }
        } else {
            //加载更多,利用假数据实现
            List<LocalVideoBean> localVideoBeen = parsedJson(result);
            data.addAll(localVideoBeen);
            //刷新适配器
            adapter.notifyDataSetChanged();
        }
    }

    private List<LocalVideoBean> parsedJson(String result) {
        List<LocalVideoBean> list = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(result);
            JSONArray jsonArray = jsonObject.getJSONArray("trailers");

            for (int i = 0; i < jsonArray.length(); i++) {
                LocalVideoBean bean = new LocalVideoBean();
                list.add(bean);//添加到集合中

                JSONObject jsonObject1 = (JSONObject) jsonArray.get(i);
                String name = jsonObject1.optString("movieName");
                bean.setName(name);
                String desc = jsonObject1.optString("videoTitle");
                bean.setDesc(desc);
                String url = jsonObject1.optString("url");
                bean.setData(url);
                String hiUrl = jsonObject1.optString("hightUrl");
                bean.setHiIimageUrl(hiUrl);
                String imageUrl = jsonObject1.optString("coverImg");
                bean.setImageUrl(imageUrl);
                int videoLength = jsonObject1.optInt("videoLength");
                bean.setDuration(videoLength);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 重写父类的刷新方法,当数据需要刷新时,回调该方法
     */
    @Override
    protected void onRefreshData() {

    }
}
