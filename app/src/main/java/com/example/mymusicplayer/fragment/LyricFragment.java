package com.example.mymusicplayer.fragment;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.mymusicplayer.R;
import com.example.mymusicplayer.activity.PlayingActivity;
import com.example.mymusicplayer.entity.TimeLineLyric;
import com.example.mymusicplayer.service.PlayerService;
import com.example.mymusicplayer.utils.NetworkUtils;
import com.example.mymusicplayer.view.LyricVIew;

import java.util.List;

import static android.content.Context.BIND_AUTO_CREATE;

public class LyricFragment extends Fragment {

    private PlayerService.PlayerBinder playerBinder;
    private List<TimeLineLyric> lyrics;
    private Handler handler = new Handler();
    private LyricVIew lyricView;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            playerBinder = (PlayerService.PlayerBinder) service;

            MutableLiveData<Integer> seqLive = (MutableLiveData<Integer>) playerBinder.getSeqLive();
            seqLive.observe(getViewLifecycleOwner(), new Observer<Integer>() {
                @Override
                public void onChanged(Integer integer) {
                    lyrics = NetworkUtils.parseStringToLyrics(NetworkUtils.getLyric(playerBinder.getSongList().get(playerBinder.getSeq()).getLyric_id()));
                    initLrc(lyrics);
                }
            });

            MutableLiveData<Boolean> isPause = (MutableLiveData<Boolean>) playerBinder.getIsPause();
            isPause.observe(getViewLifecycleOwner(), new Observer<Boolean>() {
                @Override
                public void onChanged(Boolean aBoolean) {
                    if( aBoolean ) {
                        handler.removeMessages(0);
                    }
                    else{
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                int index = getLyricIndex(lyrics);
                                lyricView.setIndex(index);
                                lyricView.invalidate();//刷新view,自动调用onDraw
                                handler.postDelayed(this, 100);//递归调用
                            }
                        });
                    }
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lyric, container, false);
        lyricView = view.findViewById(R.id.lyric_view2);

        Intent bindIntent = new Intent(getActivity(),PlayerService.class);
        getActivity().bindService(bindIntent,connection, BIND_AUTO_CREATE);

        return view;
    }

    public void initLrc(List<TimeLineLyric> lyrics){
        lyricView.setmLrcList(lyrics);
        if( lyrics == null ) return;
        handler.removeMessages(0);//清空原来已有的歌词定位任务
        handler.post(new Runnable() {
            @Override
            public void run() {
                int index = getLyricIndex(lyrics);
                lyricView.setIndex(index);
                //Log.d("handler","handler线程启动,当前index:" + index + " 歌单为空？" + (lyrics == null));
                lyricView.invalidate();//刷新view,自动调用onDraw
                handler.postDelayed(this, 100);//递归调用
            }
        });
    }

    public int getLyricIndex(List<TimeLineLyric> lyrics) {

        if( lyrics == null )
        {
            return -1;
        }

        int index = -1;
        int currentTime = 0;
        int duration = 0;

        if( playerBinder.isPlaying() ) {
            currentTime = playerBinder.getCurrentPosition();
            duration = playerBinder.getDuration();
        }
        //Log.d("getLyricIndex","currentTime=" + currentTime);
        //Log.d("getLyricIndex","duration=" + duration);

        if(currentTime < duration) {
            for (int i = 0; i < lyrics.size(); i++)
            {
                if ( i < lyrics.size() - 1 )
                {
                    if (currentTime < lyrics.get(i).getStartTime() && i == 0)
                    {
                        index = i;
                    }
                    if (currentTime > lyrics.get(i).getStartTime() && currentTime < lyrics.get(i + 1).getStartTime())
                    {
                        index = i;
                    }
                    //break;
                }
                if (i == lyrics.size() - 1 && currentTime > lyrics.get(i).getStartTime()) {
                    index = i;
                    //没有必要再break了
                }
            }
        }

        //Log.d("getLyricsIndex",index+"");
        //返回-1时说明已经播放完或未暂停播放中
        return index;
    }
}