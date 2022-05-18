package com.example.simple_recorder.recorder;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;

import com.example.simple_recorder.R;
import com.example.simple_recorder.utils.Contants;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RecorderService extends Service {
    private MediaRecorder recorder;
    private boolean isAlive = false;//控制线程
    private String recorderDirpath;//存放录音的公共目录
    private SimpleDateFormat sdf,calSdf;
    private int time;
    private RemoteViews remoteView;
    private NotificationManager manager;
    private Notification notification;
    private int NOTIFY_ID_RECORDER = 102;

    public MediaRecorder getRecorder() {
        return recorder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sdf = new SimpleDateFormat("yyyMMdd_HHmmss");
        calSdf = new SimpleDateFormat("HH:mm:ss");
        recorderDirpath = Contants.PATH_FETCH_DIR_RECORD;
        initRemoteView();
        initNotification();
    }
/*
* 初始化通知对象
* */
    private void initNotification() {
        String channelID = "2";
        String channelName = "channel_name";
        NotificationChannel channel = new NotificationChannel(channelID, channelName, NotificationManager.IMPORTANCE_LOW);
        manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.createNotificationChannel(channel);
        Notification.Builder builder = new Notification.Builder(this,channelID);
        builder.setSmallIcon(R.mipmap.icon_voice)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.icon_voice))
                .setCustomContentView(remoteView)
                .setAutoCancel(false)
                .setOngoing(true)
                .setCategory(Notification.CATEGORY_SERVICE);
        notification = builder.build();
    }
/*
* 更新发送通知的函数
* */
    private void updateNotification(String calTime){
        remoteView.setTextViewText(R.id.ny_time,calTime);
        manager.notify(NOTIFY_ID_RECORDER,notification);
    }

    /*
* 初始化通知当中的远程View
* */
    private void initRemoteView() {
        remoteView = new RemoteViews(getPackageName(), R.layout.notify_recorder);
        Intent intent = new Intent(this, RecorderActivity.class);
        PendingIntent activity = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteView.setOnClickPendingIntent(R.id.ny_layout,activity);
    }
    /*
    * 关闭通知
    * */
    private void closeNotification(){
        manager.cancel(NOTIFY_ID_RECORDER);
    }
    //设置更新Activity的UI界面的回调接口
    public interface OnRefreshUIThreadListener{
        void onRefresh(int db,String time);
    }
    private OnRefreshUIThreadListener onRefreshUIThreadListener;

    public void setOnRefreshUIThreadListener(OnRefreshUIThreadListener onRefreshUIThreadListener) {
        this.onRefreshUIThreadListener = onRefreshUIThreadListener;
    }

    Handler handler = new Handler(new Handler.Callback() {
    @Override
    public boolean handleMessage(@NonNull Message msg) {
        if (recorder==null) return false;
        double ratio = (double) recorder.getMaxAmplitude()/100;
        double db = 0;
        if (ratio > 1) {
            db = 20 * Math.log10(ratio);
        }
        time+=1000;
        if (onRefreshUIThreadListener!=null) {
            String timeStr = calTime(time);
            onRefreshUIThreadListener.onRefresh((int) db,timeStr);
            updateNotification(timeStr);
        }
        return false;
    }
});
//计算时间为指定格式
    private String calTime(int mSecond) {
        mSecond-=8*60*60*1000;
        String format = calSdf.format(new Date(mSecond));
        return format;
    }

    /*
* 开启子线程，实时的获取音量，以及当前录制的时间，反馈给主线程
* */
    Thread thread = new Thread(new Runnable() {
    @Override
    public void run() {
        while (isAlive) {
            handler.sendEmptyMessage(0);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
});
/*
* 开启录音
* */
    public void startRecorder(){
        if (recorder == null) {
            recorder = new MediaRecorder();
        }
        isAlive = true;
        recorder.reset();
        //设置录音对象的参数
        setRecorder();
        try {
            recorder.prepare();
            recorder.start();
            thread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
/*
* 停止录音
* */
    public void stopRecorder(){
        if (recorder!=null) {
            isAlive = false;
            recorder.stop();
            recorder = null;
            time = 0;
            closeNotification();
        }
    }
    private void setRecorder() {
        //获取麦克风的声音
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        //设置输出格式
        recorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_WB);
        //设置编码格式
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB);
        //设置输出文件
        String time = sdf.format(new Date());
        File file = new File(recorderDirpath,time+".amr");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        recorder.setOutputFile(file);
        //设置最多录制的时间
        recorder.setMaxDuration(10*60*1000);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new RecorderBinder();
    }
    public class RecorderBinder extends Binder {
        public RecorderService getService(){return RecorderService.this;}
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        stopRecorder();
    }
}