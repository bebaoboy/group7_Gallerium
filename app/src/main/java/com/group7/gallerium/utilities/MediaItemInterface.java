package com.group7.gallerium.utilities;

import android.widget.Button;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import com.group7.gallerium.models.Media;

public interface MediaItemInterface {
    void showActionBar(boolean trigger);
    void showVideoPlayer(VideoView videoView, ImageView img2, ImageView btn, TextView duration, Media m);
    void setVideoView(VideoView videoView);
    void setImageViewAndButton(ImageView img, ImageView playButton);
}
