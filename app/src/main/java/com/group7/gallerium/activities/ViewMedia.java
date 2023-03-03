package com.group7.gallerium.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import android.widget.VideoView;

import com.dsphotoeditor.sdk.activity.DsPhotoEditorActivity;
import com.dsphotoeditor.sdk.utils.DsPhotoEditorConstants;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.group7.gallerium.R;
import com.group7.gallerium.adapters.SlideAdapter;
import com.group7.gallerium.utilities.AccessMediaFile;
import com.group7.gallerium.utilities.MediaItemInterface;

import java.io.File;
import java.util.ArrayList;

public class ViewMedia extends AppCompatActivity implements MediaItemInterface{

    BottomNavigationView bottom_nav;
    private Toolbar toolbar;

    private VideoView videoView;
    private int mediaPos;
    private String mediaPath;
    private String mediaName;
    private Intent intent;
    private ArrayList<String> listPath;
    private MediaItemInterface mediaItemInterface;
    private ViewPager viewPager;
    private SlideAdapter slideAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_photo);
        toolbar = findViewById(R.id.toolbar_photo_view);
        bottom_nav = findViewById(R.id.view_photo_bottom_navigation);
        viewPager = findViewById(R.id.viewPager_picture);
        

        applyData();
        toolbarSetting();
        setUpSlider();
        bottomNavCustom();
    }

    void applyData() {
        intent = getIntent();
        listPath = intent.getStringArrayListExtra("data_list_path");
        mediaPos = intent.getIntExtra("pos", 0);
        mediaItemInterface = this;
    }

    private void toolbarSetting() {
        // Toolbar events
        toolbar.inflateMenu(R.menu.menu_photo);
        toolbar.setTitle("hello");
        toolbar.setTitleTextAppearance(getApplicationContext(), R.style.ToolbarTitleMediaView);

        toolbar.setNavigationIcon(R.drawable.ic_back_arrow);
        toolbar.setNavigationOnClickListener((view) -> finish());
    }

    private void showNavigation(boolean flag) {
        if (!flag) {
            bottom_nav.setVisibility(View.INVISIBLE);
            toolbar.setVisibility(View.INVISIBLE);
        } else {
            bottom_nav.setVisibility(View.VISIBLE);
            toolbar.setVisibility(View.VISIBLE);
        }
    }

    private void setUpSlider() {

        slideAdapter = new SlideAdapter(getApplicationContext());
        slideAdapter.setData(listPath);
        slideAdapter.setInterface(mediaItemInterface);
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

    public void bottomNavCustom() {
        bottom_nav.setOnItemSelectedListener((NavigationBarView.OnItemSelectedListener) item -> {
            Uri targetUri = Uri.parse("file://" + mediaPath);

            switch (item.getItemId()){
                case R.id.edit_nav_item->{
                    Intent editIntent = new Intent(ViewMedia.this, DsPhotoEditorActivity.class);

                    if(mediaPath.contains("gif")){
                        Toast.makeText(this,"Cannot edit GIF images",Toast.LENGTH_SHORT).show();
                    } else{
                        // Set data
                        editIntent.setData(Uri.fromFile(new File(mediaPath)));
                        // Set output directory
                        editIntent.putExtra(DsPhotoEditorConstants.DS_PHOTO_EDITOR_OUTPUT_DIRECTORY, "Gallerium");
                        // Set toolbar color
                        editIntent.putExtra(DsPhotoEditorConstants.DS_TOOL_BAR_BACKGROUND_COLOR, Color.parseColor("#FF000000"));
                        // Set background color
                        editIntent.putExtra(DsPhotoEditorConstants.DS_MAIN_BACKGROUND_COLOR, Color.parseColor("#FF000000"));
                        // Start activity
                        startActivity(editIntent);
                    }
                }

                case R.id.delete_nav_item ->{
                    String type = AccessMediaFile.getAllMedia().get(mediaPos).getType() == 1 ? "Image" : "Video";
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);

                    builder.setTitle("Confirm");
                    builder.setMessage("Do you want to delete this " + type + "?");
                    builder.setPositiveButton("YES", (dialog, which) -> {
                        File file = new File(targetUri.getPath());
                        if (file.exists()) {
                            if (file.delete()) {
                                AccessMediaFile.removeMediaFromAllMedia(targetUri.getPath());
                                Toast.makeText(this, "Delete successfully: " + targetUri.getPath(), Toast.LENGTH_SHORT).show();
                            } else
                                Toast.makeText(this, "Delete failed: " + targetUri.getPath(), Toast.LENGTH_SHORT).show();
                        }
                        finish();
                        dialog.dismiss();
                    });

                    builder.setNegativeButton("NO", (dialog, which) -> {
                        dialog.dismiss();
                    });

                    AlertDialog alert = builder.create();
                    alert.show();
                }

//                case R.id.view_photo_secured_nav_item->{
//                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
//
//                    builder.setTitle("Confirm");
//                    builder.setMessage("Do you want to hide/show this image?");
//
//                    builder.setPositiveButton("YES", (dialog, which) -> {
//                        String scrPath = Environment.getExternalStorageDirectory() + File.separator+".secret";
//                        File scrDir = new File(scrPath);
//                        if(!scrDir.exists()){
//                            Toast.makeText(this, "You haven't created secret album", Toast.LENGTH_SHORT).show();
//                        }
//                        else{
//                            FileUtility fu = new FileUtility();
//                            File mediaFile = new File(mediaPath);
//                            if(!(scrPath+File.separator+mediaFile.getName()).equals(mediaPath)){
//                                fu.moveFile(mediaPath, mediaFile.getName(),scrPath);
//                                Toast.makeText(this, "Your image is secured", Toast.LENGTH_SHORT).show();
//                            }
//                            else{
//                                String outputPath = Environment.getExternalStorageDirectory()+File.separator+"DCIM" + File.separator + "Restore";
//                                File folder = new File(outputPath);
//                                File file = new File(mediaFile.getPath());
//                                File desImgFile = new File(outputPath,mediaFile.getName());
//                                if(!folder.exists()) {
//                                    folder.mkdir();
//                                }
//                                mediaFile.renameTo(desImgFile);
//                                mediaFile.deleteOnExit();
//                                desImgFile.getPath();
//                                MediaScannerConnection.scanFile(getApplicationContext(), new String[]{outputPath+File.separator+desImgFile.getName()}, null, null);
//                            }
//                        }
//                        Intent intentResult = new Intent();
//                        intentResult.putExtra("path_media", mediaPath);
//                        setResult(RESULT_OK, intentResult);
//                        finish();
//                        dialog.dismiss();
//                    });
//                    builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
//
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//
//                            // Do nothing
//                            dialog.dismiss();
//                        }
//                    });
//
//                    AlertDialog alert = builder.create();
//                    alert.show();
//
//                    break;
//                }
            }
            return true;
        });
    }

    @Override
    public void showActionBar(boolean trigger) {
        showNavigation(trigger);
    }

    @Override
    public void showVideoPlayer() {

    }
}