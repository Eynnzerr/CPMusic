package com.example.mymusicplayer.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.example.mymusicplayer.room.SongListConverter;

import java.util.List;

@Entity
@TypeConverters(SongListConverter.class)
public class SongList {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "list_name")
    private String name;

    @ColumnInfo(name = "cover_id")
    private int imageId;

    @ColumnInfo(name = "num_of_songs")
    private int num;

    @ColumnInfo(name = "song_list")
    private List<Song> songList;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getImageId() {
        return imageId;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public List<Song> getSongList() {
        return songList;
    }

    public void setSongList(List<Song> songList) {
        this.songList = songList;
    }

    public SongList(String name, List<Song> songList) {
        this.name = name;
        this.songList = songList;
    }

    @Ignore
    public SongList(){
        //无参构造
    }
}
