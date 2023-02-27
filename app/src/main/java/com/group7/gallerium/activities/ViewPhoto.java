package com.group7.gallerium.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.group7.gallerium.R;

public class ViewPhoto extends AppCompatActivity {

    BottomNavigationView bottom_nav;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_photo);
        toolbar = findViewById(R.id.toolbar_view_photo); // toolbar is null ????
        bottom_nav = findViewById(R.id.view_photo_bottom_navigation);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull String name, @NonNull Context context, @NonNull AttributeSet attrs) {
        // toolbar.inflateMenu(R.menu.menu_view_photo);
//        toolbar.setTitle("View Photo");
//        toolbar.setSubtitle("Ngày tháng năm");
//        toolbar.setTitleTextAppearance(context, R.style.ToolbarTitle);
        return super.onCreateView(name, context, attrs);
    }
}