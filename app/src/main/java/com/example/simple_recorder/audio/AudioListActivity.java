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
    //绑定服务
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
    //刷新页面
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
        //设置状态栏颜色
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.grey));
            //这是状态栏文字反色
            setDarkStatusIcon(true);
        }
        //绑定服务
        Intent intent = new Intent(this, AudioService.class);
        bindService(intent, connection, BIND_AUTO_CREATE);
        //为ListView设置数据源和适配器
        mDatas = new ArrayList<>();
        adapter = new AudioListAdapter(this, mDatas);
        binding.audioLv.setAdapter(adapter);
        //将音频对象集合保存到全局变量中
        Contants.setsAudioList(mDatas);
        //加载数据
        loadDatas();
        //设置监听事件
        setEvents();


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

    //重写onKeyDown方法，使得点击返回键不退出app，而是跳转至桌面
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
        //解绑服务
        unbindService(connection);
    }

    /*
     * 设置监听
     * */
    private void setEvents() {
        adapter.setOnItemPlayClickListener(playClickListener);//播放按钮的点击事件
        binding.audioLv.setOnItemLongClickListener(longClickListener);//ListView的长按事件
        adapter.setOnSeekBarChangeListener(onSeekBarChangeListener);//拖动进度条的事件
        binding.audioIb.setOnClickListener(onClickListener);//跳转录音界面
        binding.rgTab.setOnCheckedChangeListener(changeListener);//跳转备忘录界面
    }

    //跳转记事本界面
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
    //点击录音按钮跳转录音界面
    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //关闭音乐
            audioService.closeMusic();
            //跳转到录音界面
            startActivity(new Intent(AudioListActivity.this, RecorderActivity.class));
            //销毁当前Activity
            finish();
        }
    };
    //拖动SeekBar改变音频进度
    AudioListAdapter.OnSeekBarChangeListener onSeekBarChangeListener = new AudioListAdapter.OnSeekBarChangeListener() {
        @Override
        public void onChange(AudioListAdapter adapter, View convertView, SeekBar playView, int position) {
            int current = playView.getProgress();
            audioService.changeAudioByCurrent(current, position);
        }
    };
    //点击每一个播放按钮都会回调的方法
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
            //获取当前播放状态
            boolean playing = mDatas.get(position).isPlaying();
            mDatas.get(position).setPlaying(!playing);
            adapter.notifyDataSetChanged();
            audioService.cutMusicOrPause(position);
        }
    };
    //设置长按事件的监听
    AdapterView.OnItemLongClickListener longClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            //ListView中item优先于子控件获取焦点
            parent.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
            //绘制弹出菜单
            showPopMenu(view, position);
            return false;
        }
    };

    //长按item弹出menu窗口
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

    //显示文件详情的对话框
    private void showFileInfoDialog(int position) {
        AudioBean bean = mDatas.get(position);
        AudioInfoDialog audioInfoDialog = new AudioInfoDialog(this);
        audioInfoDialog.show();
        audioInfoDialog.setDialogWidth();
        audioInfoDialog.setFileInfo(bean);
        audioInfoDialog.setCanceledOnTouchOutside(true);
    }

    //重命名对话框
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

    //对于指定位置的文件重命名
    private void renameByPosition(String msg, int position) {
        AudioBean audioBean = mDatas.get(position);
        if (audioBean.getTitle().equals(msg)) {
            return;
        }
        String path = audioBean.getPath();
        String fileSuffix = audioBean.getFileSuffix();
        File srcFile = new File(path);//原来的文件
        //获取修改路径
        String destPath = srcFile.getParent() + File.separator + msg + fileSuffix;
        File destFile = new File(destPath);
        //进行重命名物理操作
        srcFile.renameTo(destFile);
        //从内存当中进行修改
        audioBean.setTitle(msg);
        audioBean.setPath(destPath);
        adapter.notifyDataSetChanged();
    }

    //删除指定位置的文件
    private void deleteFileByPos(int position) {
        AudioBean bean = mDatas.get(position);
        String title = bean.getTitle();
        String path = bean.getPath();
        DialogUtils.showNormalDialog(this, "提示信息", "删除后将无法恢复,是否删除该指定文件?",
                "确定", new DialogUtils.OnLeftClickListener() {
                    @Override
                    public void onLeftClick() {
                        //删除前先关闭音乐资源
                        audioService.closeMusic();
                        File file = new File(path);
                        file.getAbsoluteFile().delete();//物理删除文件
                        mDatas.remove(bean);//从mDatas中删除文件对象
                        adapter.notifyDataSetChanged();//刷新listView的内容
                    }
                }, "取消", null);
    }


    //加载文件数据
    private void loadDatas() {
        //1.获取指定路径下的音源文件
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
                String fname = listFile.getName();//文件名带后缀
                String title = fname.substring(0, fname.lastIndexOf("."));
                String suffix = fname.substring(fname.lastIndexOf("."));
                //获取文件最后修改的时间
                long flastMod = listFile.lastModified();
                String time = sdf.format(flastMod);//转换为固定格式的字符串
                //获取文件的大小
                long flength = listFile.length();
                //获取文件的路径
                String audioPath = listFile.getAbsolutePath();
                long duration = audioInfoUtils.getAudioFileDuration(audioPath);
                String formatDuration = audioInfoUtils.getAudioFileFormatDuration(duration);
                AudioBean audioBean = new AudioBean(i + "", title, time, formatDuration, audioPath,
                        duration, flastMod, suffix, flength);
                mDatas.add(audioBean);
            }
        } else {
            Log.d("文件相关", "loadDatas: ");
        }
        audioInfoUtils.releseRetriever();//释放多媒体资料的资源对象

        //将集合中的元素重新排序，按照时间先后顺序排序
        Collections.sort(mDatas, new Comparator<AudioBean>() {
            @Override
            public int compare(AudioBean o1, AudioBean o2) {
                if (o1.getLastModified() < o2.getLastModified()) {
                    return 1;//返回值为>0，o1排在o2之后
                } else if (o1.getLastModified() == o2.getLastModified()) {
                    return 0;
                }
                return -1;
            }
        });
        adapter.notifyDataSetChanged();
    }
}