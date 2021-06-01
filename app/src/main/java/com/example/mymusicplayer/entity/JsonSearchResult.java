package com.example.mymusicplayer.entity;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class JsonSearchResult {

    @SerializedName("id")
    private Integer id;
    @SerializedName("name")
    private String name;
    @SerializedName("artist")
    private List<String> artist;
    @SerializedName("album")
    private String album;
    @SerializedName("pic_id")
    private String picId;
    @SerializedName("url_id")
    private Integer urlId;
    @SerializedName("lyric_id")
    private Integer lyricId;
    @SerializedName("source")
    private String source;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getArtist() {
        return artist;
    }

    public void setArtist(List<String> artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getPicId() {
        return picId;
    }

    public void setPicId(String picId) {
        this.picId = picId;
    }

    public Integer getUrlId() {
        return urlId;
    }

    public void setUrlId(Integer urlId) {
        this.urlId = urlId;
    }

    public Integer getLyricId() {
        return lyricId;
    }

    public void setLyricId(Integer lyricId) {
        this.lyricId = lyricId;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
