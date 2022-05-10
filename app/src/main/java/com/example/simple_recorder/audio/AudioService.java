package com.example.simple_recorder.audio;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;

import com.example.simple_recorder.bean.AudioBean;
import com.example.simple_recorder.utils.Contants;

import java.util.List;

public class AudioService extends Service implements MediaPlayer.OnCompletionListener {
    private MediaPlayer mediaPlayer = null;
    private List<AudioBean>mList;//播放列表
    private  int playPosition = -1;//当前播放位置
    public AudioService() {}
    public interface OnPlayChangeListener{
        public void playChange(int changePos);
    }
    private OnPlayChangeListener onPlayChangeListener;

    public void setOnPlayChangeListener(OnPlayChangeListener onPlayChangeListener) {
        this.onPlayChangeListener = onPlayChangeListener;
    }
    //多媒体服务发生变化，提示Activiy刷新UI
    public void notifyActivityRefreshUI(){
        if (onPlayChangeListener!=null) {
            onPlayChangeListener.playChange(playPosition);
        }
    }
    @Override
    public void onCompletion(MediaPlayer mp) {

    }

    public class AudioBinder extends Binder{
        public AudioService getService(){
            return AudioService.this;
        }
    }
    @Override
    public IBinder onBind(Intent intent) {
        return new AudioBinder();
    }
/*
* 播放按钮的两种可能性
* 1.不是当前播放的位置被点击了，就进行切歌操作
* 2.当前播放的位置被点击了，就进行暂停或者继续的操作
* */
    public void cutMusicOrPause(int position){
        int playposition = this.playPosition;
        if (position!=playposition) {
            //判断是否正在播放，如果切歌，把上一曲改为false
            if (playposition!=-1) {
                mList.get(playposition).setPlaying(false);
            }
            play(position);
            return;
        }else {
            //执行暂停操作
            pauseOrContinueMusic();
        }
    }
    //播放音乐，点击切歌
    public void play(int position){
        if(mediaPlayer == null){
            mediaPlayer = new MediaPlayer();
            //设置监听事件
            mediaPlayer.setOnCompletionListener(this);
        }
    //播放时获取当前歌曲的播放列表，判断是否有歌曲
    mList = Contants.getsAudioList();
        if (mList.size()<=0) {
            return;
        }
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
    //切歌之前先重置，释放掉原来的资源
        try{
        mediaPlayer.reset();
        playPosition = position;
    //设置播放音频的资源路径
        mediaPlayer.setDataSource(mList.get(position).getPath());
        mediaPlayer.prepare();
        mediaPlayer.start();
    //设置当前位置正在播放
        mList.get(position).setPlaying(true);
        notifyActivityRefreshUI();
    }catch (Exception e){
            e.printStackTrace();
        }
    }
    //暂停和继续播放音乐的
    public void pauseOrContinueMusic(){
        int playPosition = this.playPosition;
        AudioBean audioBean = mList.get(playPosition);
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            audioBean.setPlaying(false);
        }else{
            mediaPlayer.start();
            audioBean.setPlaying(true);
        }
        notifyActivityRefreshUI();
    }
}