package com.group7.gallerium.utilities;

import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;

public interface MediaItemInterface {
    void showActionBar(boolean trigger);
    void showVideoPlayer(VideoView videoView, ImageView img2, ImageView btn);
}