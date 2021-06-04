package com.example.mymusicplayer.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.mymusicplayer.activity.PlayingActivity;
import com.example.mymusicplayer.R;
import com.example.mymusicplayer.entity.Song;
import com.example.mymusicplayer.entity.SongList;

import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PlayerService extends Service {

    //需要实现的效果：点击一首recyclerView里的音乐，就启动这个服务，那么在启动时，就应该传入当前音乐的信息和当前音乐所在的列表。
    //同时需要支持：点击停止按钮就停止播放，点击继续按钮就继续播放，点击上/下一首就切换上/下一首，点击设置播放方式就设置相应方式。


    View playBar;

    private Notification notification;
    private MusicReceiver musicReceiver;

    //控制歌曲播放的广播消息
    public static final String PLAY_OR_PAUSE = "com.example.playorpause";
    public static final String LAST_MUSIC = "com.example.lastmusic";
    public static final String NEXT_MUSIC = "com.example.nextmusic";

    //播放模式
    private static final int SINGLE_MODE = 1;
    private static final int CYCLE_MODE = 2;
    private static final int RANDOM_MODE = 3;

    private int mode = CYCLE_MODE;//当前所处的播放模式,默认为循环播放
    private int seq = -1;//TODO 当前播放歌曲位于歌单中的次序,由于使用了livedata，不再必要，后续可以删除
    private MutableLiveData<Integer> seqLive = new MutableLiveData<>();

    private List<Song> songList;//当前播放音乐所处的歌单

    private ExecutorService executorService = Executors.newFixedThreadPool(1);

    private MutableLiveData<Boolean> isPause = new MutableLiveData<>();

    private static final MediaPlayer mediaPlayer = new MediaPlayer();//音乐播放器
    static
    {
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();
            }
        });
    }

    private PlayerBinder playerBinder = new PlayerBinder();

    public PlayerService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return playerBinder;
    }

    public class PlayerBinder extends Binder
    {
        public Boolean isPlaying()
        {
            return mediaPlayer.isPlaying();
        }

        public void setOnCompletion()
        {
            Log.d("PlayService","play finished");
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    switch (mode)
                    {
                        case SINGLE_MODE:
                            mediaPlayer.start();//再次播放
                            Log.d("PlayerService","单曲循环模式，重新播放");
                            break;
                        case CYCLE_MODE:
                            if( ++seq == songList.size() ) seq = 0;
                            seqLive.setValue(seq);
                            play(songList.get(seq).getPath());
                            Log.d("PlayerService","列表循环模式，播放下一首，位置更新为 " + seq);
                            break;
                        case RANDOM_MODE:
                            Random random = new Random();
                            seq = random.nextInt(songList.size()-1);
                            seqLive.setValue(seq);
                            play(songList.get(seq).getPath());
                            Log.d("PlayerService","随机循环模式，播放下一首，位置更新为 " + seq);
                            break;
                    }
                }
            });
        }

        public void playMusic(String path)
        {
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    //用于从头开始播放，点击歌曲时调用
                    play(path);
                    isPause.postValue(false);
                }
            });
        }

        public void playOrPause()
        {
            NotificationManager manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
            if( mediaPlayer.isPlaying() && !isPause.getValue() )
            {
                mediaPlayer.pause();
                isPause.setValue(true);
                notification.contentView.setImageViewUri(R.id.foreground_play,Uri.parse(getResourcesUri(R.drawable.pausebutton)));
            }
            else
            {
                mediaPlayer.start();
                isPause.setValue(false);
                notification.contentView.setImageViewUri(R.id.foreground_play,Uri.parse(getResourcesUri(R.drawable.playbutton)));
            }
            manager.notify(2,notification);//刷新
        }

        public void nextMusic()
        {
            if( seqLive.getValue() == -1 ) return;//当前没有音乐
            if( mediaPlayer.isPlaying() ) mediaPlayer.stop();
            if( ++seq == songList.size() ) seq = 0;
            seqLive.setValue(seq);
            play(songList.get(seq).getPath());
        }

        public void lastMusic()
        {
            if( seqLive.getValue() == -1 ) return;//当前没有音乐
            if( mediaPlayer.isPlaying() ) mediaPlayer.stop();
            if( --seq == -1 ) seq = songList.size() - 1;
            seqLive.setValue(seq);
            play(songList.get(seq).getPath());
        }

        public void setSingleMode()
        {
            mode = SINGLE_MODE;
            Toast.makeText(PlayerService.this,"单曲循环",Toast.LENGTH_SHORT).show();
            Log.d("PlayerService","单曲循环设置成功，当前位置为" + seq);
        }

        public void setCycleMode()
        {
            mode = CYCLE_MODE;
            Toast.makeText(PlayerService.this,"顺序循环",Toast.LENGTH_SHORT).show();
            Log.d("PlayerService","列表循环设置成功，当前位置为" + seq);
        }

        public void setRandomMode()
        {
            mode = RANDOM_MODE;
            Toast.makeText(PlayerService.this,"随机循环",Toast.LENGTH_SHORT).show();
            Log.d("PlayerService","随机循环设置成功，当前位置为" + seq);
        }

        public int getMode()
        {
            return mode;
        }

        public void setSongList(SongList mSongList)
        {
            songList = mSongList.getSongList();
        }

        public void setSongList(List<Song> mSongList)
        {
            Log.d("PlayerService","设置成功，共传入歌曲数" + mSongList.size());
            songList = mSongList;
        }

        public List<Song> getSongList()
        {
            return songList;
        }

        public void setSeq(int sequence)
        {
            seq = sequence;
            seqLive.setValue(sequence);

            Log.d("PlayerService","成功定位，当前位置为" + seq);
        }

        public int getSeq()
        {
            return seq;
        }

        public LiveData<Integer> getSeqLive()
        {
            return seqLive;
        }

        public LiveData<Boolean> getIsPause(){
            return isPause;
        }

        public int getDuration()
        {
            return mediaPlayer.getDuration();
        }

        public int getCurrentPosition()
        {
            return mediaPlayer.getCurrentPosition();
        }

        public void seekTo(int msec)
        {
            mediaPlayer.seekTo(msec);
        }
    }

    private void play(String path)
    {
        //需要在这里更新前台通知中的UI
        if( mediaPlayer.isPlaying() ) mediaPlayer.stop();
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(getApplicationContext(),Uri.parse(path));
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Song currentSong = songList.get(seq);
        notification.contentView.setTextViewText(R.id.foreground_song_name,currentSong.getName());
        notification.contentView.setTextViewText(R.id.foreground_singer,currentSong.getSinger());
        NotificationManager manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(2,notification);//刷新
        Log.d("play",currentSong.getName());
        //if( currentSong.getPic_id() != null ) notification.contentView.setImageViewUri(R.id.foreground_image,Uri.parse(currentSong.getPic_id()));
    }

    //从网上抄的将drawable转化为uri的方法
    private String getResourcesUri(@DrawableRes int id) {
        Resources resources = getResources();
        String uriPath = ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
                resources.getResourcePackageName(id) + "/" +
                resources.getResourceTypeName(id) + "/" +
                resources.getResourceEntryName(id);
        return uriPath;
    }

    class MusicReceiver extends BroadcastReceiver
    {

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction())
            {
                case PLAY_OR_PAUSE:
                    playerBinder.playOrPause();
                    break;
                case LAST_MUSIC:
                    playerBinder.lastMusic();
                    break;
                case NEXT_MUSIC:
                    playerBinder.nextMusic();
                    break;
                default:
                    break;
            }
        }
    }

    private void registerReceiver()
    {
        musicReceiver = new MusicReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(PLAY_OR_PAUSE);
        intentFilter.addAction(LAST_MUSIC);
        intentFilter.addAction(NEXT_MUSIC);
        registerReceiver(musicReceiver, intentFilter);
    }

    private void createFloatPlayBar()
    {
        if( !songList.isEmpty() )
        {
            playBar = LayoutInflater.from(getBaseContext()).inflate(R.layout.play_bar,null);
            //TODO
        }
    }

    private void getNotification(RemoteViews remoteView)
    {
        NotificationManager manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        Intent startPlayActivity = new Intent(this, PlayingActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this,0,startPlayActivity,0);
        Notification.Builder builder = new Notification.Builder(this);
        builder.setContentText("歌曲播放")//设置通知内容，不能缺少
                .setContentTitle("歌曲播放")//设置通知标题，不能缺少
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher))//大图标
                .setSmallIcon(R.mipmap.musicicon)//不能缺少的一个属性
                .setContent(remoteView)
                .setContentIntent(pi)//设置点击跳转
                .setAutoCancel(false)//点击自动取消
                .setPriority(Notification.PRIORITY_HIGH)//设置优先级
                .setWhen(System.currentTimeMillis());//设置通知时间，默认为系统发出通知的时间，通常不用设置
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("002","my_channel",NotificationManager.IMPORTANCE_DEFAULT);
            channel.enableLights(false); //是否在桌面icon右上角展示小红点
            channel.setShowBadge(true); //是否在久按桌面图标时显示此渠道的通知
            manager.createNotificationChannel(channel);
            builder.setChannelId("002");
        }
        notification = builder.build();//调用builder的build()方法可以生成一则通知
        startForeground(2,notification);
    }

    private void initRemoteView(RemoteViews remoteView)
    {
        //播放/暂停广播控制
        Intent playIntent = new Intent(PLAY_OR_PAUSE);
        PendingIntent playPendingIntent = PendingIntent.getBroadcast(this,0,playIntent,0);
        remoteView.setOnClickPendingIntent(R.id.foreground_play,playPendingIntent);
        //上一首切换广播控制
        Intent lastIntent = new Intent(LAST_MUSIC);
        PendingIntent lastPendingIntent = PendingIntent.getBroadcast(this,0,lastIntent,0);
        remoteView.setOnClickPendingIntent(R.id.foreground_last,lastPendingIntent);
        //下一首切换广播控制
        Intent nextIntent = new Intent(NEXT_MUSIC);
        PendingIntent nextPendingIntent = PendingIntent.getBroadcast(this,0,nextIntent,0);
        remoteView.setOnClickPendingIntent(R.id.foreground_next,nextPendingIntent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        registerReceiver();
        RemoteViews remoteView = new RemoteViews(getPackageName(),R.layout.foregroundplay);
        initRemoteView(remoteView);
        getNotification(remoteView);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(musicReceiver != null){
            unregisterReceiver(musicReceiver);
        }
        if (!executorService.isShutdown()) {
            executorService.shutdownNow();
        }
        stopForeground(true);
        Log.d("service onDestroy","服务被销毁了");
    }

}