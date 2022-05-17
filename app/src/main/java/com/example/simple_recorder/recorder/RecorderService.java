package com.example.simple_recorder.recorder;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Binder;
import android.os.IBinder;

import com.example.simple_recorder.utils.Contants;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RecorderService extends Service {
    private MediaRecorder recorder;
    private boolean isAlive = false;
    private String recorderDirpath;//存放录音的公共目录
    private SimpleDateFormat sdf;
    @Override
    public void onCreate() {
        super.onCreate();
        sdf = new SimpleDateFormat("yyyMMdd_HHmmss");
        recorderDirpath = Contants.PATH_FETCH_DIR_RECORD;
    }
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
/*
* 停止录音
* */
    public void stopRecorder(){
        if (recorder!=null) {
            recorder.stop();
            recorder = null;
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
    }
}