package com.group7.gallerium.models;

public class Media {
    private String type;
    private String path;
    private String thumbnail;
    private String dateTaken;
    private String placeTaken;

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
        this.type = type;
    }

    public String getDateTaken() {
        return dateTaken;
    }

    public void setDateTaken(String dateTaken) {
        this.dateTaken = dateTaken;
    }

    public void setType(String type) {
        this.type = type;
    }
    public String getType(){
        return this.type;
    }
}
