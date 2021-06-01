package com.example.mymusicplayer.databindinglistener;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;

import com.example.mymusicplayer.activity.LocalActivity;
import com.example.mymusicplayer.fragment.MineViewModel;
import com.example.mymusicplayer.R;
import com.example.mymusicplayer.entity.Song;
import com.example.mymusicplayer.entity.SongList;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MineFragHandleListener {
    private Activity activity;
    private MineViewModel viewModel;
    private ExecutorService executorService = Executors.newFixedThreadPool(1);

    public MineFragHandleListener(Activity activity, MineViewModel viewModel) {
        this.activity = activity;
        this.viewModel = viewModel;
    }

    public void onButtonClicked(View view)
    {
        switch (view.getId())
        {
            case R.id.find_local_music:
                Intent intent = new Intent(activity, LocalActivity.class);
                activity.startActivity(intent);
                break;
            case R.id.create_new:
                //点击创建按钮，即可创建一个歌单并添加至recyclerView
                View alertView = activity.getLayoutInflater().inflate(R.layout.songlist_newdialog,null,false);
                EditText newListText = alertView.findViewById(R.id.newdialog_name);
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setView(alertView);
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String newListName = newListText.getText().toString();
                        if( !newListName.equals("") )
                        {
                            SongList songList = new SongList(newListName,new ArrayList<Song>());
                            songList.setImageId(R.drawable.music);
                            songList.setNum(0);
                            executorService.submit(new Runnable() {
                                @Override
                                public void run() {
                                    viewModel.insertSongList(songList);
                                }
                            });
                        }
                    }
                });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //不作任何操作
                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
                break;
            case R.id.delete_all:
                executorService.submit(new Runnable() {
                    @Override
                    public void run() {
                        viewModel.deleteAllLists();
                    }
                });
                break;
            /*
            case R.id.open_playActivity:
                Intent intent1 = new Intent(activity,PlayingActivity.class);
                activity.startActivity(intent1);
                break;*/
            default:
                break;
        }
    }
}
