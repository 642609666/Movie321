package com.example.administrator.movie321;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.audiofx.Visualizer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.administrator.movie321.service.MusicPlayerService;
import com.example.administrator.movie321.utils.LyricParaser;
import com.example.administrator.movie321.utils.Utils;
import com.example.administrator.movie321.view.BaseVisualizerView;
import com.example.administrator.movie321.view.LyricShowView;

import java.io.File;

public class SystemMusicPlayerActivity extends AppCompatActivity implements View.OnClickListener {


    private TextView tvArtist;
    private TextView tvName;
    private TextView tvMusictime;
    private SeekBar seekbarMusic;
    private Button btnMusicPlaymode;
    private Button btnMusicLast;
    private Button btnMusicPause;

    private Button btnMusicNext;
    private Button btnSwichLyric;
    private ImageView iv_Icon;
    private int position;
    private IMusicPlayerService service;
    private MyReceiver receiver;
    private static final int PROGRESS = 1;
    private Utils utils;
    private BaseVisualizerView baseVisualizerView;

    private LyricShowView lyric_show_view;
    private ServiceConnection conn = new ServiceConnection() {
        /**
         * 当连接服务成功后回调
         * @param name
         * @param ibinder
         */
        @Override
        public void onServiceConnected(ComponentName name, IBinder ibinder) {
            service = IMusicPlayerService.Stub.asInterface(ibinder);

            if (service != null) {
                //从列表进入
                if (!notification) {
                    try {
                        //开启播放
                        service.openAudio(position);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                } else {
                    //再次显示
                    showViewData();
                }

            }
        }

        /**
         * 当服务断开时回调
         * @param name
         */
        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
    private boolean notification;
    /**
     * 歌词同步
     */
    private static final int SHOW_LYRIC = 2;
    private Visualizer mVisualizer;

    private void findViews() {
        setContentView(R.layout.activity_system_music_player);
        tvArtist = (TextView) findViewById(R.id.tv_artist);
        tvName = (TextView) findViewById(R.id.tv_name);
        tvMusictime = (TextView) findViewById(R.id.tv_musictime);
        seekbarMusic = (SeekBar) findViewById(R.id.seekbar_music);
        btnMusicPlaymode = (Button) findViewById(R.id.btn_music_playmode);
        btnMusicLast = (Button) findViewById(R.id.btn_music_last);
        btnMusicPause = (Button) findViewById(R.id.btn_music_pause);
        btnMusicNext = (Button) findViewById(R.id.btn_music_next);
        btnSwichLyric = (Button) findViewById(R.id.btn_swich_lyric);
        iv_Icon = (ImageView) findViewById(R.id.iv_Icon);
        lyric_show_view = (LyricShowView) findViewById(R.id.lyric_shouview);
        baseVisualizerView = (BaseVisualizerView) findViewById(R.id.baseVisualizerView);
        btnMusicPlaymode.setOnClickListener(this);
        btnMusicLast.setOnClickListener(this);
        btnMusicPause.setOnClickListener(this);
        btnMusicNext.setOnClickListener(this);
        btnSwichLyric.setOnClickListener(this);
        // AnimationDrawable animation = (AnimationDrawable) iv_Icon.getBackground();
        // animation.start();
        seekbarMusic.setOnSeekBarChangeListener(new MyOnSeekBarChangeListener());
    }

    class MyOnSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                try {
                    service.seekTo(progress);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();
        findViews();
        getData();
        //绑定方式启动服务
        startAndBindServide();
    }

    /**
     * 创建接受广播
     */
    private void initData() {
        receiver = new MyReceiver();
        IntentFilter inentFilter = new IntentFilter();
        inentFilter.addAction(MusicPlayerService.OPEN_COMPLETE);
        registerReceiver(receiver, inentFilter);
        utils = new Utils();
    }

    class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (MusicPlayerService.OPEN_COMPLETE.equals(intent.getAction())) {
                showViewData();
            }
        }
    }

    /**
     * 显示视图的数据
     */
    private void showViewData() {
        setupVisualizerFxAndUi();
        try {
            tvArtist.setText(service.getArtistName());
            tvName.setText(service.getAudioName());
            //得到总时长
            int duration = service.getDuration();
            seekbarMusic.setMax(duration);

            //更新进度
            handler.sendEmptyMessage(PROGRESS);

            checkButtonStatu();

            String path = service.getAudioPath();//获得播放地址

            path = path.substring(0, path.lastIndexOf("."));

            File file = new File(path + ".lrc");

            if (!file.exists()) {
                file = new File(path + ".txt");
            }

            LyricParaser lyricParaser = new LyricParaser();

            //解析歌词
            lyricParaser.readFile(file);
            if (lyricParaser.isExistsLyric()) {

                lyric_show_view.setLyrics(lyricParaser.getLyricData());
                //歌词同步
                handler.sendEmptyMessage(SHOW_LYRIC);

            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * 生成一个VisualizerView对象，使音频频谱的波段能够反映到 VisualizerView上
     */
    private void setupVisualizerFxAndUi() {

        int audioSessionid = 0;
        try {
            audioSessionid = service.getAudioSessionId();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        System.out.println("audioSessionid==" + audioSessionid);
        mVisualizer = new Visualizer(audioSessionid);
        // 参数内必须是2的位数
        mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
        // 设置允许波形表示，并且捕获它
        baseVisualizerView.setVisualizer(mVisualizer);
        mVisualizer.setEnabled(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing()) {
            mVisualizer.release();
        }
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SHOW_LYRIC:
                    //显示歌词
                    try {
                        int current = service.getCurrentPosition();

                        lyric_show_view.setNextShowLyric(current);

                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    removeMessages(SHOW_LYRIC);
                    sendEmptyMessage(SHOW_LYRIC);
                    break;
                case PROGRESS:
                    try {
                        int current = service.getCurrentPosition();

                        tvMusictime.setText(utils.stringForTime(current) + "/" + utils.stringForTime(service.getDuration()));

                        //seekbar进度更新
                        seekbarMusic.setProgress(current);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    removeMessages(PROGRESS);
                    sendEmptyMessageDelayed(PROGRESS, 1000);
                    break;
            }
        }
    };

    @Override
    protected void onDestroy() {
        if (conn != null) {
            unbindService(conn);
            conn = null;
        }
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    private void startAndBindServide() {
        Intent intent = new Intent(this, MusicPlayerService.class);
        //绑定服务
        bindService(intent, conn, Context.BIND_AUTO_CREATE);
        //启动服务
        startService(intent);//防止服务多次启动
    }

    /**
     * 得到播放位置
     */
    private void getData() {
        //true 从状态栏进入
        //false 从listview中进入
        notification = getIntent().getBooleanExtra("notification", false);
        if (notification == false) {
            position = getIntent().getIntExtra("position", 0);
        }
    }


    /**
     * Handle button click events<br />
     * <br />
     * Auto-created on 2017-01-12 15:49:38 by Android Layout Finder
     * (http://www.buzzingandroid.com/tools/android-layout-finder)
     */
    @Override
    public void onClick(View v) {
        if (v == btnMusicPlaymode) {
            //播放模式
            changePlaymode();
        } else if (v == btnMusicLast) {
            //上一首
            try {
                if (!service.isPlaying()) {
                    btnMusicPause.setBackgroundResource(R.drawable.btn_pause_selector);
                }
                service.pre();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else if (v == btnMusicPause) {
            //播放暂停
            try {
                if (service.isPlaying()) {
                    //暂停
                    service.pause();
                    //按钮状态---设置播放
                    btnMusicPause.setBackgroundResource(R.drawable.btn_music_play_selector);
                } else {
                    //播放
                    service.start();
                    //按钮状态---设置暂停
                    btnMusicPause.setBackgroundResource(R.drawable.btn_pause_selector);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else if (v == btnMusicNext) {
            //下一首
            try {
                if (!service.isPlaying()) {
                    btnMusicPause.setBackgroundResource(R.drawable.btn_pause_selector);
                }
                service.next();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else if (v == btnSwichLyric) {
            //歌词
        }
    }

    private void changePlaymode() {
        try {
            int playmode = service.getPlayMode();

            if (playmode == MusicPlayerService.REPEATE_NOMAL) {

                playmode = MusicPlayerService.REPEATE_SINGLE;

            } else if (playmode == MusicPlayerService.REPEATE_SINGLE) {

                playmode = MusicPlayerService.REPEATE_ALL;

            } else if (playmode == MusicPlayerService.REPEATE_ALL) {
                playmode = MusicPlayerService.REPEATE_NOMAL;

            }
            //保存到服务中
            service.setPlayMode(playmode);

            checkButtonStatu();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void checkButtonStatu() {
        int playmode = 0;
        try {
            playmode = service.getPlayMode();

            if (playmode == MusicPlayerService.REPEATE_NOMAL) {
                btnMusicPlaymode.setBackgroundResource(R.drawable.music_playmode_normal_selector);
            } else if (playmode == MusicPlayerService.REPEATE_SINGLE) {

                btnMusicPlaymode.setBackgroundResource(R.drawable.music_playmode_single_selector);

            } else if (playmode == MusicPlayerService.REPEATE_ALL) {

                btnMusicPlaymode.setBackgroundResource(R.drawable.music_playmode_all_selector);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }
}
