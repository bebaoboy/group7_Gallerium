package com.group7.gallerium.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.github.chrisbanes.photoview.PhotoView;
import com.group7.gallerium.R;
import com.group7.gallerium.models.Media;
import com.group7.gallerium.utilities.AccessMediaFile;
import com.bumptech.glide.Glide;
import com.group7.gallerium.utilities.MediaItemInterface;

import java.util.ArrayList;

public class SlideAdapter extends PagerAdapter {
    ArrayList<String> paths;
    Context context;
    private PhotoView img;

    private int viewType;

    private boolean trigger = false;

    private MediaItemInterface mediaItemInterface;

    public void setData(ArrayList<String> paths) {
        this.paths = paths;
        notifyDataSetChanged();
    }

    public void removePath(String path){
        paths.remove(path);
    }

    public SlideAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        if (paths != null) return paths.size();
        return 0;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }


    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        String path = paths.get(position);
        Log.d("gallerium", path + " " + position);
        Media m;
        // TODO: fix wrong video path
        if(viewType == 2) {
            m = AccessMediaFile.getMediaWithPath(path);
        }else{
            m = createMediaFromFile(path);
        }
        mediaItemInterface.showActionBar(true);
        trigger = true;
        View view;
        if(m != null) {
            if (m.getType() == 1) {
                view = LayoutInflater.from(context).inflate(R.layout.view_photo_item, container, false);
                img = view.findViewById(R.id.imageView);
                img.setMaximumScale(30);
                Glide.with(context).load(path)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(img);

                img.setOnClickListener((view1) -> {
                    mediaItemInterface.showActionBar(trigger);
                    trigger = !trigger;
                });
                ViewPager vp = (ViewPager) container;
                vp.addView(view, 0);

            } else {
                view = LayoutInflater.from(context).inflate(R.layout.watch_video_item, container, false);
                ImageView img2;
                ImageView playButton;
                img2 = view.findViewById(R.id.preview_thumbnail);
                Glide.with(context).load("file://" + m.getThumbnail())
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(img2);
                img2.setOnClickListener((view1) -> {
                    mediaItemInterface.showActionBar(trigger);
                    trigger = !trigger;
                });

                VideoView video = view.findViewById(R.id.videoView);
                playButton = view.findViewById(R.id.play_video_button);
                TextView duration;
                duration = view.findViewById(R.id.videoLength);
                duration.setText(m.getDuration());

                video.setOnClickListener((view1) -> {
                    mediaItemInterface.showActionBar(trigger);
                    trigger = !trigger;
                });

                playButton.setOnClickListener((playbutton) -> {
                    // playVideo(video, img2, playButton);
                    img2.setVisibility(View.GONE);
                    playButton.setVisibility(View.GONE);
                    mediaItemInterface.showVideoPlayer(video, img2, playButton, duration, m);
                    mediaItemInterface.showActionBar(false);
                    trigger = false;
                });
                mediaItemInterface.setVideoView(video);
                mediaItemInterface.setImageViewAndButton(img2, playButton);

                ViewPager vp = (ViewPager) container;
                vp.addView(view, 0);
            }
            return view;
        }
        return null;
    }

    String getMimeType(String path){
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(path);
        if (extension.isBlank()) {
            extension = path.substring(path.lastIndexOf(".") + 1);
        }
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        Log.d("mime-type", type
        );
        return type;
    }

    int getType(String mimeType){
        int mediaType = -1;
        if(mimeType.startsWith("image")){ mediaType = 1;}
        else mediaType = 3;
        return mediaType;
    }

    private Media createMediaFromFile(String path) {
        String[] dirs = path.split("/");
        Media media = new Media();
        String mimeType = getMimeType(path);
        int mediaType = getType(mimeType);
        media.setMimeType(mimeType);
        media.setType(mediaType);
        media.setPath(path);
        media.setTitle(dirs[dirs.length-1]);
        media.setThumbnail(path);

        return media;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        ViewPager viewPager = (ViewPager) container;
        View view = (View) object;
        viewPager.removeView(view);
    }

    public void setInterface(MediaItemInterface itemInterface) {
        this.mediaItemInterface = itemInterface;
    }

    public void setViewType(int viewType) {
        this.viewType = viewType;
    }
}
