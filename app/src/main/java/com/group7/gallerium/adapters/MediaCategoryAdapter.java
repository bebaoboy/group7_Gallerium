package com.group7.gallerium.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.group7.gallerium.R;
import com.group7.gallerium.activities.ViewAlbum;
import com.group7.gallerium.models.Media;
import com.group7.gallerium.models.MediaCategory;
import com.group7.gallerium.utilities.SelectMediaInterface;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class MediaCategoryAdapter extends ListAdapter<MediaCategory, MediaCategoryAdapter.CategoryViewHolder> implements FastScrollRecyclerView.SectionedAdapter{

    RecyclerView.RecycledViewPool viewPool = new RecyclerView.RecycledViewPool();
    private MediaAdapter mediaAdapter;
    private SelectMediaInterface selectMediaInterface;
    private final int lastBound = -1;
    private int lastDetach = -1;
    public static final DiffUtil.ItemCallback<MediaCategory> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<MediaCategory>() {
                @Override
                public boolean areItemsTheSame(@NonNull MediaCategory oldItem, @NonNull MediaCategory newItem) {
                    return Objects.equals(oldItem.getNameCategory(), newItem.getNameCategory());
                }

                @Override
                public boolean areContentsTheSame(@NonNull MediaCategory oldItem, @NonNull MediaCategory newItem) {
                    return oldItem.getList().size() == newItem.getList().size();
                }
            };
    private int spanCount = 3;
    private Context context;
    private List<MediaCategory> listMediaCategory;

    private boolean isMultipleEnabled = false;

    public int getLastBound(){
        return lastBound;
    }
    public int getLastDetach(){
        return  lastDetach;
    }
    public void setMultipleEnabled(boolean value){
        isMultipleEnabled = value;
        notifyDataSetChanged();
    }

    public MediaCategoryAdapter(@NonNull Context context, int count, @NonNull SelectMediaInterface selectMediaInterface) {
        super(DIFF_CALLBACK);
        this.context = context;
        this.spanCount = count;
        this.selectMediaInterface = selectMediaInterface;
    }

    public MediaCategoryAdapter(@NonNull ViewAlbum context, int spanCount) {
        super(DIFF_CALLBACK);
        this.context = context;
        this.spanCount = spanCount;
    }

    public void setData(@NonNull List<MediaCategory> listMediaCategory){
        this.listMediaCategory = listMediaCategory;
        submitList(listMediaCategory);
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.media_group, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        MediaCategory mediaCategory = getItem(position);
        if (mediaCategory == null)
            return;
        if(mediaCategory.getNameCategory().isEmpty()) {
            holder.tvNameCategory.setVisibility(View.GONE);
            holder.horizontalLine.setVisibility(View.GONE);
        }
        else {
            holder.tvNameCategory.setVisibility(View.VISIBLE);
            holder.horizontalLine.setVisibility(View.VISIBLE);
            holder.tvNameCategory.setText(mediaCategory.getNameCategory());
        }

        GridLayoutManager gridLayoutManager = new GridLayoutManager(context, spanCount);
        holder.rcvPictures.setLayoutManager(gridLayoutManager);

        mediaAdapter = new MediaAdapter(context.getApplicationContext(), this.selectMediaInterface, spanCount);
        mediaAdapter.setImageSize(calculateImageSize());
        mediaAdapter.setListImages((ArrayList<Media>) mediaCategory.getList());
        mediaAdapter.setListCategory((ArrayList<MediaCategory>) listMediaCategory);
        holder.rcvPictures.setAdapter(mediaAdapter);
        holder.rcvPictures.setItemViewCacheSize(24);
        holder.rcvPictures.setRecycledViewPool(viewPool);
    }

    @Override
    public void onViewAttachedToWindow(@NonNull CategoryViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        if(isMultipleEnabled){
            if(mediaAdapter != null){
                mediaAdapter.setMultipleEnabled(true);
                ArrayList<Media> selectedMedia = selectMediaInterface.getSelectedList();
                Log.d("parent size", " " + selectedMedia.size());
                mediaAdapter.setSelectedList(selectedMedia);
            }
        }else{
            mediaAdapter.setMultipleEnabled(false);
        }
    }

    public void removeMedia(Media media){
        mediaAdapter.deleteMedia(media);
    }
    public void setAllChecked(boolean value) {
        mediaAdapter.setAllChecked(value);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public String getSectionName(int position) {
        if (getItem(position).getNameCategory().isEmpty()) {
            return getItem(position).getBackup();
        } else {
            return getItem(position).getNameCategory();
        }
    }

    public class CategoryViewHolder extends RecyclerView.ViewHolder{
        TextView tvNameCategory;
        RecyclerView rcvPictures;
        View horizontalLine;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);

            tvNameCategory = itemView.findViewById(R.id.txtDate);
            rcvPictures = itemView.findViewById(R.id.photos_recview);
            horizontalLine = itemView.findViewById(R.id.category_horizontal_line);
        }
    }

    public int dpToPx(int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    private int calculateImageSize() {
        int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
        screenWidth -= dpToPx(10);
        int spacing = dpToPx(5);
        var imageSize = Math.max((screenWidth - spacing * (spanCount - 1)) / (double)spanCount, dpToPx(60));
        return (int)Math.floor(imageSize);
    }
}

