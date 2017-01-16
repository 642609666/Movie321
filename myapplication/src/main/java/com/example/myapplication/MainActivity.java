package com.example.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

public class MainActivity extends AppCompatActivity {
    private final String stt = "http://pan.baidu.com/play/video#video/path=/天才.mp4";
    private EditText et_net;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        et_net = (EditText)findViewById(R.id.et_net);
        et_net.setText(stt);
    }

    public void on(View view) {
        String srt = et_net.getText().toString();
        initData(srt);
        Intent intent = new Intent();
        //            //第一参数：播放路径
        //            //第二参数：路径对应的类型
        intent.setDataAndType(Uri.parse(srt), "video/*");
        startActivity(intent);
    }

    private void initData(String srt) {
        RequestParams params = new RequestParams(srt);
        x.http().get(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.e("TAG", "result === " + result);
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
}
