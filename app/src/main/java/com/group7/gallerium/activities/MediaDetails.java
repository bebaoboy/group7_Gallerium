package com.group7.gallerium.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.MediaExtractor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.Toolbar;
import androidx.exifinterface.media.ExifInterface;
import androidx.preference.PreferenceManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.Transformation;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapResource;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.FitCenter;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.GpsDirectory;
import com.drew.metadata.mp4.Mp4Directory;
import com.drew.metadata.mp4.media.Mp4MediaDirectory;
import com.drew.metadata.mp4.media.Mp4MetaDirectory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.group7.gallerium.R;
import com.group7.gallerium.models.Media;
import com.group7.gallerium.utilities.AccessMediaFile;
import com.group7.gallerium.utilities.FileUtils;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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
            map.setTileSource(TileSourceFactory.MAPNIK);
            map.setMultiTouchControls(false);
            IMapController mapController = map.getController();
            mapController.setZoom(19.0);
            mapController.setCenter(location);
            Marker startMarker = new Marker(map);
            startMarker.setPosition(location);
            startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
            var media = AccessMediaFile.getMediaWithPath(mediaPath);
            startMarker.setTitle(media.getDateTimeTaken());


            Geocoder geocoder;
            List<Address> addresses;
            geocoder = new Geocoder(this, Locale.getDefault());

            try {
                addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                String city = addresses.get(0).getLocality();
                String state = addresses.get(0).getAdminArea();
                String country = addresses.get(0).getCountryName();
                String postalCode = addresses.get(0).getPostalCode();
                String knownName = addresses.get(0).getFeatureName(); // Only if available else return NULL
                if (address != null) {
                    startMarker.setTitle(address);
                } else {
                    startMarker.setTitle(String.join(", ", city, state, country));
                }

            } catch (IOException e) {

            }

            Target t = new CustomTarget() {
                @Override
                public void onResourceReady(@NonNull Object resource, @Nullable Transition transition) {
                    var bm = (BitmapDrawable) resource;
                    //image.setImageDrawable((BitmapDrawable) resource);
                    startMarker.setIcon(bm);
                }

                @Override
                public void onLoadCleared(@Nullable Drawable placeholder) {

                }
            };

            Glide.with(this).load("file://" + media.getThumbnail())
                    .dontAnimate()
                    .override(Math.min(media.getWidth(), 450), Math.min(media.getHeight(), 410))
                    .centerCrop()
                    .transform(new BitmapTransformation() {
                        @Override
                        protected Bitmap transform(@NonNull BitmapPool pool, @NonNull Bitmap source, int outWidth, int outHeight) {
                            int outerMargin = 10;
                            int margin = 10;
                            Bitmap output = Bitmap.createBitmap(outWidth, outHeight + 10, Bitmap.Config.ARGB_8888);
                            Canvas canvas = new Canvas(output);

                            Paint paintBorder = new Paint();
                            paintBorder.setColor(getResources().getColor(R.color.primary_color_dark, getTheme()));
                            paintBorder.setStrokeWidth(margin);
                            canvas.drawRoundRect(new RectF(outerMargin, outerMargin, outWidth - outerMargin, outHeight - outerMargin), 0, 0, paintBorder);

                            Paint trianglePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

                            trianglePaint.setStrokeWidth(2);
                            trianglePaint.setColor(getResources().getColor(R.color.primary_color_dark, getTheme()));
                            trianglePaint.setStyle(Paint.Style.FILL_AND_STROKE);
                            trianglePaint.setAntiAlias(true);

                            Path triangle = new Path();
                            triangle.setFillType(Path.FillType.EVEN_ODD);
                            triangle.moveTo(outerMargin, outHeight / 2);
                            triangle.lineTo(outWidth/2, outHeight +  10);
                            triangle.lineTo(outWidth-outerMargin,outHeight / 2);
                            triangle.close();

                            canvas.drawPath(triangle, trianglePaint);

                            final Paint paint = new Paint();
                            paint.setAntiAlias(true);
                            paint.setShader(new BitmapShader(source, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));
                            canvas.drawRoundRect(new RectF(margin+outerMargin, margin+outerMargin, outWidth - (margin + outerMargin), outHeight - (margin + outerMargin)), 0, 0, paint);

                            return output;
                        }

                        @Override
                        public void updateDiskCacheKey(@NonNull MessageDigest messageDigest) {

                        }
                    })
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(t);

            map.getOverlays().add(startMarker);
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

