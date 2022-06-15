package com.example.simple_recorder.utils;

import android.net.Uri;
import android.provider.BaseColumns;
import android.view.inspector.StaticInspectionCompanionProvider;

public final class Author {
    //定义该ContentProvider的Authorities
    public static final String AUTHORITY =
            "notepad.providers.dictprovider";
    //定义一个静态内部类,定义该ContentProvider所包含的数据列的列名
    public static final class DataColums implements BaseColumns{
        //定义Content所允许操作的数据列
        public static final String NOTEPAD_GROUP_NAME = DBUtils.NOTEPAD_GROUP_NAME;
        public static final String NOTEPAD_GROUP_ID = DBUtils.NOTEPAD_GROUP_ID;
        public static final String NOTEPAD_ID = DBUtils.NOTEPAD_ID;
        public static final String NOTEPAD_CONTENT = DBUtils.NOTEPAD_CONTENT;
        public static final String NOTEPAD_TIME = DBUtils.NOTEPAD_TIME;
        //定义该Content提供服务的两个Uri
        public final static Uri GROUP_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/group");
        public final static Uri NOTE_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/notes");
    }
}
