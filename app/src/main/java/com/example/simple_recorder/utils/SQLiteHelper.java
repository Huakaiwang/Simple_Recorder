package com.example.simple_recorder.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.simple_recorder.bean.NoteGroupBean;
import com.example.simple_recorder.bean.NotepadBean;

import java.util.ArrayList;
import java.util.List;


public class SQLiteHelper extends SQLiteOpenHelper {
    private SQLiteDatabase sqLiteDatabase;
    //创建数据库
    public SQLiteHelper(Context context){
        //通过super()方法创建一个名为Notepad的数据库
        super(context, DBUtils.DATABASE_NAME, null, DBUtils.DATABASE_VERSION);
         sqLiteDatabase = this.getWritableDatabase();
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table "+DBUtils.DATABASE_TABLE+" ( "+DBUtils.NOTEPAD_ID+
                " integer primary key autoincrement,"+ DBUtils.NOTEPAD_CONTENT +
                " text," + DBUtils.NOTEPAD_TIME+ " text,"+DBUtils.NOTEPAD_GROUP_ID+" integer)");
        db.execSQL("create table "+DBUtils.DATABASE_GROUP_TABLE+
                "("+ DBUtils.NOTEPAD_GROUP_ID +" integer primary key autoincrement,"+
                DBUtils.NOTEPAD_GROUP_NAME+" text)");
        insertGroup("默认");
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
    //增加分组表数据
    public boolean insertGroup(String groupName){
        ContentValues values = new ContentValues();
        values.put(DBUtils.NOTEPAD_GROUP_NAME,groupName);
        return sqLiteDatabase.insert(DBUtils.DATABASE_GROUP_TABLE,null,values)>0;
    }
    //删除表数据
    public boolean deleteGroup(String groupId){
        String sql = DBUtils.NOTEPAD_GROUP_ID+"=?";
        String[] contentValuesArray = new String[]{String.valueOf(groupId)};
        return sqLiteDatabase.delete(DBUtils.DATABASE_GROUP_TABLE,null,contentValuesArray) >0;
    }
    //修改分组表数据
    public boolean updateGroup(String groupId ,String groupName){
        ContentValues values = new ContentValues();
        values.put(DBUtils.NOTEPAD_GROUP_NAME,groupName);
        String sql = DBUtils.NOTEPAD_GROUP_ID+"=?";
        String[] strings = new String[]{groupId};
        return sqLiteDatabase.update(DBUtils.DATABASE_GROUP_TABLE,values,sql,strings) > 0 ;
    }
    //查询分组表数据
    public List<NoteGroupBean> queryGroup(){
        List<NoteGroupBean> list = new ArrayList<>();
        Cursor cursor = sqLiteDatabase.query(DBUtils.DATABASE_GROUP_TABLE, null, null, null, null, null, null);
        if(cursor!=null){
            while(cursor.moveToNext()){
                NoteGroupBean groupInfo = new NoteGroupBean();
                String id = String.valueOf(cursor.getInt((int)cursor.getColumnIndex(DBUtils.NOTEPAD_GROUP_ID)));
                String name = String .valueOf(cursor.getString((int)cursor.getColumnIndex(DBUtils.NOTEPAD_GROUP_NAME)));
                groupInfo.setGroupId(id);
                groupInfo.setGroupName(name);
                list.add(groupInfo);
            }
            cursor.close();
        }
        return list;
    }
    //增加数据
    public boolean insertDate(String userContent,String userTime,int group){
        ContentValues values = new ContentValues();
        values.put(DBUtils.NOTEPAD_CONTENT,userContent);
        values.put(DBUtils.NOTEPAD_TIME,userTime);
        values.put(DBUtils.NOTEPAD_GROUP_ID,group);
        return sqLiteDatabase.insert(DBUtils.DATABASE_TABLE,null,values) > 0;
    }
    //删除数据
    public boolean deleteData(String id){
        String sql = DBUtils.NOTEPAD_ID+"=?";
        String[] contentValuesArray = new String[]{String.valueOf(id)};
        return sqLiteDatabase.delete(DBUtils.DATABASE_TABLE,sql,contentValuesArray) > 0;
    }
    //修改数据
    public boolean updateDate(String id,String content,String userTime,int group){
        ContentValues values = new ContentValues();
        values.put(DBUtils.NOTEPAD_CONTENT,content);
        values.put(DBUtils.NOTEPAD_TIME,userTime);
        values.put(DBUtils.NOTEPAD_GROUP_ID,group);
        String sql = DBUtils.NOTEPAD_ID+"=?";
        String[] strings = new String[]{id};
        return sqLiteDatabase.update(DBUtils.DATABASE_TABLE,values,sql,strings) > 0 ;
    }
    //查询数据
    public List<NotepadBean> query(){
        List<NotepadBean> list = new ArrayList<>();
        Cursor cursor = sqLiteDatabase.query(DBUtils.DATABASE_TABLE, null, null, null, null, null, null);
        if(cursor!=null){
            while(cursor.moveToNext()){
                NotepadBean noteInfo = new NotepadBean();
                String id = String.valueOf(cursor.getInt((int)cursor.getColumnIndex(DBUtils.NOTEPAD_ID)));
                String content = cursor.getString((int) cursor.getColumnIndex(DBUtils.NOTEPAD_CONTENT));
                String group_id = String.valueOf(cursor.getInt((int)cursor.getColumnIndex(DBUtils.NOTEPAD_GROUP_ID)));
                String time = cursor.getString((int)cursor.getColumnIndex(DBUtils.NOTEPAD_TIME));
                noteInfo.setId(id);
                noteInfo.setNotepadContent(content);
                noteInfo.setGroup_id(group_id);
                noteInfo.setNotepadTime(time);
                list.add(noteInfo);
            }
            cursor.close();
        }
        return list;
    }
    public List<NotepadBean> queryByGroup(String groupId){
        List<NotepadBean> list = new ArrayList<>();
        String[] strings = new String[]{groupId};
        Cursor cursor = sqLiteDatabase.query(DBUtils.DATABASE_TABLE, null, DBUtils.NOTEPAD_GROUP_ID+"=?", strings, null, null, null);
        if(cursor!=null){
            while(cursor.moveToNext()){
                NotepadBean noteInfo = new NotepadBean();
                String id = String.valueOf(cursor.getInt((int)cursor.getColumnIndex(DBUtils.NOTEPAD_ID)));
                String content = cursor.getString((int) cursor.getColumnIndex(DBUtils.NOTEPAD_CONTENT));
                String group_id = String.valueOf(cursor.getInt((int)cursor.getColumnIndex(DBUtils.NOTEPAD_GROUP_ID)));
                String time = cursor.getString((int)cursor.getColumnIndex(DBUtils.NOTEPAD_TIME));
                noteInfo.setId(id);
                noteInfo.setNotepadContent(content);
                noteInfo.setGroup_id(group_id);
                noteInfo.setNotepadTime(time);
                list.add(noteInfo);
            }
            cursor.close();
        }
        return list;
    }
    //  删除数据库
    public boolean deleteDatabase(Context context) {
        return context.deleteDatabase(DBUtils.DATABASE_NAME);
    }
}
