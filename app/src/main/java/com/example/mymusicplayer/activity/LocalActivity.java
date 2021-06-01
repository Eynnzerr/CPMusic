package com.example.mymusicplayer.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.mymusicplayer.R;
import com.example.mymusicplayer.entity.Song;
import com.example.mymusicplayer.entity.SongList;
import com.example.mymusicplayer.utils.LocalMusicUtils;
import com.example.mymusicplayer.utils.PlayerServiceUtils;

import java.util.ArrayList;
import java.util.List;

//本地歌单活动界面，由点击本地按钮跳转而来
public class LocalActivity extends AppCompatActivity {

    private PlayerServiceUtils playerServiceUtils;

    private List<Song> localSongs = new ArrayList<>();
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private PlayerServiceUtils.MusicAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local);

        //使用自己封装的帮助类
        playerServiceUtils = new PlayerServiceUtils(this);
        playerServiceUtils.showPopwindow();//加载底部菜单
        playerServiceUtils.setSongList(new SongList());//避免空指针
        playerServiceUtils.bindPlayerService();//绑定服务

        recyclerView = findViewById(R.id.local_music);
        layoutManager = new LinearLayoutManager(this);
        adapter = playerServiceUtils.getAdapter();//获取封装的适配器
        //不能在这里直接传！因为服务的连接是异构的，绑定后直接使用不能保证已经绑定成功，可能会引发空指针
        recyclerView.setLayoutManager(layoutManager);
        adapter.setLocalSongs(localSongs);
        recyclerView.setAdapter(adapter);



        //申请访问权限
        if(ContextCompat.checkSelfPermission(LocalActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)//如果未授权
        {
            ActivityCompat.requestPermissions(LocalActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);//申请权限
        }
        else {
            initLocalSongs();
        }

    }

    //载入本地音乐
    private void initLocalSongs()
    {
        //recyclerView = findViewById(R.id.local_music);
        //layoutManager = new LinearLayoutManager(this);
        //adapter = new MusicAdapter(playerBinder,connection);
        //recyclerView.setLayoutManager(layoutManager);
        localSongs = LocalMusicUtils.getMusicData(this);
        adapter.setLocalSongs(localSongs);
        //recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode)
        {
            case 1:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)//如果授权了
                {
                    initLocalSongs();
                }
                else//如果拒绝了
                {
                    Toast.makeText(this,"You denied the permission", Toast.LENGTH_SHORT).show();
                    finish();//暂时先退出
                }
                break;
            default:
                break;
        }
    }

    //必须重写这个方法
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        return playerServiceUtils.onKeyDown(keyCode,event);
    }

}