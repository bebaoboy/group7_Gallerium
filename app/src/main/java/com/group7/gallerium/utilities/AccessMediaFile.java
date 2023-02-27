package com.group7.gallerium.utilities;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import androidx.loader.content.CursorLoader;

import com.group7.gallerium.models.Media;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class AccessMediaFile {

    public static List<Media> allMedia;

    private static boolean allMediaPresent = false;
    private static boolean addNewestMediaOnly = false;
    // is used when an user deletes or takes a new media file from within
    // the app. Since the app is the only one open, we just have to check which media file has been deleted
    // or which media file has been created

    public static List<Media> getAllMedia() {
        return allMedia;
    }
    public static void refreshAllMedia(){
        allMediaPresent = false;
    }
    public static void updateNewMedia(){
        addNewestMediaOnly = true;
    }
    public static void removeMediaFromAllMedia(String path) {  // remove deleted photo from "database"
        for(int i=0;i<allMedia.size();i++) {
            if(allMedia.get(i).getPath().equals(path)) {
                allMedia.remove(i);
                break;
            }
        }
    }

    public static final List<Media> getAllMediaFromGallery1(Context context) {
        if(!allMediaPresent) {
            Uri uri;
            int columnIndexData, thumb, dateIndex, typeIndex;
            List<Media> listMedia = new ArrayList<>();
            Cursor cursor;
            String type= null;

            String absolutePath = null;
            String thumbnail = null;
            Long dateTaken = null;
            uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

            String[] projection = {
                    MediaStore.MediaColumns.DATA,
                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                    MediaStore.Images.Media.DATE_TAKEN
            };

            final String orderBy = MediaStore.Images.Media.DATE_TAKEN;

            cursor = context.getApplicationContext().getContentResolver().query(uri, projection, null, null, orderBy + " DESC");
            columnIndexData = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
            thumb = cursor.getColumnIndexOrThrow(MediaStore.Images.Thumbnails.DATA);
            dateIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN);
            int mimeindex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE);
            int titleIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.TITLE);
            Calendar myCal = Calendar.getInstance();
            SimpleDateFormat formatter = new SimpleDateFormat("EEE, dd-MM-yyyy");
            while (cursor.moveToNext()) {
                Log.d("gallerium", "reading");
                try {

                    absolutePath = cursor.getString(columnIndexData);
                    File file = new File(absolutePath);
                    if (!file.canRead()) {
                        continue;
                    }
                } catch (Exception e) {
                    continue;
                }
                thumbnail = cursor.getString(thumb);
                dateTaken = cursor.getLong(dateIndex);

                myCal.setTimeInMillis(dateTaken);
                String dateText = formatter.format(myCal.getTime());
                Media media = new Media();
                media.setPath(absolutePath);
              
                media.setThumbnail(thumbnail);
                media.setDateTaken(dateText);
                if (media.getPath() == "") {
                    continue;
                }
                if(addNewestMediaOnly){
                    boolean iscontained = false; // in the "database"
                    for(Media i : allMedia){
                        if(i.getPath().equals(media.getPath())){
                            iscontained = true;
                            break;
                        }
                    }
                    if(iscontained){
                        Log.d("Simple-Gallery","GetAllPhotosFromGallery -> Image already in allImages. Breaking");
                        addNewestMediaOnly = false;
                        allMediaPresent = true;
                        cursor.close(); // Android Studio suggestion
                        return allMedia;
                    } else{
                        Log.d("Simple-Gallery", allMedia.size() + "");
                        if(allMedia.size()>1200){
                            addNewestMediaOnly = false;
                            allMediaPresent = true;
                            cursor.close(); // Android Studio suggestion
                            return allMedia;
                        }
                        allMedia.add(0, media);
                    }
                } else {
                    listMedia.add(media);
                }

                if(listMedia.size()>1000) { // Just for testing.
                    break;                  // I don't want to load 10 000 photos at once.
                }
            }
            cursor.close(); // Android Studio suggestion
            allMedia = listMedia;
            addNewestMediaOnly = false;
            allMediaPresent = true;
            return listMedia;
        }
        else{
            return allMedia;
        }
    }

    public static final List<Media> getAllMediaFromGallery(Context context) {

        int count;
        String absolutePath;
        Long dateTaken;
        List<Media> listMedia = new ArrayList<>();
        if (!allMediaPresent) {
            String[] columns = {MediaStore.Files.FileColumns._ID,
                    MediaStore.Files.FileColumns.DATA,
                    MediaStore.Files.FileColumns.DATE_ADDED,
                    MediaStore.Files.FileColumns.MEDIA_TYPE,
                    MediaStore.Files.FileColumns.MIME_TYPE,
                    MediaStore.Files.FileColumns.TITLE,
            };
            String selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                    + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
                    + " OR "
                    + MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                    + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;
            final String orderBy = MediaStore.Files.FileColumns.DATE_ADDED;
            Uri queryUri = MediaStore.Files.getContentUri("external");
            Cursor cursor = context.getApplicationContext().getContentResolver().query(queryUri,
                    columns,
                    selection,
                    null, // Selection args (none).
                    orderBy + " DESC" // Sort order.
            );
            count = cursor.getCount();
            Log.d("Amount-pic", String.valueOf(count));
            int column_index = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA);

            Calendar myCal = Calendar.getInstance();
            SimpleDateFormat formatter = new SimpleDateFormat("EEE, dd-MM-yyyy");
            while (cursor.moveToNext()) {
                Log.d("gallerium", "reading");
                absolutePath = cursor.getString(column_index);
                int type = cursor.getColumnIndex(MediaStore.Files.FileColumns.MEDIA_TYPE);
                int t = cursor.getInt(type);
                int dateIndex = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATE_ADDED);

                dateTaken = cursor.getLong(dateIndex);

                String dateText = formatter.format(dateTaken*1000);
                Log.d("Date in long", String.valueOf(dateTaken));
                Log.d("Date string", dateText);
                Media media = new Media();
                media.setPath(absolutePath);
                media.setThumbnail(absolutePath);
                media.setDateTaken(dateText);
                media.setType(t);
                if (media.getPath().equals("")) {
                    continue;
                }
                if (addNewestMediaOnly) {
                    boolean iscontained = false; // in the "all media database"
                    for (Media i : allMedia) {
                        if (i.getPath().equals(media.getPath())) {
                            iscontained = true;
                            break;
                        }
                    }
                    if (iscontained) {
                        addNewestMediaOnly = false;
                        allMediaPresent = true;
                        cursor.close();
                        return allMedia;
                    } else {
                        if (allMedia.size() > 1000) {
                            addNewestMediaOnly = false;
                            allMediaPresent = true;
                            cursor.close();
                            return allMedia;
                        }
                        allMedia.add(0, media);
                    }
                } else {
                    listMedia.add(media);
                }
                if (listMedia.size() >= 100) {
                    break;
                }

            }
            cursor.close(); // Android Studio suggestion
            allMedia = listMedia;
            addNewestMediaOnly = false;
            allMediaPresent = true;
            return listMedia;
        } else {
            return allMedia;
        }
    }
}
