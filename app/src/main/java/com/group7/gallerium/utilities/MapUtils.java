package com.group7.gallerium.utilities;

import android.content.Context;
import android.graphics.Bitmap;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.group7.gallerium.R;

import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MapUtils {
    public static void openMap(GeoPoint location, MapView map) {
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(false);
        IMapController mapController = map.getController();
        mapController.setZoom(19.0);
        if (location != null)
        {
            mapController.setCenter(location);
        }
    }

    public static void addMarker(GeoPoint location, MapView map, String mediaPath, Context context, int size) {
        if (size < 80) size = 80;
        Marker startMarker = new Marker(map);
        startMarker.setPosition(location);
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
        var media = AccessMediaFile.getMediaWithPath(mediaPath);
        startMarker.setTitle(media.getDateTimeTaken());

        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(context, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            if (addresses.size() > 0) {
                String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                String city = addresses.get(0).getLocality();
                String state = addresses.get(0).getAdminArea();
                String country = addresses.get(0).getCountryName();
                String postalCode = addresses.get(0).getPostalCode();
                String knownName = addresses.get(0).getFeatureName(); // Only if available else return NULL
                if (address != null) {
                    startMarker.setTitle(address + "\n" + media.getDateTimeTaken());
                } else {
                    startMarker.setTitle(String.join(", ", city, state, country) + "\n" + media.getDateTimeTaken());
                }
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

        Glide.with(context).load("file://" + media.getThumbnail())
                .dontAnimate()
                .override(Math.min(media.getWidth(), size), Math.min(media.getHeight(), size - 40))
                .centerCrop()
                .transform(new BitmapTransformation() {
                    @Override
                    protected Bitmap transform(@NonNull BitmapPool pool, @NonNull Bitmap source, int outWidth, int outHeight) {
                        int outerMargin = 10;
                        int margin = 10;
                        Bitmap output = Bitmap.createBitmap(outWidth, outHeight + 10, Bitmap.Config.ARGB_8888);
                        Canvas canvas = new Canvas(output);

                        Paint paintBorder = new Paint();
                        paintBorder.setColor(context.getResources().getColor(R.color.primary_color_dark, context.getTheme()));
                        paintBorder.setStrokeWidth(margin);
                        canvas.drawRoundRect(new RectF(outerMargin, outerMargin, outWidth - outerMargin, outHeight - outerMargin), 0, 0, paintBorder);

                        Paint trianglePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

                        trianglePaint.setStrokeWidth(2);
                        trianglePaint.setColor(context.getResources().getColor(R.color.primary_color_dark, context.getTheme()));
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
