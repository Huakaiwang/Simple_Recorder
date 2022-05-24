package com.example.simple_recorder.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DBUtils {
    public static final String DATABASE_NAME = "NotePad"; //数据库名
    public static final String DATABASE_TABLE = "Note1";//表名
    public static final String DATABASE_GROUP_TABLE="Notegroup";//分组表
    public static final int DATABASE_VERSION = 1; // 数据库版本

    //数据库中的字段名
    public static final String NOTEPAD_GROUP_NAME = "groupname";
    public static final String NOTEPAD_GROUP_ID = "groupid";
    public static final String NOTEPAD_ID = "id";
    public static final String NOTEPAD_CONTENT = "context";
    public static final String NOTEPAD_TIME = "notetime";
    public static final String getTime(){
        //获取当前日期
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
        Date date = new Date(System.currentTimeMillis());
        return simpleDateFormat.format(date);
    }
}
