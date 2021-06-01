package com.example.mymusicplayer.activity;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.mymusicplayer.entity.SongList;
import com.example.mymusicplayer.room.SongListDao;
import com.example.mymusicplayer.room.SongListDatabase;

import java.util.List;


//由于只用到了数据库，写法暂时和MineViewModel完全一致
public class ListRootViewModel extends AndroidViewModel {
    private SongListDao songListDao;
    //这里要用livedata
    private LiveData<List<SongList>> allSongListLive;

    public ListRootViewModel(@NonNull Application application) {
        super(application);
        SongListDatabase songListDatabase = SongListDatabase.getDataBase(application);
        songListDao = songListDatabase.getSongListDao();
        allSongListLive = songListDao.getAllSongListsLive();
    }

    //这里要用livedata
    public LiveData<List<SongList>> getAllSongListLive() {
        //if( allSongListLive == null ) {
        //allSongListLive = new MutableLiveData<>();
        //}
        return allSongListLive;
    }

    //以下为操纵歌单列表的接口
    public void insertSongList(SongList... songLists)
    {
        songListDao.insertSong(songLists);
    }

    public void updateSongList(SongList... songLists)
    {
        songListDao.updateSong(songLists);
    }

    public void deleteSongList(SongList... songLists)
    {
        songListDao.deleteSong(songLists);
    }

    public void deleteAllLists()
    {
        songListDao.deleteAllSong();
    }
}
