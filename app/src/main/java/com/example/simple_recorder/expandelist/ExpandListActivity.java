package com.example.simple_recorder.expandelist;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.simple_recorder.R;
import com.example.simple_recorder.audio.AudioListActivity;
import com.example.simple_recorder.bean.NoteGroupBean;
import com.example.simple_recorder.bean.NotepadBean;
import com.example.simple_recorder.databinding.ActivityExpandListBinding;
import com.example.simple_recorder.notepad.NotepadActivity;
import com.example.simple_recorder.notepad.NotepadAdapter;
import com.example.simple_recorder.notepad.RecordActivity;
import com.example.simple_recorder.utils.DBUtils;
import com.example.simple_recorder.utils.DialogUtils;
import com.example.simple_recorder.utils.GroupDialog;
import com.example.simple_recorder.utils.SQLiteHelper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ExpandListActivity extends AppCompatActivity {
    private ActivityExpandListBinding expandBinding;
    private ExpandeAdapter expandeAdapter;
    private NotepadAdapter notepadAdapter;
    private List<NoteGroupBean> gList;
    private SQLiteHelper helper;
    private List<NotepadBean> mList;
    private List<NotepadBean> searchList;
    private List<List<NotepadBean>> checkList = new ArrayList<List<NotepadBean>>();
    private String targetid;
    private String content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        expandBinding = ActivityExpandListBinding.inflate(getLayoutInflater());
        setContentView(expandBinding.getRoot());
        setDarkStatusIcon(true);
        loadsDatas();
        setEvent();
    }


    /**
     * 加载数据
     */
    private void loadsDatas() {
        helper = new SQLiteHelper(this);
        gList = helper.queryGroup();
        for (int i = 0; i < gList.size(); i++) {
            List<NotepadBean> tempList = new ArrayList<>();
            tempList = helper.queryByGroup(gList.get(i).getGroupId());
            //对数组进行排序
            Collections.sort(tempList, new Comparator<NotepadBean>() {
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
            checkList.add(tempList);
        }
        expandeAdapter= new ExpandeAdapter(this,gList,checkList);
        expandBinding.noteLv2.setAdapter(expandeAdapter);
        expandeAdapter.notifyDataSetChanged();
    }
    /**
     * 设置事件
     */
    private void setEvent() {
        expandBinding.noteLv2.setOnChildClickListener(childClickListener);
        expandBinding.noteLv2.setOnItemLongClickListener(longClickListener);
        expandBinding.noteAdd2.setOnClickListener(onClickListener);
        expandBinding.rgTab3.setOnCheckedChangeListener(checkedChangeListener);
        expandBinding.edSearch.setOnFocusChangeListener(focusChangeListener);
        expandBinding.edSearch.addTextChangedListener(textWatcher);
        expandBinding.ivSearchBack.setOnClickListener(backSearchListener);
        expandBinding.noteLv.setOnItemClickListener(searchItemListener);
    }
    //搜索结果的点击事件
    AdapterView.OnItemClickListener searchItemListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            targetid = searchList.get(position).getId();
            content = searchList.get(position).getNotepadContent();
            Intent intent = new Intent(ExpandListActivity.this,RecordActivity.class);
            intent.putExtra("id",targetid);
            intent.putExtra("content",content);
            intent.putExtra("gList",(Serializable) gList);
            intent.putExtra("group",searchList.get(position).getGroup_id());
            Log.d("TAG", "onChildClick: "+gList.size());
            intent.putExtra("pos",searchList.get(position).getGroup_id());
            startActivity(intent);
        }
    };
    //推出搜索界面
    View.OnClickListener backSearchListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            expandBinding.edSearch.setText("");
            expandBinding.noteLv2.setVisibility(View.VISIBLE);
            expandBinding.noteLv.setVisibility(View.GONE);
            expandBinding.ivSearchBack.setVisibility(View.GONE);
            expandBinding.edSearch.clearFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(),0);
        }
    };
    //搜索界面
    View.OnFocusChangeListener focusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {
                expandBinding.edSearch.setText("  ");
                expandBinding.noteLv2.setVisibility(View.GONE);
                expandBinding.noteLv.setVisibility(View.VISIBLE);
                expandBinding.ivSearchBack.setVisibility(View.VISIBLE);
            }
        }
    };
    //监听输入框内容改变，实时检索数据库并显示
    TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            String detail = expandBinding.edSearch.getText().toString().trim();
            if (detail.equals("")) {
                searchList= new ArrayList<>();
            }else {
                searchList= helper.queryByContent(detail);
                //对数组进行排序
                Collections.sort(searchList, new Comparator<NotepadBean>() {
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
            }
            notepadAdapter = new NotepadAdapter(ExpandListActivity.this,searchList);
            expandBinding.noteLv.setAdapter(notepadAdapter);
            notepadAdapter.notifyDataSetChanged();
        }
    };
    //跳转至录音机界面
    RadioGroup.OnCheckedChangeListener checkedChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (checkedId == R.id.rb_recorder3) {
                Intent intent = new Intent(ExpandListActivity.this, AudioListActivity.class);
                startActivity(intent);
                ExpandListActivity.this.overridePendingTransition(0,0);
                expandBinding.rbRecorder3.setChecked(false);
            }
        }
    };
    //设置前往的添加界面的跳转事件
    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(ExpandListActivity.this,RecordActivity.class);
            intent.putExtra("group","0");
            intent.putExtra("gList",(Serializable) gList);
            startActivity(intent);
        }
    };
    //为child设置点击事件
    ExpandableListView.OnChildClickListener childClickListener = new ExpandableListView.OnChildClickListener() {
        @Override
        public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
            targetid = checkList.get(groupPosition).get(childPosition).getId();
            content = checkList.get(groupPosition).get(childPosition).getNotepadContent();
            Intent intent = new Intent(ExpandListActivity.this,RecordActivity.class);
            intent.putExtra("id",targetid);
            intent.putExtra("content",content);
            intent.putExtra("gList",(Serializable) gList);
            intent.putExtra("group",checkList.get(groupPosition).get(childPosition).getGroup_id());
            Log.d("TAG", "onChildClick: "+gList.size());
            intent.putExtra("pos",checkList.get(groupPosition).get(childPosition).getGroup_id());
            startActivity(intent);
            return true;
        }
    };
    //为child设置长按事件
    AdapterView.OnItemLongClickListener longClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            final long packedPosition = expandBinding.noteLv2.getExpandableListPosition(position);
            final int groupPosition = ExpandableListView.getPackedPositionGroup(packedPosition);
            final int childPosition = ExpandableListView.getPackedPositionChild(packedPosition);
            Log.d("TAG", "onItemLongClick: "+groupPosition+childPosition);
            if (childPosition != -1) {
                //ListView中item优先于子控件获取焦点
                parent.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
                //绘制弹出菜单
                showPopMenu(view, groupPosition,childPosition);
                Log.d("TAG", "onItemLongClick: "+position);
            }else{
                PopupMenu popupGroupMenu = new PopupMenu(ExpandListActivity.this, view, Gravity.RIGHT);
                MenuInflater menuInflater = popupGroupMenu.getMenuInflater();
                menuInflater.inflate(R.menu.group_menu, popupGroupMenu.getMenu());
                popupGroupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.group_add:
                                showAddDialog();
                                expandeAdapter.notifyDataSetChanged();
                                break;
                            case R.id.group_update:
                                showUpdateDialog(groupPosition);
                                expandeAdapter.notifyDataSetChanged();
                                break;
                            case R.id.group_del:
                                delGroup(groupPosition);
                                expandeAdapter.notifyDataSetChanged();
                                break;
                        }
                        return false;
                    }
                });
                popupGroupMenu.show();
            }
            return true;
        }
    };
    //删除分组对话框
    private void delGroup(int groupPosition){
        NoteGroupBean bean = gList.get(groupPosition);
        DialogUtils.showNormalDialog(this, "提示信息", "删除后将无法恢复,是否删除该指定分组?",
                "确定", new DialogUtils.OnLeftClickListener() {
                    @Override
                    public void onLeftClick() {
                        //从数据库删除指定的分组
                        if (helper.deleteGroup(bean.getGroupId())) {
                            Toast.makeText(ExpandListActivity.this, "分组删除成功", Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(ExpandListActivity.this, "分组删除成功", Toast.LENGTH_SHORT).show();
                        }
                        //从数组中删除指定的分组
                        gList.remove(groupPosition);
                        expandeAdapter.notifyDataSetChanged();
                    }
                },"取消",null);

        expandeAdapter.notifyDataSetChanged();
    }
    //修改分组对话框
    private void showUpdateDialog(int groupPosition) {
        NoteGroupBean bean = gList.get(groupPosition);
        GroupDialog groupDialog = new GroupDialog(this);
        groupDialog.show();
        groupDialog.setDialogWidth();
        groupDialog.setTitleRename();
        groupDialog.setTipText(bean.getGroupName());
        groupDialog.setOnEnsureListener(new GroupDialog.OnEnsureListener() {
            @Override
            public void onEnsure(String msg) {
                if (helper.updateGroup(bean.getGroupId(), msg)) {
                    loadsDatas();
                    Toast.makeText(ExpandListActivity.this, "分组修改成功", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(ExpandListActivity.this, "分组修改失败", Toast.LENGTH_SHORT).show();
                }
            }
        });
        expandeAdapter.notifyDataSetChanged();
    }
//新增分组对话框
    private void showAddDialog() {
        GroupDialog groupDialog = new GroupDialog(this);
        groupDialog.show();
        groupDialog.setDialogWidth();
        groupDialog.setOnEnsureListener(new GroupDialog.OnEnsureListener() {
            @Override
            public void onEnsure(String msg) {
                if (helper.insertGroup(msg)) {
                    loadsDatas();
                    Toast.makeText(ExpandListActivity.this, "分组添加成功", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(ExpandListActivity.this, "分组添加失败", Toast.LENGTH_SHORT).show();
                }

            }
        });
        expandeAdapter.notifyDataSetChanged();
    }
    /**
     * 弹出对话框
     */
    private void showPopMenu(View view, int groupPosition,int childPosition) {
        PopupMenu popupMenu = new PopupMenu(ExpandListActivity.this, view, Gravity.RIGHT);
        MenuInflater menuInflater = popupMenu.getMenuInflater();
        menuInflater.inflate(R.menu.note_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.note_update:
                        targetid = checkList.get(groupPosition).get(childPosition).getId();
                        content = checkList.get(groupPosition).get(childPosition).getNotepadContent();
                        Intent intent = new Intent(ExpandListActivity.this,RecordActivity.class);
                        intent.putExtra("id",targetid);
                        intent.putExtra("content",content);
                        intent.putExtra("gList",(Serializable) gList);
                        intent.putExtra("group",checkList.get(groupPosition).get(childPosition).getGroup_id());
                        intent.putExtra("pos",checkList.get(groupPosition).get(childPosition).getGroup_id());
                        startActivity(intent);
                        break;
                    case R.id.note_del:
                        deleteNoteByPos(groupPosition,childPosition);
                        break;
                }
                return false;
            }
        });
        popupMenu.show();
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
    //删除记事本中position指定的note
    private void deleteNoteByPos(int groupPosition,int childPosition) {
        DialogUtils.showNormalDialog(this, "提示信息", "删除后将无法恢复,是否删除该指定文件?",
                "确定", new DialogUtils.OnLeftClickListener() {
                    @Override
                    public void onLeftClick() {
                        //从数据库删除指定的note
                        helper.deleteData(checkList.get(groupPosition).get(childPosition).getId());
                        //从数组中删除指定的note
                        checkList.get(groupPosition).remove(childPosition);
                        expandeAdapter.notifyDataSetChanged();
                    }
                },"取消",null);
    }
    //获取time中的int日期
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