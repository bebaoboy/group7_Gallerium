package com.group7.gallerium.models;

import android.util.Log;

import org.json.JSONObject;

import java.text.SimpleDateFormat;

public class AlbumCustomContent {
    String title;
    String albumPath;
    String content;
    Long date;

    SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd-MM-yyyy");

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAlbumPath() {
        return albumPath;
    }

    public void setAlbumPath(String albumPath) {
        this.albumPath = albumPath;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDate() {
        return dateFormat.format(date);
    }

    public void setDate(Long date) {
        this.date = date;
    }

    public AlbumCustomContent(String title, String albumPath, String content, Long date) {
        this.title = title;
        this.albumPath = albumPath;
        this.content = content;
        this.date = date;
    }

    public AlbumCustomContent(JSONObject object){
        try {
            this.albumPath = object.getString("path");
            this.title = object.getString("title");
            this.date = object.getLong("date");
            this.content = object.getString("content");
        }catch (Exception e){
            Log.d("json error", e.getMessage());
        }
    }
}
