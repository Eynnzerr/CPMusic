package com.example.mymusicplayer.databindinglistener;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.example.mymusicplayer.R;
import com.example.mymusicplayer.activity.PlayingViewModel;
import com.example.mymusicplayer.service.PlayerService;

import java.util.logging.Handler;

public class PlayingActivityHandleListener {

    private static final int SINGLE_MODE = 1;
    private static final int CYCLE_MODE = 2;
    private static final int RANDOM_MODE = 3;

    private int playMode;
    private PlayingViewModel viewModel;
    private PlayerService.PlayerBinder playerBinder;

    public PlayingActivityHandleListener(Activity activity, PlayingViewModel viewModel, PlayerService.PlayerBinder playerBinder) {
        this.viewModel = viewModel;
        this.playerBinder = playerBinder;
        playMode = playerBinder.getMode();
    }

    public void onImageClicked(View view)
    {
        ImageView imageView = (ImageView) view;
        switch (view.getId())
        {
            case R.id.play_mode:
                if( playMode++ == RANDOM_MODE ) playMode = 1;
                switch (playMode)
                {
                    case 1:
                        imageView.setImageResource(R.drawable.singlemode);
                        playerBinder.setSingleMode();
                        break;
                    case 2:
                        imageView.setImageResource(R.drawable.cyclemode);
                        playerBinder.setCycleMode();
                        break;
                    case 3:
                        imageView.setImageResource(R.drawable.randommode);
                        playerBinder.setRandomMode();
                        break;
                    default:
                        break;
                }
                Log.d("HandleListener","切换模式成功，当前模式为:" + playMode);
                break;
            case R.id.play_lastmusic:
                playerBinder.lastMusic();
                viewModel.stopTiming();
                viewModel.startTiming();
                Log.d("HandleListener","上一首");
                break;
            case R.id.play_playorpause:
                playerBinder.playOrPause();
                if( playerBinder.isPlaying() ) {
                    imageView.setImageResource(R.drawable.playbutton);
                    viewModel.restartTiming();
                }
                else {
                    imageView.setImageResource(R.drawable.pausebutton);
                    viewModel.stopTiming();
                }
                Log.d("HandleListener","暂停/继续播放");
                break;
            case R.id.play_nextmusic:
                playerBinder.nextMusic();
                viewModel.stopTiming();
                viewModel.startTiming();
                Log.d("HandleListener","下一首");
                break;
            case R.id.play_list:
                //TODO
                Log.d("HandleListener","显示列表");
                break;
        }
    }
}
