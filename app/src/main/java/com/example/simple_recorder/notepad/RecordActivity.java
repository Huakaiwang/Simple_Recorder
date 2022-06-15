package com.example.simple_recorder.notepad;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.simple_recorder.bean.NoteGroupBean;
import com.example.simple_recorder.databinding.ActivityRecordBinding;
import com.example.simple_recorder.expandelist.ExpandListActivity;
import com.example.simple_recorder.utils.DBUtils;
import com.example.simple_recorder.utils.DialogUtils;
import com.example.simple_recorder.utils.SQLiteHelper;

import java.util.ArrayList;
import java.util.List;

public class RecordActivity extends AppCompatActivity {
    private ActivityRecordBinding recordBinding;
    private String content;
    private String id;
    private List<NoteGroupBean> gList;
    private int group;
    private int pos;
    private SQLiteHelper myHelper;
    private String oldcontext;
    private int oldGroup;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            content = recordBinding.recordContent.getText().toString().trim();
            if (id != null) {//修改
                Log.d("TAG", "onKeyDown: "+content.equals(oldcontext));
                boolean same = content.equals(oldcontext);
                if (content.length() > 0 && !same || group!=oldGroup) {
                    DialogUtils.showNormalDialog(this, "提示信息", "修改未保存，是否保存?", "确定", new DialogUtils.OnLeftClickListener() {
                        @Override
                        public void onLeftClick() {
                            if (myHelper.updateDate(id, content, DBUtils.getTime(),group)) {
                                Toast.makeText(RecordActivity.this, "修改成功", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(RecordActivity.this, ExpandListActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(RecordActivity.this, "修改失败", Toast.LENGTH_SHORT).show();
                            }
                        }
                    },"取消",null);
                }
            } else {//增加
                if (content.length() > 0) {
                    DialogUtils.showNormalDialog(this, "提示信息", "未保存，是否保存?", "确定", new DialogUtils.OnLeftClickListener() {
                        @Override
                        public void onLeftClick() {
                            if (myHelper.insertDate(content, DBUtils.getTime(),group)) {
                                Toast.makeText(RecordActivity.this, "保存成功", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(RecordActivity.this, ExpandListActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(RecordActivity.this, "保存失败", Toast.LENGTH_SHORT).show();
                            }
                        }
                    },"取消",null);
                }
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        recordBinding = ActivityRecordBinding.inflate(getLayoutInflater());
        setContentView(recordBinding.getRoot());
        setDarkStatusIcon(true);
        initData();//初始化试图
        setEvent();//设置事件监听
    }
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction()== MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (isShouldHideKeyboard(v,ev)) {
                //隐藏软键盘
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(),0);
            }
        }
        return super.dispatchTouchEvent(ev);
    }
//点击输入框以外的区域关闭键盘
    public static boolean isShouldHideKeyboard(View v, MotionEvent event) {
        if (v != null && (v instanceof EditText)){
            int[] l = {0, 0};
            v.getLocationInWindow(l);
            int left = l[0],
                    top = l[1],
                    bottom = top + v.getHeight(),
                    right = left + v.getWidth();
            if (event.getX() > left && event.getX() < right
                    && event.getY() > top && event.getY() < bottom) {
// 点击EditText的事件，忽略它。
                return false;
            } else {
                return true;
            }
        }
// 如果焦点不是EditText则忽略，这个发生在视图刚绘制完，第一个焦点不在EditText上，和用户用轨迹球选择其他的焦点
        return false;
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

    //设置事件监听
    private void setEvent() {
        recordBinding.recordBack.setOnClickListener(backListener);
        recordBinding.recordDel.setOnTouchListener(touchListener);
        recordBinding.recordAdd.setOnTouchListener(touchListener);
        recordBinding.recordDel.setOnClickListener(delOnClickListener);
        recordBinding.recordAdd.setOnClickListener(addOrUpdateListener);
        recordBinding.recordSpinner.setOnItemSelectedListener(spinnerClick);

    }
    //下拉选择框事件
    AdapterView.OnItemSelectedListener spinnerClick = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            TextView tv = (TextView)view;
            tv.setGravity(android.view.Gravity.CENTER_HORIZONTAL);//设置居中
            tv.setMaxEms(3);
            tv.setEllipsize(TextUtils.TruncateAt.END);
            tv.setSingleLine(true);
            group = Integer.parseInt(gList.get(position).getGroupId());
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };
    //单击返回
    View.OnClickListener backListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
                content = recordBinding.recordContent.getText().toString().trim();
                if (id != null) {//修改
                    Log.d("TAG", "onKeyDown: "+content.equals(oldcontext));
                    boolean same = content.equals(oldcontext);
                    if (content.length() > 0 && !same || group!=oldGroup) {
                        DialogUtils.showNormalDialog(RecordActivity.this, "提示信息", "修改未保存，是否保存?", "确定", new DialogUtils.OnLeftClickListener() {
                            @Override
                            public void onLeftClick() {
                                if (myHelper.updateDate(id, content, DBUtils.getTime(),group)) {
                                    Toast.makeText(RecordActivity.this, "修改成功", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(RecordActivity.this, ExpandListActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Toast.makeText(RecordActivity.this, "修改失败", Toast.LENGTH_SHORT).show();
                                }
                            }
                        },"取消",null);
                    }else{
                        Intent back = new Intent(RecordActivity.this,ExpandListActivity.class);
                        startActivity(back);
                        finish();
                    }
                } else {//增加
                    if (content.length() > 0) {
                        DialogUtils.showNormalDialog(RecordActivity.this, "提示信息", "未保存，是否保存?", "确定", new DialogUtils.OnLeftClickListener() {
                            @Override
                            public void onLeftClick() {
                                if (myHelper.insertDate(content, DBUtils.getTime(),group)) {
                                    Toast.makeText(RecordActivity.this, "保存成功", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(RecordActivity.this, ExpandListActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Toast.makeText(RecordActivity.this, "保存失败", Toast.LENGTH_SHORT).show();
                                }
                            }
                        },"取消",null);
                    }else{
                        Intent back = new Intent(RecordActivity.this,ExpandListActivity.class);
                        startActivity(back);
                        finish();
                    }
                }
        }
    };
    //设置按压效果
    View.OnTouchListener touchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                //更改为按下时的背景图片
                v.setBackgroundColor(Color.parseColor("#F5F5F5"));
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                //改为抬起时的图片
                v.setBackgroundColor(Color.parseColor("#FFFFFF"));
            }
            return false;
        }
    };
    //单击清空输入框内容
    View.OnClickListener delOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            recordBinding.recordContent.setText("");
        }
    };
    //单击保存添加或者修改输入框内容至数据库
    View.OnClickListener addOrUpdateListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //获取输入框内容
            content = recordBinding.recordContent.getText().toString().trim();
            Log.d("TAG", "onClick: " + content);
            if (id != null) {//修改
                if (content.length() > 0) {
                    if (myHelper.updateDate(id, content, DBUtils.getTime(),group)) {
                        Toast.makeText(RecordActivity.this, "修改成功", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(RecordActivity.this, ExpandListActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(RecordActivity.this, "修改失败", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(RecordActivity.this, "内容不能为空", Toast.LENGTH_SHORT).show();
                }
            } else {//增加
                if (content.length() > 0) {
                    if (myHelper.insertDate(content, DBUtils.getTime(),group)) {
                        Toast.makeText(RecordActivity.this, "保存成功", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(RecordActivity.this, ExpandListActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(RecordActivity.this, "保存失败", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(RecordActivity.this, "内容不能为空", Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

    //初始化视图
    protected void initData() {
        myHelper = new SQLiteHelper(this);
        Intent intent = getIntent();
        gList = (List<NoteGroupBean>) intent.getSerializableExtra("gList");
        group = Integer.parseInt(intent.getStringExtra("group"));
        oldcontext = intent.getStringExtra("content");
        oldGroup = group;
        if (intent.getStringExtra("pos")!=null) {
             pos = Integer.parseInt(intent.getStringExtra("pos"));
        }else {
             pos = 0;
        }
        List tempList = new ArrayList();
        for (int i = 0; i < gList.size(); i++) {
            tempList.add(gList.get(i).getGroupName());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, tempList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        recordBinding.recordSpinner.setAdapter(adapter);
        Log.d("TAG", "initData: "+pos);
        recordBinding.recordSpinner.setSelection(pos,true);
        if (intent != null) {
            id = intent.getStringExtra("id");
            if (id != null) {
                recordBinding.recordTitle.setText("修改");
                recordBinding.recordContent.setText(intent.getStringExtra("content"));
            } else {
                recordBinding.recordTitle.setText("添加");
            }
        }

    }
}