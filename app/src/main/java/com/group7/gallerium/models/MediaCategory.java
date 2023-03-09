package com.group7.gallerium.models;

import java.util.List;

public class MediaCategory {

    private String nameCategory;
    private List<Media> listMedia;

    public String getNameCategory() {
        return nameCategory;
    }

    public void setNameCategory(String nameCategory) {
        this.nameCategory = nameCategory;
    }

    public List<Media> getList() {
        return listMedia;
    }

    public void setList(List<Media> listMedia) {
        this.listMedia = listMedia;
    }

    public void addMediaToList(Media media){this.listMedia.add(media);}

    public MediaCategory(String nameCategory, List<Media> listMedia) {
        this.nameCategory = nameCategory;
        this.listMedia = listMedia;
    }

    public MediaCategory(List<Media> listMedia) {
        this.listMedia = listMedia;
    }
}
