package com.example.simple_recorder.recorder;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;

import com.example.simple_recorder.R;
import com.example.simple_recorder.audio.AudioListActivity;
import com.example.simple_recorder.databinding.ActivityRecorderBinding;

public class RecorderActivity extends AppCompatActivity {
    private ActivityRecorderBinding binding;
    private RecorderService recorderService;
    ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            RecorderService.RecorderBinder binder = (RecorderService.RecorderBinder)service;
            recorderService = binder.getService();
            recorderService.startRecorder();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRecorderBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Intent intent = new Intent(this,RecorderService.class);
        bindService(intent,connection,BIND_AUTO_CREATE);
    }
    public void onClick(View view){
        switch (view.getId()) {
            case R.id.iv_back:

                break;
            case R.id.iv_stop:
                recorderService.stopRecorder();
                Intent intent = new Intent(this, AudioListActivity.class);
                startActivity(intent);
                finish();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //解绑服务
        if (connection!=null) {
            unbindService(connection);
        }
    }
}