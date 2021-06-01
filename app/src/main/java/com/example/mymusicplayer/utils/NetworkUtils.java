package com.example.mymusicplayer.utils;

import android.util.Log;

import com.example.mymusicplayer.entity.GsonInstance;
import com.example.mymusicplayer.entity.JsonLyrics;
import com.example.mymusicplayer.entity.JsonSearchResult;
import com.example.mymusicplayer.entity.Song;
import com.example.mymusicplayer.entity.TimeLineLyric;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

//网络操作的工具类
public class NetworkUtils {
    public static final String SEARCH_SONGS = "http://api.sunyj.xyz?site=netease&search=";
    public static final String GET_LYRIC = "http://api.sunyj.xyz/?site=netease&lyric=";
    //public static final String GET_PIC = "http://api.sunyj.xyz/?site=netease&pic=";
    //将获取到的jsonResult对象转化为Song对象
    public static List<Song> parseJOBToSong(List<JsonSearchResult> jsonSearchResults)
    {
        List<Song> songs = new ArrayList<>();
        for( JsonSearchResult single : jsonSearchResults )
        {
            Song song = new Song();
            song.setSize(0);//获取不到，设为0
            song.setDuration(0);//获取不到，设为0
            song.setPath("http://music.163.com/song/media/outer/url?id=" + single.getId() + ".mp3");
            song.setAlbum(single.getAlbum());
            song.setName(single.getName());
            song.setSinger(single.getArtist().toString());
            song.setLyric_id(single.getLyricId());
            song.setPic_id(single.getPicId());
            songs.add(song);
        }
        return songs;
    }

    //通过url获取歌词
    public static String getLyric(int lyric_id)
    {
        OkHttpClient okHttpClient = new OkHttpClient();
        Gson gson = GsonInstance.getInstance().getGson();
        Request request = new Request.Builder().url(NetworkUtils.GET_LYRIC + lyric_id).build();

        FutureTask<String> futureTask = new FutureTask<>(new Callable<String>() {
            @Override
            public String call() throws Exception {
                Response response = okHttpClient.newCall(request).execute();
                String responseData = response.body().string();
                JsonLyrics lyrics = gson.fromJson(responseData,JsonLyrics.class);
                Log.d("NetWorkUtils","获取歌词:" + lyrics.getLyric().substring(1,7));
                return lyrics.getLyric();
            }
        });

        new Thread(futureTask).start();
        try {
            return futureTask.get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "";//获取失败，返回空串
    }

    //解析歌曲歌词
    public static List<TimeLineLyric> parseStringToLyrics(String lyrics)
    {
        if( lyrics.equals("")) return null;
        List<TimeLineLyric> timeLineLyrics = new ArrayList<>();
        TimeLineLyric curLineLyric;
        //TimeLineLyric prevLyric = null;
        String[] preProcessed = lyrics.split("\\n");
        for( String lyric : preProcessed)
        {
            curLineLyric = new TimeLineLyric();
            //String time = lyric.substring(1,10);
            //String text = lyric.substring(11);
            lyric = lyric.replace("[","");
            lyric = lyric.replace("]","@");
            //Log.d("parseString",lyric);
            String[] splitData = lyric.split("@");
            curLineLyric.setStartTime(getTime(splitData[0]));
            if( splitData.length > 1 ) curLineLyric.setLyric(splitData[1]);
            else curLineLyric.setLyric("");//这行可能不太需要，因为默认值就是空串
            //curLineLyric.setStartTime(getTime(time));
            //curLineLyric.setLyric(text);
            //if( prevLyric != null ) prevLyric.setEndTime(curLineLyric.getStartTime());
            //Log.d("parseString","startTime="+curLineLyric.getStartTime());
            timeLineLyrics.add(curLineLyric);
        }
        return timeLineLyrics;
    }

    //计算一行歌词的开始时间(单位为毫秒)
    private static int getTime(String timeStr) {
        timeStr = timeStr.replace(":", ".");
        timeStr = timeStr.replace(".", "@");

        String timeData[] = timeStr.split("@"); //将时间分隔成字符串数组

        //分离出分、秒并转换为整型
        int minute = Integer.parseInt(timeData[0]);
        int second = Integer.parseInt(timeData[1]);
        int millisecond = Integer.parseInt(timeData[2]);

        //转换为毫秒数
        //针对不同格式(毫秒的位数)进行转换
        int currentTime;
        if( timeData[2].length() == 3 )
            currentTime = (minute * 60 + second) * 1000 + millisecond;
        else
            currentTime = (minute * 60 + second) * 1000 + millisecond * 10;
        return currentTime;
    }
}
