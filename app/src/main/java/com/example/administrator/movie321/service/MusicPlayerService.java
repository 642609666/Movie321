package com.example.administrator.movie321.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.example.administrator.movie321.IMusicPlayerService;
import com.example.administrator.movie321.R;
import com.example.administrator.movie321.SystemMusicPlayerActivity;
import com.example.administrator.movie321.bean.LocalVideoBean;
import com.example.administrator.movie321.utils.CacheUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by Administrator on 2017/1/12 0012.
 * 服务,播放音乐的服务
 */

public class MusicPlayerService extends Service {
    public static final String OPEN_COMPLETE = "open_complete";
    /**
     * AIDL生成的类
     */
    IMusicPlayerService.Stub stub = new IMusicPlayerService.Stub() {
        MusicPlayerService service = MusicPlayerService.this;

        //
        @Override
        public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {

        }

        @Override
        public void openAudio(int position) throws RemoteException {
            service.openAudio(position);
        }

        @Override
        public void start() throws RemoteException {
            service.start();
        }

        @Override
        public void pause() throws RemoteException {
            service.pause();
        }

        @Override
        public String getAudioName() throws RemoteException {
            return service.getAudioName();
        }

        @Override
        public String getArtistName() throws RemoteException {
            return service.getArtistName();
        }

        @Override
        public int getCurrentPosition() throws RemoteException {
            return service.getCurrentPosition();
        }

        @Override
        public int getDuration() throws RemoteException {
            return service.getDuration();
        }

        @Override
        public void next() throws RemoteException {
            service.next();
        }

        @Override
        public void pre() throws RemoteException {
            service.pre();
        }

        @Override
        public int getPlayMode() throws RemoteException {
            return service.getPlayMode();
        }

        @Override
        public void setPlayMode(int mode) throws RemoteException {
            service.setPlayMode(mode);
        }

        @Override
        public boolean isPlaying() throws RemoteException {
            return mediaPlayer.isPlaying();
        }

        @Override
        public void seekTo(int postion) throws RemoteException {
            mediaPlayer.seekTo(postion);
        }

        @Override
        public String getAudioPath() throws RemoteException {
            return bean.getData();
        }

        @Override
        public int getAudioSessionId() throws RemoteException {
            return mediaPlayer.getAudioSessionId();
        }
    };
    /**
     * 音频是否加载完成
     */
    private boolean isLoaded = false;
    /**
     * 顺序播放
     */
    public static final int REPEATE_NOMAL = 1;
    /**
     * 单曲播放
     */
    public static final int REPEATE_SINGLE = 2;
    /**
     * 循环播放
     */
    public static final int REPEATE_ALL = 3;
    public int playmode = REPEATE_NOMAL;
    private boolean isNext = false;

    /**
     * 返回代理类
     *
     * @param intent
     * @return
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return stub;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        playmode = CacheUtils.getPlaymode(this, "playmode");
        getDataFromLocal();
    }

    private List<LocalVideoBean> data;

    /**
     * 子线程中得到音频数据
     */
    private void getDataFromLocal() {
        new Thread() {
            public void run() {
                //初始化数据
                data = new ArrayList<LocalVideoBean>();
                //访问内置的数据库
                ContentResolver resolver = getContentResolver();
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
                //音频加载完成
                isLoaded = true;
            }
        }.start();
    }

    private LocalVideoBean bean;
    private int position;
    private MediaPlayer mediaPlayer;

    /**
     * 根据位置打开一个音频并且播放
     *
     * @param position
     */
    void openAudio(int position) {
        if (data != null && data.size() > 0) {
            bean = data.get(position);
            this.position = position;

            if (mediaPlayer != null) {
                mediaPlayer.reset();//上一曲重置
                mediaPlayer = null;//释放
            }

            mediaPlayer = new MediaPlayer();
            //准备监听
            mediaPlayer.setOnPreparedListener(new MyOnPreparedListener());
            //错误监听
            mediaPlayer.setOnErrorListener(new MyOnErrorListener());
            //完成监听
            mediaPlayer.setOnCompletionListener(new MyOnCompletionListener());
            //设置播放地址
            try {
                mediaPlayer.setDataSource(bean.getData());
                mediaPlayer.prepareAsync();
                isNext = false;
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (!isLoaded) {
            Toast.makeText(MusicPlayerService.this, "没有加载完成", Toast.LENGTH_SHORT).show();
        }
    }

    class MyOnCompletionListener implements MediaPlayer.OnCompletionListener {

        @Override
        public void onCompletion(MediaPlayer mp) {
            isNext = true;
            next();
        }
    }

    class MyOnErrorListener implements MediaPlayer.OnErrorListener {

        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            next();
            return true;
        }
    }

    class MyOnPreparedListener implements MediaPlayer.OnPreparedListener {

        @Override
        public void onPrepared(MediaPlayer mp) {
            notifyChange(OPEN_COMPLETE);
            start();
        }
    }

    private void notifyChange(String action) {
        Intent intent = new Intent();
        intent.setAction(action);
        //发广播
        sendBroadcast(intent);
    }

    private NotificationManager nm;

    /**
     * 开始播放音频
     */
    void start() {
        mediaPlayer.start();
        //在状态栏创建通知
        nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        Intent intent = new Intent(this, SystemMusicPlayerActivity.class);
        intent.putExtra("notification", true);//标识来自状态栏
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        Notification notification = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            notification = new Notification.Builder(this)
                    .setSmallIcon(R.drawable.notification_music_playing)
                    .setContentTitle("666播放器")
                    .setContentText("正在播放" + getAudioName())
                    .setContentIntent(pendingIntent)
                    .build();

            //点击后还存在属性
            notification.flags = Notification.FLAG_ONGOING_EVENT;
        }
        nm.notify(1, notification);
    }

    /**
     * 暂停
     */
    void pause() {
        mediaPlayer.pause();
        //移除状态栏通知
        nm.cancel(1);
    }

    /**
     * 得到歌曲的名称
     */
    String getAudioName() {
        if (bean != null) {
            return bean.getName();
        }
        return "";
    }

    /**
     * 得到歌曲演唱者的名字
     */
    String getArtistName() {
        if (bean != null) {
            return bean.getArtist();
        }
        return "";
    }

    /**
     * 得到歌曲的当前播放进度
     */
    int getCurrentPosition() {
        if (mediaPlayer != null) {
            return mediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    /**
     * 得到歌曲的当前总进度
     */
    int getDuration() {
        if (mediaPlayer != null) {
            return mediaPlayer.getDuration();
        }
        return 0;
    }

    /**
     * 播放下一首歌曲
     */
    void next() {
        //设置下一曲对应的位置
        setNextPostion();
        //根据对应的位置去播放
        openNextMusic();
    }

    private void openNextMusic() {
        int playmode = getPlayMode();

        if (playmode == MusicPlayerService.REPEATE_NOMAL) {
            if (position <= data.size() - 1) {
                openAudio(position);
            } else {
                position = data.size() - 1;
                openAudio(position);
            }
        } else if (playmode == MusicPlayerService.REPEATE_SINGLE) {
            openAudio(position);
        } else if (playmode == MusicPlayerService.REPEATE_ALL) {
            openAudio(position);
        }
    }

    private void setNextPostion() {
        int playmode = getPlayMode();

        if (playmode == MusicPlayerService.REPEATE_NOMAL) {
            position++;

        } else if (playmode == MusicPlayerService.REPEATE_SINGLE) {
            if (!isNext) {
                position++;
                if (position > data.size() - 1) {
                    position = 0;
                }
            } else {
                position = position;
            }

        } else if (playmode == MusicPlayerService.REPEATE_ALL) {
            position++;
            if (position > data.size() - 1) {
                position = 0;
            }
        }
    }

    /**
     * 播放上一首歌曲
     */
    void pre() {
        setPrePosition();
        setPreMusic();
    }

    private void setPrePosition() {
        int playmode = getPlayMode();

        if (playmode == MusicPlayerService.REPEATE_NOMAL) {
            position--;

        } else if (playmode == MusicPlayerService.REPEATE_SINGLE) {
            if (!isNext) {
                position--;
                if (position < 0) {
                    position = data.size() - 1;
                }
            } else {
                position = position;
            }

        } else if (playmode == MusicPlayerService.REPEATE_ALL) {
            position--;
            if (position < 0) {
                position = data.size() - 1;
            }
        }
    }

    private void setPreMusic() {
        int playmode = getPlayMode();

        if (playmode == MusicPlayerService.REPEATE_NOMAL) {
            if (position >= 0) {
                openAudio(position);
            } else {
                position = 0;
                openAudio(position);
            }
        } else if (playmode == MusicPlayerService.REPEATE_SINGLE) {
            openAudio(position);
        } else if (playmode == MusicPlayerService.REPEATE_ALL) {
            openAudio(position);
        }
    }

    /**
     * 得到播放模式
     */
    int getPlayMode() {
        return playmode;
    }

    /**
     * 设置播放模式
     */
    void setPlayMode(int mode) {
        this.playmode = mode;
        CacheUtils.setPlaymode(this, "playmode", playmode);
    }
}
