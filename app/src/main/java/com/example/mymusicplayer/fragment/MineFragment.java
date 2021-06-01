package com.example.mymusicplayer.fragment;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.mymusicplayer.R;
import com.example.mymusicplayer.databinding.MineFragmentBinding;
import com.example.mymusicplayer.databindinglistener.MineFragHandleListener;
import com.example.mymusicplayer.entity.SongList;
import com.example.mymusicplayer.utils.SongListUtils;

import java.util.List;

public class MineFragment extends Fragment {

    private SongListUtils songListUtils;

    private View view;
    private MineFragmentBinding binding;

    private MineViewModel mViewModel;
    private LiveData<List<SongList>> allSongListLive;

    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    //private SongListAdapter adapter;
    private SongListUtils.SongListsAdapter adapter;

    public static MineFragment newInstance() {
        return new MineFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.mine_fragment,container,false);
        view = binding.getRoot();

        //显示弹窗
        songListUtils = new SongListUtils(getActivity());
        songListUtils.showPopwindow();

        recyclerView = view.findViewById(R.id.songlist_root);
        layoutManager = new LinearLayoutManager(this.getActivity());//那为什么这句代码可以正常获得activity?
        recyclerView.setLayoutManager(layoutManager);
        //adapter = new SongListAdapter();
        adapter = songListUtils.getAdapter();
        recyclerView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(MineViewModel.class);
        allSongListLive = mViewModel.getAllSongListLive();
        allSongListLive.observe(getViewLifecycleOwner(), new Observer<List<SongList>>() {
            @Override
            public void onChanged(List<SongList> songLists) {
                adapter.setSongLists(songLists);
                adapter.notifyDataSetChanged();//增删歌单时自动更新
            }
        });
        //databinding
        binding.setMineFragHandleListener(new MineFragHandleListener(getActivity(),mViewModel));
        songListUtils.setViewModel(mViewModel);

    }

}