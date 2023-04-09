package com.group7.gallerium.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.group7.gallerium.R;
import com.group7.gallerium.utilities.AccessMediaFile;
import com.group7.gallerium.utilities.FileUtils;

import java.io.File;

import life.knowledge4.videotrimmer.K4LVideoTrimmer;
import life.knowledge4.videotrimmer.interfaces.OnTrimVideoListener;

public class VideoTrimActivity extends AppCompatActivity {

    String mediaPath;
    FileUtils fileUtils;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_trim);

        mediaPath = getIntent().getStringExtra("path");
        fileUtils = new FileUtils();
        Uri uri = fileUtils.getUri(mediaPath, AccessMediaFile.getMediaWithPath(mediaPath).getType(), this);

        K4LVideoTrimmer videoTrimmer = findViewById(R.id.timeLine);
        if (videoTrimmer != null) {
            videoTrimmer.setVideoURI(Uri.parse(mediaPath));
        }

        String dir = mediaPath.substring(0, mediaPath.lastIndexOf("/"));
        videoTrimmer.setDestinationPath(dir + "/");
        videoTrimmer.setMaxDuration(200);

        videoTrimmer.setOnTrimVideoListener(new OnTrimVideoListener() {
            @Override
            public void getResult(Uri uri) {
                fileUtils.insertMediaToMediaStore(VideoTrimActivity.this, uri.toString(), "/storage/emulated/0/DCIM/Movies/GTrimmed/");
                VideoTrimActivity.this.finish();
            }

            @Override
            public void cancelAction() {
                VideoTrimActivity.this.finish();
            }
        });
    }
}