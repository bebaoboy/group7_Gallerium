package com.group7.gallerium.activities;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.TextView;
import android.widget.Toast;

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
import com.group7.gallerium.models.Media;
import com.group7.gallerium.utilities.AccessMediaFile;
import com.group7.gallerium.utilities.MapUtils;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.io.FileDescriptor;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

public class MediaDetails extends AppCompatActivity {

    private int viewType;
    private String mediaPath;
    private TextView txtMediaTakenTime, txtMediaPath;
    private TextView txtMediaName, txtMediaSize;
    private TextView txtMediaResolution, txtExifData, txtLocation;

    private Toolbar toolbar;
    private MapView map;
    private GeoPoint location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_details);

        mediaPath = getIntent().getStringExtra("media_path");
        viewType = getIntent().getIntExtra("view-type", 2);
        toolbar = findViewById(R.id.toolbar_media_details);
        toolbar.setTitle("Thông tin chi tiết");
        toolbar.setTitleTextAppearance(this, R.style.ToolbarTitle);
        toolbar.setNavigationIcon(R.drawable.ic_back_arrow);
        toolbar.setNavigationOnClickListener((view) -> finish());
        applyData();

        //load/initialize the osmdroid configuration, this can be done
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        //setting this before the layout is inflated is a good idea
        //it 'should' ensure that the map has a writable location for the map cache, even without permissions
        //if no tiles are displayed, you can try overriding the cache path using Configuration.getInstance().setCachePath
        //see also StorageUtils
        //note, the load method also sets the HTTP User Agent to your application's package name, abusing osm's
        //tile servers will get you banned based on this string

        //inflate and create the map
        map = (MapView) findViewById(R.id.mapview);
        if (location == null) {
            map.setVisibility(View.GONE);
        }
        else {
            MapUtils.openMap(location, map);
            MapUtils.addMarker(location, map, mediaPath, this, 250);
        }
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
        super.onPause();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        map.onPause();  //needed for compass, my location overlays, v6.0.0 and up
    }

    public void setViewType(int viewType) {
        this.viewType = viewType;
    }

    String getMimeType(String path) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(path);
        if (extension.isBlank()) {
            extension = path.substring(path.lastIndexOf(".") + 1);
        }
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        Log.d("mime-type", type
        );
        return type;
    }

    int getType(String mimeType) {
        int mediaType = -1;
        if (mimeType.startsWith("image")) {
            mediaType = 1;
        } else mediaType = 3;
        return mediaType;
    }

    private Media createMediaFromFile(String path) {
        String[] dirs = path.split("/");
        Media media = new Media();
        String mimeType = getMimeType(path);
        int mediaType = getType(mimeType);
        media.setMimeType(mimeType);
        media.setType(mediaType);
        media.setPath(path);
        media.setTitle(dirs[dirs.length - 1]);
        media.setThumbnail(path);

        return media;
    }

    private void applyData() {
        Uri mediaUri = Uri.parse("file://" + mediaPath);
//        File file = new File(mediaPath);
//        float fileSizeInBytes = file.length();
//        float fileSizeInKb = Math.round(fileSizeInBytes / 1024);
//        float fileSizeInMb = Math.round(fileSizeInKb/1024);
        if (mediaUri != null) {
            txtMediaTakenTime = findViewById(R.id.media_taken_time);
            txtMediaPath = findViewById(R.id.media_path);
            txtMediaResolution = findViewById(R.id.media_resolution);
            txtMediaSize = findViewById(R.id.media_size);
            txtMediaName = findViewById(R.id.media_name);
            txtExifData = findViewById(R.id.exif_value);
            txtLocation = findViewById(R.id.location_value);

            Media media;
            if (viewType == 2) {
                media = AccessMediaFile.getMediaWithPath(mediaPath);
            } else {
                media = createMediaFromFile(mediaPath);
            }
            String[] subDir = media.getPath().split("/");
            String name = subDir[subDir.length - 1];
            if (media.getType() == 1) {
                try (ParcelFileDescriptor parcelFileDescriptor = getContentResolver().openFileDescriptor(mediaUri, "r")) {
                    FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();

                    ExifInterface exifInterface = new ExifInterface(fileDescriptor);

                    txtMediaName.setText(name);
                    txtMediaResolution.setText(
                            String.format(getResources().getString(
                                            R.string.resolution_placeholder),
                                    media.getWidth(),
                                    media.getHeight(),
                                    (media.getWidth() * media.getHeight()) / 1024000f
                            ));
//                    if(fileSizeInMb < 1)
//                        txtMediaSize.setText("" + fileSizeInKb + "KB");
//                    else
//                        txtMediaSize.setText("" + fileSizeInMb + "MB");
                    txtMediaSize.setText(media.getSize());
                    txtMediaPath.setText(mediaPath);
                    txtMediaTakenTime.setText(media.getDateTimeTaken());
                    double[] latLong = exifInterface.getLatLong();
                    if (latLong != null) {
                        System.out.println("Latitude: " + latLong[0]);
                        System.out.println("Longitude: " + latLong[1]);
                        txtLocation.setText("Lat: " + latLong[0] + "\n" + "Long: " + latLong[1]);
                        location = new GeoPoint(latLong[0], latLong[1]);
                    }

                    String flashValue = Objects.equals(exifInterface.getAttribute(ExifInterface.TAG_FLASH), "0") ? "Không có flash" : "Có flash";

                    if (exifInterface.getAttribute(ExifInterface.TAG_MAKE) != null) {
                        String focal = exifInterface.getAttribute(ExifInterface.TAG_FOCAL_LENGTH);
                        assert focal != null;
                        String[] values = focal.split("/");
                        float value = Float.parseFloat(values[0]) / Float.parseFloat(values[1]);
                        String apVal = exifInterface.getAttribute(ExifInterface.TAG_APERTURE_VALUE);
                        float exp = Float.parseFloat(Objects.requireNonNull(exifInterface.getAttribute(ExifInterface.TAG_EXPOSURE_TIME)));
                        String iso = exifInterface.getAttribute(ExifInterface.TAG_ISO_SPEED);
                        String exi = String.format(getResources().getString(R.string.exif_place_holder),
                                exifInterface.getAttribute(ExifInterface.TAG_MAKE),
                                exifInterface.getAttribute(ExifInterface.TAG_MODEL),
                                value,
                                apVal,
                                "1/" + Math.ceil(1 / exp),
                                flashValue,
                                iso != null ? iso : "Không có ISO");
                        txtExifData.setText(exi);

                    } else {
                        txtExifData.setText("");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(),
                            "Something wrong:\n" + e,
                            Toast.LENGTH_LONG).show();
                }
            }
            else {
                txtMediaName.setText(name);
                txtMediaResolution.setText(
                        String.format(getResources().getString(
                                        R.string.video_resolution_placeholder),
                                media.getHeight(),
                                media.getWidth(),
                                "Bitrate: " + Math.ceil(media.getBitrate() / 1024000f) + "Mb/s",
                                "Độ dài: " + media.getDuration()
                        ));
                txtMediaSize.setText(media.getSize());
                txtMediaPath.setText(mediaPath);
                txtMediaTakenTime.setText(media.getDateTimeTaken());
                txtExifData.setText("");
            }
            var sharedPref = android.preference.PreferenceManager.getDefaultSharedPreferences(this);
            var locEnabled =  sharedPref.getString(SettingsActivity.KEY_PREF_LOCATION, "0");
            if (locEnabled.equals("1")) {
                location = null;
                txtLocation.setText("");
                return;
            }
            if (media.getLocation() != null) {
                txtLocation.setText("Lat: " + media.getLocation().getLatitude() + "\n" + "Long: " + media.getLocation().getLongitude());
                this.location = media.getLocation();
            } else {
            try {
                Metadata m = ImageMetadataReader.readMetadata(getContentResolver().openInputStream(mediaUri));
                if (media.getMimeType().endsWith("jpg") || media.getMimeType().endsWith("jpeg"))
                {
                    var location = m.getFirstDirectoryOfType(GpsDirectory.class);
                    if (location != null) {
                        var locationData = location.getGeoLocation();
                        if (locationData != null) {
                            txtLocation.setText("Lat: " + locationData.getLatitude() + "\n" + "Long: " + locationData.getLongitude());
                            this.location = new GeoPoint(locationData.getLatitude(), locationData.getLongitude());
                        }
                    }
                }
                if (media.getMimeType().endsWith("mp4")) {
                    var location = m.getFirstDirectoryOfType(Mp4Directory.class);
                    if (location != null) {
                        txtLocation.setText("Lat: " + location.getFloat(Mp4MediaDirectory.TAG_LATITUDE) + "\n" + "Long: " + location.getFloat(Mp4MediaDirectory.TAG_LONGITUDE));
                        this.location = new GeoPoint(location.getFloat(Mp4MediaDirectory.TAG_LATITUDE), location.getFloat(Mp4MediaDirectory.TAG_LONGITUDE));
                    }
                }

            } catch (Exception e) {
                txtLocation.setText("");
                Log.d("tag", Arrays.toString(e.getStackTrace()) + e.getMessage());
            }
            }
        }
    }

}

