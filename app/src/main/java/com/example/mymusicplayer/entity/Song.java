package com.example.mymusicplayer.entity;

public class Song {
    private String singer;

    private String name;

    private String path;

    private String album;

    private int duration;

    private long size;

    private String pic_id;

    private int lyric_id;

    public String getSinger() {
        return singer;
    }

    public void setSinger(String singer) {
        this.singer = singer;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getPic_id() {
        return pic_id;
    }

    public void setPic_id(String pic_id) {
        this.pic_id = pic_id;
    }

    public int getLyric_id() {
        return lyric_id;
    }

    public void setLyric_id(int lyric_id) {
        this.lyric_id = lyric_id;
    }
}
