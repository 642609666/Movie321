package com.example.administrator.movie321;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.administrator.movie321.base.BaseFragment;
import com.example.administrator.movie321.fragment.LocalMusicFragment;
import com.example.administrator.movie321.fragment.LocalVideoFragment;
import com.example.administrator.movie321.fragment.NetMusicFragment;
import com.example.administrator.movie321.fragment.NetVideoFragment;

import java.util.ArrayList;
import java.util.List;

import fm.jiecao.jcvideoplayer_lib.JCVideoPlayer;

public class MainActivity extends AppCompatActivity {
    private RadioGroup rg_main;
    private List<BaseFragment> data;
    private int total;
    //缓存的碎片
    private Fragment cacheFragment;
    SensorManager sensorManager;
    JCVideoPlayer.JCAutoFullscreenListener sensorEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rg_main = (RadioGroup) findViewById(R.id.rg_main);

        //Android6.0动态获取权限
        isGrantExternalRW(this);

        //添加儿子数据
        initFragment();
        //添加RadioFroup监听方法

        initRadioFroup();
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorEventListener = new JCVideoPlayer.JCAutoFullscreenListener();
    }

    /**
     * 单选按钮的监听方法
     */
    private void initRadioFroup() {
        rg_main.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rb_video:
                        total = 0;
                        break;
                    case R.id.rb_music:
                        total = 1;
                        break;
                    case R.id.rb_netmusic:
                        total = 2;
                        break;
                    case R.id.rb_netvideo:
                        total = 3;
                        break;
                }
                Fragment fragment = data.get(total);
                switchFragment(fragment);
            }
        });
        //默认选中的按钮
        rg_main.check(R.id.rb_video);
    }

    private void switchFragment(Fragment fragment) {
        if (fragment != cacheFragment) {
            //开启事物
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            if (fragment != null) {
                //是否添加过
                if (!fragment.isAdded()) {
                    //之前有显示的隐藏起来
                    if (cacheFragment != null) {
                        ft.hide(cacheFragment);
                    }
                    //添加
                    ft.add(R.id.fl_content, fragment);

                } else {
                    //之前有显示的隐藏起来
                    if (cacheFragment != null) {
                        ft.hide(cacheFragment);
                    }
                    //如果添加了就直接显示
                    ft.show(fragment);
                }
            }
            //提交
            ft.commit();
        }
        cacheFragment = fragment;
    }

    private void initFragment() {
        data = new ArrayList<>();
        data.add(new LocalVideoFragment()); //添加本地视频
        data.add(new LocalMusicFragment());//添加本地音乐
        data.add(new NetMusicFragment());//添加网络音乐
        data.add(new NetVideoFragment());//添加网络视频
    }

    /**
     * 解决安卓6.0以上版本不能读取外部存储权限的问题
     *
     * @param activity
     * @return
     */
    public static boolean isGrantExternalRW(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && activity.checkSelfPermission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            activity.requestPermissions(new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, 1);

            return false;
        }
        return true;
    }

    private boolean isExit = false;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (total != 0) {
                //选中首页
                rg_main.check(R.id.rb_video);
                return true;
            } else if (!isExit) {
                isExit = true;
                Toast.makeText(MainActivity.this, "在按一次退出", Toast.LENGTH_SHORT).show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        isExit = false;
                    }
                }, 2000);
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Sensor accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(sensorEventListener, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(sensorEventListener);
        JCVideoPlayer.releaseAllVideos();
    }


    @Override
    public void onBackPressed() {
        if (JCVideoPlayer.backPress()) {
            return;
        }
        super.onBackPressed();
    }
}
