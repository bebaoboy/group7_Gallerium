package com.group7.gallerium.adapters;

import android.content.Context;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;
import com.group7.gallerium.R;
import com.group7.gallerium.utilities.AccessMediaFile;

import java.util.ArrayList;

public class SlideAdapter extends PagerAdapter {
    ArrayList<String> paths;
    Context context;
    private PhotoView img;
    private ImageView img2;
    private ImageView playButton;
    private VideoView videoThumbnail;
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
        if (AccessMediaFile.getAllMedia().get(position).getType() == 1) {
            View view = LayoutInflater.from(context).inflate(R.layout.view_photo_item, container, false);
            img = view.findViewById(R.id.imageView);
            img.setMaximumScale(10);
            Glide.with(context).load(path).into(img);
            ViewPager vp = (ViewPager) container;
            vp.addView(view, 0);
            return view;

        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.watch_video_item, container, false);
            img2 =  view.findViewById(R.id.preview_thumbnail);
            Glide.with(context).load(path).into(img2);

            videoThumbnail = view.findViewById(R.id.videoView);
            playButton = view.findViewById(R.id.play_video_button);

            playButton.setOnClickListener((playbutton)->{
                playVideo(path);
            });
            return view;
        }
    }

    //TODO add logic to play video
    void playVideo(String path){
        // ẩn các image view tên img ở trên
        // làm hiện các videoThumbnail ở trên
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        ViewPager viewPager = (ViewPager) container;
        View view = (View) object;
        viewPager.removeView(view);
    }
}
