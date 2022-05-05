package com.example.simple_recorder.utils;

import android.media.MediaMetadataRetriever;

import java.text.SimpleDateFormat;
import java.util.Date;
/*一个通用的多媒体音频文件数据获取的工具类，可拓展功能使用*/
public class AudioInfoUtils {

    private MediaMetadataRetriever mediaMetadataRetriever;
    //获取音频文件相关内容的工具类
    private AudioInfoUtils(){}
    private static AudioInfoUtils utils;
    public static AudioInfoUtils getInstance(){
        if (utils == null) {
            synchronized (AudioInfoUtils.class){
                if (utils == null){
                    utils = new AudioInfoUtils();
                }
            }
        }
        return utils;
    }
    public  long getAudioFileDuration(String filePath){
        long duration = 0;
        if (mediaMetadataRetriever == null) {
            mediaMetadataRetriever = new MediaMetadataRetriever();
        }
        mediaMetadataRetriever.setDataSource(filePath);
        String s  = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        duration = Long.parseLong(s);
        return duration;
    }
    public String getAudioFileFormatDuration(String format,long durlong){
        durlong -= 8*3600*1000;
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return  sdf.format(new Date(durlong));
    }
    /*转换成固定类型的时长 HH：mm：ss*/
    public String getAudioFileFormatDuration(long durlong){
        return getAudioFileFormatDuration("HH:mm:ss",durlong);
    }
    //获取音频文件的艺术家
    public String getAudioFileArtist(String filepath){
    if (mediaMetadataRetriever == null){
        mediaMetadataRetriever = new MediaMetadataRetriever();
    }
    mediaMetadataRetriever.setDataSource(filepath);
    String artist =  mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
    return artist;
    }
    public void releseRetriever(){
        if (mediaMetadataRetriever!=null) {
            mediaMetadataRetriever.release();
            mediaMetadataRetriever=null;
        }
    }
}
