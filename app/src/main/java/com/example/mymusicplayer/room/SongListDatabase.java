package com.example.mymusicplayer.room;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.mymusicplayer.entity.SongList;

@Database(entities = {SongList.class},version = 1,exportSchema = false)
public abstract class SongListDatabase extends RoomDatabase {
    //singleton
    private static final String DATABASE_NAME = "songlist_database";
    private static SongListDatabase INSTANCE;
    public static synchronized SongListDatabase getDataBase(Context context)
    {
        if( INSTANCE == null )
        {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(),SongListDatabase.class,DATABASE_NAME)
                    .build();
        }
        return INSTANCE;
    }
    public abstract SongListDao getSongListDao();
}
