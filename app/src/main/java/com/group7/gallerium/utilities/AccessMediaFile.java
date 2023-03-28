package com.group7.gallerium.utilities;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;

import com.group7.gallerium.models.Media;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class AccessMediaFile {

    //public static List<Media> allMedia;
    private static HashMap<String, Media> allMedia = new HashMap<>();
    private static HashMap<String, Boolean> allFavMedia = new HashMap<>();
    private static HashMap<String, Boolean> allYourAlbum = new HashMap<>();
    private static HashMap<String, Boolean> allTrashedMedia = new HashMap<>();
    private static ArrayList<Media> cacheAllMedia = new ArrayList<>();
    private static boolean cached = false;
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
            MediaStore.Files.FileColumns.BITRATE


    };
    static String selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "="
            + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
            + " OR "
            + MediaStore.Files.FileColumns.MEDIA_TYPE + "="
            + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;
    static final String orderBy = MediaStore.Files.FileColumns.DATE_MODIFIED;
    static Uri queryUri = MediaStore.Files.getContentUri("external");

    public static void setAllFavMedia(Set<String> paths) {
        allFavMedia.clear();
        paths.forEach(AccessMediaFile::addToFavMedia);
    }

    public static void addToFavMedia(String path) {
        allFavMedia.put(path, false);
    }

    public static void removeFromFavMedia(String path) {
        allFavMedia.remove(path);
    }

    public static List<Media> getAllFavMedia() {
        return getFavMedia().stream().map(x -> allMedia.get(x)).sorted((x1, x2) -> {
            if (x1.getRawDate() == x2.getRawDate()) {
                return 0;
            } else if (x1.getRawDate() > x2.getRawDate()) {
                return -1;
            } else return 1;
        }).collect(Collectors.toList());
    }

    public static void setAllYourALbum(Set<String> paths) {
        allYourAlbum.clear();
        paths.forEach(AccessMediaFile::addToYourAlbum);
    }

    public static void addToYourAlbum(String path) {
        allYourAlbum.put(path, false);
    }

    public static void removeFromYourAlbum(String path) {
        allYourAlbum.remove(path);
    }

    public static Set<String> getAllYourAlbum() {
        return allYourAlbum.keySet().stream().sorted(Comparator.naturalOrder()).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public static Set<String> getFavMedia() {
        var s = allFavMedia.keySet();
        s.removeIf(x -> !allMedia.containsKey(x));
        return new HashSet<>(s);
    }

    public static boolean isExistedAnywhere(String path) {
        return allFavMedia.containsKey(path) || allTrashedMedia.containsKey(path);
    }

    private static HashMap<String, Media> getAllMedia() {
        return allMedia;
    }

    public static Media getMediaWithPath(String path){
        return allMedia.get(path);
    }
    public static void refreshAllMedia(){
        allMediaPresent = false;
    }
    public static String renameMedia(String path, String fullName) {
        var newPath = path.substring(0, path.lastIndexOf("/")) + "/" + fullName;
        allMedia.put(newPath, allMedia.remove(path));
        Objects.requireNonNull(allMedia.get(newPath)).setPath(newPath);
        Objects.requireNonNull(allMedia.get(newPath)).setTitle(fullName);
        return newPath;
    }
    public static void updateNewMedia(){
        addNewestMediaOnly = true;
        allMediaPresent = false;
    }
    public static void removeMediaFromAllMedia(String path) {  // remove deleted media from "database"
        allMedia.remove(path);
        refreshAllMedia();
    }

    public static ArrayList<Media> getAllMedia(Context context) {
        Log.d("CACHED", String.valueOf(cached));
        var temp = getAllMediaFromGallery(context);
        if (!cached) {
            cacheAllMedia.clear();
            Comparator<Map.Entry<String, Media>> customComparator = (media1, media2) -> {
                return Long.compare(media2.getValue().getRawDate(), media1.getValue().getRawDate());
            };
            cached = true;
            cacheAllMedia = temp.entrySet()
                    .stream()
                    .sorted(customComparator)
                    .map(Map.Entry::getValue).collect(Collectors.toCollection(ArrayList::new));
        }
        return cacheAllMedia;
    }

    private static HashMap<String, Media> getAllMediaFromGallery(Context context) {

        if (!allMediaPresent) {
            int typeColumn, titleColumn, dateColumn, pathColumn, idColumn, mimeTypeColumn,
                    videoLengthColumn, widthColumn, heightColumn, sizeColumn, bitrateColumn, resColumn;
            int count, type, width, height, bitrate;
            String mimeType;
            String absolutePath, id, title, res;
            long dateTaken, videoLength=0, size;
            HashMap<String, Media> listMedia = new HashMap<>();


            Cursor cursor = context.getContentResolver().query(queryUri,
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
            sizeColumn = cursor.getColumnIndex(MediaStore.Files.FileColumns.SIZE);
            bitrateColumn = cursor.getColumnIndex(MediaStore.Files.FileColumns.BITRATE);
            resColumn = cursor.getColumnIndex(MediaStore.Files.FileColumns.RESOLUTION);

            dateColumn = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATE_MODIFIED);
            int dt = 0;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                dt = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATE_TAKEN);
            }
            var da = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATE_ADDED);
            titleColumn = cursor.getColumnIndex(MediaStore.Files.FileColumns.TITLE);
            if (!allMediaPresent || cursor.getCount() != allMedia.size()) {
                cached = false;
                allMedia.clear();
            }
            while (cursor.moveToNext()) {
                id = cursor.getString(idColumn);
                absolutePath = cursor.getString(pathColumn);
                if (allMedia.containsKey(absolutePath)) {
                    addNewestMediaOnly = false;
                    allMediaPresent = true;
                    break;
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
                size = cursor.getLong(sizeColumn);
                bitrate = cursor.getInt(bitrateColumn);
                res = cursor.getString(resColumn);

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
                media.setSize(size);
                media.setBitrate(bitrate);
                media.setResolution(res);

                if (media.getPath().equals("")) {
                    continue;
                }
                if (addNewestMediaOnly) {
                    boolean iscontained = allMedia.containsKey(media.getPath()); // in the "all media database"
//                    for(Media m: allMedia){
//                        if(m.getPath().equals(media.getPath())){
//                            iscontained = true;
//                            break;
//                        }
//                    }
                    if (iscontained) {
                        addNewestMediaOnly = false;
                        allMediaPresent = true;
                        break;
                    } else {
                        if (allMedia.size() == count) {
                            addNewestMediaOnly = false;
                            allMediaPresent = true;
                            cursor.close();
                            return getAllMedia();
                        }
                        allMedia.put(media.getPath(), media);
                        cached = false;
                        //Log.d("gallerium", "adding " + dateText + ", real date: " + dateTaken);
                    }
                } else {
                    listMedia.put(media.getPath(), media);
                    cached = false;
//                    paths.put(media.getPath(), true);
//                    Log.d("gallerium", "adding new pic" + dateTaken);
                }
//                if (listMedia.size() >= 2000) {
//                    break;
//                }

            }
            cursor.close(); // Android Studio suggestion
            allMedia.putAll(listMedia);
            addNewestMediaOnly = false;
            allMediaPresent = true;
            cached = false;
        }
        return getAllMedia();
    }
}
