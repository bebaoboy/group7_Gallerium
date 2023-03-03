package com.group7.gallerium.models;

public class Media {
    private String path;

    private int type;
    private String mimeType; // 1 is image 3 is video
    private String thumbnail;
    private String dateTaken;



    private String title;

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
}
