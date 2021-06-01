package com.example.mymusicplayer.entity;

import com.google.gson.Gson;

//单例模式创建gson对象
public class GsonInstance {
    private static GsonInstance INSTANCE;
    private static Gson gson;

    public static GsonInstance getInstance() {
        if (INSTANCE == null) {
            synchronized (GsonInstance.class) {
                if (INSTANCE == null) {
                    INSTANCE = new GsonInstance();
                }
            }
        }
        return INSTANCE;
    }

    public Gson getGson() {
        if (gson == null) {
            synchronized (GsonInstance.class) {
                if (gson == null) {
                    gson = new Gson();
                }
            }
        }
        return gson;
    }
}
