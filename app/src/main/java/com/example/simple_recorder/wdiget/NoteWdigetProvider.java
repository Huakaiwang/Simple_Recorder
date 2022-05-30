package com.example.simple_recorder.wdiget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.LongDef;

import com.example.simple_recorder.R;
import com.example.simple_recorder.bean.NotepadBean;
import com.example.simple_recorder.expandelist.ExpandListActivity;
import com.example.simple_recorder.utils.SQLiteHelper;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class NoteWdigetProvider extends AppWidgetProvider {
    private List<NotepadBean> mList;
    private SQLiteHelper mHelper ;
    private int[] idSet;
    private WdigetService wdigetService;
    private final Intent ACTIOn_SERVICE = new Intent("android.appwidget.action.EXAMPLE_APP_WIDGET_SERVICE");
    private final String ACTION_UPDATE_ALL ="com.example.user.widget.UPDATE_ALL";
    private Context mcontext;
    public NoteWdigetProvider() {
        super();
    }
    ServiceConnection myconnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            WdigetService.LocalBinder binder = (WdigetService.LocalBinder) service;
            wdigetService = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("TAG", "onReceive: 我被调用了222");
        super.onReceive(context, intent);
        mHelper = new SQLiteHelper(context);
        if (ACTION_UPDATE_ALL.equals(intent.getAction())) {
            mList = mHelper.query();
            //对数组进行排序
            Collections.sort(mList, new Comparator<NotepadBean>() {
                @Override
                public int compare(NotepadBean o1, NotepadBean o2) {
                    if (toInt(o1.getNotepadTime()) < toInt(o2.getNotepadTime())) {
                        return 1;
                    } else if (toInt(o1.getNotepadTime()) == toInt(o2.getNotepadTime())) {
                        return 0;
                    }
                    return -1;
                }
            });
            // 创建一个intent对象
            Intent intent2 = new Intent(context, ExpandListActivity.class);
            // 创建一个PandingIntent
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent2, 0);
            // RemoteViews代表的是我们创建的所有的appwidget的控件
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.note_wdiget_layout);
            //填充数据
            String content = mList.get(0).getNotepadContent();
            String time = mList.get(0).getNotepadTime();
            Log.d("TAG", "UpdateWediget: "+mList.get(0).getGroup_id());
            remoteViews.setTextViewText(R.id.wdiget_content, content);
            remoteViews.setTextViewText(R.id.wdiget_time, time);
            AppWidgetManager appwidgetManager = AppWidgetManager.getInstance(context);
            ComponentName componentname = new ComponentName(context, NoteWdigetProvider.class);
            appwidgetManager.updateAppWidget(componentname, remoteViews);
        }

    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        idSet = appWidgetIds.clone();
        UpdateWediget(context,appWidgetManager,appWidgetIds);
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        ACTIOn_SERVICE.setPackage(context.getPackageName());
        mcontext = context.getApplicationContext();
        ACTIOn_SERVICE.setClass(context,WdigetService.class);
        mcontext.bindService(ACTIOn_SERVICE,myconnection,context.BIND_AUTO_CREATE);
        context.startService(ACTIOn_SERVICE);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        if (mcontext!=null) {
            mcontext.unbindService(myconnection);
        }
    }

    @Override
    public void onRestored(Context context, int[] oldWidgetIds, int[] newWidgetIds) {
        super.onRestored(context, oldWidgetIds, newWidgetIds);
    }

    private void UpdateWediget(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d("TAG", "UpdateWediget: "+appWidgetIds.length);
        if (appWidgetIds.length != 0){
        for (int i = 0; i < appWidgetIds.length; i++) {
            mHelper = new SQLiteHelper(context);
            mList = mHelper.query();
            //对数组进行排序
            Collections.sort(mList, new Comparator<NotepadBean>() {
                @Override
                public int compare(NotepadBean o1, NotepadBean o2) {
                    if (toInt(o1.getNotepadTime()) < toInt(o2.getNotepadTime())) {
                        return 1;
                    } else if (toInt(o1.getNotepadTime()) == toInt(o2.getNotepadTime())) {
                        return 0;
                    }
                    return -1;
                }
            });
            // 创建一个intent对象
            Intent intent = new Intent(context, ExpandListActivity.class);
            // 创建一个PandingIntent
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
            // RemoteViews代表的是我们创建的所有的appwidget的控件
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.note_wdiget_layout);
            //填充数据
            String content = mList.get(0).getNotepadContent();
            String time = mList.get(0).getNotepadTime();
            Log.d("TAG", "UpdateWediget: "+mList.get(0).getGroup_id());
            remoteViews.setTextViewText(R.id.wdiget_content, content);
            remoteViews.setTextViewText(R.id.wdiget_time, time);
            // 更新AppWidget
            // 第一个参数用于指定被更新Appwidget的ID
            // 第二个参数用户指定被更新RemoteViews对象
            appWidgetManager.updateAppWidget(appWidgetIds[i], remoteViews);
            }
        }
        }
        //获取time中的int日期
        private long toInt (String str){
            String str2 = "";
            if (str != null && !"".equals(str)) {
                for (int i1 = 0; i1 < str.length(); i1++) {
                    if (str.charAt(i1) >= 48 && str.charAt(i1) <= 57) {
                        str2 += str.charAt(i1);
                    }
                }
            }
            return Long.parseLong(str2);
        }
}
