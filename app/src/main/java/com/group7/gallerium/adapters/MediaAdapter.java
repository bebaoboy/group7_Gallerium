package com.group7.gallerium.adapters;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.group7.gallerium.R;
import com.group7.gallerium.activities.ViewPhoto;
import com.group7.gallerium.activities.WatchVideo;
import com.group7.gallerium.models.Category;
import com.group7.gallerium.models.Media;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MediaAdapter extends RecyclerView.Adapter<MediaAdapter.PhotoViewHolder> {

    public static final int PHOTOVIEW = 0, VIDEOVIEW = 1;
    enum Type{video, photo};
    private List<Media> listMedia;
    private Context context;
    private List<Category> listCategory;
    private Intent intent;
    private ArrayList<String> listPath ;
    private ArrayList<String> listThumb ;

    public MediaAdapter(){}

    public MediaAdapter(Context context){
        this.context = context;
    }

    public void setListImages(ArrayList<Media> media){
        this.listMedia = media;
        notifyDataSetChanged();
    }

    public  void setListCategory(ArrayList<Category> categories){
        this.listCategory = categories;
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        return new PhotoViewHolder(layoutInflater.inflate(R.layout.photo_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        Media media = listMedia.get(position);
        if (media == null) {
            return;
        }
        Glide.with(context).load(media.getThumbnail()).into(holder.image);

//
//        if (holder instanceof PhotoViewHolder) {
//            Log.d("photo-type", "1");
//            PhotoViewHolder photoViewHolder = (PhotoViewHolder) holder;
//            // Glide.with(context).load(media.getThumb()).into(photoViewHolder.image);
//
//        } else {
//            Log.d("video-type", "2");
//            VideoViewHolder videoViewHolder = (VideoViewHolder) holder;
//
//            Glide.with(context).load(media.getThumbnail()).into(videoViewHolder.videoThumbnail);
//            videoViewHolder.videoThumbnail.setOnClickListener((view -> {
//                intent = new Intent(context, WatchVideo.class);
//                MyAsyncTask myAsyncTask = new MyAsyncTask();
//                myAsyncTask.setPos(position);
//                myAsyncTask.execute();
//            }));
//        }


    }

    @Override
    public int getItemCount() {
        if(listMedia.size() != 0){
            return listMedia.size();
        }
        return 0;
    }
//
//    @Override
//    public int getItemViewType(int position) {
//        if(listMedia.get(position).getType().equals("photo")){
//            return PHOTOVIEW;
//        }else if(listMedia.get(position).getType().equals("video")){
//            return VIDEOVIEW;
//        }
//        return -1;
//    }

//
//    class VideoViewHolder extends RecyclerView.ViewHolder  {
//        private ImageView videoThumbnail;
//        private ImageView fav_icon;
//
//        private VideoViewHolder(View itemView) {
//            super(itemView);
//            videoThumbnail = itemView.findViewById(R.id.video_item);
//        }
//    }

    class PhotoViewHolder extends RecyclerView.ViewHolder  {
        private ImageView image;
        private ImageView fav_icon;
        private PhotoViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.photoItem);
            fav_icon = itemView.findViewById(R.id.fav_icon);
        }
    }


    public class MyAsyncTask extends AsyncTask<Void, Integer, Void> {
        public int pos;

        public void setPos(int pos) {
            this.pos = pos;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            listPath = new ArrayList<>();
            listThumb = new ArrayList<>();
            for(int i = 0;i<listCategory.size();i++) {
                List<Media> listCat = listCategory.get(i).getList();
                for (int j = 0; j < listCat.size(); j++) {
                    listPath.add(listCat.get(j).getPath());
                    listThumb.add(listCat.get(j).getThumbnail());
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            intent.putStringArrayListExtra("data_list_path", listPath);
            intent.putStringArrayListExtra("data_list_thumb", listThumb);
            intent.putExtra("pos", listPath.indexOf(listMedia.get(pos).getPath()));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }
}
