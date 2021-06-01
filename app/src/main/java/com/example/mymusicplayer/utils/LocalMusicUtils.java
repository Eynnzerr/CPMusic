package com.example.mymusicplayer.utils;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

import com.example.mymusicplayer.entity.Song;

import java.util.ArrayList;
import java.util.List;

public class LocalMusicUtils {
    public static List<Song> getMusicData(Context context) {
        List<Song> list = new ArrayList<Song>();
        //使用getContentResolver查询本地媒体库，展示本地音乐
        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null,
                null, MediaStore.Audio.AudioColumns.IS_MUSIC);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                Song song = new Song();
                song.setName(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)));
                song.setSinger(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)));
                song.setAlbum(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)));
                song.setPath(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)));//播放时需要用
                song.setDuration(cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)));
                song.setSize(cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)));
                song.setPic_id("LOCAL_ABSENT");//获取不到
                song.setLyric_id(0);//获取不到
                if (song.getSize() > 1000 * 800) {
                    // 注释部分是切割标题，分离出歌曲名和歌手 （本地媒体库读取的歌曲信息不规范）
                    if (song.getName().contains("-")) {
                        String[] str = song.getName().split("-");
                        song.setSinger(str[0]);
                        song.setName(str[1]);
                    }
                    list.add(song);
                }
            }
            // 释放资源
            cursor.close();
        }

        return list;
    }

    /**
     * 定义一个方法用来格式化获取到的时间
     */
    public static String formatTime(int time) {
        if (time / 1000 % 60 < 10) {
            return time / 1000 / 60 + ":0" + time / 1000 % 60;

        } else {
            return time / 1000 / 60 + ":" + time / 1000 % 60;
        }
    }
}
