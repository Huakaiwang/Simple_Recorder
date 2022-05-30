package com.example.simple_recorder.wdiget;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.constraintlayout.solver.widgets.WidgetContainer;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class WdigetService extends Service  {
    private final IBinder mBinder=new LocalBinder();
    private final String ACTION_UPDATE_ALL ="com.example.user.widget.UPDATE_ALL";
    private Context context;
    public WdigetService() {
    }
    public class LocalBinder extends Binder{
        public  WdigetService getService(){
            return WdigetService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        UpdateThread thread = new UpdateThread();
        thread.start();
        context = this.getApplicationContext();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return new LocalBinder();
    }
    private class UpdateThread extends Thread{
        @Override
        public void run() {
            super.run();
            try {
                while (true) {
                    Intent intent =new Intent(ACTION_UPDATE_ALL);
                    intent.setPackage(context.getPackageName());
                    sendBroadcast(intent);
                    Log.d("TAG", "run: 线程执行中");
                    Thread.sleep(5000);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}