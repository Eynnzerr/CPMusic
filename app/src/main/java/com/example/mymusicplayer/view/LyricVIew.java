package com.example.mymusicplayer.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.mymusicplayer.entity.TimeLineLyric;

import java.util.ArrayList;
import java.util.List;

//动态图形绘制解决歌词追踪问题
public class LyricVIew extends androidx.appcompat.widget.AppCompatTextView {

    private float width;        //歌词视图宽度
    private float height;       //歌词视图高度
    private Paint currentPaint; //当前画笔对象，负责绘制当前所在歌词
    private Paint notCurrentPaint;  //非当前画笔对象，负责绘制其它行
    private float textHeight = 45;  //文本高度
    private float textSize = 30;        //文本大小
    private int index = 0;      //list集合下标


    private List<TimeLineLyric> lineLyrics = new ArrayList<>();

    public void setmLrcList(List<TimeLineLyric> lineLyrics) {
        this.lineLyrics = lineLyrics;
    }

    public LyricVIew(@NonNull Context context) {
        super(context);
        init();
    }

    public LyricVIew(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LyricVIew(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setFocusable(true);     //设置可对焦

        //高亮部分
        currentPaint = new Paint();
        currentPaint.setAntiAlias(true);    //设置抗锯齿，让文字美观饱满
        currentPaint.setTextAlign(Paint.Align.CENTER);//设置文本对齐方式

        //非高亮部分
        notCurrentPaint = new Paint();
        notCurrentPaint.setAntiAlias(true);
        notCurrentPaint.setTextAlign(Paint.Align.CENTER);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(canvas == null || index == -1) {
            return;
        }

        if(lineLyrics == null)
        {
            setText("本歌曲暂无歌词哦");
            return;
        }

        currentPaint.setColor(Color.BLUE);
        notCurrentPaint.setColor(Color.BLACK);

        currentPaint.setTextSize(40);
        currentPaint.setTypeface(Typeface.SERIF);

        notCurrentPaint.setTextSize(textSize);
        notCurrentPaint.setTypeface(Typeface.DEFAULT);

        try {
            setText("");
            canvas.drawText(lineLyrics.get(index).getLyric(), width / 2, height / 2, currentPaint);

            //Log.d("onDraw","当前位置为"+index);

            float tempY = height / 2;
            //画出本句之前的句子
            for(int i = index - 1; i >= 0; i--) {
                //向上推移
                tempY = tempY - textHeight;
                canvas.drawText(lineLyrics.get(i).getLyric(), width / 2, tempY, notCurrentPaint);
            }
            tempY = height / 2;
            //画出本句之后的句子
            for(int i = index + 1; i < lineLyrics.size(); i++) {
                //往下推移
                tempY = tempY + textHeight;
                canvas.drawText(lineLyrics.get(i).getLyric(), width / 2, tempY, notCurrentPaint);
            }
        } catch (Exception e) {
            //e.printStackTrace();越界异常
            setText("本音乐暂无歌词");
        }
    }

    /**
     * 当view大小改变的时候调用
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.width = w;
        this.height = h;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
