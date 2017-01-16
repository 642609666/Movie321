package com.example.administrator.movie321;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.movie321.bean.LocalVideoBean;
import com.example.administrator.movie321.utils.Utils;
import com.example.administrator.movie321.view.VideoView;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class SystemVideoPlayerActivity extends Activity implements View.OnClickListener {
    /**
     * 视频默认屏幕大小播放
     */
    private static final int VIDEO_TYPE_DEFAULT = 1;
    /**
     * 视频全屏播放
     */
    private static final int VIDEO_TYPE_FULL = 2;
    /**
     * 视频是否全屏显示
     */
    private float startX;
    private boolean isFullScreen = false;
    private int screenWidth = 0;
    private int screenHeight = 0;
    private int videoWidth = 0;
    private int videoHeight = 0;
    /**
     * 音频管理者
     */
    private AudioManager am;
    /**
     * 当前音量
     */
    private int currentVolume;
    /**
     * 最大音量
     */
    private int maxVolume;
    /**
     * 是否静音
     */
    private boolean isMute = false;
    /**
     * 进度跟新
     */
    private static final int PROGRESS = 0;
    /**
     * 影藏控制面板
     */
    private static final int HIDE_MEDIA_CONTROLLER = 1;
    /**
     * 显示网络速度
     */
    private static final int SHOW_NET_SPEED = 2;
    /**
     * isShowMediaController 是否显示控制面板
     * detector 手势识别器
     */
    private GestureDetector detector;
    private boolean isShowMediaController = false;

    private TextView tv_top_time;
    private LinearLayout llTop;
    private TextView tvGame;
    private ImageView ivBattery;
    private Button btnVoice;
    private SeekBar seekbarVoice;
    private Button btnSwichePlayer;
    private LinearLayout llBottom;
    private TextView tvCurrentTime;
    private SeekBar seekbarVideo;
    private TextView tvTime;
    private Button btnBack;
    private Button btnLast;
    private Button btnPlayPause;
    private Button btnNext;
    private Button btnFullDefault;
    private Utils utils;
    private MyBroadcastReceiver receiver;
    /**
     * 加载视频loading
     */
    private TextView tv_loading;
    private LinearLayout ll_loading;
    /**
     * 是否是网络视频
     */
    private boolean isNetUri;
    /**
     * 缓存时
     */
    private LinearLayout ll_buffer;
    private TextView tv_buffer;
    private int prePosition;
    /**
     * 视频列表数据
     */
    private List<LocalVideoBean> data;
    private int position;
    /**
     * 振动
     */
    private Vibrator vibrator;

    private void findViews() {
        setContentView(R.layout.activity_system_video_player);
        vv_video = (VideoView) findViewById(R.id.vv_video);
        llTop = (LinearLayout) findViewById(R.id.ll_top);
        tvGame = (TextView) findViewById(R.id.tv_game);
        ivBattery = (ImageView) findViewById(R.id.iv_battery);
        btnVoice = (Button) findViewById(R.id.btn_voice);
        seekbarVoice = (SeekBar) findViewById(R.id.seekbar_voice);
        btnSwichePlayer = (Button) findViewById(R.id.btn_swiche_player);
        llBottom = (LinearLayout) findViewById(R.id.ll_bottom);
        tvCurrentTime = (TextView) findViewById(R.id.tv_current_time);
        seekbarVideo = (SeekBar) findViewById(R.id.seekbar_time);
        tvTime = (TextView) findViewById(R.id.tv_time);
        btnBack = (Button) findViewById(R.id.btn_back);
        btnLast = (Button) findViewById(R.id.btn_last);
        btnPlayPause = (Button) findViewById(R.id.btn_play_pause);
        btnNext = (Button) findViewById(R.id.btn_next);
        btnFullDefault = (Button) findViewById(R.id.btn_full_default);
        tv_top_time = (TextView) findViewById(R.id.tv_top_time);
        tv_loading = (TextView) findViewById(R.id.tv_loading);
        ll_loading = (LinearLayout) findViewById(R.id.ll_loading);
        ll_buffer = (LinearLayout) findViewById(R.id.ll_buffer);
        tv_buffer = (TextView) findViewById(R.id.tv_buffer);

        btnVoice.setOnClickListener(this);
        btnSwichePlayer.setOnClickListener(this);
        btnBack.setOnClickListener(this);
        btnLast.setOnClickListener(this);
        btnPlayPause.setOnClickListener(this);
        btnNext.setOnClickListener(this);
        btnFullDefault.setOnClickListener(this);

        hideMediaController();//隐藏控制面板

        //获取音频的最大值15,当前值
        am = (AudioManager) getSystemService(AUDIO_SERVICE);
        currentVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        maxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        //和SeekBar关联

        seekbarVoice.setMax(maxVolume);
        seekbarVoice.setProgress(currentVolume);

        //发消息,设置网络速度
        handler.sendEmptyMessage(SHOW_NET_SPEED);
    }

    /**
     * 隐藏控制面板
     */
    private void hideMediaController() {
        isShowMediaController = false;
        llTop.setVisibility(View.GONE);
        llBottom.setVisibility(View.GONE);
    }

    /**
     * 显示控制面板
     */
    private void showMediaController() {
        isShowMediaController = true;
        llTop.setVisibility(View.VISIBLE);
        llBottom.setVisibility(View.VISIBLE);
    }

    private VideoView vv_video;
    //视频播放的地址
    private Uri uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();
        //初始化所有控件
        findViews();
        //获得播放地址
        getUri();
        //设置视频加载的监听
        setLinstener();
        setData();
    }

    private void initData() {
        utils = new Utils();
        //动态注册广播 -----电量广播
        receiver = new MyBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        //监听电量变化
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(receiver, filter);
        //初始化手势识别器
        detector = new GestureDetector(this, new MyOnGestureListener());

        //得到屏幕的宽和高
        DisplayMetrics metrics = new DisplayMetrics();
        //得到屏幕参数类
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        //屏幕的宽和高

        screenWidth = metrics.widthPixels;
        screenHeight = metrics.heightPixels;
    }

    /**
     * 手势识别器
     */
    class MyOnGestureListener extends GestureDetector.SimpleOnGestureListener {
        /**
         * 长按监听
         *
         * @param e
         */
        @Override
        public void onLongPress(MotionEvent e) {
            super.onLongPress(e);
            playAndPause();
        }

        /**
         * 双击
         *
         * @param e
         * @return
         */
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if (isFullScreen) {
                //设置默认
                setVideoType(VIDEO_TYPE_DEFAULT);
            } else {
                //全屏显示
                setVideoType(VIDEO_TYPE_FULL);
            }
            return super.onDoubleTap(e);
        }

        /**
         * 单击
         *
         * @param e
         * @return
         */
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (isShowMediaController) {
                //隐藏
                hideMediaController();
                //把消息移除
                handler.removeMessages(HIDE_MEDIA_CONTROLLER);
            } else {
                //显示
                showMediaController();
                //重新发消息----  4秒隐藏
                handler.sendEmptyMessageDelayed(HIDE_MEDIA_CONTROLLER, 4000);
            }
            return super.onSingleTapConfirmed(e);
        }
    }

    private void setVideoType(int videoTypeDefault) {
        switch (videoTypeDefault) {
            case VIDEO_TYPE_FULL:
                isFullScreen = true;
                vv_video.setViewSize(screenWidth, screenHeight);
                //把按钮设置--默认
                btnFullDefault.setBackgroundResource(R.drawable.btn_default_selector);
                break;
            case VIDEO_TYPE_DEFAULT:
                isFullScreen = false;
                //视频原始的画面大小
                //视频原始的画面大小

                int mVideoWidth = videoWidth;
                int mVideoHeight = videoHeight;

                /**
                 * 计算后的值
                 */
                int width = screenWidth;
                int height = screenHeight;

                if (mVideoWidth * height < width * mVideoHeight) {
                    //Log.i("@@@", "image too wide, correcting");
                    width = height * mVideoWidth / mVideoHeight;
                } else if (mVideoWidth * height > width * mVideoHeight) {
                    //Log.i("@@@", "image too tall, correcting");
                    height = width * mVideoHeight / mVideoWidth;
                }
                //把计算好的视频大小传递过去
                vv_video.setViewSize(width, height);
                //把按钮设置-全屏
                btnFullDefault.setBackgroundResource(R.drawable.btn_full_selector);
                break;
        }
    }

    class MyBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            //得到当前系统电量
            int level = intent.getIntExtra("level", 0);
            setBattery(level);
        }
    }

    private void setBattery(int level) {
        if (level <= 0) {
            ivBattery.setImageResource(R.drawable.ic_battery_0);
        } else if (level <= 10) {
            ivBattery.setImageResource(R.drawable.ic_battery_10);
        } else if (level <= 20) {
            ivBattery.setImageResource(R.drawable.ic_battery_20);
        } else if (level <= 40) {
            ivBattery.setImageResource(R.drawable.ic_battery_40);
        } else if (level <= 60) {
            ivBattery.setImageResource(R.drawable.ic_battery_60);
        } else if (level <= 80) {
            ivBattery.setImageResource(R.drawable.ic_battery_80);
        } else if (level <= 100) {
            ivBattery.setImageResource(R.drawable.ic_battery_100);
        }
    }

    @Override
    public void onClick(View v) {
        if (v == btnVoice) {
            // 声音按钮
            isMute = !isMute;
            updateVoice(currentVolume);
        } else if (v == btnSwichePlayer) {
            // 切换万能播放器
            showSwichPlayerDialog();
        } else if (v == btnBack) {
            // 返回按钮
            finish();
        } else if (v == btnLast) {
            //播放上一个视频
            setLastVideo();
            // Handle clicks for btnLast
        } else if (v == btnPlayPause) {
            // 播放或者暂停
            playAndPause();
        } else if (v == btnNext) {
            //播放下一个视频
            setNextVideo();
        } else if (v == btnFullDefault) {
            // 是否全屏
            if (isFullScreen) {
                //设置默认
                setVideoType(VIDEO_TYPE_DEFAULT);
            } else {
                //全屏显示
                setVideoType(VIDEO_TYPE_FULL);
            }
        }
        //移除消息
        handler.removeMessages(HIDE_MEDIA_CONTROLLER);
        //重新发消息
        handler.sendEmptyMessageDelayed(HIDE_MEDIA_CONTROLLER, 4000);
    }

    private void showSwichPlayerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("提醒");
        builder.setMessage("是否切换万能播放器");
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setPositiveButton("切换", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startVitamioVideoPlayer();
            }
        });
        builder.show();
    }

    private void updateVoiceProgress(int progress) {

        //第一个参数：声音的类型
        //第二个参数：声音的值：0~15
        //第三个参数：1，显示系统调声音的；0，不显示
        am.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
        seekbarVoice.setProgress(progress);
        //设置静音
        if (progress <= 0) {
            isMute = true;
        } else {
            isMute = false;
        }
        currentVolume = progress;
    }

    private void updateVoice(int progress) {
        if (isMute) {
            am.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
            seekbarVoice.setProgress(0);
        } else {
            //第一个参数：声音的类型
            //第二个参数：声音的值：0~15
            //第三个参数：1，显示系统调声音的；0，不显示
            am.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
            seekbarVoice.setProgress(progress);
        }
        currentVolume = progress;
    }

    private void playAndPause() {
        if (vv_video.isPlaying()) {
            vv_video.pause();
            btnPlayPause.setBackgroundResource(R.drawable.btn_play_selector);
        } else {
            vv_video.start();
            btnPlayPause.setBackgroundResource(R.drawable.btn_pause_selector);
        }
    }

    private void setNextVideo() {
        if (data != null && data.size() > 0) {
            position++;
            if (position < data.size()) {
                LocalVideoBean bean = data.get(position);
                //显示加载页面
                ll_loading.setVisibility(View.VISIBLE);
                //设置标题
                tvGame.setText(bean.getName());

                //判断是否为网络视频
                isNetUri = utils.isNetUrl(bean.getData());
                //设置播放地址
                vv_video.setVideoPath(bean.getData());
                checkButtonStatus();
            } else {
                //越界
                position = data.size() - 1;
                finish();
            }
        } else if (uri != null) { //单个视频
            finish();
        }
    }


    private void setLastVideo() {
        if (data != null && data.size() > 0) {
            position--;
            if (position >= 0) {
                //显示加载页面
                ll_loading.setVisibility(View.VISIBLE);
                LocalVideoBean bean = data.get(position);
                //设置标题
                tvGame.setText(bean.getName());
                //判断是否为网络视频
                isNetUri = utils.isNetUrl(bean.getData());
                //设置播放地址
                vv_video.setVideoPath(bean.getData());
                //效验按钮状态
                checkButtonStatus();
            } else {
                //越界
                position = 0;
            }
        } else if (uri != null) { //单个视频
            finish();
        }
    }

    private void checkButtonStatus() {
        //1.判断一下列表
        if (data != null && data.size() > 0) {
            //1.先设置默认
            setButtonEnable(true);
            //2.当播放第一个视频时 ,上 设置不可播放
            if (position == 0) {
                btnLast.setBackgroundResource(R.drawable.btn_pre_gray);
                btnLast.setEnabled(false);
            } else if (position == data.size() - 1) { //播放最后一个时,下设置不可播放
                btnNext.setBackgroundResource(R.drawable.btn_next_gray);
                btnNext.setEnabled(false);
            }
        }
        //2.单个视频
        else if (uri != null) {
            //上一个和下一个都要设置灰色
            setButtonEnable(false);
        }
    }

    private void setButtonEnable(boolean b) {
        if (b) {
            btnLast.setBackgroundResource(R.drawable.btn_last_selector);
            btnNext.setBackgroundResource(R.drawable.btn_next_selector);
        } else {
            btnLast.setBackgroundResource(R.drawable.btn_pre_gray);
            btnNext.setBackgroundResource(R.drawable.btn_next_gray);
        }

        btnNext.setEnabled(b);
        btnLast.setEnabled(b);
    }

    private void setLinstener() {
        //设置视频播放监听：准备好的监听，播放出错监听，播放完成监听
        vv_video.setOnPreparedListener(new MyOnPreparedListener());
        vv_video.setOnErrorListener(new MyOnErrorListener());
        vv_video.setOnCompletionListener(new MyOnCompletionListener());
        //设置控制面板
        //vv_video.setMediaController(new MediaController(this));
        //设置视频的拖动监听
        seekbarVideo.setOnSeekBarChangeListener(new VideoOnSeekBarChangeListener());
        seekbarVoice.setOnSeekBarChangeListener(new VoiceOnSeekBarChangeListener());
    }

    class VoiceOnSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {
        /**
         * 状态变化的时候回调
         *
         * @param seekBar
         * @param progress 当前改变的进度-要拖动到的位置
         * @param fromUser 用户导致的改变true,否则false
         */
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                //响应用户拖动
                updateVoiceProgress(progress);
            }
        }

        /**
         * 当手指按下的时候回调
         *
         * @param seekBar
         */
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            //移除消息
            handler.removeMessages(HIDE_MEDIA_CONTROLLER);
        }

        /**
         * 当手指松开的时候回调
         *
         * @param seekBar
         */

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            //发送延迟消息
            handler.sendEmptyMessageDelayed(HIDE_MEDIA_CONTROLLER, 4000);
        }
    }

    class VideoOnSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {
        /**
         * 状态变化的时候回调
         *
         * @param seekBar
         * @param progress 当前改变的进度-要拖动到的位置
         * @param fromUser 用户导致的改变true,否则false
         */
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                //响应用户拖动
                vv_video.seekTo(progress);
            }
        }

        /**
         * 当手指按下的时候回调
         *
         * @param seekBar
         */
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            //移除消息
            handler.removeMessages(HIDE_MEDIA_CONTROLLER);
        }

        /**
         * 当手指松开的时候回调
         *
         * @param seekBar
         */

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            //发送延迟消息
            handler.sendEmptyMessageDelayed(HIDE_MEDIA_CONTROLLER, 4000);
        }
    }

    private void setData() {

        // vv_video.setVideoURI(uri);
        if (data != null && data.size() > 0) {
            //根据位置播放视频
            LocalVideoBean bean = data.get(position);
            vv_video.setVideoPath(bean.getData());
            tvGame.setText(bean.getName());

            //判断是否为网络视频
            isNetUri = utils.isNetUrl(bean.getData());
        } else {
            //设置播放地址
            vv_video.setVideoURI(uri);
            tvGame.setText(uri.toString());

            isNetUri = utils.isNetUrl(uri.toString());
        }
        //设置按钮状态
        checkButtonStatus();
    }

    public void getUri() {
        uri = getIntent().getData();

        //得到视频列表
        data = (List<LocalVideoBean>) getIntent().getSerializableExtra("videolist");
        position = getIntent().getIntExtra("position", 0);
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SHOW_NET_SPEED: //设置网络下载速度
                    String speed = utils.showNetSpeed(SystemVideoPlayerActivity.this);
                    //不为空
                    tv_loading.setText("正在加载....." + speed);
                    tv_buffer.setText("缓冲中....." + speed);

                    removeMessages(SHOW_NET_SPEED);
                    sendEmptyMessageDelayed(SHOW_NET_SPEED, 1000);

                    break;
                case HIDE_MEDIA_CONTROLLER:
                    hideMediaController();//隐藏控制面板
                    break;
                //视频播放进度更新
                case PROGRESS:
                    int position = vv_video.getCurrentPosition();//获得当前视频进度
                    seekbarVideo.setProgress(position);   //设置进度条进度
                    tvCurrentTime.setText(utils.stringForTime(position));//设置当前时间
                    tv_top_time.setText(getTime());   //设置系统时间并更新到控件
                    //设置视频缓存进度更新
                    if (isNetUri) {
                        int buffer = vv_video.getBufferPercentage();//0--100
                        //缓存进度
                        int secondary = buffer * seekbarVideo.getMax() / 100;
                        seekbarVideo.setSecondaryProgress(secondary);
                    }
                    if (isNetUri && vv_video.isPlaying()) {
                        int buffer = position - prePosition;//1000左右
                        //一秒之内播放的进度小于500毫秒就是卡了,否则不卡
                        if (buffer < 500) {
                            //卡就显示缓冲
                            ll_buffer.setVisibility(View.VISIBLE);
                        } else {
                            //不卡就隐藏缓冲loading
                            ll_buffer.setVisibility(View.GONE);
                        }
                    }
                    prePosition = position;

                    //不断清除消息,发送每秒消息,更新每秒进度条
                    handler.removeMessages(PROGRESS);
                    handler.sendEmptyMessageDelayed(PROGRESS, 1000);
                    break;
            }
        }
    };

    private class MyOnPreparedListener implements MediaPlayer.OnPreparedListener {
        @Override
        public void onPrepared(MediaPlayer mp) {
            //得到视频原始的大小
            videoWidth = mp.getVideoWidth();
            videoHeight = mp.getVideoHeight();
            //设置默认大小
            setVideoType(VIDEO_TYPE_DEFAULT);
            //播放视频
            vv_video.start();

            //准备好的时候把视频和进度条关联起来
            int duration = vv_video.getDuration(); //获取视频总时长
            seekbarVideo.setMax(duration);

            //设置总视频和时间,更新进度消息
            tvTime.setText(utils.stringForTime(duration));


            //发送消息
            handler.sendEmptyMessage(PROGRESS);
            //隐藏加载等待的页面
            ll_loading.setVisibility(View.GONE);

        }
    }

    private class MyOnErrorListener implements MediaPlayer.OnErrorListener {
        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            Toast.makeText(SystemVideoPlayerActivity.this, "播放出错了，亲", Toast.LENGTH_SHORT).show();
            //1.播放的视频格式不支持--跳转万能播放器播放
            startVitamioVideoPlayer();
            //2.播放网络资源视频的时候，断网了==提示-重试（3次）
            //3.视频内容有缺损
            return true;
        }
    }

    /**
     * 启动万能解码器
     */
    private void startVitamioVideoPlayer() {
        //释放播放器
        if (vv_video != null) {
            vv_video.stopPlayback();
        }
        Intent intent = new Intent(this, VitamioVideoPlayerActivity.class);
        if (data != null && data.size() > 0) {
            Bundle bundle = new Bundle();
            //列表数据
            bundle.putSerializable("videolist", (Serializable) data);
            intent.putExtras(bundle);
            //传递点击的位置
            intent.putExtra("position", position);
        } else if (uri != null) {
            intent.setDataAndType(uri, "video/*");
        }
        startActivity(intent);
        finish();
    }

    private class MyOnCompletionListener implements MediaPlayer.OnCompletionListener {
        /**
         * 当底层视频加载完成的时候回调
         *
         * @param mp
         */
        @Override
        public void onCompletion(MediaPlayer mp) {
            //1.单个视频-退出播放器
            //2.视频列表-播放下一个
            // Toast.makeText(SystemVideoPlayerActivity.this, "视频播放完成", Toast.LENGTH_SHORT).show();
            setNextVideo();
        }
    }

    /**
     * 得到系统时间
     *
     * @return
     */
    private String getTime() {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        return format.format(new Date());
    }

    @Override
    protected void onDestroy() {
        //释放资源,释放广播资源
        if (receiver != null) {
            unregisterReceiver(receiver);
            receiver = null;
        }
        //释放handler消息
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    private float startY;
    //滑动的区域
    private int touchRang = 0;
    //当按下的时候的音量
    private int mVol;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        detector.onTouchEvent(event);
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            //1.按下
            //按下的时候记录起始坐标,最大的滑动区域(屏幕的高),当前的音量
            startY = event.getY();
            startX = event.getX();
            touchRang = Math.min(screenHeight, screenWidth);
            mVol = am.getStreamVolume(AudioManager.STREAM_MUSIC);

            //把消息移除
            handler.removeMessages(HIDE_MEDIA_CONTROLLER);
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            float endY = event.getY();
            //屏幕滑动的距离
            float distanceY = startY - endY;
            if (startX > screenWidth / 2) {
                //滑动屏幕的距离 ： 总距离  = 改变的声音 ： 总声音

                //改变的声音 = （滑动屏幕的距离 / 总距离)*总声音
                float delta = (distanceY / touchRang) * maxVolume;
                // 设置的声音  = 原来记录的   改变的声音
                int volue = (int) Math.min(Math.max(mVol + delta, 0), maxVolume);
                //判断
                if (delta != 0) {
                    updateVoiceProgress(volue);
                }
                //startY = event.getY();//不能添加
            } else {
                //左边屏幕-----改变亮度
                //左边屏幕--改变亮度
                final double FLING_MIN_DISTANCE = 0.5;
                final double FLING_MIN_VELOCITY = 0.5;
                if (startY - endY > FLING_MIN_DISTANCE
                        && Math.abs(distanceY) > FLING_MIN_VELOCITY) {
                    setBrightness(20);
                }
                if (startY - endY < FLING_MIN_DISTANCE
                        && Math.abs(distanceY) > FLING_MIN_VELOCITY) {
                    setBrightness(-20);
                }
            }

        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            handler.sendEmptyMessageDelayed(HIDE_MEDIA_CONTROLLER, 4000);
        }
        return true;
    }

    /*
         *
         * 设置屏幕亮度 lp = 0 全暗 ，lp= -1,根据系统设置， lp = 1; 最亮
         */
    public void setBrightness(float brightness) {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        // if (lp.screenBrightness <= 0.1) {
        // return;
        // }
        lp.screenBrightness = lp.screenBrightness + brightness / 255.0f;
        if (lp.screenBrightness > 1) {
            lp.screenBrightness = 1;
            vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            long[] pattern = {10, 200}; // OFF/ON/OFF/ON...
            vibrator.vibrate(pattern, -1);
        } else if (lp.screenBrightness < 0.2) {
            lp.screenBrightness = (float) 0.2;
            vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            long[] pattern = {10, 200}; // OFF/ON/OFF/ON...
            vibrator.vibrate(pattern, -1);
        }
        getWindow().setAttributes(lp);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            //改变音量值
            currentVolume--;
            updateVoiceProgress(currentVolume);
            //移除消息
            handler.removeMessages(HIDE_MEDIA_CONTROLLER);
            //发消息
            handler.sendEmptyMessageDelayed(HIDE_MEDIA_CONTROLLER, 4000);
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            currentVolume++;
            updateVoiceProgress(currentVolume);
            handler.removeMessages(HIDE_MEDIA_CONTROLLER);
            handler.sendEmptyMessageDelayed(HIDE_MEDIA_CONTROLLER, 4000);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
