package com.group7.gallerium.utilities;

import static com.google.android.material.internal.ContextUtils.getActivity;

import android.app.PendingIntent;
import android.app.RecoverableSecurityException;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.IntentSender;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.core.content.FileProvider;

import com.group7.gallerium.fragments.MediaFragment;

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
import java.util.Objects;

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

    public void moveFile(String inputPath, String inputFileName, String outputPath, Context context) {

        InputStream in = null;
        Uri outputFile, inputFile, mediaSource;

        try {
            String parentPath = inputPath.substring(0, inputPath.lastIndexOf("/"));
            Log.d("Parent path", parentPath);

            var type = AccessMediaFile.getMediaWithPath(inputPath).getType();
            inputFile = getUri(context, inputPath, type);
            outputFile = getUri(context, outputPath, type);
            mediaSource = getUri(context, parentPath, type);


            try (InputStream inputStream = new FileInputStream(inputPath)) { //input stream

                OutputStream out = context.getContentResolver().openOutputStream(outputFile); //output stream

                byte[] buf = new byte[1024];
                int len;
                while ((len = inputStream.read(buf)) > 0) {
                    out.write(buf, 0, len); //write input file data to output file
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
           // Toast.makeText(context, "Move file " + newPath, Toast.LENGTH_SHORT).show();
        }

//        catch (FileNotFoundException fnfe1) {
//            Log.e("tag", fnfe1.getMessage());
//        }
        catch (Exception e) {
            Log.e("tag", e.getMessage());
        }

    }

    public Uri getUri(Context context, String inputPath, int mediaType) {
        ContentValues values = new ContentValues();
        if (mediaType == 1) {
            values.put(MediaStore.Images.Media.DATA, inputPath);
            return context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        } else {
            values.put(MediaStore.Video.Media.DATA, inputPath);
            return context.getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
        }

    }
    public void copyFile(String inputPath, String inputFileName, String outputPath, Context context) {

        InputStream in = null;
        Uri outputFile, inputFile, mediaSource;

        try {
            String parentPath = inputPath.substring(0, inputPath.lastIndexOf("/"));
            Log.d("Parent path", parentPath);

            var type = AccessMediaFile.getMediaWithPath(inputPath).getType();
            inputFile = getUri(context, inputPath, type);
            outputFile = getUri(context, outputPath, type);
            mediaSource = getUri(context, parentPath, type);


            Log.d("parent uri", Uri.parse("content:" + File.separator + inputPath).toString());
            try (InputStream inputStream = new FileInputStream(inputPath)) { //input stream

                OutputStream out = context.getContentResolver().openOutputStream(outputFile); //output stream

                byte[] buf = new byte[1024];
                int len;
                while ((len = inputStream.read(buf)) > 0) {
                    out.write(buf, 0, len); //write input file data to output file
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            // Toast.makeText(context, "Move file " + newPath, Toast.LENGTH_SHORT).show();
        }

//        catch (FileNotFoundException fnfe1) {
//            Log.e("tag", fnfe1.getMessage());
//        }
        catch (Exception e) {
            Log.e("tag", e.getMessage());
        }
    }

    /**
     * Delete file.
     * <p>
     * If {@link ContentResolver} failed to delete the file, use trick,
     * SDK version is >= 29(Q)? use {@link SecurityException} and again request for delete.
     * SDK version is >= 30(R)? use {@link //MediaStore#createDeleteRequest(ContentResolver, Collection)}.
     */
    public void delete(ActivityResultLauncher<IntentSenderRequest> launcher, String path, Context context) {

        Uri uri = getUri(context, path, AccessMediaFile.getMediaWithPath(path).getType());
        ContentResolver contentResolver = context.getContentResolver();

        try {
            //delete object using resolver
            contentResolver.delete(uri, null, null);

        } catch (SecurityException e) {

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
        }
        AccessMediaFile.removeMediaFromAllMedia(path);
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
