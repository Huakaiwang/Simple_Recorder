package com.example.simple_recorder.notepad;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.Instrumentation;
import android.content.ClipData;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.simple_recorder.R;
import com.example.simple_recorder.audio.AudioListActivity;
import com.example.simple_recorder.bean.NotepadBean;
import com.example.simple_recorder.databinding.ActivityNotepadBinding;
import com.example.simple_recorder.utils.DBUtils;
import com.example.simple_recorder.utils.DialogUtils;
import com.example.simple_recorder.utils.SQLiteHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class NotepadActivity extends AppCompatActivity {
    private List<NotepadBean> mList;
    private SQLiteHelper helper;
    private NotepadAdapter notepadAdapter;
    private ActivityNotepadBinding notepadBinding;
    private String id;
    private String content;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        notepadBinding = ActivityNotepadBinding.inflate(getLayoutInflater());
        setContentView(notepadBinding.getRoot());
        //?????????????????????
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.grey));
            //???????????????????????????
            setDarkStatusIcon(true);
        }
        mList = new ArrayList<>();
        loadsDatas();
        setEvent();
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
    //????????????
    private void loadsDatas() {
        helper = new SQLiteHelper(this);
        mList = helper.query();
        //?????????????????????
        Collections.sort(mList, new Comparator<NotepadBean>() {
            @Override
            public int compare(NotepadBean o1, NotepadBean o2) {
                if (toInt(o1.getNotepadTime())<toInt(o2.getNotepadTime())) {
                    return 1;
                }else if (toInt(o1.getNotepadTime())==toInt(o2.getNotepadTime())){
                    return 0;
                }
                return -1;
            }
        });
        notepadAdapter = new NotepadAdapter(this,mList);
        notepadBinding.noteLv.setAdapter(notepadAdapter);
        notepadAdapter.notifyDataSetChanged();
    }
//????????????
    private void setEvent() {
        notepadBinding.noteLv.setOnItemLongClickListener(longClickListener);
        notepadBinding.noteLv.setOnItemClickListener(itemlistener);
        notepadBinding.noteAdd.setOnClickListener(onClickListener);
        notepadBinding.rgTab2.setOnCheckedChangeListener(checkedChangeListener);
    }
    AdapterView.OnItemClickListener itemlistener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id2) {
            id = mList.get(position).getId();
            content = mList.get(position).getNotepadContent();
            Intent intent = new Intent(NotepadActivity.this,RecordActivity.class);
            intent.putExtra("id",id);
            intent.putExtra("content",content);
            startActivity(intent);
        }
    };
    //????????????????????????
    RadioGroup.OnCheckedChangeListener checkedChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (checkedId == R.id.rb_recorder2) {
                Intent intent = new Intent(NotepadActivity.this, AudioListActivity.class);
                startActivity(intent);
                NotepadActivity.this.overridePendingTransition(0,0);
                notepadBinding.rbRecorder2.setChecked(false);
            }
        }
    };
    //?????????????????????????????????
    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(NotepadActivity.this,RecordActivity.class);
            startActivity(intent);
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
            return true;
        }

        private void showPopMenu(View view, int position) {
            PopupMenu popupMenu = new PopupMenu(NotepadActivity.this, view, Gravity.RIGHT);
            MenuInflater menuInflater = popupMenu.getMenuInflater();
            menuInflater.inflate(R.menu.note_menu, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.note_update:
                            id = mList.get(position).getId();
                            content = mList.get(position).getNotepadContent();
                            Intent intent = new Intent(NotepadActivity.this,RecordActivity.class);
                            intent.putExtra("id",id);
                            intent.putExtra("content",content);
                            startActivity(intent);
                            break;
                        case R.id.note_del:
                            deleteNoteByPos(position);
                            break;
                    }
                    return false;
                }

            });
            popupMenu.show();
        }
    };
    //??????????????????position?????????note
    private void deleteNoteByPos(int position) {
        DialogUtils.showNormalDialog(this, "????????????", "????????????????????????,????????????????????????????",
                "??????", new DialogUtils.OnLeftClickListener() {
                    @Override
                    public void onLeftClick() {
                        //???????????????????????????note
                        helper.deleteData(mList.get(position).getId());
                        //???????????????????????????note
                        mList.remove(mList.get(position));
                        notepadAdapter.notifyDataSetChanged();
                    }
                },"??????",null);
    }
    //??????time??????int??????
    private long toInt(String str){
        String str2= "";
        if (str!=null && !"".equals(str)) {
            for (int i1 = 0; i1 < str.length(); i1++) {
                if (str.charAt(i1)>=48 && str.charAt(i1)<=57) {
                    str2+=str.charAt(i1);
                }
            }
        }
        return Long.parseLong(str2);
    }

}