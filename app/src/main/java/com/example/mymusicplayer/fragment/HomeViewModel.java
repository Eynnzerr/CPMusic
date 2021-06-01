package com.example.mymusicplayer.fragment;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.mymusicplayer.entity.SongList;

import java.util.List;

public class HomeViewModel extends ViewModel {
    private LiveData<List<SongList>> allSongListLive;

    public LiveData<List<SongList>> getAllSongListLive() {
        return allSongListLive;
    }

}