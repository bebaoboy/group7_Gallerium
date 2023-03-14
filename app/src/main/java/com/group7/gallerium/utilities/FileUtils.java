package com.group7.gallerium.utilities;

import android.os.Environment;
import android.util.Log;

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

    public void moveFile(String inputPath, String inputFileName, String outputPath) {

        InputStream in = null;
        OutputStream out = null;
        File original = new File(inputPath);
        try {

            //create output directory if it doesn't exist
            File dir = new File (outputPath);
            if (!dir.exists())
            {
                dir.mkdirs();
            }


            in = new FileInputStream(inputPath);
            out = new FileOutputStream(outputPath + File.separator + inputFileName);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;

            // write the output file
            out.flush();
            out.close();
            out = null;

            // delete the original file
            if(original.exists()){
                if(original.delete()){
                    AccessMediaFile.removeMediaFromAllMedia(inputPath);
                }
            }
        }

        catch (FileNotFoundException fnfe1) {
            Log.e("tag", fnfe1.getMessage());
        }
        catch (Exception e) {
            Log.e("tag", e.getMessage());
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
