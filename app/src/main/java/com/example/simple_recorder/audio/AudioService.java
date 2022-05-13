package com.example.simple_recorder.audio;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.example.simple_recorder.bean.AudioBean;
import com.example.simple_recorder.utils.Contants;

import java.util.List;

public class AudioService extends Service implements MediaPlayer.OnCompletionListener {
    private MediaPlayer mediaPlayer = null;
    private List<AudioBean> mList;//播放列表
    private int playPosition = -1;//当前播放位置

    public AudioService() {
    }
//创建通知对象和远程View对象

    public interface OnPlayChangeListener {
        public void playChange(int changePos);
    }

    private OnPlayChangeListener onPlayChangeListener;

    public void setOnPlayChangeListener(OnPlayChangeListener onPlayChangeListener) {
        this.onPlayChangeListener = onPlayChangeListener;
    }

    //多媒体服务发生变化，提示Activiy刷新UI
    public void notifyActivityRefreshUI() {
        if (onPlayChangeListener != null) {
            onPlayChangeListener.playChange(playPosition);
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {

    }

    public class AudioBinder extends Binder {
        public AudioService getService() {
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
    public void cutMusicOrPause(int position) {
        int playposition = this.playPosition;
        if (position != playposition) {
            //判断是否正在播放，如果切歌，把上一曲改为false
            if (playposition != -1) {
                mList.get(playposition).setPlaying(false);
            }
            play(position);
            return;
        } else {
            //执行暂停操作
            pauseOrContinueMusic();
        }
    }

    //播放音乐，点击切歌
    public void play(int position) {
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
            //设置监听事件
            mediaPlayer.setOnCompletionListener(this);
        }
        //播放时获取当前歌曲的播放列表，判断是否有歌曲
        mList = Contants.getsAudioList();
        if (mList.size() <= 0) {
            return;
        }
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
        //切歌之前先重置，释放掉原来的资源
        try {
            mediaPlayer.reset();
            playPosition = position;
            //设置播放音频的资源路径
            mediaPlayer.setDataSource(mList.get(position).getPath());
            mediaPlayer.prepare();
            mediaPlayer.start();
            //设置当前位置正在播放
            mList.get(position).setPlaying(true);
            notifyActivityRefreshUI();
            setFlagControlThread(true);
            updateProgress();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //改变音频播放位置
    public void changeAudioByCurrent(int current, int position) {
        try {
            mediaPlayer.reset();
            if (mediaPlayer == null) {
                mediaPlayer = new MediaPlayer();
                //设置监听事件
                mediaPlayer.setOnCompletionListener(this);
            }
            //设置播放音频的资源路径
            playPosition = position;
            //设置播放音频的资源路径
            mediaPlayer.setDataSource(mList.get(position).getPath());
            mediaPlayer.prepare();
            mediaPlayer.seekTo((int) ((mList.get(position).getDurationLong() / 100) * current));
            mediaPlayer.start();
            notifyActivityRefreshUI();
            setFlagControlThread(true);
            updateProgress();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //暂停和继续播放音乐的
    public void pauseOrContinueMusic() {
        int playPosition = this.playPosition;
        AudioBean audioBean = mList.get(playPosition);
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            audioBean.setPlaying(false);
        } else {
            mediaPlayer.start();
            audioBean.setPlaying(true);
        }
        notifyActivityRefreshUI();
    }

    /*
     * 进度条更新显示进度
     * */
    private boolean flag = true;//控制线程
    private final int PROGRESS_ID = 1;
    private final int INTERMINATE_TIME = 1000;

    private void setFlagControlThread(boolean flag) {
        this.flag = flag;
    }

    public void updateProgress() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (flag) {
                    //获取总时长
                    long total = mList.get(playPosition).getDurationLong();
                    //获取当前播放的位置
                    int currentPosition = mediaPlayer.getCurrentPosition();
                    //计算播放进度
                    int progress = (int) (currentPosition * 100 / total);
                    mList.get(playPosition).setCurrentProgress(progress);
                    handler.sendEmptyMessageDelayed(PROGRESS_ID, INTERMINATE_TIME);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            if (msg.what == PROGRESS_ID) {
                notifyActivityRefreshUI();
            }
            return true;
        }
    });


    @Override
    public void onDestroy() {
        super.onDestroy();
        mediaPlayer.stop();
    }
}