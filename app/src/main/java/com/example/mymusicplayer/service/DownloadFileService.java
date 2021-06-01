package com.example.mymusicplayer.service;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;
import retrofit2.http.Streaming;

public interface DownloadFileService {
    @Streaming
    @GET("url")
    Call<ResponseBody> downloadFile(@Query("id") String id);

}
