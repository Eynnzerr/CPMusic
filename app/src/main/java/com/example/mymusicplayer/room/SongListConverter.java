package com.example.mymusicplayer.room;

import androidx.room.TypeConverter;

import com.example.mymusicplayer.entity.GsonInstance;
import com.example.mymusicplayer.entity.Song;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

//使用gson解决room不能存储list的问题。思路是将list转化为json字符串存入room，再在使用时解析回list
public class SongListConverter {

    @TypeConverter
    public String songListToString(List<Song> list)
    {
        return GsonInstance.getInstance().getGson().toJson(list);
    }

    @TypeConverter
    public List<Song> StringToSongList(String json)
    {
        Type listType = new TypeToken<List<Song>>(){}.getType();
        return GsonInstance.getInstance().getGson().fromJson(json,listType);
    }
}
