package com.example.simple_recorder.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

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
        db.execSQL("create table "+DBUtils.DATABASE_TABLE+"("+DBUtils.NOTEPAD_ID+
                " integer primary key autoincrement,"+ DBUtils.NOTEPAD_CONTENT +
                " text," + DBUtils.NOTEPAD_TIME+ " text)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
    //增加数据
    public boolean insertDate(String userContent,String userTime){
        ContentValues values = new ContentValues();
        values.put(DBUtils.NOTEPAD_CONTENT,userContent);
        values.put(DBUtils.NOTEPAD_TIME,userTime);
        return sqLiteDatabase.insert(DBUtils.DATABASE_TABLE,null,values) > 0;
    }
    //删除数据
    public boolean deleteData(String id){
        String sql = DBUtils.NOTEPAD_ID+"=?";
        String[] contentValuesArray = new String[]{String.valueOf(id)};
        return sqLiteDatabase.delete(DBUtils.DATABASE_TABLE,sql,contentValuesArray) > 0;
    }
    //修改数据
    public boolean updateDate(String id,String content,String userTime){
        ContentValues values = new ContentValues();
        values.put(DBUtils.NOTEPAD_CONTENT,content);
        values.put(DBUtils.NOTEPAD_TIME,userTime);
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
                String time = cursor.getString((int)cursor.getColumnIndex(DBUtils.NOTEPAD_TIME));
                noteInfo.setId(id);
                noteInfo.setNotepadContent(content);
                noteInfo.setNotepadTime(time);
                list.add(noteInfo);
            }
            cursor.close();
        }
        return list;
    }
}
