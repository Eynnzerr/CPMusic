package com.example.mymusicplayer.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;

import com.example.mymusicplayer.R;
import com.example.mymusicplayer.entity.Song;
import com.example.mymusicplayer.entity.SongList;
import com.example.mymusicplayer.utils.PlayerServiceUtils;

import java.util.ArrayList;
import java.util.List;

//一个歌单的详情，仅在MineFragment中歌单列表的某一歌单被点击时跳转至该活动，并展示歌单内所有音乐
public class SongListActivity extends AppCompatActivity {

    private SongListViewModel viewModel;
    private SongList songList;
    private List<Song> songs = new ArrayList<>();
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private PlayerServiceUtils.MusicAdapter adapter;

    private PlayerServiceUtils playerServiceUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_list);

        playerServiceUtils = new PlayerServiceUtils(this);
        playerServiceUtils.showPopwindow();//加载底部菜单
        playerServiceUtils.bindPlayerService();//绑定服务
        adapter = playerServiceUtils.getAdapter();//获取封装的适配器

        //获取viewModel进而获取当前歌单
        viewModel = new ViewModelProvider(this).get(SongListViewModel.class);

        //获得到当前所处歌单及歌曲
        Intent intent = getIntent();
        String songListName = intent.getStringExtra("list_name");
        Log.d("debug",songListName);
        new Thread(new Runnable() {
            @Override
            public void run() {
                songList = viewModel.getSongList(songListName);
                songs = songList.getSongList();
                adapter.setLocalSongs(songs);
                playerServiceUtils.setSongList(songList);//把当前所在歌单也传入帮助类
            }
        }).start();



        //这部分代码需要改进，改成等待上个线程获取到songs后再设置。
        //设置recyclerView
        recyclerView = findViewById(R.id.songs);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        if( songs.isEmpty() ) {//等一下
            try {
                Thread.currentThread().sleep(100);//TODO 笨拙的休眠，日后改进
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        adapter.setLocalSongs(songs);
        recyclerView.setAdapter(adapter);

    }

    //必须重写这个方法
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        return playerServiceUtils.onKeyDown(keyCode,event);
    }


}