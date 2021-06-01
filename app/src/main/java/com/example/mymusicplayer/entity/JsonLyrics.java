package com.example.mymusicplayer.entity;

import com.google.gson.annotations.SerializedName;

public class JsonLyrics {

    @SerializedName("lyric")
    private String lyric;
    @SerializedName("tlyric")
    private String tlyric;

    public String getLyric() {
        return lyric;
    }

    public void setLyric(String lyric) {
        this.lyric = lyric;
    }

    public String getTlyric() {
        return tlyric;
    }

    public void setTlyric(String tlyric) {
        this.tlyric = tlyric;
    }
}
