package com.example.mymusicplayer.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mymusicplayer.R;
import com.example.mymusicplayer.entity.Song;
import com.example.mymusicplayer.entity.SongList;

import java.util.ArrayList;
import java.util.List;

//歌单活动界面，暂时只显示一个recyclerView
//该活动仅由点击歌单父目录的子项跳转而来。跳转时，应当找到数据库中对应的歌单，并载入歌单
//TODO 怎么把选中的歌曲传给当前活动？笨方法：用putextra把song的各个字段逐个传进来
public class ListRootActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private SongListAddAdapter adapter;

    private ListRootViewModel viewModel;
    private LiveData<List<SongList>> allSongListLive;
    private List<SongList> lists;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_root);

        recyclerView = findViewById(R.id.songlist_choosetoadd);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new SongListAddAdapter();
        recyclerView.setAdapter(adapter);

        //当前页面并不需要对songlist进行改动，但仍需要通过observe获得列表。
        viewModel = new ViewModelProvider(this).get(ListRootViewModel.class);
        allSongListLive = viewModel.getAllSongListLive();
        allSongListLive.observe(this, new Observer<List<SongList>>() {
            @Override
            public void onChanged(List<SongList> songLists) {
                lists = songLists;
                adapter.setSongLists(songLists);
                adapter.notifyDataSetChanged();//增删歌单时自动更新
            }
        });

    }


    //TODO 这里只为了更改点击事件就直接copy了SongListAdapter，十分臃肿，令人难受，但不知如何改进。
    public class SongListAddAdapter extends RecyclerView.Adapter<SongListAddAdapter.ViewHolder>{

        //一个装载有歌单的列表
        private List<SongList> songLists = new ArrayList<>();

        public SongListAddAdapter(List<SongList> songLists) {
            this.songLists = songLists;
        }

        public SongListAddAdapter() {
        }

        public void setSongLists(List<SongList> songLists) {
            this.songLists = songLists;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.songlist_item,parent,false);
            final ViewHolder holder = new ViewHolder(view);
            holder.songListView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Song song = new Song();
                    Intent intent = getIntent();
                    song.setSinger(intent.getStringExtra("singer"));
                    song.setName(intent.getStringExtra("name"));
                    song.setAlbum(intent.getStringExtra("album"));
                    song.setPath(intent.getStringExtra("path"));
                    song.setDuration(intent.getIntExtra("duration",0));
                    song.setSize(intent.getLongExtra("size",0));
                    song.setPic_id(intent.getStringExtra("pic_id"));
                    song.setLyric_id(intent.getIntExtra("lyric_id",0));

                    SongList songList = lists.get(holder.getAdapterPosition());//获取选中的歌单实例
                    songList.getSongList().add(song);
                    songList.setNum(songList.getNum()+1);//歌曲数要+1
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            viewModel.updateSongList(songList);//更新对应的歌单的歌曲列表
                        }
                    }).start();
                    Toast.makeText(ListRootActivity.this,"添加成功",Toast.LENGTH_SHORT);
                    finish();
                    Log.d("onClick","添加成功");
                }
            });
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            SongList songList = songLists.get(position);
            holder.songListName.setText(songList.getName());
            holder.songListNum.setText(songList.getNum()+"");
            holder.songListCover.setImageResource(songList.getImageId());
        }

        @Override
        public int getItemCount() {
            return songLists.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            View songListView;
            TextView songListName;
            TextView songListNum;
            ImageView songListCover;

            public ViewHolder(@NonNull View itemView) {
                //itemView是RecyclerView的子项的最外层布局，进而获取该布局中存放的image和name
                super(itemView);
                songListView= itemView;
                songListName = itemView.findViewById(R.id.songlist_name);
                songListNum = itemView.findViewById(R.id.songlist_musicnums);
                songListCover = itemView.findViewById(R.id.songlist_cover);
            }
        }
    }
}