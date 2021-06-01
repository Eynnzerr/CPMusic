package com.example.mymusicplayer.utils;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mymusicplayer.fragment.MineViewModel;
import com.example.mymusicplayer.R;
import com.example.mymusicplayer.entity.SongList;
import com.example.mymusicplayer.activity.SongListActivity;

import java.util.ArrayList;
import java.util.List;

public class SongListUtils {

    //private static SongListUtils INSTANCE;

    private Activity activity;//当前活动

    private SongList chosedList;

    private SongListsAdapter adapter = new SongListsAdapter();

    private View contentView;
    private PopupWindow popupWindow;
    private MineViewModel viewModel;

    public SongListsAdapter getAdapter() {
        return adapter;
    }

    public void setViewModel(MineViewModel viewModel) {
        this.viewModel = viewModel;

    }



    /*public static SongListUtils getSongListUtils(Activity activity)
    {
        if( INSTANCE == null )
        {
            INSTANCE = new SongListUtils(activity);
        }
        return INSTANCE;
    }*/

    public SongListUtils(Activity activity) {
        this.activity = activity;
    }

    //自带的adapter，复制自SongListAdapter
    public class SongListsAdapter extends RecyclerView.Adapter<SongListsAdapter.ViewHolder>{

        //一个装载有歌单的列表
        private List<SongList> songLists = new ArrayList<>();

        public SongListsAdapter(List<SongList> songLists) {
            this.songLists = songLists;
        }

        public SongListsAdapter() {
        }

        public void setSongLists(List<SongList> songLists) {
            this.songLists = songLists;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.songlist_item,parent,false);
            final ViewHolder holder = new ViewHolder(view);
            holder.songListView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("debug","你点击了一个歌单");
                    //TODO 这里需要点击进入歌单展示活动SongListActivity，难点在于如何获取当前所选歌单并传递给下一个activity，并且在下一个activity中实现增删改查
                    //TODO 一个很自然的想法是把当前歌单的名称传给下一个activity里，再在下一个activity查询数据库来得到当前歌单。

                    SongList songList = songLists.get(holder.getAdapterPosition());
                    Intent intent = new Intent(view.getContext(), SongListActivity.class);
                    intent.putExtra("list_name",songList.getName());
                    view.getContext().startActivity(intent);
                }
            });
            holder.songListChoice.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //弹出底部菜单
                    chosedList = songLists.get(holder.getAdapterPosition());
                    openPopWindow(v);
                }
            });

            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            SongList songList = songLists.get(position);
            holder.songListName.setText(songList.getName());
            holder.songListNum.setText(songList.getNum()+"");
            holder.songListCover.setImageResource(songList.getImageId());
        }

        @Override
        public int getItemCount() {
            return songLists.size();
        }

         class ViewHolder extends RecyclerView.ViewHolder {
            View songListView;
            TextView songListName;
            TextView songListNum;
            ImageView songListCover;
            ImageView songListChoice;

            public ViewHolder(@NonNull View itemView) {
                //itemView是RecyclerView的子项的最外层布局，进而获取该布局中存放的image和name
                super(itemView);
                songListView= itemView;
                songListName = itemView.findViewById(R.id.songlist_name);
                songListNum = itemView.findViewById(R.id.songlist_musicnums);
                songListCover = itemView.findViewById(R.id.songlist_cover);
                songListChoice = itemView.findViewById(R.id.songlist_choice);
            }
        }
    }

    public void showPopwindow()
    {
        //加载弹出框的布局
        //TODO 空指针：没有获取到activity
        contentView = LayoutInflater.from(activity).inflate(
                R.layout.singlelist_popup, null);

        popupWindow = new PopupWindow(contentView,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setFocusable(true);// 取得焦点
        //给弹框随意设置一个背景
        Bitmap bmp = BitmapFactory.decodeResource(activity.getResources(), R.drawable.music);
        Drawable drawable = new BitmapDrawable(activity.getResources(), bmp);

        popupWindow.setBackgroundDrawable(drawable);
        //点击外部消失
        popupWindow.setOutsideTouchable(true);
        //设置可以点击
        popupWindow.setTouchable(true);
        //进入退出的动画，指定刚才定义的style
        popupWindow.setAnimationStyle(R.style.popwindow_anim_style);

        //删除歌单
        Button btn_delete = contentView.findViewById(R.id.songlist_delete);
        btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        viewModel.deleteSongList(chosedList);
                    }
                }).start();
                popupWindow.dismiss();
            }
        });
    }

    public void openPopWindow(View v)
    {
        //从底部显示
        popupWindow.showAtLocation(contentView, Gravity.BOTTOM, 0, 0);

    }

}
