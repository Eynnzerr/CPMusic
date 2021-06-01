package com.example.mymusicplayer.fragment;

import androidx.appcompat.widget.SearchView;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.mymusicplayer.R;
import com.example.mymusicplayer.databinding.HomeFragmentBinding;
import com.example.mymusicplayer.databindinglistener.HomeFragHandleListener;
import com.example.mymusicplayer.entity.GsonInstance;
import com.example.mymusicplayer.entity.JsonSearchResult;
import com.example.mymusicplayer.entity.Song;
import com.example.mymusicplayer.entity.SongList;
import com.example.mymusicplayer.utils.NetworkUtils;
import com.example.mymusicplayer.utils.PlayerServiceUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HomeFragment extends Fragment {

    private PlayerServiceUtils playerServiceUtils;

    private View view;
    private HomeViewModel mViewModel;
    private HomeFragmentBinding binding;
    private ExecutorService executorService = Executors.newFixedThreadPool(1);
    private OkHttpClient client = new OkHttpClient();
    private List<JsonSearchResult> searchResultList = new ArrayList<>();

    private List<Song> searchSongList = new ArrayList<>();
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private PlayerServiceUtils.MusicAdapter adapter;



    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.home_fragment,container,false);
        view = binding.getRoot();




        binding.searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //这里就要启动搜索了
                Request request = new Request.Builder().url(NetworkUtils.SEARCH_SONGS + query).build();
                executorService.submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Response response = client.newCall(request).execute();
                            String responseData = response.body().string();
                            Gson gson = GsonInstance.getInstance().getGson();
                            searchResultList = gson.fromJson(responseData,new TypeToken<List<JsonSearchResult>>(){}.getType());
                            searchSongList = NetworkUtils.parseJOBToSong(searchResultList);//进一步解析
                            playerServiceUtils.setSongList(searchSongList);//手动为服务设置歌单
                            Log.d("HomeFrag","设置成功");
                            adapter.setLocalSongs(searchSongList);
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    adapter.notifyDataSetChanged();//加载获得的结果
                                }
                            });
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        /*
        binding.searchBar.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                Log.d("HomeFrag","OnClear");
                searchSongList.clear();
                return true;
            }
        });*/

        //使用自己封装的帮助类
        playerServiceUtils = new PlayerServiceUtils(getActivity());
        playerServiceUtils.showPopwindow();//加载底部菜单
        playerServiceUtils.setSongList(new SongList());//避免空指针
        playerServiceUtils.bindPlayerService();//绑定服务

        recyclerView = view.findViewById(R.id.search_result);
        layoutManager = new LinearLayoutManager(getActivity());
        adapter = playerServiceUtils.getAdapter();//获取封装的适配器
        //不能在这里直接传！因为服务的连接是异构的，绑定后直接使用不能保证已经绑定成功，可能会引发空指针
        recyclerView.setLayoutManager(layoutManager);
        adapter.setLocalSongs(searchSongList);
        recyclerView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        //databinding
        binding.setHomeFragHandleListener(new HomeFragHandleListener());
    }

}