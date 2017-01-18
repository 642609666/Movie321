package com.example.administrator.movie321.fragment;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cjj.MaterialRefreshLayout;
import com.cjj.MaterialRefreshListener;
import com.example.administrator.movie321.R;
import com.example.administrator.movie321.activity.ShowImageAndGifActivity;
import com.example.administrator.movie321.adapter.NetAudioFragmentAdapter;
import com.example.administrator.movie321.base.BaseFragment;
import com.example.administrator.movie321.bean.NetAudioBean;
import com.example.administrator.movie321.utils.CacheUtils;
import com.example.administrator.movie321.utils.Constants;
import com.google.gson.Gson;

import org.xutils.common.Callback;
import org.xutils.common.util.LogUtil;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Administrator on 2017/1/6 0006.
 */

public class NetMusicFragment extends BaseFragment {
    @Bind(R.id.listview)
    ListView listview;
    @Bind(R.id.progressbar)
    ProgressBar progressbar;
    @Bind(R.id.tv_nomedia)
    TextView tvNomedia;
    private NetAudioFragmentAdapter myAdapter;
    List<NetAudioBean.ListBean> datas;

    @Bind(R.id.refreshmusic)
    MaterialRefreshLayout refreshLayout;


    private boolean isLoadMore = false;

    @Override
    public View initView() {
        View view = View.inflate(mContext, R.layout.fragment_net_audio, null);
        ButterKnife.bind(NetMusicFragment.this, view);
        //设置点击事件
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


                NetAudioBean.ListBean listEntity = datas.get(position);
                if (listEntity != null) {
                    //3.传递视频列表
                    Intent intent = new Intent(mContext, ShowImageAndGifActivity.class);
                    if (listEntity.getType().equals("gif")) {
                        String url = listEntity.getGif().getImages().get(0);
                        intent.putExtra("url", url);
                        mContext.startActivity(intent);
                    } else if (listEntity.getType().equals("image")) {
                        String url = listEntity.getImage().getBig().get(0);
                        intent.putExtra("url", url);
                        mContext.startActivity(intent);
                    }
                }


            }
        });
//监听上啦和下拉刷新
        refreshLayout.setMaterialRefreshListener(new MyMaterialRefreshListener());
        return view;
    }

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

    @Override
    public void init() {
        super.init();
        String saveJson = CacheUtils.getString(mContext, Constants.NET_AUDIO_URL);
        if (!TextUtils.isEmpty(saveJson)) {
            processData(saveJson);
        }

        getDataFromNet();
    }

    private void getDataFromNet() {
        RequestParams reques = new RequestParams(Constants.NET_AUDIO_URL);
        x.http().get(reques, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {

                CacheUtils.putString(mContext, Constants.NET_AUDIO_URL, result);
                LogUtil.e("onSuccess==" + result);
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
                LogUtil.e("onError==" + ex.getMessage());
            }

            @Override
            public void onCancelled(CancelledException cex) {
                LogUtil.e("onCancelled==" + cex.getMessage());
            }

            @Override
            public void onFinished() {
                LogUtil.e("onFinished==");
            }
        });
    }

    private void processData(String json) {
        if (!isLoadMore) {
            NetAudioBean netAudioBean = paraseJons(json);
            //Log.e("TAG", "+++++" + netAudioBean.getList().get(0).getText());
            //设置适配器
            datas = netAudioBean.getList();
            if (datas != null && datas.size() > 0) {
                //有视频
                tvNomedia.setVisibility(View.GONE);
                //设置适配器
                myAdapter = new NetAudioFragmentAdapter(mContext, datas);
                listview.setAdapter(myAdapter);
            } else {
                //没有视频
                tvNomedia.setVisibility(View.VISIBLE);
            }
        } else {
            //加载更多,利用假数据实现
            NetAudioBean netAudioBean = paraseJons(json);
            datas.addAll(netAudioBean.getList());
            //刷新适配器
            myAdapter.notifyDataSetChanged();
        }

        progressbar.setVisibility(View.GONE);
    }

    /**
     * json解析数据
     *
     * @param json
     * @return
     */
    private NetAudioBean paraseJons(String json) {
        NetAudioBean netAudioBean = new Gson().fromJson(json, NetAudioBean.class);
        return netAudioBean;
    }

    /**
     * 重写父类的刷新方法,当数据需要刷新时,回调该方法
     */
    @Override
    protected void onRefreshData() {
    }
}
