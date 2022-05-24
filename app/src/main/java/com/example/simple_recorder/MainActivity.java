package com.example.simple_recorder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;

import com.example.simple_recorder.audio.AudioListActivity;
import com.example.simple_recorder.databinding.ActivityMainBinding;
import com.example.simple_recorder.expandelist.ExpandListActivity;
import com.example.simple_recorder.notepad.NotepadActivity;
import com.example.simple_recorder.utils.Contants;
import com.example.simple_recorder.utils.IFileInter;
import com.example.simple_recorder.utils.PermissionUtils;
import com.example.simple_recorder.utils.SDCardUtils;

import java.io.File;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private int time= 3;//倒计时时间
    String[] permissions = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            if (message.what == 1) {
                time--;
                if (time==0) {
                    startActivity(new Intent(MainActivity.this, ExpandListActivity.class));
                    finish();
                }else {
                    binding.mainTv.setText(time+"");
                    handler.sendEmptyMessageDelayed(1,1000);
                }
            }
            return false;
        }
    });
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setDarkStatusIcon(true);
        binding.mainTv.setText(time+"");
        PermissionUtils.getInstance().onRequestPermission(this,permissions,listener);
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

    PermissionUtils.OnPermissionCallbackListener listener = new PermissionUtils.OnPermissionCallbackListener() {
        @Override
        public void onGranted() {
            //判断是否有应用文件夹，如果没有就创建应用文件夹
            createAppDir();
            //倒计时进入播放录音界面
            handler.sendEmptyMessageDelayed(1,1000);
        }

        @Override
        public void onDenied(List<String> deniedPermissions) {
            PermissionUtils.getInstance().showDialogTipUserGotoAppSetting(MainActivity.this);
        }
    };
    private void createAppDir(){
        File recorderDir = SDCardUtils.getInstance().createAppFetchDir(IFileInter.FETCH_DIR_AUDIO);
        Contants.PATH_FETCH_DIR_RECORD = recorderDir.getAbsolutePath();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionUtils.getInstance().onRequestPermissionResult(this,requestCode,permissions,grantResults);
    }
}