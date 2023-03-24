package com.group7.gallerium.utilities;

import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;

import com.group7.gallerium.models.Media;

public interface MediaItemInterface {
    void showActionBar(boolean trigger);
    void showVideoPlayer(@NonNull VideoView videoView, @NonNull ImageView img2, @NonNull ImageView btn, @NonNull TextView duration, @NonNull Media m);
    void setVideoView(@NonNull VideoView videoView);
    void setImageViewAndButton(@NonNull ImageView img, @NonNull ImageView playButton);
}
