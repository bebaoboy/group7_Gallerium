package com.group7.gallerium.activities;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.exifinterface.media.ExifInterface;
import androidx.preference.PreferenceManager;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.GpsDirectory;
import com.drew.metadata.mp4.Mp4Directory;
import com.drew.metadata.mp4.media.Mp4MediaDirectory;
import com.group7.gallerium.R;
import com.group7.gallerium.utilities.AccessMediaFile;
import com.group7.gallerium.utilities.MapUtils;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.io.FileDescriptor;
import java.util.Arrays;

public class PhotoMapActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private MapView map;
    boolean isRunning = true;
    private AsyncTask<Void, Integer, Void> a;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_map);
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        toolbar = findViewById(R.id.photomap_tb);

        toolbar.setTitle("Photo Map");
        toolbar.setTitleTextAppearance(this, R.style.ToolbarTitle);
        toolbar.setNavigationIcon(R.drawable.ic_back_arrow);
        toolbar.setNavigationOnClickListener((view) -> finish());

        map = findViewById(R.id.mapview);

        var sharedPref = android.preference.PreferenceManager.getDefaultSharedPreferences(this);
        var locEnabled =  sharedPref.getString(SettingsActivity.KEY_PREF_LOCATION, "0");
        MapUtils.openMap(null, map);
        IMapController mapController = map.getController();
        mapController.setCenter(new GeoPoint(10.762730555555f, 106.6823694444f));
        map.setMultiTouchControls(true);
        mapController.setZoom(20.0);
        a = new AsyncTask<Void, Integer, Void>() {
            Context context = PhotoMapActivity.this;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected Void doInBackground(Void... voids) {
                if (locEnabled.equals("1")) return null;

                for (var media : AccessMediaFile.getAllMedia(context)) {
                    if (!isRunning) break;
                    try {
                        if (media.getLocation() == null) {
                            Uri mediaUri = Uri.parse("file://" + media.getPath());
                            if (media.getMimeType().endsWith("jpg") || media.getMimeType().endsWith("jpeg")) {
                                try (ParcelFileDescriptor parcelFileDescriptor = getContentResolver().openFileDescriptor(mediaUri, "r")) {
                                    FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();

                                    ExifInterface exifInterface = new ExifInterface(fileDescriptor);
                                    double[] latLong = exifInterface.getLatLong();
                                    if (latLong != null) {
                                        System.out.println("Latitude: " + latLong[0]);
                                        System.out.println("Longitude: " + latLong[1]);
                                        media.setLocation(latLong[0], latLong[1]);
                                        MapUtils.addMarker(media.getLocation(), map, media.getPath(), context, 280);
                                        continue;
                                    }
                                }
                            }
                            var u = context.getContentResolver().openInputStream(mediaUri);
                            Metadata m = ImageMetadataReader.readMetadata(u);
                            if (media.getMimeType().endsWith("jpg") || media.getMimeType().endsWith("jpeg")) {
                                var location = m.getFirstDirectoryOfType(GpsDirectory.class);
                                if (location != null) {
                                    var locationData = location.getGeoLocation();
                                    if (locationData != null) {
                                        media.setLocation(locationData.getLatitude(), locationData.getLongitude());
                                    }
                                }
                            }
                            if (media.getMimeType().endsWith("mp4")) {
                                var location = m.getFirstDirectoryOfType(Mp4Directory.class);
                                if (location != null && location.containsTag(Mp4MediaDirectory.TAG_LATITUDE)) {
                                    media.setLocation(location.getFloat(Mp4MediaDirectory.TAG_LATITUDE), location.getFloat(Mp4MediaDirectory.TAG_LONGITUDE));
                                }
                            }

                            u.close();
                        }
                        if (media.getLocation() != null) {
                            MapUtils.addMarker(media.getLocation(), map, media.getPath(), context, 280);
                        }
                    } catch (Exception e) {
                        Log.d("load-map", Arrays.toString(e.getStackTrace()) + e.getMessage());
                    }
                }

                return null;
            }
        };
        a.execute();
    }

    @Override
    public void onResume() {
        super.onResume();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        map.onResume(); //needed for compass, my location overlays, v6.0.0 and up
    }

    @Override
    public void onPause() {
        a.cancel(true);
        isRunning = false;
        super.onPause();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        map.onPause();  //needed for compass, my location overlays, v6.0.0 and up
    }
}