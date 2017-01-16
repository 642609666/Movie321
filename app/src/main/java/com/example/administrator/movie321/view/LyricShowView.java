package com.example.administrator.movie321.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.TextView;

import com.example.administrator.movie321.bean.LyricBean;
import com.example.administrator.movie321.utils.DensityUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/1/14 0014.
 * 自定义显示歌词的控件
 */

public class LyricShowView extends TextView {
    private Context context;
    private int width;
    private int height;
    private List<LyricBean> lyricBeen;
    private Paint paint;
    private Paint nopaint;
    /**
     * 歌词索引
     */
    private int index = 0;
    private float textHeight;
    /**
     * 歌词的索引
     */
    private int currentPosition;
    private float sleepTime;
    private float timePoint;

    public LyricShowView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        initView();
    }

    private void initView() {
        textHeight = DensityUtil.dip2px(context, 20);
        //创建画笔
        paint = new Paint();
        paint.setTextSize(DensityUtil.dip2px(context, 20));
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setAntiAlias(true);
        paint.setColor(Color.GREEN);

        nopaint = new Paint();
        nopaint.setTextSize(DensityUtil.dip2px(context, 20));
        nopaint.setTextAlign(Paint.Align.CENTER);
        nopaint.setAntiAlias(true);
        nopaint.setColor(Color.WHITE);
//        lyricBeen = new ArrayList<>();
        //添加歌词列表
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (lyricBeen != null && lyricBeen.size() > 0) {

            if (index != lyricBeen.size() - 1) {
                float plush = 0;

                if (sleepTime == 0) {
                    plush = 0;
                } else {
                    // 这一句花的时间： 这一句休眠时间  =  这一句要移动的距离：总距离(行高)
                    //这一句要移动的距离 = （这一句花的时间/这一句休眠时间） * 总距离(行高)
                    plush = ((currentPosition - timePoint) / sleepTime) * textHeight;
                }
                canvas.translate(0, -plush);
            }


            //绘制歌词
            //当前句
            String content;
            try {
                content = lyricBeen.get(index).getContent();
                canvas.drawText(content, width / 2, height / 2, paint);
                //绘制前面部分
                float tempY = height / 2;
                for (int i = index - 1; i >= 0; i--) {
                    tempY -= textHeight;
                    if (tempY < 0) {
                        break;
                    }
                    String preContent = lyricBeen.get(i).getContent();
                    canvas.drawText(preContent, width / 2, tempY, nopaint);
                }


                //绘制后面部分
                tempY = height / 2;
                for (int i = index + 1; i <= lyricBeen.size() - 1; i++) {
                    tempY += textHeight;
                    if (tempY > height) {
                        break;
                    }
                    String nextContent = lyricBeen.get(i).getContent();
                    canvas.drawText(nextContent, width / 2, tempY, nopaint);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }


        } else {
            //没有歌词
            canvas.drawText("没有找到歌词", width / 2, height / 2, paint);

        }
    }

    /**
     * 根据当前播放的位置,计算高亮哪句,并且与歌曲同步
     *
     * @param current
     */
    public void setNextShowLyric(int current) {
        this.currentPosition = current;
        if (lyricBeen == null || lyricBeen.size() == 0) {
            return;
        }
        for (int i = 1; i < lyricBeen.size(); i++) {
            if (currentPosition < lyricBeen.get(i).getTimePoint()) {
                int indexTemp = i - 1;

                if (currentPosition >= lyricBeen.get(indexTemp).getTimePoint()) {
                    //就是我们要找的高亮的哪句
                    index = indexTemp;
                    sleepTime = lyricBeen.get(indexTemp).getSleepTime();
                    timePoint = lyricBeen.get(indexTemp).getTimePoint();
                }
            } else {
                index = i;
            }
        }
        invalidate();//强制绘制
    }

    /**
     * 设置列表
     *
     * @param lyricData
     */

    public void setLyrics(ArrayList<LyricBean> lyricData) {
        this.lyricBeen = lyricData;
    }
}
