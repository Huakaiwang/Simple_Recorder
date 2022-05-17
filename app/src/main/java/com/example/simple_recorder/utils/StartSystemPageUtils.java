package com.example.simple_recorder.utils;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
/*
* 跳转系统的相关页面
* */
public class StartSystemPageUtils {
/*
* 跳转到系统中当前应用的设置界面
* */
    public static void goToAppSetting(Activity context){
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package",context.getPackageName(),null);
        intent.setData(uri);
        context.startActivity(intent);
    }
    public static void goToHomePage(Activity context){
        Intent home = new Intent(Intent.ACTION_MAIN);
        home.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        home.addCategory(Intent.CATEGORY_HOME);
        context.startActivity(home);
    }
}
