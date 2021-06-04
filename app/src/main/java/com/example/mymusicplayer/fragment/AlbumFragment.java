package com.example.mymusicplayer.fragment;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.mymusicplayer.R;
import com.example.mymusicplayer.service.PlayerService;
import com.example.mymusicplayer.utils.NetworkUtils;
import com.example.mymusicplayer.view.RotateAlbumView;

import static android.content.Context.BIND_AUTO_CREATE;

public class AlbumFragment extends Fragment {

    private RotateAlbumView rotateAlbumView;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            PlayerService.PlayerBinder playerBinder = (PlayerService.PlayerBinder) service;

            MutableLiveData<Boolean> isPause = (MutableLiveData<Boolean>) playerBinder.getIsPause();
            isPause.observe(getViewLifecycleOwner(), new Observer<Boolean>() {
                @Override
                public void onChanged(Boolean aBoolean) {
                    rotateAlbumView.setPlaying(!aBoolean);
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
        View view = inflater.inflate(R.layout.fragment_album, container, false);
        rotateAlbumView = view.findViewById(R.id.rotate_album);

        Intent bindIntent = new Intent(getActivity(), PlayerService.class);
        getActivity().bindService(bindIntent,connection, BIND_AUTO_CREATE);

        return view;
    }
}