package com.example.administrator.movie321.utils;

import com.example.administrator.movie321.bean.LyricBean;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by Administrator on 2017/1/14 0014.
 */

public class LyricParaser {
    private ArrayList<LyricBean> lyricData;
    private boolean isExistsLyric = false;

    public ArrayList<LyricBean> getLyricData() {
        return lyricData;
    }

    public void setLyricData(ArrayList<LyricBean> lyricData) {
        this.lyricData = lyricData;
    }

    public boolean isExistsLyric() {
        return isExistsLyric;
    }

    public void setExistsLyric(boolean existsLyric) {
        isExistsLyric = existsLyric;
    }

    public void readFile(File file) {
        if (file == null || !file.exists()) {
            //歌词文件不存在
            lyricData = null;
            isExistsLyric = false;
        } else {
            //歌词文件存在
            lyricData = new ArrayList<>();
            isExistsLyric = true;
            //解析歌词-一句句
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), getCharset(file)));
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.equals(null)) {
                        continue;
                    }
                    line = analyzeLyrc(line);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            //排序
            Collections.sort(lyricData, new MyComparator());
            //就算每句的高亮时间
            for (int i = 0; i < lyricData.size(); i++) {
                //得到
                LyricBean one = lyricData.get(i);
                if (i + 1 < lyricData.size()) {
                    LyricBean two = lyricData.get(i + 1);
                    one.setSleepTime(two.getTimePoint() - one.getTimePoint());
                }
            }
        }

    }


    class MyComparator implements Comparator<LyricBean> {

        @Override
        public int compare(LyricBean o1, LyricBean o2) {
            if (o1.getTimePoint() < o2.getTimePoint()) {
                return -1;
            } else if (o1.getTimePoint() > o2.getTimePoint()) {
                return 1;
            } else {
                return 0;
            }
        }
    }

    /**
     * 解析每一句歌词
     *
     * @param line
     * @return
     */
    private String analyzeLyrc(String line) {
        int pos1 = line.indexOf("[");//0
        int pos2 = line.indexOf("]");//9
        if (pos1 == 0 && pos2 != -1) {
            //解析歌词
            long[] timpLongs = new long[getCountTag(line)];
            String timeStr = line.substring(pos1 + 1, pos2);//02:04:12
            timpLongs[0] = stTimeLong(timeStr);//02:04:12---转换毫秒
            if (timpLongs[0] == -1) {
                return "";
            }
            int i = 1;
            String content = line;//[02:04.12][03:37.32][00:59.73]我在这里欢笑
            while (pos1 == 0 && pos2 != -1) {
                content = content.substring(pos2 + 1);
                pos1 = content.indexOf("[");//0
                pos2 = content.indexOf("]");//9
                if (pos2 != -1) {//还有歌词
                    timeStr = content.substring(pos1 + 1, pos2);//02:04:12
                    timpLongs[i] = stTimeLong(timeStr);//02:04:12---转换毫秒
                    if (timpLongs[i] == -1) {
                        return "";
                    }
                    i++;
                }
            }
            //把解析好的时间和歌词对应起来
            LyricBean lyricBean = new LyricBean();
            for (int i1 = 0; i1 < timpLongs.length; i1++) {

                if (timpLongs[i1] != 0) {
                    //显示歌词内容
                    lyricBean.setContent(content);
                    lyricBean.setTimePoint(timpLongs[i1]);
                    lyricData.add(lyricBean);
                    lyricBean = new LyricBean();
                }
            }
            return content;
        }
        return null;
    }

    /**
     * 02:04.12-->毫秒
     *
     * @param timeStr
     * @return
     */
    private long stTimeLong(String timeStr) {
        long time = -1;
        try {
            //1.根据":"切成 02 和 04.12
            String[] s1 = timeStr.split(":");
            //2.根据"."把04.12切成 04  和 12
            String[] s2 = s1[1].split("\\.");
            //转换成long 类型的毫秒时间
            long min = Long.valueOf(s1[0]);
            long sec = Long.valueOf(s2[0]);
            long mil = Long.valueOf(s2[1]);

            time = min * 60 * 1000 + sec * 1000 + mil * 10;
        } catch (Exception e) {
            e.printStackTrace();
        }


        return time;
    }

    /**
     * @param line [02:04.12][03:37.32][00:59.73]我在这里欢笑
     * @return
     */
    private int getCountTag(String line) {
        int conut = 1;
        String[] left = line.split("\\[");
        String[] right = line.split("\\]");
        if (left.length == 0 && right.length == 0) {
            //插在这里
            conut = 1;
        } else if (left.length > right.length) {
            conut = left.length;
        } else {
            conut = right.length;
        }

        return conut;
    }

    /**
     * 判断文件编码
     *
     * @param file 文件
     * @return 编码：GBK,UTF-8,UTF-16LE
     */
    public String getCharset(File file) {
        String charset = "GBK";
        byte[] first3Bytes = new byte[3];
        try {
            boolean checked = false;
            BufferedInputStream bis = new BufferedInputStream(
                    new FileInputStream(file));
            bis.mark(0);
            int read = bis.read(first3Bytes, 0, 3);
            if (read == -1)
                return charset;
            if (first3Bytes[0] == (byte) 0xFF && first3Bytes[1] == (byte) 0xFE) {
                charset = "UTF-16LE";
                checked = true;
            } else if (first3Bytes[0] == (byte) 0xFE
                    && first3Bytes[1] == (byte) 0xFF) {
                charset = "UTF-16BE";
                checked = true;
            } else if (first3Bytes[0] == (byte) 0xEF
                    && first3Bytes[1] == (byte) 0xBB
                    && first3Bytes[2] == (byte) 0xBF) {
                charset = "UTF-8";
                checked = true;
            }
            bis.reset();
            if (!checked) {
                int loc = 0;
                while ((read = bis.read()) != -1) {
                    loc++;
                    if (read >= 0xF0)
                        break;
                    if (0x80 <= read && read <= 0xBF)
                        break;
                    if (0xC0 <= read && read <= 0xDF) {
                        read = bis.read();
                        if (0x80 <= read && read <= 0xBF)
                            continue;
                        else
                            break;
                    } else if (0xE0 <= read && read <= 0xEF) {
                        read = bis.read();
                        if (0x80 <= read && read <= 0xBF) {
                            read = bis.read();
                            if (0x80 <= read && read <= 0xBF) {
                                charset = "UTF-8";
                                break;
                            } else
                                break;
                        } else
                            break;
                    }
                }
            }
            bis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return charset;
    }
}
