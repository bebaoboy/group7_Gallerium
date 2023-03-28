package com.group7.gallerium.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.WallpaperManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.group7.gallerium.R;
import com.group7.gallerium.models.Media;
import com.group7.gallerium.utilities.AccessMediaFile;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

public class WallpaperPreview extends AppCompatActivity {

    String mediaPath;
    ImageView previewImage;
    TextView systemTime;

    Button setWallpaperButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallpaper_preview);

        previewImage = findViewById(R.id.preview_wallpaper_image);
        systemTime = findViewById(R.id.system_time);
        setWallpaperButton = findViewById(R.id.set_wallpaper_button);

        mediaPath = getIntent().getStringExtra("media_path");
        Media media = AccessMediaFile.getMediaWithPath(mediaPath);
        Log.d("media-thumbnail", media.getThumbnail());

        Glide.with(getApplicationContext()).asGif().load("file://" + media.getThumbnail())
                .diskCacheStrategy(DiskCacheStrategy.ALL).into(previewImage);

        Date currentTime = Calendar.getInstance().getTime();
        systemTime.setText(currentTime.toString());

        setWallpaperButton.setOnClickListener((view)->{
            WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);
            try {
                Bitmap bm=((BitmapDrawable)previewImage.getDrawable()).getBitmap();
                wallpaperManager.setBitmap(bm);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}