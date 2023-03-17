package com.group7.gallerium.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;


import com.bumptech.glide.Glide;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.group7.gallerium.R;
import com.group7.gallerium.activities.ViewMedia;
import com.group7.gallerium.models.MediaCategory;
import com.group7.gallerium.models.Media;
import com.group7.gallerium.utilities.AccessMediaFile;
import com.group7.gallerium.utilities.MediaItemInterface;
import com.group7.gallerium.utilities.SelectMediaInterface;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class MediaAdapter extends ListAdapter<Media, MediaAdapter.MediaViewHolder> {
    public static final DiffUtil.ItemCallback<Media> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Media>() {
                @Override
                public boolean areItemsTheSame(@NonNull Media oldItem, @NonNull Media newItem) {
                    return Objects.equals(oldItem.getPath(), newItem.getPath()) &&
                            AccessMediaFile.isFavMediaContains(oldItem.getPath())
                                    == AccessMediaFile.isFavMediaContains(newItem.getPath());
                }

                @Override
                public boolean areContentsTheSame(@NonNull Media oldItem, @NonNull Media newItem) {
                    return Objects.equals(oldItem.getTitle(), newItem.getTitle());
                }
            };
    private SelectMediaInterface selecteMediaInterface;
    private List<Media> listMedia;
    private Context context;
    private List<MediaCategory> listMediaCategory;
    private Intent intent;
    private ArrayList<String> listPath;

    private boolean isAllChecked = false;
    private ArrayList<Media> selectedMedia;
    private boolean isMultipleEnabled = false;

    public void setMultipleEnabled(boolean value){
        isMultipleEnabled = value;
        notifyDataSetChanged();
    }

    public MediaAdapter(Context context, SelectMediaInterface selectMediaInterface) {
        super(DIFF_CALLBACK);
        this.context = context;
        this.selecteMediaInterface = selectMediaInterface;
    }

    protected MediaAdapter() {
        super(DIFF_CALLBACK);
    }


    public void deleteMedia(Media media){
        int position = this.getCurrentList().indexOf(media);
        this.getCurrentList().remove(media);
        notifyItemRemoved(position);
    }
    public void setListImages(ArrayList<Media> media) {
        this.listMedia = media;
        submitList(listMedia);
    }

    public void setListCategory(ArrayList<MediaCategory> categories) {
        this.listMediaCategory = categories;
    }

    @NonNull
    @Override
    public MediaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        return new MediaViewHolder(layoutInflater.inflate(R.layout.media_item, parent, false));
    }

    @Override
    public void onViewAttachedToWindow(@NonNull MediaViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        //Log.d("Attached", "" + holder.getBindingAdapterPosition());

        if(isMultipleEnabled){
            holder.select.setVisibility(View.VISIBLE);
            if(isAllChecked){
                holder.select.setChecked(true);
            }else {
                holder.select.setChecked(false);
                if(selectedMedia != null) {
                    if (selectedMedia.contains(this.getCurrentList().get(holder.getBindingAdapterPosition()))) {
                        holder.select.setChecked(true);
                        //Log.d("Contained", "false");
                    } else {
                        holder.select.setChecked(false);
                        //Log.d("Contained", "false");
                    }
                }
            }
        }else{
            holder.select.setVisibility(View.GONE);
        }
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull MediaViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @Override
    public void onBindViewHolder(@NonNull MediaViewHolder holder, int position) {
        Media media = getItem(position);
        if (media == null) {
            return;
        }

        if(selectedMedia != null) {
          //  Log.d("selected media size", " " + selectedMedia.size());
            holder.select.setChecked(selectedMedia.contains(media));
        }
        // Log.d("gallerium", media.getMimeType());
        String[] gifList = {"image/gif"};
        if (Arrays.asList(gifList).contains(media.getMimeType())) {
            Glide.with(context).asGif().load("file://" + media.getThumbnail())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .listener(new RequestListener<GifDrawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<GifDrawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GifDrawable resource, Object model, Target<GifDrawable> target, DataSource dataSource, boolean isFirstResource) {
                            return false;
                        }
                    })
                    .into(holder.image);
        }
       else {
            Glide.with(context).load("file://" + media.getThumbnail())
                    //.dontAnimate()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.image);
        }

       if (AccessMediaFile.isFavMediaContains(media.getPath())) {
           holder.fav_icon.setVisibility(View.VISIBLE);
       }
       else {
           holder.fav_icon.setVisibility(View.GONE);
       }

        if(media.getType() == 3) holder.play_icon.setVisibility(View.VISIBLE);

        holder.image.setOnClickListener((view -> {
            if(isMultipleEnabled) {
                if(holder.select.isChecked()){
                    holder.select.setChecked(false);
                    selecteMediaInterface.deleteFromSelectedList(media);
                }else{
                    holder.select.setChecked(true);
                    selecteMediaInterface.addToSelectedList(media);
                }
            }else {
                intent = new Intent(context, ViewMedia.class);
                navAsyncTask navAsyncTask = new navAsyncTask();
                navAsyncTask.setPos(position);
                navAsyncTask.execute();
            }
        }));

        holder.image.setOnLongClickListener((view -> {
            if(holder.select.isChecked()){
                Log.d("checkbox", "is checked");
                selecteMediaInterface.deleteFromSelectedList(media);
                holder.select.setChecked(false);
            }else{
                Log.d("checkbox", "is not checked");
                selecteMediaInterface.addToSelectedList(media);
                holder.select.setChecked(true);
            }
            if(!isMultipleEnabled) {
                selecteMediaInterface.showAllSelect();
            }
           return true;
        }));

        holder.select.setOnClickListener((view)->{
            if(((CompoundButton) view).isChecked()){
                selecteMediaInterface.addToSelectedList(media);
            }else{
                selecteMediaInterface.deleteFromSelectedList(media);
            }
        });
    }
    public void setSelectedList(ArrayList<Media> list) {
        selectedMedia = list;
    }

    public void setAllChecked(boolean b) {
        isAllChecked = b;
        notifyItemRangeChanged(0, this.getCurrentList().size());
    }

    static class MediaViewHolder extends RecyclerView.ViewHolder  {
        ImageView image;
        ImageView fav_icon;
        ImageView play_icon;

        CheckBox select;
        MediaViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.photoItem);
            fav_icon = itemView.findViewById(R.id.fav_icon);
            play_icon = itemView.findViewById(R.id.play_video_button_child);
            select = itemView.findViewById(R.id.selectButton);
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
            for(int i = 0; i< listMediaCategory.size(); i++) {
                List<Media> listCat = listMediaCategory.get(i).getList();
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
