package com.group7.gallerium.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.group7.gallerium.R;
import com.group7.gallerium.activities.ViewMedia;
import com.group7.gallerium.models.Category;
import com.group7.gallerium.models.Media;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MediaAdapter extends RecyclerView.Adapter<MediaAdapter.MediaViewHolder> {

    private List<Media> listMedia;
    private Context context;
    private List<Category> listCategory;
    private Intent intent;
    private ArrayList<String> listPath;

    public MediaAdapter(Context context) {
        this.context = context;
    }

    public void setListImages(ArrayList<Media> media) {
        this.listMedia = media;

//        for (Media media1: media
//             ) {
//            Log.d("Thumbnail-d", media1.getThumbnail());
//        }
        notifyDataSetChanged();
    }

    public void setListCategory(ArrayList<Category> categories) {
        this.listCategory = categories;
    }

    @NonNull
    @Override
    public MediaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

        return new MediaViewHolder(layoutInflater.inflate(R.layout.media_item, parent, false));
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @Override
    public void onBindViewHolder(@NonNull MediaViewHolder holder, int position) {
        Media media = listMedia.get(position);
        if (media == null) {
            return;
        }
        // Log.d("gallerium", media.getMimeType());
        String[] gifList = {"image/gif"};
        if (Arrays.asList(gifList).contains(media.getMimeType())) {
            Glide.with(context).asGif().load("file://" + media.getThumbnail())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.image);
        }
       else {
            Glide.with(context).load("file://" + media.getThumbnail())
                    //.dontAnimate()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.image);
        }


        if(media.getType() == 3) holder.play_icon.setVisibility(View.VISIBLE);

        holder.image.setOnClickListener((view -> {
            intent = new Intent(context, ViewMedia.class);
            navAsyncTask navAsyncTask = new navAsyncTask();
            navAsyncTask.setPos(position);
            navAsyncTask.execute();
        }));

    }

    @Override
    public int getItemCount() {
        if(listMedia.size() != 0){
            return listMedia.size();
        }
        return 0;
    }

    static class MediaViewHolder extends RecyclerView.ViewHolder  {
        ImageView image;
        ImageView fav_icon;
        ImageView play_icon;
        MediaViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.photoItem);
            fav_icon = itemView.findViewById(R.id.fav_icon);
            play_icon = itemView.findViewById(R.id.play_video_button_child);
        }
    }


    class navAsyncTask extends AsyncTask<Void, Integer, Void> {
        public int pos;

        public void setPos(int pos) {
            this.pos = pos;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            listPath = new ArrayList<>();
            for(int i = 0;i<listCategory.size();i++) {
                List<Media> listCat = listCategory.get(i).getList();
                for (int j = 0; j < listCat.size(); j++) {
                    listPath.add(listCat.get(j).getPath());
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            intent.putStringArrayListExtra("data_list_path", listPath);
            intent.putExtra("pos", listPath.indexOf(listMedia.get(pos).getPath()));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }
}
