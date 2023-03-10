package com.group7.gallerium.models;

import java.util.ArrayList;
import java.util.List;

public class MediaCategory {

    private String nameCategory;
    private ArrayList<Media> listMedia;

    public String getNameCategory() {
        return nameCategory;
    }

    public void setNameCategory(String nameCategory) {
        this.nameCategory = nameCategory;
    }

    public ArrayList<Media> getList() {
        return listMedia;
    }

    public void setList(ArrayList<Media> listMedia) {
        this.listMedia = listMedia;
    }

    public void addMediaToList(Media media){this.listMedia.add(media);}

    public MediaCategory(String nameCategory, ArrayList<Media> listMedia) {
        this.nameCategory = nameCategory;
        this.listMedia = listMedia;
    }

    public MediaCategory(ArrayList<Media> listMedia) {
        this.listMedia = listMedia;
    }
}
