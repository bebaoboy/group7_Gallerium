package com.group7.gallerium.utilities;

import android.content.Context;

import com.group7.gallerium.models.Album;
import com.group7.gallerium.models.AlbumCategory;
import com.group7.gallerium.models.Media;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FilterAlbum {
    Context context;
    static ArrayList<Media> listMedia;
    static ArrayList<Album> albumList = new ArrayList<>();

    public FilterAlbum(Context context) {
        AccessMediaFile.refreshAllMedia();
        listMedia = AccessMediaFile.getAllMedia(context);
        this.context = context;
    }

    public static ArrayList<Album> getAllAlbum(boolean addCustomChoice) {
        if (albumList.size() == 0) {
            List<String> paths = new ArrayList<>();
            ArrayList<Album> albums = new ArrayList<>();

            if (addCustomChoice) {
                Album video = new Album("Video");
                Album image = new Album("Ảnh");
                for (var media : listMedia) {
                    if (media.getType() == 1) {
                        image.addMedia(media);
                    } else if (media.getType() == 3) {
                        video.addMedia(media);
                    }
                }
                if (image.getListMedia().size() > 0) {
                    image.setAvatar(image.getListMedia().get(0));
                }
                if (video.getListMedia().size() > 0) {
                    video.setAvatar(video.getListMedia().get(0));
                }
                image.setPath("/internal/DCIM/Ảnh");
                video.setPath("/internal/DCIM/Video");
                paths.add(image.getPath());
                paths.add(video.getPath());
                albums.add(image);
                albums.add(video);
            }

            for (int i = 0; i < listMedia.size(); i++) {
                String[] subDirectories = listMedia.get(i).getPath().split("/");
                String folderPath = listMedia.get(i).getPath().substring(0, listMedia.get(i).getPath().lastIndexOf("/"));
                String name = subDirectories[subDirectories.length - 2];
                if (!paths.contains(folderPath)) {
                    paths.add(folderPath);
                    Album album = new Album(listMedia.get(i), name);
                    album.setPath(folderPath);
                    album.addMedia(listMedia.get(i));
                    albums.add(album);
                } else {
                    albums.get(paths.indexOf(folderPath)).addMedia(listMedia.get(i));
                }
            }
            albumList.addAll(albums);
            return albumList;
        } else {
            return albumList;
        }
    }
}
