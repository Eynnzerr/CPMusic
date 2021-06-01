package com.example.mymusicplayer.fxxk;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mymusicplayer.R;
import com.example.mymusicplayer.activity.SongListActivity;
import com.example.mymusicplayer.entity.SongList;

import java.util.ArrayList;
import java.util.List;

//在"我的"活动页面加载所有歌单
@Deprecated
public class SongListAdapter extends RecyclerView.Adapter<SongListAdapter.ViewHolder>{

    //一个装载有歌单的列表
    private List<SongList> songLists = new ArrayList<>();

    public SongListAdapter(List<SongList> songLists) {
        this.songLists = songLists;
    }

    public SongListAdapter() {
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
                Log.d("debug","你点击了一个歌单");
                //TODO 这里需要点击进入歌单展示活动SongListActivity，难点在于如何获取当前所选歌单并传递给下一个activity，并且在下一个activity中实现增删改查
                //TODO 一个很自然的想法是把当前歌单的名称传给下一个activity里，再在下一个activity查询数据库来得到当前歌单。

                SongList songList = songLists.get(holder.getAdapterPosition());
                Intent intent = new Intent(view.getContext(), SongListActivity.class);
                intent.putExtra("list_name",songList.getName());
                view.getContext().startActivity(intent);
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

    static class ViewHolder extends RecyclerView.ViewHolder {
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
