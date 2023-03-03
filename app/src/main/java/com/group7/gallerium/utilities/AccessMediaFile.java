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
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class AccessMediaFile {

    public static List<Media> allMedia;
    public static HashMap<String, Boolean> paths = new HashMap<>();

    private static boolean allMediaPresent = false;
    private static boolean addNewestMediaOnly = false;
    // is used when an user deletes or takes a new media file from within
    // the app. Since the app is the only one open, we just have to check which media file has been deleted
    // or which media file has been created

    static String[] columns = {MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.DATE_ADDED,
            MediaStore.Files.FileColumns.MEDIA_TYPE,
            MediaStore.Files.FileColumns.MIME_TYPE,
            MediaStore.Files.FileColumns.TITLE,
    };
    static String selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "="
            + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
            + " OR "
            + MediaStore.Files.FileColumns.MEDIA_TYPE + "="
            + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;
    static final String orderBy = MediaStore.Files.FileColumns.DATE_ADDED;
    static Uri queryUri = MediaStore.Files.getContentUri("external");

    public static List<Media> getAllMedia() {
        return allMedia;
    }
    public static void refreshAllMedia(){
        allMediaPresent = false;
    }
    public static void updateNewMedia(){
        addNewestMediaOnly = true;
    }
    public static void removeMediaFromAllMedia(String path) {  // remove deleted media from "database"
        for(int i=0;i<allMedia.size();i++) {
            if(allMedia.get(i).getPath().equals(path)) {
                allMedia.remove(i);
                break;
            }
        }
    }

    public static List<Media> getAllMediaFromGallery(Context context) {

        if (!allMediaPresent) {
            int typeColumn, titleColumn, dateColumn, pathColumn, idColumn;
            int count, type;
            String absolutePath, id, dateText, title;
            long dateTaken;
            List<Media> listMedia = new ArrayList<>();


            Cursor cursor = context.getApplicationContext().getContentResolver().query(queryUri,
                    columns,
                    selection,
                    null, // Selection args (none).
                    orderBy + " DESC" // Sort order.
            );
            count = cursor.getCount();
            Log.d("Amount-pic", String.valueOf(count));

            idColumn = cursor.getColumnIndex(MediaStore.Files.FileColumns._ID);
            pathColumn = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA);
            typeColumn = cursor.getColumnIndex(MediaStore.Files.FileColumns.MEDIA_TYPE);
            dateColumn = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATE_ADDED);
            titleColumn = cursor.getColumnIndex(MediaStore.Files.FileColumns.TITLE);
            SimpleDateFormat formatter = new SimpleDateFormat("EEE, dd-MM-yyyy");
            while (cursor.moveToNext()) {
                id = cursor.getString(idColumn);
                absolutePath = cursor.getString(pathColumn);
                if (paths.containsKey(absolutePath)) {
                    addNewestMediaOnly = false;
                    allMediaPresent = true;
                    cursor.close();
                    return allMedia;
                }

                type = cursor.getInt(typeColumn);
                dateTaken = cursor.getLong(dateColumn);
                dateText = formatter.format(dateTaken*1000);
                title = cursor.getString(titleColumn);
                Log.d("gallerium", "reading");

                Media media = new Media();
                media.setPath(absolutePath);
                media.setThumbnail(absolutePath);
                media.setDateTaken(dateText);
                media.setType(type);
                media.setTitle(title);

                if (media.getPath().equals("")) {
                    continue;
                }
                if (addNewestMediaOnly) {
                    boolean iscontained = paths.containsKey(media.getPath()); // in the "all media database"
//                    for(Media m: allMedia){
//                        if(m.getPath().equals(media.getPath())){
//                            iscontained = true;
//                            break;
//                        }
//                    }
                    if (iscontained) {
                        addNewestMediaOnly = false;
                        allMediaPresent = true;
                        cursor.close();
                        return allMedia;
                    } else {
                        if (allMedia.size() == count) {
                            addNewestMediaOnly = false;
                            allMediaPresent = true;
                            cursor.close();
                            return allMedia;
                        }
                        allMedia.add(0, media);
                        paths.put(media.getPath(), true);
                    }
                } else {
                    listMedia.add(media);
                    paths.put(media.getPath(), true);
                }
//                if (listMedia.size() >= 2000) {
//                    break;
//                }

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
