package com.group7.gallerium.utilities;

import androidx.annotation.NonNull;

import com.group7.gallerium.models.Media;

import java.util.ArrayList;

public interface SelectMediaInterface {

    void showAllSelect();

    void addToSelectedList(@NonNull Media media);

    @NonNull
    ArrayList<Media> getSelectedList();

    void deleteFromSelectedList(@NonNull Media media);

    void moveMedia(@NonNull String albumPath);
}
