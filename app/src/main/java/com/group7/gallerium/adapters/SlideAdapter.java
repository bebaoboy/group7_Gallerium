package com.group7.gallerium.adapters;

import android.content.Context;
import android.media.Image;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;
import com.group7.gallerium.R;
import com.group7.gallerium.models.Media;
import com.group7.gallerium.utilities.AccessMediaFile;
import com.group7.gallerium.utilities.MediaItemInterface;

import java.util.ArrayList;

public class SlideAdapter extends PagerAdapter {
    ArrayList<String> paths;
    Context context;
    private PhotoView img;

    private TextView duration;
    private boolean trigger = false;

    private MediaItemInterface mediaItemInterface;

    private boolean flag = false;

    public void setData(ArrayList<String> paths) {
        this.paths = paths;
        notifyDataSetChanged();
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


    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        String path = paths.get(position);
        Log.d("gallerium", path + " " + position);
        Media m = AccessMediaFile.getAllMedia().get(position);
        mediaItemInterface.showActionBar(true);
        trigger = true;
        if (m.getType() == 1) {
            View view = LayoutInflater.from(context).inflate(R.layout.view_photo_item, container, false);
            img = view.findViewById(R.id.imageView);
            img.setMaximumScale(10);
            Glide.with(context).load(path).into(img);

            img.setOnClickListener((view1)->{
                mediaItemInterface.showActionBar(trigger);
                trigger = !trigger;
            });
            ViewPager vp = (ViewPager) container;
            vp.addView(view, 0);
            return view;

        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.watch_video_item, container, false);
            ImageView img2;
            ImageView playButton;
            img2 =  view.findViewById(R.id.preview_thumbnail);
            Glide.with(context).load("file://" + m.getThumbnail()).into(img2);
//            img2.setOnClickListener((view1)->{
//                mediaItemInterface.showActionBar(trigger);
//                trigger = !trigger;
//            });


            VideoView video = view.findViewById(R.id.videoView);
            playButton = view.findViewById(R.id.play_video_button);
            duration = view.findViewById(R.id.videoLength);
            duration.setText(m.getDuration());

            playButton.setOnClickListener((playbutton)->{
                playVideo(video, img2, playButton);
                mediaItemInterface.showActionBar(false);
                trigger = false;
            });
            mediaItemInterface.setVideoView(video);
            mediaItemInterface.setImageViewAndButton(img2, playButton);

            ViewPager vp = (ViewPager) container;
            vp.addView(view, 0);
            return view;
        }
    }

    //TODO add logic to play video
    void playVideo(VideoView video, ImageView img2, ImageView playButton){
        // ẩn các image view tên img ở trên
        // làm hiện các videoThumbnail ở trên
            img2.setVisibility(View.GONE);
            video.setVisibility(View.VISIBLE);
            playButton.setVisibility(View.GONE);
            mediaItemInterface.showVideoPlayer(video, img2, playButton);
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
}
