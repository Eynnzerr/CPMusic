package com.example.mymusicplayer.room;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.mymusicplayer.entity.SongList;

import java.util.List;

@Dao
public interface SongListDao {
    @Insert
    void insertSong(SongList... songLists);

    @Update
    void updateSong(SongList... songLists);

    @Delete
    void deleteSong(SongList... songLists);

    @Query("DELETE FROM SongList")
    void deleteAllSong();

    //查询特定项
    @Query("SELECT * FROM SongList WHERE list_name = :name")
    SongList getSongList(String name);

    @Query("SELECT * FROM SongList ORDER BY ID DESC")
        //List<SongList> getAllSongLists();
    LiveData<List<SongList>> getAllSongListsLive();

}
