package com.example.simple_recorder.audio;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.PopupMenu;

import com.example.simple_recorder.R;
import com.example.simple_recorder.bean.AudioBean;
import com.example.simple_recorder.databinding.ActivityAudioListBinding;
import com.example.simple_recorder.databinding.ActivityMainBinding;
import com.example.simple_recorder.utils.AudioInfoUtils;
import com.example.simple_recorder.utils.Contants;

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
    private List<AudioBean>mDatas;
    private AudioListAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAudioListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        //为ListView设置数据源和适配器
        mDatas = new ArrayList<>();
        adapter = new AudioListAdapter(this,mDatas);
        binding.audioLv.setAdapter(adapter);
        //加载数据
        loadDatas();
        //设置监听事件
        setEvents();


    }
    /*
    * 设置监听
    * */
    private void setEvents() {
        adapter.setOnItemPlayClickListener(playClickListener);
        binding.audioLv.setOnItemLongClickListener(longClickListener);
    }
    //设置长按事件的监听
    AdapterView.OnItemLongClickListener longClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            showPopMenu(view,position);
            return false;
        }
    };
    //长按item弹出menu窗口
    private void showPopMenu(View view, int position) {
        PopupMenu popupMenu = new PopupMenu(this,view, Gravity.RIGHT);
        MenuInflater menuInflater = popupMenu.getMenuInflater();
        menuInflater.inflate(R.menu.audio_menu,popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_info:
                        break;
                    case R.id.menu_del:
                        break;
                    case R.id.menu_rename:
                        break;
                }
                return false;
            }
        });
        popupMenu.show();
    }

    //点击每一个播放按钮都会回调的方法
    AudioListAdapter.OnItemPlayClickListener playClickListener = new AudioListAdapter.OnItemPlayClickListener() {
        @Override
        public void onItemPlayClick(AudioListAdapter adapter, View convertView, View playView, int position) {

        }
    };

    private void loadDatas() {
        //1.获取指定路径下的音源文件

        File fetchFile = new File(Contants.PATH_FETCH_DIR_RECORD);
        File[] listFiles = fetchFile.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                if (new File(file,s).isDirectory()) {
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
        if(listFiles.length>0) {
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
        }else{
            Log.d("文件相关", "loadDatas: ");
        }
        audioInfoUtils.releseRetriever();//释放多媒体资料的资源对象

        //将集合中的元素重新排序，按照时间先后顺序排序
        Collections.sort(mDatas, new Comparator<AudioBean>() {
            @Override
            public int compare(AudioBean o1, AudioBean o2) {
                if (o1.getLastModified()<o2.getLastModified()) {
                    return 1;//返回值为>0，o1排在o2之后
                }else if(o1.getLastModified() == o2.getLastModified()) {
                    return 0;
                }
                return -1;
            }
        });
        adapter.notifyDataSetChanged();
    }
}