package com.example.mymusicplayer.utils;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mymusicplayer.activity.ListRootActivity;
import com.example.mymusicplayer.R;
import com.example.mymusicplayer.entity.Song;
import com.example.mymusicplayer.entity.SongList;
import com.example.mymusicplayer.room.SongListDatabase;
import com.example.mymusicplayer.service.DownloadService;
import com.example.mymusicplayer.service.PlayerService;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.BIND_AUTO_CREATE;

//旨在为每一个歌单界面提供帮助类，顺利完成recyclerView的加载和服务的绑定，同时集成了一个popWindow
//警告：必须在活动中才能使用！暂时不适配碎片。

/**
 * 功能① 解决绑定服务和列表加载的异构冲突
 * 功能② 内部集成了一个用于展示歌单内容的recyclerView的adapter，且设置了点击弹出底部菜单的功能。菜单中又实现了添加单曲的功能
 */
public class PlayerServiceUtils {

    private Activity activity;//当前活动

    private SongList currentList;
    private Song chosedSong;

    private MusicAdapter adapter = new MusicAdapter();

    private View contentView;
    private PopupWindow popupWindow;

    private PlayerService.PlayerBinder playerBinder;
    private DownloadService.DownloadBinder downloadBinder;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d("LocalActivity","服务已绑定");
            playerBinder = (PlayerService.PlayerBinder) service;
            playerBinder.setOnCompletion();//设置播放结束的监听
            if( ((PlayerService.PlayerBinder) service).getSongList() == null )
                playerBinder.setSongList(currentList);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            //TODO
        }
    };

    private ServiceConnection connection2 = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            downloadBinder=(DownloadService.DownloadBinder)service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    public void setSongList(SongList songList) {
        currentList = songList;
        //playerBinder.setSongList(songList);//作为中继站，将歌单信息传入服务
    }

    public void setSongList(List<Song> songList){
        Log.d("PlayerServiceUtils","设置成功,共传入歌曲数 " + songList.size());
        playerBinder.setSongList(songList);
    }

    public PlayerServiceUtils(Activity activity) {
        this.activity = activity;
    }

    public MusicAdapter getAdapter() {
        return adapter;
    }

    public PlayerService.PlayerBinder getPlayerBinder() {
        return playerBinder;
    }

    public void bindPlayerService()
    {
        //绑定播放服务
        Intent bindIntent = new Intent(activity,PlayerService.class);
        activity.bindService(bindIntent,connection, BIND_AUTO_CREATE);
        //绑定下载服务
        Intent bindIntent2 = new Intent(activity,DownloadService.class);
        activity.bindService(bindIntent2,connection2,BIND_AUTO_CREATE);
    }

    //本来并不是按内部类来写的，但是想要设置recyclerview的点击事件为调用binder里的方法，则必定与异构的服务连接相冲突
    public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.ViewHolder> {

        private List<Song> localSongs = new ArrayList<>();


        public MusicAdapter() {

        }

        public void setLocalSongs(List<Song> localSongs) {
            this.localSongs = localSongs;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.music_item,parent,false);
            final ViewHolder holder = new ViewHolder(view);
            holder.musicView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Song song = localSongs.get(holder.getAdapterPosition());//得到当前选中的歌曲的实例
                    playerBinder.setSeq(holder.getAdapterPosition());//传入当前所在位置
                    playerBinder.playMusic(song.getPath());//播放音乐
                    playerBinder.setSongList(localSongs);//播放音乐时更新当前服务中的歌单
                }
            });
            holder.musicChoice.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    chosedSong = localSongs.get(holder.getAdapterPosition());//得到当前选中的歌曲的实例
                    openPopWindow(v);
                }
            });
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Song song = localSongs.get(position);
            holder.musicSerial.setText(String.valueOf(position+1));
            holder.musicName.setText(song.getName());
            holder.musicSingerAlbum.setText(song.getSinger()+" - "+song.getAlbum());
        }

        @Override
        public int getItemCount() {
            return localSongs.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            View musicView;
            TextView musicSerial;
            TextView musicName;
            TextView musicSingerAlbum;
            ImageView musicChoice;

            public ViewHolder(@NonNull View itemView) {
                //itemView是RecyclerView的子项的最外层布局，进而获取该布局中存放的image和name
                super(itemView);
                musicView = itemView;
                musicSerial = itemView.findViewById(R.id.music_serial);
                musicName = itemView.findViewById(R.id.music_name);
                musicSingerAlbum = itemView.findViewById(R.id.music_singer_and_album);
                musicChoice = itemView.findViewById(R.id.music_choice);
            }
        }
    }

    public void showPopwindow()
    {
        //加载弹出框的布局
        contentView = LayoutInflater.from(activity).inflate(
                R.layout.singlesong_popup, null);

        popupWindow = new PopupWindow(contentView,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setFocusable(true);// 取得焦点
        //给弹框随意设置一个背景
        Bitmap bmp = BitmapFactory.decodeResource(activity.getResources(), R.drawable.music);
        Drawable drawable = new BitmapDrawable(activity.getResources(), bmp);

        popupWindow.setBackgroundDrawable(drawable);
        //点击外部消失
        popupWindow.setOutsideTouchable(true);
        //设置可以点击
        popupWindow.setTouchable(true);
        //进入退出的动画，指定刚才定义的style
        popupWindow.setAnimationStyle(R.style.popwindow_anim_style);

        Button btn_add = contentView.findViewById(R.id.music_addtolist);
        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //添加至已有歌单：本想再开一个底部菜单，但还是直接到下一个activity更省事
                popupWindow.dismiss();
                //TODO 难点：怎么在底部菜单中确认当前点击的是哪一个歌曲？解决：用一个Song对象来记录选中过的。
                //TODO 后期应该再传一个歌单名进来，方便实现歌单内的播放模式：单曲循环（用mediaplayer()的方法设置即可） 顺序播放 随机播放
                Intent intent = new Intent(activity, ListRootActivity.class);
                intent.putExtra("singer",chosedSong.getSinger());
                intent.putExtra("name",chosedSong.getName());
                intent.putExtra("album",chosedSong.getAlbum());
                intent.putExtra("path",chosedSong.getPath());
                intent.putExtra("duration",chosedSong.getDuration());
                intent.putExtra("size",chosedSong.getSize());
                intent.putExtra("pic_id",chosedSong.getPic_id());
                intent.putExtra("lyric_id",chosedSong.getLyric_id());
                activity.startActivity(intent);
            }
        });

        Button btn_download = contentView.findViewById(R.id.music_download);
        btn_download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //当前为网络音乐时，就进行下载。网络音乐与本地音乐的一个区别是网络音乐的歌词id不为0
                if( chosedSong.getLyric_id() != 0 )
                {
                    downloadBinder.startDownload("http://music.163.com/song/media/outer/url?id=" + chosedSong.getLyric_id() + ".mp3");
                    //downloadBinder.startDownload2(chosedSong.getLyric_id());
                }
            }
        });

        Button btn_delete = contentView.findViewById(R.id.music_delete);
        btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentList.setNum(currentList.getNum()-1);
                currentList.getSongList().remove(chosedSong);
                adapter.notifyDataSetChanged();
                popupWindow.dismiss();

                SongListDatabase database = SongListDatabase.getDataBase(activity);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        database.getSongListDao().updateSong(currentList);
                    }
                }).start();
            }
        });
    }

    public void openPopWindow(View v)
    {
        //从底部显示
        popupWindow.showAtLocation(contentView, Gravity.BOTTOM, 0, 0);
    }

    //为保证底部菜单的正确显示与退出，必须在原活动中重写onKeyDown,并在里面调用这个方法！！！
    public Boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if( event.getKeyCode() == KeyEvent.KEYCODE_BACK ){
            if( popupWindow != null && popupWindow.isShowing() ){
                popupWindow.dismiss();
                return true;
            }
            else
            {
                activity.finish();
                return true;
            }
        }
        return false;
    }

    //以下为binder对外的接口
    public void playOrPause()
    {
        playerBinder.playOrPause();
    }

    public Boolean isPlaying()
    {
        return playerBinder.isPlaying();
    }

    public void playMusic(String path)
    {
        playerBinder.playMusic(path);
    }

    public void nextMusic(){
        playerBinder.nextMusic();
    }

    public void lastMusic(){
        playerBinder.lastMusic();
    }

    public void setSingleMode(){
        playerBinder.setSingleMode();
    }

    public void setCycleMode(){
        playerBinder.setCycleMode();
    }

    public void setRandomMode(){
        playerBinder.setRandomMode();
    }

}
