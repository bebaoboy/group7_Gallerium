package com.group7.gallerium.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import com.group7.gallerium.R;
import com.group7.gallerium.models.Media;
import com.group7.gallerium.utilities.AccessMediaFile;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;

public class MediaDetails extends AppCompatActivity {

    private String mediaPath;
    private TextView txtMediaTakenTime, txtMediaPath;
    private TextView txtMediaName, txtMediaSize;
    private TextView txtMediaResolution;

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_details);

        mediaPath = getIntent().getStringExtra("media_path");
        toolbar = findViewById(R.id.toolbar_media_details);
        toolbar.setTitle("Thông tin chi tiết");
        toolbar.setTitleTextAppearance(this, R.style.ToolbarTitle);
        toolbar.setNavigationIcon(R.drawable.ic_back_arrow);
        toolbar.setNavigationOnClickListener((view) -> finish());
        applyData();
    }

    private void applyData() {
        Uri mediaUri = Uri.parse("file://" +  mediaPath);
        File file = new File(mediaPath);
        float fileSizeInBytes = file.length();
        float fileSizeInKb = fileSizeInBytes/ 1024;
        float fileSizeInMb = fileSizeInKb / 1024;
        if(mediaUri != null) {
            txtMediaTakenTime = (TextView) findViewById(R.id.media_taken_time);
            txtMediaPath = (TextView) findViewById(R.id.media_path);
            txtMediaResolution = (TextView) findViewById(R.id.media_resolution);
            txtMediaSize = (TextView) findViewById(R.id.media_size);
            txtMediaName = (TextView) findViewById(R.id.media_name);

            Media media = AccessMediaFile.getMediaWithPath(mediaPath);
            String[] subDir = media.getPath().split("/");
            String name = subDir[subDir.length - 1];
            if(media.getType() == 1) {
                ParcelFileDescriptor parcelFileDescriptor = null;
                try {
                    parcelFileDescriptor = getContentResolver().openFileDescriptor(mediaUri, "r");
                    FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();

                    ExifInterface exifInterface = new ExifInterface(fileDescriptor);

                    txtMediaName.setText(name);
                    txtMediaResolution.setText(
                            String.format(getResources().getString(
                                    R.string.resolution_placeholder),
                                    Integer.parseInt(exifInterface.getAttribute(ExifInterface.TAG_IMAGE_LENGTH)),
                                    Integer.parseInt(exifInterface.getAttribute(ExifInterface.TAG_IMAGE_WIDTH)),
                                    1
                            ));
                    txtMediaSize.setText("" + fileSizeInMb);
                    txtMediaPath.setText(mediaPath);
                    txtMediaTakenTime.setText(media.getDateTaken());

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(),
                            "Something wrong:\n" + e.toString(),
                            Toast.LENGTH_LONG).show();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(),
                            "Something wrong:\n" + e.toString(),
                            Toast.LENGTH_LONG).show();
                }
            }
        }
    }


}