package com.example.administrator.movie321.base;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Administrator on 2017/1/6 0006.
 */

public abstract class BaseFragment extends Fragment {


    public Context mContext;

    /**
     * 当系统创建当前BaseFragment类的时候回调
     *
     * @param savedInstanceState
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
    }

    /**
     * 当系统要创建Fragment的视图的时候回调这个方法
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return initView();
    }

    /**
     * 让子类去实现自己的界面
     *
     * @return
     */
    public abstract View initView();

    /**
     * 当Activty创建成功的时候回调该方法
     * 初始化数据：
     * 联网请求数据
     * 绑定数据
     *
     * @param savedInstanceState
     */
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        init();
    }

    /**
     * 1.联网请求网络，的时候重写该方法
     * 2.绑定数据
     */
    public void init() {
    }

    /**
     * 刷新的时候回调该方法
     *
     * @param hidden false  当前类隐藏
     *               true   当前类显示
     */
    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            onRefreshData();
        }
    }

    /**
     * 子类刷新时调用该方法
     */
    protected abstract void onRefreshData();
}
