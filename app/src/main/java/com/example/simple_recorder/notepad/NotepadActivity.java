package com.example.simple_recorder.notepad;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Instrumentation;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.example.simple_recorder.R;
import com.example.simple_recorder.bean.NotepadBean;
import com.example.simple_recorder.databinding.ActivityNotepadBinding;
import com.example.simple_recorder.utils.DBUtils;
import com.example.simple_recorder.utils.DialogUtils;
import com.example.simple_recorder.utils.SQLiteHelper;

import java.util.ArrayList;
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
        mList = new ArrayList<>();
        loadsDatas();
        setEvent();
    }

    private void loadsDatas() {
        helper = new SQLiteHelper(this);
        mList = helper.query();
        notepadAdapter = new NotepadAdapter(this,mList);
        notepadBinding.noteLv.setAdapter(notepadAdapter);
        notepadAdapter.notifyDataSetChanged();
    }
//设置事件
    private void setEvent() {
        notepadBinding.noteLv.setOnItemLongClickListener(longClickListener);
        notepadBinding.noteAdd.setOnClickListener(onClickListener);
    }
    //设置跳转添加页面的监听
    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(NotepadActivity.this,RecordActivity.class);
            startActivity(intent);
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
    //删除记事本中position指定的note
    private void deleteNoteByPos(int position) {
        DialogUtils.showNormalDialog(this, "提示信息", "删除后将无法恢复,是否删除该指定文件?",
                "确定", new DialogUtils.OnLeftClickListener() {
                    @Override
                    public void onLeftClick() {
                        //从数据库删除指定的note
                        helper.deleteData(mList.get(position).getId());
                        //从数组中删除指定的note
                        mList.remove(mList.get(position));
                        notepadAdapter.notifyDataSetChanged();
                    }
                },"取消",null);
    }
    public void refreshUI(){
        notepadAdapter.notifyDataSetChanged();
    }
}