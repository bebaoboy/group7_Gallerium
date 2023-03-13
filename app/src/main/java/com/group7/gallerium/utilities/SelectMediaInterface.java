package com.group7.gallerium.utilities;

import com.group7.gallerium.models.Media;

import java.util.ArrayList;

public interface SelectMediaInterface {

    void showAllSelect();

    void addToSelectedList(Media media);

    ArrayList<Media> getSelectedList();

    void deleteFromSelectedList(Media media);

    void moveMedia(String albumPath);
}
