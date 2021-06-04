package com.example.mymusicplayer.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.SeekBar;

import com.example.mymusicplayer.R;
import com.example.mymusicplayer.databinding.ActivityPlayingBinding;
import com.example.mymusicplayer.databindinglistener.PlayingActivityHandleListener;
import com.example.mymusicplayer.entity.TimeLineLyric;
import com.example.mymusicplayer.fragment.AlbumFragment;
import com.example.mymusicplayer.fragment.LyricFragment;
import com.example.mymusicplayer.service.PlayerService;
import com.example.mymusicplayer.utils.NetworkUtils;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

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
                    if(playerBinder.isPlaying()) {
                        playingViewModel.restartTiming();
                    }
                    else playingViewModel.startTiming();//TODO bug1 这一行是为了自动切换歌曲时进度条从0开始，但导致暂停音乐时退出重进播放活动也会因此开始计时任务
                    //bug2 haihui导致暂停后如果直接下一首会多一个计时任务
                    binding.playProgress.setMax(duration);//进度条的进度单位是毫秒！
                    binding.playCurrentTime.setText(0+"");
                    binding.playEndTime.setText(formatMilliTime(duration));

                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            //TODO
        }
    };


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_playing);

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

        binding.viewpager2.setAdapter(new FragmentStateAdapter(PlayingActivity.this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                if( position == 0 ) {
                    return new LyricFragment();
                }
                else return new AlbumFragment();
            }

            @Override
            public int getItemCount() {
                return 2;
            }
        });

        TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(binding.tabLayout, binding.viewpager2, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                switch (position){
                    case 0:
                        tab.setText("歌词");
                        break;
                    case 1:
                        tab.setText("专辑");
                        break;
                    default:
                        break;
                }
            }
        });
        tabLayoutMediator.attach();

    }

    //将毫秒转换为"分：秒"格式
    private String formatMilliTime(int duration) {
        int second = duration / 1000;
        int minute = second / 60;
        second %= 60;
        return(minute+":"+second);
    }
}