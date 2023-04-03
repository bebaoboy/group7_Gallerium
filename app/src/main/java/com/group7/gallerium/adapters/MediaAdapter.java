package com.group7.gallerium.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.card.MaterialCardView;
import com.group7.gallerium.R;
import com.group7.gallerium.activities.ViewMedia;
import com.group7.gallerium.activities.ViewMediaStandalone;
import com.group7.gallerium.models.Media;
import com.group7.gallerium.models.MediaCategory;
import com.group7.gallerium.utilities.AccessMediaFile;
import com.group7.gallerium.utilities.SelectMediaInterface;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class MediaAdapter extends ListAdapter<Media, MediaAdapter.MediaViewHolder> {
    public static final DiffUtil.ItemCallback<Media> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<>() {
                @Override
                public boolean areItemsTheSame(@NonNull Media oldItem, @NonNull Media newItem) {
                    return Objects.equals(oldItem.getPath(), newItem.getPath()) &&
                            AccessMediaFile.isExistedAnywhere(oldItem.getPath())
                                    == AccessMediaFile.isExistedAnywhere(newItem.getPath());
                }

                @Override
                public boolean areContentsTheSame(@NonNull Media oldItem, @NonNull Media newItem) {
                    return Objects.equals(oldItem.getTitle(), newItem.getTitle());
                }
            };
    private int spanCount = 3;
    private SelectMediaInterface selecteMediaInterface;
    List<Media> listMedia;
    Context context;
    List<MediaCategory> listMediaCategory;
    Intent intent;
    float mul = 0.9f;
    ArrayList<String> listPath;

    final int UI_MODE_GRID = 1, UI_MODE_LIST = 2;

    boolean[] med;
    private static RequestOptions requestOptions = new RequestOptions().format(DecodeFormat.PREFER_RGB_565);

    private boolean isAllChecked = false;
    private ArrayList<Media> selectedMedia;
    private boolean isMultipleEnabled = false;
    private int imageSize = 0, uiMode; // uiMode = 1 -> grid, uiMode = 2 -> list

    public void setMultipleEnabled(boolean value){
        isMultipleEnabled = value;
        notifyDataSetChanged();
    }

    public MediaAdapter(@NonNull Context context, @NonNull SelectMediaInterface selectMediaInterface) {
        super(DIFF_CALLBACK);
        this.context = context;
        this.selecteMediaInterface = selectMediaInterface;
    }

    public MediaAdapter(@NonNull Context context, @NonNull SelectMediaInterface selectMediaInterface, int spanCount) {
        super(DIFF_CALLBACK);
        this.context = context;
        this.spanCount = spanCount;
        this.selecteMediaInterface = selectMediaInterface;
    }

    public void setImageSize(int size) {
        imageSize = size;
        requestOptions = requestOptions.override((int)(size * 0.95));
        mul = spanCount == 3 ? 0.9f : spanCount == 4 ? 0.85f : 0.8f;
    }

    public void deleteMedia(@NonNull Media media){
        int position = this.getCurrentList().indexOf(media);
        this.getCurrentList().remove(media);
        med = new boolean[this.getCurrentList().size()];
        Arrays.fill(med, false);
        notifyItemRemoved(position);
    }
    public void setListImages(@NonNull ArrayList<Media> media) {
        this.listMedia = media;
        submitList(listMedia);
        med = new boolean[this.getCurrentList().size()];
        Arrays.fill(med, false);
    }

    public void setListCategory(@NonNull ArrayList<MediaCategory> categories) {
        this.listMediaCategory = categories;
    }

    public void setUiMode(int uiMode){
        this.uiMode = uiMode;
        notifyItemRangeChanged(0, getItemCount());
    }

    @NonNull
    @Override
    public MediaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

        MediaViewHolder holder;
        if(uiMode == UI_MODE_GRID)
            holder = new MediaViewHolder(layoutInflater.inflate(R.layout.media_item, parent, false), uiMode);
        else
            holder = new MediaViewHolder(layoutInflater.inflate(R.layout.media_item_list, parent, false), uiMode);
        return holder;
    }

    @Override
    public void onViewAttachedToWindow(@NonNull MediaViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        //Log.d("Attached", "" + holder.getBindingAdapterPosition());

        if(isMultipleEnabled){
            holder.select.setCheckable(true);
            holder.blur.setVisibility(View.VISIBLE);
            if(isAllChecked){
                holder.select.setChecked(true);
            }else {
                holder.select.setChecked(false);
                if(selectedMedia != null) {
                    //Log.d("Contained", "false");
                    //Log.d("Contained", "false");
                    holder.select.setChecked(selectedMedia.contains(this.getCurrentList().get(holder.getBindingAdapterPosition())));
                }
            }
        }else{
            holder.blur.setVisibility(View.GONE);
            holder.select.setChecked(false);
            holder.select.setCheckable(false);
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

        if(context == null) return;
        if (media == null || media.getPath() == null) {
            return;
        }

        if(uiMode == UI_MODE_LIST){
            String[] dirs = media.getPath().split("/");
            holder.media_name.setText(dirs[dirs.length - 1]);
            holder.created_time.setText(media.getTimeTaken());
            holder.media_size.setText(media.getSize());
        }
        else {
            holder.image.getLayoutParams().height = imageSize;
            holder.image.getLayoutParams().width = imageSize;
            holder.blur.getLayoutParams().height = imageSize;
            holder.blur.getLayoutParams().width = imageSize;
        }

//        if(selectedMedia != null) {
//          //  Log.d("selected media size", " " + selectedMedia.size());
//            holder.select.setChecked(selectedMedia.contains(media));
//        }

        // Log.d("gallerium", media.getMimeType());
        if (!med[position]) {
            if (media.getMimeType() != null && media.getMimeType().startsWith("image/gif")) {
                Glide.with(context).asGif().sizeMultiplier(mul)
                        .load("file://" + media.getThumbnail())
                        .onlyRetrieveFromCache(true)
                        //.diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                        .apply(requestOptions)
                        .listener(new RequestListener<>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<GifDrawable> target, boolean isFirstResource) {
                                med[position] = false;
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(GifDrawable resource, Object model, Target<GifDrawable> target, DataSource dataSource, boolean isFirstResource) {
                                // Log.d("def", "");
                                med[position] = true;
                                return false;
                            }
                        })
                        .into(holder.image);
            }
            else {
                Glide.with(context).load("file://" + media.getThumbnail())
                        .dontAnimate()
                        .sizeMultiplier(mul)
                        .onlyRetrieveFromCache(true)
                        .apply(requestOptions)
                        //.diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                        .listener(new RequestListener<>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                med[position] = false;
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                // Log.d("def", "");
                                med[position] = true;
                                return false;
                            }
                        })
                        .into(holder.image);
               // Log.d("abc", "");
            }
        }

        if (holder.image.getDrawable() != null) {
            //Log.d("draw", holder.image.getDrawable().toString());
        } else {
            //Log.d("draw", "nulll");
            if (media.getMimeType() != null && media.getMimeType().startsWith("image/gif")) {
                Glide.with(context).asGif().sizeMultiplier(mul).load("file://" + media.getThumbnail())
                        .apply(requestOptions)
                        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                        .apply(requestOptions)
                        .listener(new RequestListener<>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<GifDrawable> target, boolean isFirstResource) {
                                med[position] = false;
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(GifDrawable resource, Object model, Target<GifDrawable> target, DataSource dataSource, boolean isFirstResource) {
              //                  Log.d("def", "");
                                med[position] = true;
                                return false;
                            }
                        })
                        .into(holder.image);
            }
            else {
                Glide.with(context).load("file://" + media.getThumbnail())
                        .dontAnimate()
                        .sizeMultiplier(mul)
                        .apply(requestOptions)
                        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                        .listener(new RequestListener<>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                med[position] = false;
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                //                Log.d("def", "");
                                med[position] = true;
                                return false;
                            }
                        })
                        .into(holder.image);
                // Log.d("abc", "");
            }
        }


       if (AccessMediaFile.isExistedAnywhere(media.getPath())) {
           holder.fav_icon.setVisibility(View.VISIBLE);
       } else {
           holder.fav_icon.setVisibility(View.GONE);

       }

        if(media.getType() != 1) {
            holder.play_icon.setVisibility(View.VISIBLE);
        }
        else {
            holder.play_icon.setVisibility(View.GONE);
        }
        holder.image.setOnClickListener((view -> {
            if(isMultipleEnabled) {
                holder.blur.setVisibility(View.VISIBLE);
                if(holder.select.isChecked()){
                    holder.select.setChecked(false);
                    selecteMediaInterface.deleteFromSelectedList(listMedia.get(position));
                }else{
                    holder.select.setChecked(true);
                    selecteMediaInterface.addToSelectedList(listMedia.get(position));
                }
            }else {
                holder.blur.setVisibility(View.GONE);
                intent = new Intent(context, ViewMedia.class);
                navAsyncTask navAsyncTask = new navAsyncTask();
                navAsyncTask.setPos(holder.getBindingAdapterPosition());
                navAsyncTask.execute();
            }
        }));

        if(selecteMediaInterface != null) {
            holder.image.setOnLongClickListener((view -> {
//            if(holder.select.isChecked()){
//                Log.d("checkbox", "is checked");
//                selecteMediaInterface.deleteFromSelectedList(listMedia.get(position));
//                holder.select.setChecked(false);
//            }else{
//                Log.d("checkbox", "is not checked");
//                selecteMediaInterface.addToSelectedList(listMedia.get(position));
//                holder.select.setChecked(true);
//            }
                if (!isMultipleEnabled) {
                    selecteMediaInterface.showAllSelect();
                } else {
                    Intent intent = new Intent(context, ViewMediaStandalone.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setDataAndType(Uri.fromFile(new File(media.getPath())), media.getMimeType());
                    context.startActivity(intent);
                }
                return true;
            }));

            holder.select.setOnClickListener((view) -> {
                if (((MaterialCardView) view).isChecked()) {
                    selecteMediaInterface.addToSelectedList(listMedia.get(position));
                } else {
                    selecteMediaInterface.deleteFromSelectedList(listMedia.get(position));
                }
            });
        }

        if(uiMode == UI_MODE_LIST) {
            holder.linearLayout.setOnClickListener((view -> {
                if (isMultipleEnabled) {
                    holder.blur.setVisibility(View.VISIBLE);
                    if (holder.select.isChecked()) {
                        holder.select.setChecked(false);
                        selecteMediaInterface.deleteFromSelectedList(listMedia.get(position));
                    } else {
                        holder.select.setChecked(true);
                        selecteMediaInterface.addToSelectedList(listMedia.get(position));
                    }
                } else {
                    holder.blur.setVisibility(View.GONE);
                    intent = new Intent(context, ViewMedia.class);
                    navAsyncTask navAsyncTask = new navAsyncTask();
                    navAsyncTask.setPos(holder.getBindingAdapterPosition());
                    navAsyncTask.execute();
                }
            }));

            holder.linearLayout.setOnLongClickListener((view -> {
//            if(holder.select.isChecked()){
//                Log.d("checkbox", "is checked");
//                selecteMediaInterface.deleteFromSelectedList(listMedia.get(position));
//                holder.select.setChecked(false);
//            }else{
//                Log.d("checkbox", "is not checked");
//                selecteMediaInterface.addToSelectedList(listMedia.get(position));
//                holder.select.setChecked(true);
//            }
                if (!isMultipleEnabled) {
                    selecteMediaInterface.showAllSelect();
                } else {
                    Intent intent = new Intent(context, ViewMediaStandalone.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setDataAndType(Uri.fromFile(new File(media.getPath())), media.getMimeType());
                    context.startActivity(intent);
                }
                return true;
            }));
        }

    }
    public void setSelectedList(@NonNull ArrayList<Media> list) {
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
        View blur;

        TextView media_name, created_time, media_size;
        LinearLayout linearLayout;

        MaterialCardView select;
        MediaViewHolder(View itemView, int uiMode) {
            super(itemView);
            image = itemView.findViewById(R.id.photoItem);
            fav_icon = itemView.findViewById(R.id.fav_icon);
            play_icon = itemView.findViewById(R.id.play_video_button_child);
            select = itemView.findViewById(R.id.media_card);
            blur = itemView.findViewById(R.id.blur_view);

            if(uiMode == 2){
                linearLayout = itemView.findViewById(R.id.list_layout);
                media_name = itemView.findViewById(R.id.media_name);
                created_time = itemView.findViewById(R.id.media_taken_time);
                media_size = itemView.findViewById(R.id.media_size);
            }
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
            if(listMediaCategory != null) {
                for (int i = 0; i < listMediaCategory.size(); i++) {
                    List<Media> listCat = listMediaCategory.get(i).getList();
                    for (int j = 0; j < listCat.size(); j++) {
                        listPath.add(listCat.get(j).getPath());
                    }
                }
            }else{
                for (int j = 0; j < getCurrentList().size(); j++) {
                    listPath.add(getCurrentList().get(j).getPath());
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            intent.putStringArrayListExtra("data_list_path", listPath);
            intent.putExtra("pos", listPath.indexOf(listMedia.get(pos).getPath()));
            if(listMediaCategory == null)
                intent.putExtra("view-type", 1);
            else
                intent.putExtra("view-type", 2);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }

    public int dpToPx(int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }
}
