package com.example.mymusicplayer.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.os.Looper;
import android.widget.Toast;

import com.example.mymusicplayer.activity.MainActivity;
import com.example.mymusicplayer.R;

import java.io.File;

//直接借鉴了郭霖大神
public class DownloadService extends Service {

    private DownloadTask downloadTask;
    private String downloadUrl;
    private DownloadListener listener = new DownloadListener() {
        @Override
        public void onProgress(int progress) {
            getNotificationManager().notify(1,getNotification("下载中...",progress));
        }

        @Override
        public void onSuccess() {
            downloadTask=null;
            stopForeground(true);
            getNotificationManager().notify(1,getNotification("下载成功",-1));
            Looper.prepare();
            Toast.makeText(DownloadService.this,"下载成功", Toast.LENGTH_SHORT).show();
            Looper.loop();
        }

        @Override
        public void onFailed() {
            Looper.prepare();
            downloadTask=null;
            stopForeground(true);
            getNotificationManager().notify(1,getNotification("下载失败",-1));
            Toast.makeText(DownloadService.this,"下载失败",Toast.LENGTH_SHORT).show();
            Looper.loop();
        }

        @Override
        public void onPaused() {
            downloadTask=null;
            Looper.prepare();
            Toast.makeText(DownloadService.this,"下载暂停",Toast.LENGTH_SHORT).show();
            Looper.loop();
        }

        @Override
        public void onCanceld() {
            downloadTask=null;
            stopForeground(true);
            Looper.prepare();
            Toast.makeText(DownloadService.this,"下载取消",Toast.LENGTH_SHORT).show();
            Looper.loop();
        }
    };

    private DownloadBinder downloadBinder = new DownloadBinder();

    public DownloadService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        return downloadBinder;
    }

    public class DownloadBinder extends Binder
    {
        public void startDownload(String url)
        {
            if(downloadTask == null)
            {
                downloadUrl = url;
                downloadTask = new DownloadTask(listener);
                downloadTask.download(downloadUrl);
                //downloadTask.download2(downloadUrl);
                startForeground(1,getNotification("开始下载...",0));
                Toast.makeText(DownloadService.this,"开始下载...",Toast.LENGTH_SHORT).show();
            }

        }

        /*public void startDownload2(int songId)
        {
            downloadTask = new DownloadTask(listener);
            downloadTask.download2(songId+"");
        }*/

        public void pauseDownload()
        {
            if(downloadTask!=null) {
                downloadTask.pauseDownload();
                Toast.makeText(DownloadService.this,"下载暂停",Toast.LENGTH_SHORT).show();
            }
        }
        public void cancleDownload()
        {
            if(downloadTask!=null)
                downloadTask.cancelDownload();
            else
            {
                if(downloadUrl!=null)
                {
                    String fileName=downloadUrl.substring(downloadUrl.lastIndexOf("/"));
                    String directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
                    File file=new File(directory+fileName);
                    if(file.exists())
                    {
                        file.delete();
                    }
                    getNotificationManager().cancel(1);
                    stopForeground(true);
                    Toast.makeText(DownloadService.this,"下载取消",Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private NotificationManager getNotificationManager()
    {
        return (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
    }

    //得到一条前台通知
    private Notification getNotification(String title, int progress)
    {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,intent,0);
        Notification.Builder builder = new Notification.Builder(this);
        builder.setSmallIcon(R.mipmap.download)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.download))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setContentTitle(title);
        if( progress > 0 )
        {
            builder.setContentText(progress + "%");
            builder.setProgress(100,progress,false);
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("001","my_channel",NotificationManager.IMPORTANCE_DEFAULT);
            channel.enableLights(true); //是否在桌面icon右上角展示小红点
            channel.setLightColor(Color.GREEN); //小红点颜色
            channel.setShowBadge(true); //是否在久按桌面图标时显示此渠道的通知
            getNotificationManager().createNotificationChannel(channel);
            builder.setChannelId("001");
        }
        return builder.build();
    }
}