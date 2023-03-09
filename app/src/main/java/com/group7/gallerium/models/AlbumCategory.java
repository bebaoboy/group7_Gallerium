package com.group7.gallerium.models;

import java.util.List;

public class AlbumCategory {

    private String nameCategory;
    private List<Album> listAlbum;

    public String getNameCategory() {
        return nameCategory;
    }

    public void setNameCategory(String nameCategory) {
        this.nameCategory = nameCategory;
    }

    public List<Album> getList() {
        return listAlbum;
    }

    public void setList(List<Album> listAlbum) {
        this.listAlbum= listAlbum;
    }

    public void addAlbumToList(Album album){this.listAlbum.add(album);}

    public AlbumCategory(String nameCategory, List<Album> listAlbum) {
        this.nameCategory = nameCategory;
        this.listAlbum = listAlbum;
    }

    public AlbumCategory() {
    }
}
