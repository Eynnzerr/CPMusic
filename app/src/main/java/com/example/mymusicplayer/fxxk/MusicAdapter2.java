package com.example.mymusicplayer.fxxk;

import android.content.ServiceConnection;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mymusicplayer.R;
import com.example.mymusicplayer.entity.Song;
import com.example.mymusicplayer.service.PlayerService;

import java.util.ArrayList;
import java.util.List;

//暂被废弃
@Deprecated
public class MusicAdapter2 extends RecyclerView.Adapter<MusicAdapter2.ViewHolder> {

    private List<Song> localSongs = new ArrayList<>();

    private PlayerService.PlayerBinder playerBinder;
    private ServiceConnection connection;

    public MusicAdapter2(PlayerService.PlayerBinder playerBinder, ServiceConnection connection) {
        this.playerBinder = playerBinder;
        this.connection = connection;
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
                Log.d("debug","debug");
                //if( !playerBinder.isPlaying() )
                //{
                    //Song song = localSongs.get(holder.getAdapterPosition());//得到当前选中的歌曲的实例
                    //playerBinder.playMusic(song.getPath());
                //}
                //playerBinder.test();
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Song song = localSongs.get(position);
        holder.musicName.setText(song.getName());
        holder.musicSingerAlbum.setText(song.getSinger()+" - "+song.getAlbum());
    }

    @Override
    public int getItemCount() {
        return localSongs.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        View musicView;
        TextView musicName;
        TextView musicSingerAlbum;

        public ViewHolder(@NonNull View itemView) {
            //itemView是RecyclerView的子项的最外层布局，进而获取该布局中存放的image和name
            super(itemView);
            musicView = itemView;
            musicName = itemView.findViewById(R.id.music_name);
            musicSingerAlbum = itemView.findViewById(R.id.music_singer_and_album);
        }
    }
}
