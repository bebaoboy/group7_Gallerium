package com.group7.gallerium.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.navigation.NavigationBarView;
import com.group7.gallerium.R;
import com.group7.gallerium.adapters.SlideAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ViewPhoto extends AppCompatActivity {

    BottomNavigationView bottom_nav;
    private Toolbar toolbar;
    private int mediaPos;
    private String mediaPath;
    private String mediaName;
    private Intent intent;


    private ArrayList<String> listPath;

    private ViewPager viewPager;
    private SlideAdapter slideAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_photo);
        toolbar = findViewById(R.id.toolbar_photo_view); // toolbar is null ????
        bottom_nav = findViewById(R.id.view_photo_bottom_navigation);
        viewPager = findViewById(R.id.viewPager_picture);

        applyData();
        toolbarSetting();
        setUpSilder();
    }

    void applyData(){
        intent = getIntent();
        listPath = intent.getStringArrayListExtra("data_list_path");
        mediaPos = intent.getIntExtra("pos", 0);
    }

    private void toolbarSetting() {
        // Toolbar events
        toolbar.inflateMenu(R.menu.menu_photo);
        toolbar.setTitle("hello");
        toolbar.setTitleTextAppearance(getApplicationContext(), R.style.ToolbarTitleMediaView);

        toolbar.setNavigationIcon(R.drawable.ic_back_arrow);
        toolbar.setNavigationOnClickListener((view)-> finish());
    }

    private void setUpSilder() {

        slideAdapter = new SlideAdapter(getApplicationContext());
        slideAdapter.setData(listPath);
        // slideAdapter.setPictureInterface(activityPicture);
        viewPager.setAdapter(slideAdapter);
        viewPager.setCurrentItem(mediaPos);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                mediaPath = listPath.get(position);
                setTitleToolbar(mediaPath.substring(mediaPath.lastIndexOf('/') + 1));
            }

            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    public void setTitleToolbar(String name) {
        this.mediaName = name;
        toolbar.setTitle(name);
    }
}