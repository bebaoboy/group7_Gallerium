package com.group7.gallerium.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.group7.gallerium.R;

public class AlbumFragment extends Fragment {
    private View view;
    private Toolbar toolbar;
    private Context context;

    public AlbumFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_album, container, false);
        context = getContext();
        toolbarSetting();
        return view;
    }

    void toolbarSetting(){
        toolbar = view.findViewById(R.id.toolbar_album);
        toolbar.inflateMenu(R.menu.menu_album);
        toolbar.setTitle(R.string.album);
        toolbar.setTitleTextAppearance(context, R.style.ToolbarTitle);
    }
}
