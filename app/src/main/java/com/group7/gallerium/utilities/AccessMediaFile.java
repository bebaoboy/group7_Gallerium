package com.group7.gallerium.utilities;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;

import com.group7.gallerium.models.Media;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
            MediaStore.Files.FileColumns.DATE_MODIFIED,
            MediaStore.Files.FileColumns.DATE_ADDED,
            MediaStore.Files.FileColumns.DATE_TAKEN,
            MediaStore.Files.FileColumns.MEDIA_TYPE,
            MediaStore.Files.FileColumns.MIME_TYPE,
            MediaStore.Files.FileColumns.TITLE,
            MediaStore.Files.FileColumns.RESOLUTION,
            MediaStore.Files.FileColumns.WIDTH,
            MediaStore.Files.FileColumns.HEIGHT,
            MediaStore.Files.FileColumns.DURATION,
            MediaStore.Files.FileColumns.SIZE,


    };
    static String selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "="
            + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
            + " OR "
            + MediaStore.Files.FileColumns.MEDIA_TYPE + "="
            + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;
    static final String orderBy = MediaStore.Files.FileColumns.DATE_MODIFIED;
    static Uri queryUri = MediaStore.Files.getContentUri("external");

    public static List<Media> getAllMedia() {
        return allMedia;
    }

    public static Media getMediaWithPath(String path){
        for(Media media: allMedia){
            if(media.getPath().equals(path)){
                return media;
            }
        }
        return null;
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
            int typeColumn, titleColumn, dateColumn, pathColumn, idColumn, mimeTypeColumn, videoLengthColumn, widthColumn, heightColumn;
            int count, type, width, height;
            String mimeType;
            String absolutePath, id, dateText, title;
            long dateTaken, videoLength=0;
            List<Media> listMedia = new ArrayList<>();


            Cursor cursor = context.getApplicationContext().getContentResolver().query(queryUri,
                    columns,
                    selection,
                    null, // Selection args (none).
                    orderBy + " DESC" // Sort order.
            );
            count = cursor.getCount();
            Log.d("Amount-pic", String.valueOf(count));
            int multiplier = 1000;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                multiplier = 1;
            }

            idColumn = cursor.getColumnIndex(MediaStore.Files.FileColumns._ID);
            pathColumn = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA);
            typeColumn = cursor.getColumnIndex(MediaStore.Files.FileColumns.MEDIA_TYPE);
            mimeTypeColumn = cursor.getColumnIndex(MediaStore.Files.FileColumns.MIME_TYPE);
            videoLengthColumn = cursor.getColumnIndex(MediaStore.Files.FileColumns.DURATION);
            widthColumn = cursor.getColumnIndex(MediaStore.Files.FileColumns.WIDTH);
            heightColumn = cursor.getColumnIndex(MediaStore.Files.FileColumns.HEIGHT);

            dateColumn = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATE_MODIFIED);
            int dt = 0;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                dt = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATE_TAKEN);
            }
            var da = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATE_ADDED);
            titleColumn = cursor.getColumnIndex(MediaStore.Files.FileColumns.TITLE);
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
                mimeType = cursor.getString(mimeTypeColumn);
                dateTaken = cursor.getLong(dateColumn);
                title = cursor.getString(titleColumn);
//                Log.d("gallerium", "reading " + dateText + ", date modified: " + dateTaken
//                        + ", date taken: " + cursor.getLong(dt) + ", date added" + cursor.getLong(da));
                if (mimeType!=null && mimeType.startsWith("video")) {
                    videoLength = cursor.getLong(videoLengthColumn);
                }
                width = cursor.getInt(widthColumn);
                height = cursor.getInt(heightColumn);

                Media media = new Media();
                media.setPath(absolutePath);
                media.setThumbnail(absolutePath);
                media.setDateTaken(dateTaken * multiplier);
                media.setType(type);
                media.setMimeType(mimeType);
                media.setTitle(title);
                media.setDuration(videoLength);
                media.setWidth(width);
                media.setHeight(height);

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
                        //Log.d("gallerium", "adding " + dateText + ", real date: " + dateTaken);
                    }
                } else {
                    listMedia.add(media);
//                    paths.put(media.getPath(), true);
//                    Log.d("gallerium", "adding new pic" + dateTaken);
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
