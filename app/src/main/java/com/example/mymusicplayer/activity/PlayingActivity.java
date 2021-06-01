package com.example.mymusicplayer.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.SeekBar;

import com.example.mymusicplayer.R;
import com.example.mymusicplayer.databinding.ActivityPlayingBinding;
import com.example.mymusicplayer.databindinglistener.PlayingActivityHandleListener;
import com.example.mymusicplayer.entity.TimeLineLyric;
import com.example.mymusicplayer.service.PlayerService;
import com.example.mymusicplayer.utils.NetworkUtils;

import java.util.List;

//点击播放栏时自动跳转到该活动界面，展示歌词。
//界面内容只与当前service的状态有关。全部从service获取
//歌词显示的实现：需要能随时得知当前服务正在播放的音乐是哪一首，并网络请求获得其歌词。解析歌词后以某种方法展示出来。
//一种思路：借助livedata对service中的seq进行观察，每当seq发生改变时就重新获取歌词。这需要重写service里设置的内容，以及更改其它用到了seq的地方
//至于歌词随播放进度滚动，可以调用这些方法getDuration() seekTo(int msec) setOnSeekCompleteListener(MediaPlayer.OnSeekCompleteListener listener)
//甚至可以用来实现指定位置播放的效果
public class PlayingActivity extends AppCompatActivity {

    private PlayingViewModel playingViewModel;

    private PlayerService.PlayerBinder playerBinder;

    private ActivityPlayingBinding binding;

    private MutableLiveData<Integer> seqLive;

    private List<TimeLineLyric> lyrics;

    private Handler handler = new Handler();

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d("PlayingActivity","服务已绑定");
            playerBinder = (PlayerService.PlayerBinder) service;

            //根据状态不同对UI进行设置
            if( playerBinder.isPlaying() ) binding.playPlayorpause.setImageResource(R.drawable.playbutton);
            else binding.playPlayorpause.setImageResource(R.drawable.pausebutton);
            switch (playerBinder.getMode())
            {
                case 1:
                    binding.playMode.setImageResource(R.drawable.singlemode);
                    break;
                case 2:
                    binding.playMode.setImageResource(R.drawable.cyclemode);
                    break;
                case 3:
                    binding.playMode.setImageResource(R.drawable.randommode);
                    break;
                default:
                    break;
            }

            binding.setPlayingActivityHandleListener(new PlayingActivityHandleListener(PlayingActivity.this,playingViewModel,playerBinder));
            seqLive = (MutableLiveData<Integer>) playerBinder.getSeqLive();
            seqLive.observe(PlayingActivity.this, new Observer<Integer>() {
                @Override
                public void onChanged(Integer integer) {
                    //每当播放音乐的seq发生改变，则说明音乐换了，需要更新进度条，歌词等一系列ui。
                    Log.d("PlayingActivity","观察到seq更新为" + integer);
                    playingViewModel.stopTiming();
                    int duration = playerBinder.getDuration();
                    //playingViewModel.setCurrentProgress(0);
                    playingViewModel.setCurrentProgress(playerBinder.getCurrentPosition());
                    //Log.d("PlayingActivity","currentPosition: " + playerBinder.getCurrentPosition());

                    //主要问题是seq改变有两种情况：1.进入当前活动，此时实际上没有切换歌曲，progress应继承自currentProgress；2.切换歌曲，此时progress应该置零
                    //playingViewModel.startTiming(playerBinder.getCurrentPosition());
                    if(playerBinder.isPlaying()) {
                        playingViewModel.restartTiming();
                    }

                    binding.playProgress.setMax(duration);//进度条的进度单位是毫秒！
                    binding.playCurrentTime.setText(0+"");
                    binding.playEndTime.setText(formatMilliTime(duration));
                    //解析得到单行歌曲的列表
                    lyrics = NetworkUtils.parseStringToLyrics(NetworkUtils.getLyric(playerBinder.getSongList().get(playerBinder.getSeq()).getLyric_id()));
                    initLrc(lyrics);
                }
            });
            MutableLiveData<Boolean> isPause = (MutableLiveData<Boolean>) playerBinder.getIsPause();
            isPause.observe(PlayingActivity.this, new Observer<Boolean>() {
                @Override
                public void onChanged(Boolean aBoolean) {
                    if( aBoolean ) {
                        //binding.playPlayorpause.setImageResource(R.drawable.pausebutton);
                        handler.removeMessages(0);
                        //playingViewModel.stopTiming();
                    }
                    else{
                        //binding.playPlayorpause.setImageResource(R.drawable.playbutton);
                        //playingViewModel.restartTiming();
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                int index = getLyricIndex(lyrics);
                                binding.lyricView.setIndex(index);
                                //Log.d("handler","handler线程启动,当前index:" + index + " 歌单为空？" + (lyrics == null));
                                binding.lyricView.invalidate();//刷新view,自动调用onDraw
                                handler.postDelayed(this, 100);//递归调用
                                //TODO 这样无限期地递归调用post，不停创建新的线程，是否太过耗时？可不可以只post一个任务，其中使用while永真循环和sleep实现同样效果？
                            }
                        });
                    }
                }
            });

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            //TODO
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_playing);
        binding.lyricView.setMovementMethod(ScrollingMovementMethod.getInstance());

        //只绑定播放服务
        Intent bindIntent = new Intent(PlayingActivity.this,PlayerService.class);
        bindService(bindIntent,connection, BIND_AUTO_CREATE);
        Log.d("PlayingActivity","绑定服务成功");

        //获取viewModel
        playingViewModel = new ViewModelProvider(this).get(PlayingViewModel.class);
        final MutableLiveData<Integer> currentProgress = (MutableLiveData<Integer>) playingViewModel.getCurrentProgress();
        currentProgress.observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                binding.playProgress.setProgress(integer);
                binding.playCurrentTime.setText(formatMilliTime(integer));
            }
        });

        //设置可拖动的进度条
        binding.playProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //这里应该实现的逻辑是改变play_current_time
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //这里不需要进行任何操作
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //这里需要实现的逻辑是拖动完成后，将歌曲进度更新到当前进度，同时更新一系列UI
                playerBinder.seekTo(binding.playProgress.getProgress());
                currentProgress.setValue(binding.playProgress.getProgress());
            }
        });
    }


    public void initLrc(List<TimeLineLyric> lyrics){
        binding.lyricView.setmLrcList(lyrics);
        if( lyrics == null ) return;
        //切换带动画显示歌词
        //binding.lyricView.setAnimation(AnimationUtils.loadAnimation(PlayingActivity.this,R.anim.alpha_z));
        handler.removeMessages(0);//清空原来已有的歌词定位任务
        handler.post(new Runnable() {
            @Override
            public void run() {
                int index = getLyricIndex(lyrics);
                binding.lyricView.setIndex(index);
                //Log.d("handler","handler线程启动,当前index:" + index + " 歌单为空？" + (lyrics == null));
                binding.lyricView.invalidate();//刷新view,自动调用onDraw
                handler.postDelayed(this, 100);//递归调用
                //TODO 这样无限期地递归调用post，不停创建新的线程，是否太过耗时？可不可以只post一个任务，其中使用while永真循环和sleep实现同样效果？
            }
        });
    }



    //获取当前时间所在歌词下标
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

    //将毫秒转换为"分：秒"格式
    private String formatMilliTime(int duration)
    {
        int second = duration / 1000;
        int minute = second / 60;
        second %= 60;
        return(minute+":"+second);
    }
}