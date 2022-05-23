package com.example.simple_recorder.recorder;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.view.KeyEvent;
import android.view.View;

import com.example.simple_recorder.R;
import com.example.simple_recorder.audio.AudioListActivity;
import com.example.simple_recorder.databinding.ActivityRecorderBinding;
import com.example.simple_recorder.utils.StartSystemPageUtils;

public class RecorderActivity extends AppCompatActivity {
    private ActivityRecorderBinding binding;
    private RecorderService recorderService;
    RecorderService.OnRefreshUIThreadListener refreshUIListener = new RecorderService.OnRefreshUIThreadListener() {
        @Override
        public void onRefresh(int db, String time) {
            binding.voicLine.setVolume(db);
            binding.tvDuration.setText(time);
        }
    };
    ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            RecorderService.RecorderBinder binder = (RecorderService.RecorderBinder)service;
            recorderService = binder.getService();
            recorderService.startRecorder();
            recorderService.setOnRefreshUIThreadListener(refreshUIListener);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setDarkStatusIcon(true);
        binding = ActivityRecorderBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Intent intent = new Intent(this,RecorderService.class);
        bindService(intent,connection,BIND_AUTO_CREATE);
    }
    /**
     * 设置状态栏反色
     */
    protected void setDarkStatusIcon(boolean isDark) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decorView = getWindow().getDecorView();
            if (decorView != null) {
                int vis = decorView.getSystemUiVisibility();
                if (isDark) {
                    vis |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                } else {
                    vis &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                }
                decorView.setSystemUiVisibility(vis);
            }
        }
    }
    //点击事件
    public void onClick(View view){
        switch (view.getId()) {
            case R.id.iv_back:
                if (recorderService.getRecorder()!=null) {
                    Intent intent = new Intent(this,AudioListActivity.class);
                    startActivity(intent);
                }else{
                    StartSystemPageUtils.goToHomePage(this);
                }
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
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            StartSystemPageUtils.goToHomePage(this);
            return true;
        }
        return super.onKeyDown(keyCode, event);
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