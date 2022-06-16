package com.example.simple_recorder.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.simple_recorder.expandelist.ExpandListActivity;
import com.example.simple_recorder.expandelist.ExpandeAdapter;
import com.example.simple_recorder.utils.Author;
import com.example.simple_recorder.utils.DBUtils;
import com.example.simple_recorder.utils.SQLiteHelper;

public class DictProvider extends ContentProvider {

    private static UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
    private static final int GROUP = 1;
    private static final int NOTES = 2;
    private static final int ITEMS = 3;
    private static final int NOTE= 4;
    private SQLiteHelper helper;
    static {
        matcher.addURI(Author.AUTHORITY,"group",GROUP);
        matcher.addURI(Author.AUTHORITY,"notes",NOTES);
        matcher.addURI(Author.AUTHORITY,"group/#",ITEMS);
        matcher.addURI(Author.AUTHORITY,"notes/#",NOTE);
    }
    @Override
    public boolean onCreate() {
        helper = new SQLiteHelper(this.getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        switch (matcher.match(uri)){
            case GROUP:
                Cursor cursor =  helper.getWritableDatabase().query(DBUtils.DATABASE_GROUP_TABLE, null, null, null, null, null, null);
                return cursor;
            case NOTES:
                Cursor cursor1 = (Cursor) helper.query();
                return cursor1;
            case ITEMS:
                long groupId = ContentUris.parseId(uri);
                String[] strings = new String[]{String.valueOf(groupId)};
                Cursor cursor2 = helper.getWritableDatabase().query(DBUtils.DATABASE_TABLE,null,DBUtils.NOTEPAD_GROUP_ID+"=?",strings,null,null,null);
                //Cursor cursor = sqLiteDatabase.query(DBUtils.DATABASE_TABLE, null, DBUtils.NOTEPAD_GROUP_ID+"=?", strings, null, null, null);
                //Cursor cursor2 = (Cursor) helper.queryByGroup(String.valueOf(groupId));
                return cursor2;
            default:
                throw new IllegalStateException("未知Uri"+uri);
        }
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (matcher.match(uri)){
            case GROUP:
                return "GROUP";
            case NOTES:
                return "NOTES";
            case ITEMS:
                return "ITEMS";
            case NOTE:
                return "NOTE";
            default:
                throw new IllegalStateException("未知Uri"+uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        switch (matcher.match(uri)){
            case GROUP:
               helper.insertGroup(values.getAsString(DBUtils.NOTEPAD_GROUP_NAME));
               break;
            case NOTES:
                helper.insertDate(values.getAsString(DBUtils.NOTEPAD_CONTENT),DBUtils.getTime(),values.getAsInteger(DBUtils.NOTEPAD_ID));
                break;
            case ITEMS:
                break;
            case NOTE:
                String time = values.getAsString("time");
                String contents = values.getAsString("content");
                int group =values.getAsInteger("group");
                ContentValues value = new ContentValues();
                value.put(DBUtils.NOTEPAD_CONTENT,contents);
                value.put(DBUtils.NOTEPAD_TIME,time);
                value.put(DBUtils.NOTEPAD_GROUP_ID,group);
                helper.getWritableDatabase().insert(DBUtils.DATABASE_TABLE,null,value);
                break;
            default:
                throw new IllegalStateException("未知Uri"+uri);
        }
        return uri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        switch (matcher.match(uri)){
            case GROUP:
                /*long groupId = ContentUris.parseId(uri);
                Log.d("TAG", "delete: "+groupId);
                String sql = DBUtils.NOTEPAD_GROUP_ID+"=?";
                String[] contentValuesArray = new String[]{String.valueOf(groupId)};
                int is = helper.getWritableDatabase().delete(DBUtils.DATABASE_GROUP_TABLE,sql,contentValuesArray);
                if (is>0) Log.d("TAG", "delete: 删除成功");*/
                break;
            case NOTES:
                Log.d("TAG", "delete: "+selectionArgs[0]);
                helper.deleteData(selectionArgs[0]);
                break;
            case ITEMS:
                long groupId = ContentUris.parseId(uri);
                Log.d("TAG", "delete: "+groupId);
                String sql = DBUtils.NOTEPAD_GROUP_ID+"=?";
                String[] contentValuesArray = new String[]{String.valueOf(groupId)};
                int is = helper.getWritableDatabase().delete(DBUtils.DATABASE_GROUP_TABLE,sql,contentValuesArray);
                if (is>0) Log.d("TAG", "delete: 删除成功");
                break;
            default:
                throw new IllegalStateException("未知Uri"+uri);
        }
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        switch (matcher.match(uri)){
            case GROUP:
                /*long groupId = ContentUris.parseId(uri);
                String group_name = values.getAsString(DBUtils.NOTEPAD_GROUP_NAME);
                helper.updateGroup(String.valueOf(groupId),group_name);*/
                break;
            case NOTES:
                String noteId = String.valueOf(ContentUris.parseId(uri));
                int group_id = Integer.parseInt(values.getAsString(DBUtils.NOTEPAD_GROUP_ID));
                String content = values.getAsString(DBUtils.NOTEPAD_CONTENT);
                helper.updateDate(noteId,content,DBUtils.getTime(),group_id);
                break;
            case ITEMS:
                long groupId = ContentUris.parseId(uri);
                String sql = DBUtils.NOTEPAD_GROUP_ID+"=?";
                String[] strings = new String[]{String.valueOf(groupId)};
                helper.getWritableDatabase().update(DBUtils.DATABASE_GROUP_TABLE,values,sql,strings);
                break;
            case NOTE:
                long id = ContentUris.parseId(uri);
                String time = values.getAsString("time");
                String contents = values.getAsString("content");
                int group =values.getAsInteger("group");
                ContentValues value = new ContentValues();
                value.put(DBUtils.NOTEPAD_CONTENT,contents);
                value.put(DBUtils.NOTEPAD_TIME,time);
                value.put(DBUtils.NOTEPAD_GROUP_ID,group);
                String sqls = DBUtils.NOTEPAD_ID+"=?";
                String[] stringss = new String[]{String.valueOf(id)};
                helper.getWritableDatabase().update(DBUtils.DATABASE_TABLE,value,sqls,stringss);
                break;
            default:
                throw new IllegalStateException("未知Uri"+uri);
        }
        return 0;
    }
}
