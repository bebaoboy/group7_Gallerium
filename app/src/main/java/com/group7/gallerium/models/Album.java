package com.group7.gallerium.models;

import android.media.Image;

import java.util.ArrayList;

public class Album {
    String path;
    ArrayList<Media> listMedia;
    String dateCreated;
    String name;

    String memoryTitle;
    String memoryContent;
    String memoryDate;

    public String getMemoryTitle() {
        return memoryTitle;
    }

    public void setMemoryTitle(String memoryTitle) {
        this.memoryTitle = memoryTitle;
    }

    public String getMemoryContent() {
        return memoryContent;
    }

    public void setMemoryContent(String memoryContent) {
        this.memoryContent = memoryContent;
    }

    public String getMemoryDate() {
        return memoryDate;
    }

    public void setMemoryDate(String memoryDate) {
        this.memoryDate = memoryDate;
    }

    public Media getAvatar() {
        return avatar;
    }

    public void setAvatar(Media avatar) {
        this.avatar = avatar;
    }

    Media avatar;

    public Album(Media albumImage, String name) {
        this.avatar = albumImage;
        this.name = name;
        this.listMedia = new ArrayList<>();
    }

    public Album(String name) {
        this.name = name;
        this.listMedia = new ArrayList<>();
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public ArrayList<Media> getListMedia() {
        return listMedia;
    }

    public void setListMedia(ArrayList<Media> listMedia) {
        this.listMedia = listMedia;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addMedia(Media media){
        listMedia.add(media);
    }
}
