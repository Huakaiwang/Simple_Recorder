package com.example.simple_recorder.audio;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.RadioGroup;
import android.widget.SeekBar;

import com.example.simple_recorder.R;
import com.example.simple_recorder.audio.AudioService.AudioBinder;
import com.example.simple_recorder.bean.AudioBean;
import com.example.simple_recorder.databinding.ActivityAudioListBinding;
import com.example.simple_recorder.databinding.ActivityMainBinding;
import com.example.simple_recorder.expandelist.ExpandListActivity;
import com.example.simple_recorder.notepad.NotepadActivity;
import com.example.simple_recorder.recorder.RecorderActivity;
import com.example.simple_recorder.recorder.RecorderService;
import com.example.simple_recorder.utils.AudioInfoDialog;
import com.example.simple_recorder.utils.AudioInfoUtils;
import com.example.simple_recorder.utils.Contants;
import com.example.simple_recorder.utils.DialogUtils;
import com.example.simple_recorder.utils.RenameDialog;
import com.example.simple_recorder.utils.StartSystemPageUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.SimpleFormatter;

public class AudioListActivity extends AppCompatActivity {
    private ActivityAudioListBinding binding;
    private List<AudioBean> mDatas;
    private AudioListAdapter adapter;
    private AudioService audioService;
    //????????????
    ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            AudioBinder audioBinder = (AudioBinder) service;
            Log.d("audio", "onServiceConnected: " + audioBinder.getService());
            audioService = audioBinder.getService();
            audioService.setOnPlayChangeListener(playChangeListener);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    public boolean moveTaskToBack(boolean nonRoot) {
        return super.moveTaskToBack(true);
    }
    //????????????
    AudioService.OnPlayChangeListener playChangeListener = new AudioService.OnPlayChangeListener() {
        @Override
        public void playChange(int changePos) {
            adapter.notifyDataSetChanged();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAudioListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        //?????????????????????
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.grey));
            //???????????????????????????
            setDarkStatusIcon(true);
        }
        //????????????
        Intent intent = new Intent(this, AudioService.class);
        bindService(intent, connection, BIND_AUTO_CREATE);
        //???ListView???????????????????????????
        mDatas = new ArrayList<>();
        adapter = new AudioListAdapter(this, mDatas);
        binding.audioLv.setAdapter(adapter);
        //?????????????????????????????????????????????
        Contants.setsAudioList(mDatas);
        //????????????
        loadDatas();
        //??????????????????
        setEvents();


    }

    /**
     * ?????????????????????
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

    //??????onKeyDown???????????????????????????????????????app????????????????????????
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            StartSystemPageUtils.goToHomePage(this);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //????????????
        unbindService(connection);
    }

    /*
     * ????????????
     * */
    private void setEvents() {
        adapter.setOnItemPlayClickListener(playClickListener);//???????????????????????????
        binding.audioLv.setOnItemLongClickListener(longClickListener);//ListView???????????????
        adapter.setOnSeekBarChangeListener(onSeekBarChangeListener);//????????????????????????
        binding.audioIb.setOnClickListener(onClickListener);//??????????????????
        binding.rgTab.setOnCheckedChangeListener(changeListener);//?????????????????????
    }

    //?????????????????????
    RadioGroup.OnCheckedChangeListener changeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (checkedId == R.id.rb_noter) {
                Intent intent = new Intent(AudioListActivity.this, ExpandListActivity.class);
                startActivity(intent);
                AudioListActivity.this.overridePendingTransition(0, 0);
                binding.rbNoter.setChecked(false);
            }
        }
    };
    //????????????????????????????????????
    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //????????????
            audioService.closeMusic();
            //?????????????????????
            startActivity(new Intent(AudioListActivity.this, RecorderActivity.class));
            //????????????Activity
            finish();
        }
    };
    //??????SeekBar??????????????????
    AudioListAdapter.OnSeekBarChangeListener onSeekBarChangeListener = new AudioListAdapter.OnSeekBarChangeListener() {
        @Override
        public void onChange(AudioListAdapter adapter, View convertView, SeekBar playView, int position) {
            int current = playView.getProgress();
            audioService.changeAudioByCurrent(current, position);
        }
    };
    //????????????????????????????????????????????????
    AudioListAdapter.OnItemPlayClickListener playClickListener = new AudioListAdapter.OnItemPlayClickListener() {
        @Override
        public void onItemPlayClick(AudioListAdapter adapter, View convertView, View playView, int position) {
            for (int i = 0; i < mDatas.size(); i++) {
                if (i == position) {
                    continue;
                }
                AudioBean audioBean = mDatas.get(i);
                audioBean.setPlaying(false);
            }
            //????????????????????????
            boolean playing = mDatas.get(position).isPlaying();
            mDatas.get(position).setPlaying(!playing);
            adapter.notifyDataSetChanged();
            audioService.cutMusicOrPause(position);
        }
    };
    //???????????????????????????
    AdapterView.OnItemLongClickListener longClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            //ListView???item??????????????????????????????
            parent.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
            //??????????????????
            showPopMenu(view, position);
            return false;
        }
    };

    //??????item??????menu??????
    private void showPopMenu(View view, int position) {
        PopupMenu popupMenu = new PopupMenu(this, view, Gravity.RIGHT);
        MenuInflater menuInflater = popupMenu.getMenuInflater();
        menuInflater.inflate(R.menu.audio_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_info:
                        showFileInfoDialog(position);
                        break;
                    case R.id.menu_del:
                        deleteFileByPos(position);
                        break;
                    case R.id.menu_rename:
                        showRenameDialog(position);
                        break;
                }
                return false;
            }

        });
        popupMenu.show();
    }

    //??????????????????????????????
    private void showFileInfoDialog(int position) {
        AudioBean bean = mDatas.get(position);
        AudioInfoDialog audioInfoDialog = new AudioInfoDialog(this);
        audioInfoDialog.show();
        audioInfoDialog.setDialogWidth();
        audioInfoDialog.setFileInfo(bean);
        audioInfoDialog.setCanceledOnTouchOutside(true);
    }

    //??????????????????
    private void showRenameDialog(int position) {
        AudioBean bean = mDatas.get(position);
        String title = bean.getTitle();
        RenameDialog dialog = new RenameDialog(this);
        dialog.show();
        dialog.setDialogWidth();
        dialog.setTipText(title);
        dialog.setOnEnsureListener(new RenameDialog.OnEnsureListener() {
            @Override
            public void onEnsure(String msg) {
                renameByPosition(msg, position);
            }
        });
    }

    //????????????????????????????????????
    private void renameByPosition(String msg, int position) {
        AudioBean audioBean = mDatas.get(position);
        if (audioBean.getTitle().equals(msg)) {
            return;
        }
        String path = audioBean.getPath();
        String fileSuffix = audioBean.getFileSuffix();
        File srcFile = new File(path);//???????????????
        //??????????????????
        String destPath = srcFile.getParent() + File.separator + msg + fileSuffix;
        File destFile = new File(destPath);
        //???????????????????????????
        srcFile.renameTo(destFile);
        //???????????????????????????
        audioBean.setTitle(msg);
        audioBean.setPath(destPath);
        adapter.notifyDataSetChanged();
    }

    //???????????????????????????
    private void deleteFileByPos(int position) {
        AudioBean bean = mDatas.get(position);
        String title = bean.getTitle();
        String path = bean.getPath();
        DialogUtils.showNormalDialog(this, "????????????", "????????????????????????,????????????????????????????",
                "??????", new DialogUtils.OnLeftClickListener() {
                    @Override
                    public void onLeftClick() {
                        //??????????????????????????????
                        audioService.closeMusic();
                        File file = new File(path);
                        file.getAbsoluteFile().delete();//??????????????????
                        mDatas.remove(bean);//???mDatas?????????????????????
                        adapter.notifyDataSetChanged();//??????listView?????????
                    }
                }, "??????", null);
    }


    //??????????????????
    private void loadDatas() {
        //1.????????????????????????????????????
        File fetchFile = new File(Contants.PATH_FETCH_DIR_RECORD);
        File[] listFiles = fetchFile.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                if (new File(file, s).isDirectory()) {
                    return false;
                }
                if (s.endsWith(".mp3") || s.endsWith(".amr")) {
                    return true;
                }
                return false;
            }
        });
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        AudioInfoUtils audioInfoUtils = AudioInfoUtils.getInstance();
        if (listFiles.length > 0) {
            for (int i = 0; i < listFiles.length; i++) {
                File listFile = listFiles[i];
                String fname = listFile.getName();//??????????????????
                String title = fname.substring(0, fname.lastIndexOf("."));
                String suffix = fname.substring(fname.lastIndexOf("."));
                //?????????????????????????????????
                long flastMod = listFile.lastModified();
                String time = sdf.format(flastMod);//?????????????????????????????????
                //?????????????????????
                long flength = listFile.length();
                //?????????????????????
                String audioPath = listFile.getAbsolutePath();
                long duration = audioInfoUtils.getAudioFileDuration(audioPath);
                String formatDuration = audioInfoUtils.getAudioFileFormatDuration(duration);
                AudioBean audioBean = new AudioBean(i + "", title, time, formatDuration, audioPath,
                        duration, flastMod, suffix, flength);
                mDatas.add(audioBean);
            }
        } else {
            Log.d("????????????", "loadDatas: ");
        }
        audioInfoUtils.releseRetriever();//????????????????????????????????????

        //??????????????????????????????????????????????????????????????????
        Collections.sort(mDatas, new Comparator<AudioBean>() {
            @Override
            public int compare(AudioBean o1, AudioBean o2) {
                if (o1.getLastModified() < o2.getLastModified()) {
                    return 1;//????????????>0???o1??????o2??????
                } else if (o1.getLastModified() == o2.getLastModified()) {
                    return 0;
                }
                return -1;
            }
        });
        adapter.notifyDataSetChanged();
    }
}