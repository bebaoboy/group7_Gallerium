package com.group7.gallerium.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.group7.gallerium.R;
import com.group7.gallerium.models.Media;
import com.group7.gallerium.utilities.AccessMediaFile;
import com.group7.gallerium.utilities.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

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
                var inputPath =  uri.toString();
                var outputPath = "Movies/GTrimmed/";
                Cursor cursor;
                String name = "";
                String[] parse;
                Media media = new Media();
                media.setPath(inputPath);
                parse = inputPath.split("/");
                name = parse[parse.length - 1];

                Uri outputFile;

                cursor = getContentResolver().query(
                            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                            , new String[]{MediaStore.Video.Media._ID}
                            , MediaStore.Video.Media.DATA + "=? "
                            , new String[]{outputPath}, null);

                if (cursor != null && cursor.moveToFirst()) {
                    int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
                    cursor.close();
                    ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id);
                }
                else if (!inputPath.isEmpty()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        ContentValues values = new ContentValues();
                        var resolver = getContentResolver();

                        Uri vidCollection = MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
                        if(Objects.equals(outputPath, "Download/")) {
                            vidCollection = MediaStore.Downloads.EXTERNAL_CONTENT_URI;
                        }
                        values.put(MediaStore.Video.Media.DISPLAY_NAME, name);
                        values.put(MediaStore.Video.Media.MIME_TYPE, AccessMediaFile.getMediaWithPath(mediaPath).getMimeType());
                        values.put(MediaStore.Video.Media.RELATIVE_PATH, outputPath);

                        outputFile = resolver.insert(vidCollection, values);
                        try (InputStream inputStream = new FileInputStream(inputPath)) { //input stream

                            OutputStream out = getContentResolver().openOutputStream(outputFile); //output stream

                            byte[] buf = new byte[8096];
                            int len;
                            while ((len = inputStream.read(buf)) > 0) {
                                out.write(buf, 0, len); //write input file data to output file
                            }
                            out.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        new File(inputPath).delete();

                    } else {
                        ContentValues values = new ContentValues();
                        values.put(MediaStore.Video.Media.DATA, media.getPath());
                        getContentResolver().insert(
                                MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
                    }
                } else {
                }

                VideoTrimActivity.this.finish();
            }

            @Override
            public void cancelAction() {
                VideoTrimActivity.this.finish();
            }
        });
    }
}