package com.example.mymusicplayer.activity;

import android.widget.ProgressBar;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class PlayingViewModel extends ViewModel {
    private Timer timer;
    private MutableLiveData<Integer> currentProgress = new MutableLiveData<>();//单位为ms
    //private MutableLiveData<Integer> seqLive;


    public MutableLiveData<Integer> getCurrentProgress() {
        //if( currentProgress == null ) currentProgress = new MutableLiveData<>();
        return currentProgress;
    }

    //计时器单位：ms
    public void startTiming(int pos) {
        if (timer == null)
            timer = new Timer();

        currentProgress.setValue(pos);

        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                //这里不能用setValue！
                currentProgress.postValue(getCurrentProgress().getValue() + 1000);
            }
        };
        timer.schedule(timerTask, 1000, 1000);
    }

    //暂停后重新开始计时，也就是不将进度值清零
    public void restartTiming() {
        if (timer == null)
            timer = new Timer();

        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                //这里不能用setValue！
                currentProgress.postValue(getCurrentProgress().getValue() + 1000);
            }
        };
        timer.schedule(timerTask, 1000, 1000);
    }

    public void stopTiming() {
        //需要实现在点击暂停播放按钮后，进度条更新的计时任务停止，但是进度条的进度值不变。
        //所以一种简单暴力的方法是直接将计时器取消并置空
        timer.cancel();
        timer = null;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        timer.cancel();
    }
}
