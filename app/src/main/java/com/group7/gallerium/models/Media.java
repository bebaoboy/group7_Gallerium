package com.group7.gallerium.models;

import java.util.Locale;

public class Media {
    private String path;

    private int type;
    private String mimeType; // 1 is image 3 is video
    private String thumbnail;
    private String dateTaken;

    private long duration;
    private long size;

    private String title;
    private int width;
    private int height;

    public Media() {

    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumb) {
        this. thumbnail = thumb;
    }

    public Media(String path, String thumb, String type) {
        this.path = path;
        this.thumbnail = thumb;
    }

    public String getDateTaken() {
        return dateTaken;
    }

    public void setDateTaken(String dateTaken) {
        this.dateTaken = dateTaken;
    }

    public void setType(int t) {
        this.type = t;
    }

    public int getType() {
        return this.type;
    }

    public void setMimeType(String t) {
        this.mimeType = t;
    }

    public String getMimeType() {
        return this.mimeType;
    }
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    public void setWidth(int t) {
        this.width = t;
    }

    public int getWidth() {
        return this.width;
    }
    public void setHeight(int t) {
        this.height = t;
    }

    public int getHeight() {
        return this.height;
    }

    public void setDuration(long videoLength) {
        duration = videoLength;
    }

    public String getDuration() {
        String newDuration;
        long duration = this.duration / 1000;
        var hours = duration / 3600;
        var minutes = (duration % 3600) / 60;
        var seconds = duration % 60;

        newDuration = String.format(Locale.CHINA,"%02d:%02d:%02d", hours, minutes, seconds);
        return newDuration;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getSize() {
        String format = "%1$.2f ";
        if (size < 1024) {
            return String.format(Locale.CHINA, format + "B", (double)size);
        }
        if (size < Math.pow(1024, 2)) {
            return String.format(Locale.CHINA, format + "KB", (double)size / Math.pow(1024, 1));
        }
        if (size < Math.pow(1024, 3)) {
            return String.format(Locale.CHINA, format + "MB", (double)size / Math.pow(1024, 2));
        }
        return String.format(Locale.CHINA, format + "GB", (double)size / Math.pow(1024, 3));
    }
}
