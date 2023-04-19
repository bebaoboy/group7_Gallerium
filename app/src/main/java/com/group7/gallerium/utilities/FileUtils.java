package com.group7.gallerium.utilities;

import android.app.PendingIntent;
import android.app.RecoverableSecurityException;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.IntentSender;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.annotation.NonNull;

import com.group7.gallerium.models.Media;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

public class FileUtils {

    public void deleteRecursiveInternal(File fileOrDirectory) {

        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                deleteRecursiveInternal(child);
            }
        }

        fileOrDirectory.delete();
    }

    public void deleteRecursive(@NonNull ActivityResultLauncher<IntentSenderRequest> launcher, @NonNull ArrayList<Media> medias, @NonNull Context context, String albumPath){

        Media media = new Media();
        media.setPath(albumPath + "/" + " " + ".jpeg");
        medias.add(media);

        deleteMultiple(launcher, medias, context);
        try{
            File albumFolder = new File(albumPath);
            if(albumFolder.exists() && albumFolder.isDirectory()){
                if(albumFolder.delete()){
                    Toast.makeText(context, "Xóa thành công", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(context, "Xóa không thành công", Toast.LENGTH_SHORT).show();
                }
            }
        }catch (Exception e){
            Log.d("tag", e.getMessage());
        }
    }

    public void saveImageToMediaStore(Context context, Bitmap bitmap) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, "image_name");
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        Uri uri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        OutputStream outstream;
        try {
            outstream = context.getContentResolver().openOutputStream(uri);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outstream);
            outstream.close();
            Toast.makeText(context, "Image saved successfully", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(context, "Error while saving image", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    public void moveFile(@NonNull String inputPath, @NonNull ActivityResultLauncher<IntentSenderRequest> launcher, @NonNull String outputPath, @NonNull Context context) {

        Uri outputFile;

        try {
            Log.d("Parent path", outputPath);
            String[] parse = outputPath.split("/");
            var dirList = Arrays.stream(parse).skip(4).collect(Collectors.toList());

            StringBuilder relativePath= new StringBuilder();
            for(var item: dirList){
                relativePath.append(item).append("/");
                Log.d("Item", item);
            }
            Log.d("relativePath", relativePath.toString());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                //outputFolderUri = getUriOfFolder(context, outputPath);
                outputFile = insertMediaToMediaStore(context, inputPath, relativePath.toString());

                try (InputStream inputStream = new FileInputStream(inputPath)) { //input stream

                    OutputStream out = context.getContentResolver().openOutputStream(outputFile); //output stream

                    byte[] buf = new byte[8096];
                    int len;
                    while ((len = inputStream.read(buf)) > 0) {
                        out.write(buf, 0, len); //write input file data to output file
                    }
                    out.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }
                 Toast.makeText(context, "Move file " + outputFile.getPath(), Toast.LENGTH_SHORT).show();

            }
            delete(launcher, inputPath, context);

//        catch (FileNotFoundException fnfe1) {
//            Log.e("tag", fnfe1.getMessage());
//        }
        }
        catch(Exception e){
                Log.e("tag", e.getMessage());
            }
    }


//    public Uri getUriOfFolder(Context context, String inputPath){
//        Log.d("uri folder", MediaStore.Files.getContentUri(inputPath).toString());
//        Log.d("volumn", MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString());
//
//
//       return MediaStore.Files.getContentUri(inputPath);
//    }

    public void secureFile(@NonNull Context context, @NonNull String inputPath, @NonNull String inputName, @NonNull ActivityResultLauncher<IntentSenderRequest> launcher){
        File secureDir = new File(context.getFilesDir(), "secure-subfolder");

        try (InputStream inputStream = new FileInputStream(inputPath)){
            FileOutputStream out = new FileOutputStream(new File(secureDir, inputName));
            byte[] buf = new byte[8096];
            int len;
            while ((len = inputStream.read(buf)) > 0) {
                out.write(buf, 0, len); //write input file data to output file
            }
            out.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        delete(launcher, inputPath, context);
    }

    public void moveFromInternal(@NonNull String inputPath, @NonNull ActivityResultLauncher<IntentSenderRequest> launcher, @NonNull String outputPath, String mimeType, int mediaType, @NonNull Context context) {

        Uri outputFile;

        try {
            Log.d("Parent path", outputPath);
            String[] parse = outputPath.split("/");
            var dirList = Arrays.stream(parse).skip(4).collect(Collectors.toList());

            StringBuilder relativePath= new StringBuilder();
            for(var item: dirList){
                relativePath.append(item).append("/");
                Log.d("Item", item);
            }
            Log.d("relativePath", relativePath.toString());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                //outputFolderUri = getUriOfFolder(context, outputPath);
                outputFile = insertMediaFromInternal(context, mimeType, mediaType, inputPath,  relativePath.toString());

                try (InputStream inputStream = new FileInputStream(inputPath)) { //input stream

                    OutputStream out = context.getContentResolver().openOutputStream(outputFile); //output stream

                    byte[] buf = new byte[8096];
                    int len;
                    while ((len = inputStream.read(buf)) > 0) {
                        out.write(buf, 0, len); //write input file data to output file
                    }
                    out.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }
                Toast.makeText(context, "Move file " + outputFile.getPath(), Toast.LENGTH_SHORT).show();

            }
            File f = new File(inputPath);
            if(f.delete()){
                Log.d("file-deleted", "true");
            }else{
                Log.d("file-not-deleted", "false");
            }

//        catch (FileNotFoundException fnfe1) {
//            Log.e("tag", fnfe1.getMessage());
//        }
        }
        catch(Exception e){
            Log.e("tag", e.getMessage());
        }
    }

    public Uri insertMediaFromInternal(@NonNull Context context, @NonNull String mimeType, int mediaType, @NonNull String inputPath, @NonNull String outputPath) {
        Cursor cursor;
        if (mediaType == 1) {
            cursor = context.getContentResolver().query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    , new String[]{MediaStore.Images.Media._ID}
                    , MediaStore.Images.Media.DATA + "=? "
                    , new String[]{outputPath}, null);
        } else {
            cursor = context.getContentResolver().query(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    , new String[]{MediaStore.Video.Media._ID}
                    , MediaStore.Video.Media.DATA + "=? "
                    , new String[]{outputPath}, null);
        }

        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
            cursor.close();
            if(mediaType == 1)
                return ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
            else
                return ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id);

        }
        else if (!inputPath.isEmpty()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                var resolver = context.getContentResolver();
                if (mediaType == 1) {
                    Uri picCollection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
                    if(Objects.equals(outputPath, "Download/")) {
                        picCollection = MediaStore.Downloads.EXTERNAL_CONTENT_URI;
                    }
                    values.put(MediaStore.Images.Media.MIME_TYPE, mimeType);
                    values.put(MediaStore.Images.Media.RELATIVE_PATH, outputPath);
                    values.put(MediaStore.Images.Media.IS_PENDING, 0);

                    return resolver.insert(picCollection, values);
                } else {
                    Uri vidCollection = MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
                    if(Objects.equals(outputPath, "Download/")) {
                        vidCollection = MediaStore.Downloads.EXTERNAL_CONTENT_URI;
                    }
                    values.put(MediaStore.Video.Media.MIME_TYPE, mimeType);
                    values.put(MediaStore.Video.Media.RELATIVE_PATH, outputPath);
                    values.put(MediaStore.Video.Media.IS_PENDING, 0);

                    return resolver.insert(vidCollection, values);
                }
            } else {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, inputPath);
                return context.getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            }
        } else {
            return null;
        }
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

    public Uri insertMediaToMediaStore(@NonNull Context context, @NonNull String inputPath, @NonNull String outputPath) {
        Cursor cursor;
        String name = "";
        String[] parse;
        int mediaType = 1;
        Media media = AccessMediaFile.getMediaWithPath(inputPath);
        if(media != null){
            parse = media.getPath().split("/");
            name = parse[parse.length - 1];
        }else{
            parse = inputPath.split("/");
            name = parse[parse.length - 1];
        }

        if (name.strip().length() == 0) return null;
        if(media != null) mediaType = media.getType();
        else{
            mediaType = 3;
        }
        if (mediaType == 1) {
            cursor = context.getContentResolver().query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    , new String[]{MediaStore.Images.Media._ID}
                    , MediaStore.Images.Media.DATA + "=? "
                    , new String[]{outputPath}, null);
        } else {
            cursor = context.getContentResolver().query(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    , new String[]{MediaStore.Video.Media._ID}
                    , MediaStore.Video.Media.DATA + "=? "
                    , new String[]{outputPath}, null);
        }

        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
            cursor.close();
            if(mediaType == 1)
                return ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
            else
                return ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id);

        }
        else if (!inputPath.isEmpty()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                var resolver = context.getContentResolver();
                if (mediaType == 1) {
                    Uri picCollection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
                    if(Objects.equals(outputPath, "Download/")) {
                        picCollection = MediaStore.Downloads.EXTERNAL_CONTENT_URI;
                    }
                    values.put(MediaStore.Images.Media.DISPLAY_NAME, name);
                    if(media != null)
                        values.put(MediaStore.Images.Media.MIME_TYPE, media.getMimeType());
                    else
                        values.put(MediaStore.Images.Media.MIME_TYPE, getMimeType(inputPath));
                    values.put(MediaStore.Images.Media.RELATIVE_PATH, outputPath);
                    values.put(MediaStore.Images.Media.IS_PENDING, 0);

                    return resolver.insert(picCollection, values);
                } else {
                    Uri vidCollection = MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
                    if(Objects.equals(outputPath, "Download/")) {
                        vidCollection = MediaStore.Downloads.EXTERNAL_CONTENT_URI;
                    }
                    values.put(MediaStore.Video.Media.DISPLAY_NAME, name);
                    if(media != null)
                        values.put(MediaStore.Video.Media.MIME_TYPE, media.getMimeType());
                    else
                        values.put(MediaStore.Video.Media.MIME_TYPE, getMimeType(inputPath));
                    values.put(MediaStore.Video.Media.RELATIVE_PATH, outputPath);
                    values.put(MediaStore.Video.Media.IS_PENDING, 0);

                    return resolver.insert(vidCollection, values);
                }
            } else {
                ContentValues values = new ContentValues();
                if(media != null)
                    values.put(MediaStore.Images.Media.DATA, media.getPath());
                else{
                    values.put(MediaStore.Images.Media.DATA, inputPath);
                }
                return context.getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            }
        } else {
            return null;
        }
    }

    public void createDir(@NonNull Context context, @NonNull String path, @NonNull String name, @NonNull String relativePath) {
        if (name.strip().length() == 0) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentValues values = new ContentValues();
            var resolver = context.getContentResolver();
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//                if (!Environment.isExternalStorageManager()) {
                    Uri picCollection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);

                    values.put(MediaStore.Images.Media.DISPLAY_NAME, " " + ".jpeg");
                    values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                    values.put(MediaStore.Images.Media.RELATIVE_PATH, relativePath);
                    values.put(MediaStore.Images.Media.IS_PENDING, 0);
                    values.put(MediaStore.Images.Media.HEIGHT, 0);
                    values.put(MediaStore.Images.Media.SIZE, 0);

                    resolver.insert(picCollection, values);
//                }
//            }
            Uri collection = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath);
            resolver.insert(collection, values);

        } else {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Files.FileColumns.DATA, path);
            context.getContentResolver().insert(
                    MediaStore.Files.getContentUri("external"), values);
        }
    }
    public void copyFile(@NonNull String inputPath, @NonNull String outputPath, @NonNull Context context) {

        Uri outputFile;

        try {
            Log.d("Parent path", outputPath);
            String[] parse = outputPath.split("/");
            var dirList = Arrays.stream(parse).skip(4).collect(Collectors.toList());

            StringBuilder relativePath= new StringBuilder();
            for(var item: dirList){
                relativePath.append(item).append("/");
                Log.d("Item", item);
            }
            Log.d("relativePath", relativePath.toString());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                //outputFolderUri = getUriOfFolder(context, outputPath);
                outputFile = insertMediaToMediaStore(context, inputPath, relativePath.toString());

                try (InputStream inputStream = new FileInputStream(inputPath)) { //input stream

                    OutputStream out = context.getContentResolver().openOutputStream(outputFile); //output stream

                    byte[] buf = new byte[8096];
                    int len;
                    while ((len = inputStream.read(buf)) > 0) {
                        out.write(buf, 0, len); //write input file data to output file
                    }
                    out.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }
                Toast.makeText(context, "Copy file " + outputFile.getPath(), Toast.LENGTH_SHORT).show();

            }

//        catch (FileNotFoundException fnfe1) {
//            Log.e("tag", fnfe1.getMessage());
//        }
        }
        catch(Exception e){
            Log.e("tag", e.getMessage());
        }
    }

    public void moveFileMultiple(@NonNull ArrayList<Media> medias, @NonNull ActivityResultLauncher<IntentSenderRequest> launcher, @NonNull String outputPath, @NonNull Context context) {
        if (medias.size() <= 0) return;
        if (medias.size() <= 1){
            moveFile(medias.get(0).getPath(), launcher, outputPath, context);
            return;
        }
        Uri outputFile;

        try {
            Log.d("Parent path", outputPath);
            String[] parse = outputPath.split("/");
            var dirList = Arrays.stream(parse).skip(4).collect(Collectors.toList());

            StringBuilder relativePath= new StringBuilder();
            for(var item: dirList){
                relativePath.append(item).append("/");
                Log.d("Item", item);
            }
            Log.d("relativePath", relativePath.toString());
            for(var m : medias) {
                String inputPath = m.getPath();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    //outputFolderUri = getUriOfFolder(context, outputPath);
                    try {
                        outputFile = insertMediaToMediaStore(context, inputPath, relativePath.toString());
                    }catch(Exception e){
                        Log.d("tag", e.getMessage());
                        continue;
                    }
                    try (InputStream inputStream = new FileInputStream(inputPath)) { //input stream

                        OutputStream out = context.getContentResolver().openOutputStream(outputFile); //output stream

                        byte[] buf = new byte[8096];
                        int len;
                        while ((len = inputStream.read(buf)) > 0) {
                            out.write(buf, 0, len); //write input file data to output file
                        }
                        out.close();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(context, "Move file " + outputFile.getPath(), Toast.LENGTH_SHORT).show();
                }
            }
            deleteMultiple(launcher, medias, context);

//        catch (FileNotFoundException fnfe1) {
//            Log.e("tag", fnfe1.getMessage());
//        }
        }
        catch(Exception e){
            Log.e("tag", e.getMessage());
        }
    }

    public void copyFileMultiple(@NonNull ArrayList<Media> medias, @NonNull String outputPath, @NonNull Context context) {
        if (medias.size() <= 0) return;
        if (medias.size() <= 1){
            copyFile(medias.get(0).getPath(), outputPath, context);
            return;
        }
        Uri outputFile;

        try {
            Log.d("Parent path", outputPath);
            String[] parse = outputPath.split("/");
            var dirList = Arrays.stream(parse).skip(4).collect(Collectors.toList());

            StringBuilder relativePath= new StringBuilder();
            for(var item: dirList){
                relativePath.append(item).append("/");
                Log.d("Item", item);
            }
            Log.d("relativePath", relativePath.toString());
            for(var m : medias) {
                String inputPath = m.getPath();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    //outputFolderUri = getUriOfFolder(context, outputPath);
                    outputFile = insertMediaToMediaStore(context, inputPath, relativePath.toString());

                    try (InputStream inputStream = new FileInputStream(inputPath)) { //input stream

                        OutputStream out = context.getContentResolver().openOutputStream(outputFile); //output stream

                        byte[] buf = new byte[8096];
                        int len;
                        while ((len = inputStream.read(buf)) > 0) {
                            out.write(buf, 0, len); //write input file data to output file
                        }
                        out.close();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(context, "Move file " + outputFile.getPath(), Toast.LENGTH_SHORT).show();
                }
            }

//        catch (FileNotFoundException fnfe1) {
//            Log.e("tag", fnfe1.getMessage());
//        }
        }
        catch(Exception e){
            Log.e("tag", e.getMessage());
        }
    }

    @NonNull
    public Uri getUri(@NonNull String path, int type, @NonNull Context context) {
        try {
            Cursor cursor;
            if (type == 1) {
                cursor = context.getContentResolver().query(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        , new String[]{MediaStore.Images.Media._ID}
                        , MediaStore.Images.Media.DATA + "=? "
                        , new String[]{path}, null);
            } else {
                cursor = context.getContentResolver().query(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                        , new String[]{MediaStore.Video.Media._ID}
                        , MediaStore.Video.Media.DATA + "=? "
                        , new String[]{path}, null);
            }

            if (cursor != null && cursor.moveToFirst()) {
                int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
                cursor.close();
                if (type == 1)
                    return ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
                else
                    return ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id);
            }
        }catch (Exception e){
            Log.d("tag", e.getMessage());
        }
        return null;
    }

    /**
     * Delete file.
     * <p>
     * If {@link ContentResolver} failed to delete the file, use trick,
     * SDK version is >= 29(Q)? use {@link SecurityException} and again request for delete.
     * SDK version is >= 30(R)? use {@link //MediaStore#createDeleteRequest(ContentResolver, Collection)}.
     */

    public int delete(@NonNull ActivityResultLauncher<IntentSenderRequest> launcher, @NonNull String path, @NonNull Context context){
        Media media = AccessMediaFile.getMediaWithPath(path);
        ContentResolver contentResolver = context.getContentResolver();
        Uri uri;
        try {
            Cursor cursor;
            if(media.getType() == 1) {
               cursor  = context.getContentResolver().query(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        , new String[]{MediaStore.Images.Media._ID}
                        , MediaStore.Images.Media.DATA + "=? "
                        , new String[]{path}, null);
            } else {
                cursor  = context.getContentResolver().query(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                        , new String[]{MediaStore.Video.Media._ID}
                        , MediaStore.Video.Media.DATA + "=? "
                        , new String[]{path}, null);
            }

        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
            cursor.close();
            if(media.getType() == 1)
                uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
            else
                uri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id);
            try {
                return contentResolver.delete(uri, null, null);
            }
            catch (SecurityException e) {

                PendingIntent pendingIntent = null;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

                    ArrayList<Uri> collection = new ArrayList<>();
                    collection.add(uri);
                    pendingIntent = MediaStore.createDeleteRequest(contentResolver, collection);

                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                    //if exception is recoverable then again send delete request using intent
                    if (e instanceof RecoverableSecurityException) {
                        RecoverableSecurityException exception = (RecoverableSecurityException) e;
                        pendingIntent = exception.getUserAction().getActionIntent();
                    }
                }

                if (pendingIntent != null) {
                    IntentSender sender = pendingIntent.getIntentSender();
                    IntentSenderRequest request = new IntentSenderRequest.Builder(sender).build();
                    launcher.launch(request);
                }
                return 0;
            }
        }

        }catch (Exception e){
            Log.d("tag", e.getMessage());
        }
        return 0;
    }

    public int deleteMultiple(@NonNull ActivityResultLauncher<IntentSenderRequest> launcher, @NonNull ArrayList<Media> medias, @NonNull Context context){
        if (medias.size() <= 0) return 1;
        if (medias.size() <= 1) return delete(launcher, medias.get(0).getPath(), context);

        ContentResolver contentResolver = context.getContentResolver();
        ArrayList<Uri> collection = new ArrayList<>();
        for(var m : medias) {
            collection.add(getUri(m.getPath(), m.getType(), context));
        }

        try {
            try {
                int row = 0;
                for (var uri : collection) {
                    row += contentResolver.delete(uri, null, null);
                }
                return row;
            } catch (SecurityException e) {

                PendingIntent pendingIntent = null;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

                    pendingIntent = MediaStore.createDeleteRequest(contentResolver, collection);

                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                    //if exception is recoverable then again send delete request using intent
                    if (e instanceof RecoverableSecurityException) {
                        RecoverableSecurityException exception = (RecoverableSecurityException) e;
                        pendingIntent = exception.getUserAction().getActionIntent();
                    }
                }

                if (pendingIntent != null) {
                    IntentSender sender = pendingIntent.getIntentSender();
                    IntentSenderRequest request = new IntentSenderRequest.Builder(sender).build();
                    launcher.launch(request);
                }
                return 0;
            }


        } catch (Exception e){
            Log.d("tag", e.getMessage());
        }
        return 0;
    }

    public void renameFile(@NonNull String name, int type, @NonNull String path, @NonNull Context context, @NonNull ActivityResultLauncher<IntentSenderRequest> launcher) {
        if (name.strip().length() == 0) return ;
        ContentValues values = new ContentValues();
        ContentResolver resolver = context.getContentResolver();
        Uri uri = getUri(path, type, context);
        try {
            if (type == 1) {
                values.put(MediaStore.Images.Media.IS_PENDING, 1);
            } else {
                values.put(MediaStore.Video.Media.IS_PENDING, 1);
            }
            resolver.update(uri, values, null, null);
            values.clear();
            if (type == 1) {
                values.put(MediaStore.Images.Media.DISPLAY_NAME, name);
            } else {
                values.put(MediaStore.Video.Media.DISPLAY_NAME, name);
            }
            if (type == 1) {
                values.put(MediaStore.Images.Media.IS_PENDING, 0);
            } else {
                values.put(MediaStore.Video.Media.IS_PENDING, 0);
            }
            resolver.update(uri, values, null, null);

        } catch (SecurityException e) {

            PendingIntent pendingIntent = null;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

                ArrayList<Uri> collection = new ArrayList<>();
                collection.add(uri);
                pendingIntent = MediaStore.createWriteRequest(context.getContentResolver(), collection);

            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                //if exception is recoverable then again send delete request using intent
                if (e instanceof RecoverableSecurityException) {
                    RecoverableSecurityException exception = (RecoverableSecurityException) e;
                    pendingIntent = exception.getUserAction().getActionIntent();
                }
            }

            if (pendingIntent != null) {
                IntentSender sender = pendingIntent.getIntentSender();
                IntentSenderRequest request = new IntentSenderRequest.Builder(sender).build();
                launcher.launch(request);
            }

        } catch (Exception e) {
            Log.d("Tag", e.getMessage());
        }
    }

    public  void updateInfoFile(@NonNull String password) {
        File info = new File(Environment.getExternalStorageDirectory()+File.separator+".secret"+ File.separator+"info.txt");
        try{
            BufferedReader br = new BufferedReader(new FileReader(info));
            String info_question = br.readLine();
            String info_answer = br.readLine();
            br.close();
            FileOutputStream fos = new FileOutputStream(info);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
            bw.write(password);
            bw.newLine();
            bw.write(info_question);
            bw.newLine();
            bw.write(info_answer);
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<String> trashFileMultiple(ArrayList<Media> selectedMedia) {
        ArrayList<String> newNames = new ArrayList<>();
        for(var media : selectedMedia) {
            var s = media.getPath();
            var parentPath = s.substring(0, s.lastIndexOf("/"));
            File fileDir = new File(parentPath);
            var fileName = s.substring(s.lastIndexOf("/") + 1);
            File from = new File(fileDir, fileName);
            var nf = ".gtrashed-" + System.currentTimeMillis() + "-" + fileName;
            File to = new File(fileDir, nf);
            from.renameTo(to);
            newNames.add(fileDir + "/" + nf);
            AccessMediaFile.addToTrashMedia(fileDir + "/" + nf);
        }
        return newNames;
    }

    public void restoreFileMultiple(ArrayList<Media> selectedMedia) {
        for(var media : selectedMedia) {
            var s = media.getPath();
            var parentPath = s.substring(0, s.lastIndexOf("/"));
            File fileDir = new File(parentPath);
            var fileName = s.substring(s.lastIndexOf("/") + 1);
            File from = new File(fileDir, fileName);
            fileName = fileName.substring(1);
            var nf = fileName.substring(fileName.indexOf("-", fileName.indexOf("-") + 1) + 1);
            File to = new File(fileDir, nf);
            from.renameTo(to);
            Log.d("RESTORE", nf);
            AccessMediaFile.removeFromTrashMedia( media.getPath());
        }
    }

    public void deleteTrashMultiple(ArrayList<Media> selectedMedia) {
        for (var m : selectedMedia) {
            new File(m.getPath()).delete();
            AccessMediaFile.removeFromTrashMedia(m.getPath());
        }
    }
}
