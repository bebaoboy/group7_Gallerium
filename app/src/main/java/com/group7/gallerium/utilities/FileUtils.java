package com.group7.gallerium.utilities;

import static com.google.android.material.internal.ContextUtils.getActivity;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

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
        //OutputStream out = null;
        Uri outputFile, inputFile, mediaSource;

        try {

            //create output directory if it doesn't exist
            File dir = new File (outputPath);
            if (!dir.exists())
            {
                dir.mkdirs();
            }

//
//            in = new FileInputStream(inputPath);
//            out = new FileOutputStream(outputPath + File.separator + inputFileName);
//
//            byte[] buffer = new byte[1024];
//            int read;
//            while ((read = in.read(buffer)) != -1) {
//                out.write(buffer, 0, read);
//            }
//            in.close();
//            in = null;
//
//            // write the output file
//            out.flush();
//            out.close();
//            out = null;

//            original.delete();
//            // delete the original file
//            if(original.exists()){
//                original.getCanonicalFile().delete();
//                if(original.exists()){
//                    context.getApplicationContext().deleteFile(original.getName());
//                    AccessMediaFile.removeMediaFromAllMedia(inputPath);
//                    AccessMediaFile.updateNewMedia();
//                }
//            }


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
    public void copyFile(String inputPath, String inputFile, String outputPath) {

        InputStream in = null;
        OutputStream out = null;
        try {

            //create output directory if it doesn't exist
            File dir = new File (outputPath);
            if (!dir.exists())
            {
                dir.mkdirs();
            }


            in = new FileInputStream(inputPath + inputFile);
            out = new FileOutputStream(outputPath + inputFile);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;

            // write the output file (You have now copied the file)
            out.flush();
            out.close();
            out = null;

        }  catch (FileNotFoundException fnfe1) {
            Log.e("tag", fnfe1.getMessage());
        }
        catch (Exception e) {
            Log.e("tag", e.getMessage());
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
