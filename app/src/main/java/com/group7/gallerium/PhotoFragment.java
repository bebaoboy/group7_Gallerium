package com.group7.gallerium;

import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PhotoFragment#} factory method to
 * create an instance of this fragment.
 */
public class PhotoFragment extends Fragment {
    private View view;
    private Toolbar toolbar;
    public PhotoFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_photo, container, false);
        toolbar = view.findViewById(R.id.toolbar_photo);

        toolbar.inflateMenu(R.menu.main_bottom_nav);
        toolbar.setTitle(R.string.app_name);
        toolbar.setTitleTextAppearance(this.getContext(), R.style.ToolbarTitle);

        return view;
    }
}