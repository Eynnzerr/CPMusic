package com.example.mymusicplayer.entity;

//一行歌词的类
public class TimeLineLyric {
    private int startTime;
    private String lyric;

    public int getStartTime() {
        return startTime;
    }

    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }

    public String getLyric() {
        return lyric;
    }

    public void setLyric(String lyric) {
        this.lyric = lyric;
    }
}
