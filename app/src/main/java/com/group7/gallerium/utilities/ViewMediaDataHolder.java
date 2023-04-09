package com.group7.gallerium.utilities;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class ViewMediaDataHolder {
    static ArrayList<String> currentMediaList = new ArrayList<>();

    public static void setList(@NonNull ArrayList<String> medias) {
        currentMediaList = medias;
    }

    @NonNull
    public static ArrayList<String> getList() {
        var newList = new ArrayList<>(currentMediaList);
        currentMediaList.clear();
        return newList;
    }
}
