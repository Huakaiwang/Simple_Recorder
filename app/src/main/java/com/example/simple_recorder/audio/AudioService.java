package com.example.simple_recorder.audio;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.example.simple_recorder.R;
import com.example.simple_recorder.bean.AudioBean;
import com.example.simple_recorder.utils.Contants;

import java.util.List;

public class AudioService extends Service implements MediaPlayer.OnCompletionListener {
    private MediaPlayer mediaPlayer = null;
    private List<AudioBean> mList;//播放列表
    private int playPosition = -1;//当前播放位置
    private RemoteViews remoteView;//通知对应的布局
    private NotificationManager manager;
    private final int NOTIFY_ID_MUSIC = 100;//发送通知的id
    private AudioReceiver receiver;
    /*
     * 接收通知发出的广播的action
     * */
    private final String PRE_ACTION_LAST = "com.animee.last";
    private final String PRE_ACTION_PLAY = "com.animee.play";
    private final String PRE_ACTION_NEXT = "com.animee.next";
    private final String PRE_ACTION_CLOSE = "com.animee.close";
    private Notification notification;

    public AudioService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initRegisterReceiver();
        initRemoteView();
        initNotification();
    }


    //创建广播接收者
    class AudioReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            notifyUIControl(action);
        }

        private void notifyUIControl(String action) {
            switch (action) {
                case PRE_ACTION_LAST:
                    previousMusic();
                    break;
                case PRE_ACTION_PLAY:
                    pauseOrContinueMusic();
                    break;
                case PRE_ACTION_NEXT:
                    nextMusic();
                    break;
                case PRE_ACTION_CLOSE:
                    closeNotification();
                    break;
            }
        }
    }


    //注册广播接收者，用于接受用户点击通知栏按钮发出的信息
    private void initRegisterReceiver() {
        receiver = new AudioReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(PRE_ACTION_LAST);
        filter.addAction(PRE_ACTION_PLAY);
        filter.addAction(PRE_ACTION_NEXT);
        filter.addAction(PRE_ACTION_CLOSE);
        registerReceiver(receiver, filter);
    }

    //设置通知栏显示效果以及通知的点击事件
    private void initRemoteView() {
        remoteView = new RemoteViews(getPackageName(), R.layout.notify_audio);
        PendingIntent lastPI = PendingIntent.getBroadcast(this, 1, new Intent(PRE_ACTION_LAST), PendingIntent.FLAG_UPDATE_CURRENT);
        remoteView.setOnClickPendingIntent(R.id.ny_iv_last, lastPI);

        PendingIntent playPI = PendingIntent.getBroadcast(this, 1, new Intent(PRE_ACTION_PLAY), PendingIntent.FLAG_UPDATE_CURRENT);
        remoteView.setOnClickPendingIntent(R.id.ny_iv_play, playPI);

        PendingIntent nextPI = PendingIntent.getBroadcast(this, 1, new Intent(PRE_ACTION_NEXT), PendingIntent.FLAG_UPDATE_CURRENT);
        remoteView.setOnClickPendingIntent(R.id.ny_iv_next, nextPI);

        PendingIntent closePI = PendingIntent.getBroadcast(this, 1, new Intent(PRE_ACTION_CLOSE), PendingIntent.FLAG_UPDATE_CURRENT);
        remoteView.setOnClickPendingIntent(R.id.ny_iv_close, closePI);
    }

    //初始化通知栏
    private void initNotification() {
        String channelID = "1";
        String channelName = "channel_name";
        NotificationChannel channel = new NotificationChannel(channelID, channelName, NotificationManager.IMPORTANCE_LOW);
        manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.createNotificationChannel(channel);
        Notification.Builder builder = new Notification.Builder(this, channelID);
        builder.setSmallIcon(R.mipmap.icon_app_logo)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.icon_app_logo))
                .setCustomContentView(remoteView)
                .setAutoCancel(false)
                .setOngoing(true)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setChannelId(channelID);
        notification = builder.build();
    }

    /*
     * 更新通知栏信息
     * */
    private void updateNotification(int playPosition) {
        //根据多媒体的播放状态显示图片
        if (mediaPlayer.isPlaying()) {
            remoteView.setImageViewResource(R.id.ny_iv_play, R.mipmap.red_pause);
        } else {
            remoteView.setImageViewResource(R.id.ny_iv_play, R.mipmap.red_play);
        }
        remoteView.setTextViewText(R.id.ny_tv_tiltle, mList.get(playPosition).getTitle());
        remoteView.setTextViewText(R.id.ny_tv_durtion, mList.get(playPosition).getDuration());
        //发送通知
        manager.notify(NOTIFY_ID_MUSIC, notification);
    }

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
            updateNotification(position);
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
        updateNotification(playPosition);
    }

    //关闭通知栏，停止播放音乐
    private void closeNotification() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            mList.get(playPosition).setPlaying(false);
        }
        manager.cancel(NOTIFY_ID_MUSIC);
    }

    //播放下一曲
    private void nextMusic() {
        mList.get(playPosition).setPlaying(false);
        if (playPosition >= mList.size() - 1) {
            playPosition = 0;
        } else {
            playPosition++;
        }
        mList.get(playPosition).setPlaying(true);
        play(playPosition);
    }

    //播放上一曲
    private void previousMusic() {
        mList.get(playPosition).setPlaying(false);
        //当前播放位置为0,则播放mList对象数组中的最后一曲，否则播放上一曲
        if (playPosition == 0) {
            playPosition = mList.size() - 1;
        } else {
            playPosition--;
        }
        mList.get(playPosition).setPlaying(true);
        play(playPosition);
    }

    //停止音乐
    public void closeMusic() {
        if (mediaPlayer != null) {
            setFlagControlThread(false);
            closeNotification();
            mediaPlayer.stop();
            playPosition = -1;
        }
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
        if (receiver != null) {
            unregisterReceiver(receiver);
        }
        closeMusic();
    }
}