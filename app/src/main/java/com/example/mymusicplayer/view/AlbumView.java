package com.example.mymusicplayer.view;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.SweepGradient;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.Nullable;

import com.example.mymusicplayer.R;

public class AlbumView extends View {

    private Paint paint;
    // 圆环半径
    private int ringWidth;
    // 渐变色
    private int[] colors;
    private SweepGradient gradient;
    // 圆线距圆环内边的距离
    private int[] ringLinesMarginOut = {
            dp2px(3.78F),
            dp2px(7.03F),
            dp2px(10.27F),
            dp2px(12.97F)
    };
    // 圆线高度
    private int ringLineWidth;

    public AlbumView(Context context) {
        super(context);
        //init(context, attrs);
    }

    public AlbumView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public AlbumView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        colors = new int[]{getColor(R.color.widget_album_ring_color1), getColor(R.color.widget_album_ring_color2),
                getColor(R.color.widget_album_ring_color1), getColor(R.color.widget_album_ring_color2),
                getColor(R.color.widget_album_ring_color1)};

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.WidgetAlbumBgView);
        ringWidth = (int) typedArray.getDimension(R.styleable.WidgetAlbumBgView_ring_width, getResources().getDimension(R.dimen.widget_album_ring_width));
        ringLineWidth = (int) typedArray.getDimension(R.styleable.WidgetAlbumBgView_ring_line_width, getResources().getDimension(R.dimen.widget_album_ring_line_width));
        typedArray.recycle();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        paint.setStrokeWidth(ringWidth);
        paint.setColor(getColor(R.color.widget_album_ring_color1));
        if (gradient == null) {
            gradient = new SweepGradient(getWidth() * 0.5F, getHeight() * 0.5F, colors, new float[]{
                    0F, 0.25F, 0.5F, 0.75F, 1F
            });
        }
        paint.setShader(gradient);
        // 画圆环
        canvas.drawCircle(getWidth() * 0.5F, getHeight() * 0.5F, (getWidth() - ringWidth) * 0.5F, paint);
        paint.setShader(null);
        paint.setStrokeWidth(ringLineWidth);
        paint.setColor(getColor(R.color.widget_album_ring_line_color));
        // 画圆线
        for (int marginOut : ringLinesMarginOut) {
            canvas.drawCircle(getWidth() * 0.5F, getHeight() * 0.5F, getWidth() * 0.5F - marginOut - ringLineWidth * 0.5F, paint);
        }
    }

    @ColorInt
    private int getColor(@ColorRes int colorId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return getResources().getColor(colorId, null);
        }
        return 0;
    }

    public static int dp2px(float dpValue) {
        return (int) (0.5f + dpValue * Resources.getSystem().getDisplayMetrics().density);
    }
}
