package com.group7.gallerium.utilities;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import androidx.loader.content.CursorLoader;

import com.group7.gallerium.models.Media;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

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

    public static final List<Media> getAllMediaFromGallery(Context context) {
        if(!allMediaPresent) { // Do not fetch photos between Activity switching.
            // MASSIVE performance improvement. Like over 9000.
            Uri uri;
            int columnIndexData, thumb, dateIndex, typeIndex;
            List<Media> listImage = new ArrayList<>();
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
//            int mimeindex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE);
//            int titleIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.TITLE);
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
                media.setType("photo");
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
                    listImage.add(media);
                }

                if(listImage.size()>1000) { // Just for testing.
                    break;                  // I don't want to load 10 000 photos at once.
                }
            }
            cursor.close(); // Android Studio suggestion
            allMedia = listImage;
            addNewestMediaOnly = false;
            allMediaPresent = true;
            return listImage;
        }
        else{
            return allMedia;
        }
    }
}
