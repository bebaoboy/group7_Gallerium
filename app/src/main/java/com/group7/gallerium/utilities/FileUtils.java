package com.group7.gallerium.utilities;

import android.app.PendingIntent;
import android.app.RecoverableSecurityException;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.IntentSender;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;

import com.group7.gallerium.models.Media;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
    public void deleteFile(String Path) {
        try {
            // delete the original file
            new File(Path).delete();
        }
        catch (Exception e) {
            Log.e("tag", e.getMessage());
        }
    }

    public static void deleteRecursive(File fileOrDirectory) {

        if (fileOrDirectory.isDirectory())
            for (File child : Objects.requireNonNull(fileOrDirectory.listFiles()))
                deleteRecursive(child);

        fileOrDirectory.delete();

    }

    public void moveFile1(String inputPath, String inputFilename, String outputPath, Context context){
        String[] subDirs = inputPath.split("/");
        String parent = subDirs[subDirs.length - 2];
        String root = subDirs[2];

      var contentUri = MediaStore.Files.getContentUri("external");
      var selection = MediaStore.MediaColumns.RELATIVE_PATH + "=?";
      var selectionArgs = new ArrayList<String>();
      selectionArgs.add(Environment.DIRECTORY_DCIM + "/parent/");

    }

    public void moveFile(String inputPath, ActivityResultLauncher<IntentSenderRequest> launcher, String outputPath, Context context) {

        InputStream in = null;
        Uri outputFile, outputFolderUri;

        try {
            String parentPath = outputPath;
            Log.d("Parent path", parentPath);
            String[] parse = parentPath.split("/");
            var dirList = Arrays.stream(parse).skip(4).collect(Collectors.toList());

            String relativePath="";
            for(var item: dirList){
                relativePath += item + "/";
                Log.d("Item", item);
            }
            Log.d("relativePath", relativePath);
            var type = AccessMediaFile.getMediaWithPath(inputPath).getType();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                //outputFolderUri = getUriOfFolder(context, outputPath);
                outputFile = insertMediaToMediaStore(context, inputPath, relativePath);

                try (InputStream inputStream = new FileInputStream(inputPath)) { //input stream

                    OutputStream out = context.getContentResolver().openOutputStream(outputFile); //output stream

                    byte[] buf = new byte[8096];
                    int len;
                    while ((len = inputStream.read(buf)) > 0) {
                        out.write(buf, 0, len); //write input file data to output file
                    }

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

    public Uri insertMediaToMediaStore(Context context, String inputPath, String outputPath) {
        Cursor cursor = null;
        Media media = AccessMediaFile.getMediaWithPath(inputPath);
        String[] parse = media.getPath().split("/");
        String name = parse[parse.length - 1];
        int mediaType = media.getType();
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
                    values.put(MediaStore.Images.Media.MIME_TYPE, media.getMimeType());
                    values.put(MediaStore.Images.Media.RELATIVE_PATH, outputPath);
                    values.put(MediaStore.Images.Media.IS_PENDING, 0);
                    Uri finaluri = resolver.insert(picCollection, values);

                    return finaluri;
                } else {
                    Uri vidCollection = MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
                    if(Objects.equals(outputPath, "Download/")) {
                        vidCollection = MediaStore.Downloads.EXTERNAL_CONTENT_URI;
                    }
                    values.put(MediaStore.Video.Media.DISPLAY_NAME, name);
                    values.put(MediaStore.Video.Media.MIME_TYPE, media.getMimeType());
                    values.put(MediaStore.Video.Media.RELATIVE_PATH, outputPath);
                    values.put(MediaStore.Video.Media.IS_PENDING, 1);
                    Uri finaluri = resolver.insert(vidCollection, values);
                    values.clear();
                    values.put(MediaStore.Images.Media.IS_PENDING, 0);
                    resolver.update(vidCollection, values, null, null);
                    return finaluri;
                }
            } else {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, media.getPath());
                return context.getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            }
        } else {
            return null;
        }
    }
    public void copyFile(String inputPath, String outputPath, Context context) {

        InputStream in = null;
        Uri outputFile, outputFolderUri;

        try {
            String parentPath = outputPath;
            Log.d("Parent path", parentPath);
            String[] parse = parentPath.split("/");
            var dirList = Arrays.stream(parse).skip(4).collect(Collectors.toList());

            String relativePath="";
            for(var item: dirList){
                relativePath += item + "/";
                Log.d("Item", item);
            }
            Log.d("relativePath", relativePath);
            var type = AccessMediaFile.getMediaWithPath(inputPath).getType();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                //outputFolderUri = getUriOfFolder(context, outputPath);
                outputFile = insertMediaToMediaStore(context, inputPath, relativePath);

                try (InputStream inputStream = new FileInputStream(inputPath)) { //input stream

                    OutputStream out = context.getContentResolver().openOutputStream(outputFile); //output stream

                    byte[] buf = new byte[8096];
                    int len;
                    while ((len = inputStream.read(buf)) > 0) {
                        out.write(buf, 0, len); //write input file data to output file
                    }

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

    public void moveFileMultiple(ArrayList<Media> medias, ActivityResultLauncher<IntentSenderRequest> launcher, String outputPath, Context context) {
        if (medias.size() <= 0) return;
        if (medias.size() <= 1){
            moveFile(medias.get(0).getPath(), launcher, outputPath, context);
        }
        InputStream in = null;
        Uri outputFile, outputFolderUri;

        try {
            String parentPath = outputPath;
            Log.d("Parent path", parentPath);
            String[] parse = parentPath.split("/");
            var dirList = Arrays.stream(parse).skip(4).collect(Collectors.toList());

            String relativePath="";
            for(var item: dirList){
                relativePath += item + "/";
                Log.d("Item", item);
            }
            Log.d("relativePath", relativePath);
            for(var m : medias) {
                String inputPath = m.getPath();
                var type = AccessMediaFile.getMediaWithPath(inputPath).getType();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    //outputFolderUri = getUriOfFolder(context, outputPath);
                    outputFile = insertMediaToMediaStore(context, inputPath, relativePath);

                    try (InputStream inputStream = new FileInputStream(inputPath)) { //input stream

                        OutputStream out = context.getContentResolver().openOutputStream(outputFile); //output stream

                        byte[] buf = new byte[8096];
                        int len;
                        while ((len = inputStream.read(buf)) > 0) {
                            out.write(buf, 0, len); //write input file data to output file
                        }

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

    public void copyFileMultiple(ArrayList<Media> medias, String outputPath, Context context) {
        if (medias.size() <= 0) return;
        if (medias.size() <= 1){
            copyFile(medias.get(0).getPath(), outputPath, context);
            return;
        }
        InputStream in = null;
        Uri outputFile, outputFolderUri;

        try {
            String parentPath = outputPath;
            Log.d("Parent path", parentPath);
            String[] parse = parentPath.split("/");
            var dirList = Arrays.stream(parse).skip(4).collect(Collectors.toList());

            String relativePath="";
            for(var item: dirList){
                relativePath += item + "/";
                Log.d("Item", item);
            }
            Log.d("relativePath", relativePath);
            for(var m : medias) {
                String inputPath = m.getPath();
                var type = AccessMediaFile.getMediaWithPath(inputPath).getType();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    //outputFolderUri = getUriOfFolder(context, outputPath);
                    outputFile = insertMediaToMediaStore(context, inputPath, relativePath);

                    try (InputStream inputStream = new FileInputStream(inputPath)) { //input stream

                        OutputStream out = context.getContentResolver().openOutputStream(outputFile); //output stream

                        byte[] buf = new byte[8096];
                        int len;
                        while ((len = inputStream.read(buf)) > 0) {
                            out.write(buf, 0, len); //write input file data to output file
                        }

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

    public Uri getUri(String path, int type, Context context) {
        ContentResolver contentResolver = context.getContentResolver();
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

    public int delete(ActivityResultLauncher<IntentSenderRequest> launcher, String path, Context context){
        Media media = AccessMediaFile.getMediaWithPath(path);
        ContentResolver contentResolver = context.getContentResolver();
        boolean returnVal = false;
        Uri uri = Uri.EMPTY;
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
                int row = contentResolver.delete(uri, null, null);
                return row;
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

    public int deleteMultiple(ActivityResultLauncher<IntentSenderRequest> launcher, ArrayList<Media> medias, Context context){
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


    public void delete1(ActivityResultLauncher<IntentSenderRequest> launcher, String path, Context context) {

        String[] parse = path.substring(0, path.lastIndexOf("/")).split("/");
        var dirList = Arrays.stream(parse).skip(4).collect(Collectors.toList());

        Media media = AccessMediaFile.getMediaWithPath(path);
        String relativePath = "";
        for (var item : dirList) {
            relativePath += item + "/";
            Log.d("Item", item);
        }

        Uri uri = insertMediaToMediaStore(context, path, relativePath);
        ContentResolver contentResolver = context.getContentResolver();
        PendingIntent pendingIntent = null;


        try {
            //delete object using resolver

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

                ArrayList<Uri> collection = new ArrayList<>();
                collection.add(uri);
                pendingIntent = MediaStore.createDeleteRequest(contentResolver, collection);

            } else
                try {
                    MediaScannerConnection.scanFile(context, new String[] { path },
                            new String[]{media.getMimeType()}, (path1, uri1) -> context.getContentResolver()
                                    .delete(uri1, null, null));
                } catch (Exception e) {
                    e.printStackTrace();
                }

        } catch (Exception e) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                //if exception is recoverable then again send delete request using intent
                if (e instanceof RecoverableSecurityException) {
                    RecoverableSecurityException exception = (RecoverableSecurityException) e;
                    pendingIntent = exception.getUserAction().getActionIntent();
                }
            }


        } finally {
            if (pendingIntent != null) {
                IntentSender sender = pendingIntent.getIntentSender();
                IntentSenderRequest request = new IntentSenderRequest.Builder(sender).build();
                launcher.launch(request);
            }
        }
        // AccessMediaFile.removeMediaFromAllMedia(path);
    }

    public void renameFile(String name, int type, String path, Context context, ActivityResultLauncher<IntentSenderRequest> launcher) {
        ContentValues values = new ContentValues();
        ContentResolver resolver = context.getContentResolver();
        Uri uri = getUri(path, type, context);
        try {
            values.put(MediaStore.Images.Media.IS_PENDING, 1);
            resolver.update(uri, values, null, null);
            values.clear();
            if (type == 1) {
                values.put(MediaStore.Images.Media.DISPLAY_NAME, name);
            } else {
                values.put(MediaStore.Video.Media.DISPLAY_NAME, name);
            }
            values.put(MediaStore.Images.Media.IS_PENDING, 0);
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

    public  void updateInfoFile(String password) throws FileNotFoundException {
        File info = new File(Environment.getExternalStorageDirectory()+File.separator+".secret"+ File.separator+"info.txt");
        try{
            BufferedReader br = new BufferedReader(new FileReader(info));
            String info_password = br.readLine();
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
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
