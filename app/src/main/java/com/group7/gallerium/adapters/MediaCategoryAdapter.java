package com.group7.gallerium.adapters;

import android.content.Context;
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
import com.group7.gallerium.models.MediaCategory;
import com.group7.gallerium.models.Media;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MediaCategoryAdapter extends ListAdapter<MediaCategory, MediaCategoryAdapter.CategoryViewHolder> {

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

    public MediaCategoryAdapter(Context context, int count) {
        super(DIFF_CALLBACK);
        this.context = context;
        this.spanCount = count;
    }

    public void setData(List<MediaCategory> listMediaCategory){
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

        MediaAdapter mediaAdapter = new MediaAdapter(context.getApplicationContext());
        mediaAdapter.setListImages((ArrayList<Media>) mediaCategory.getList());
        mediaAdapter.setListCategory((ArrayList<MediaCategory>) listMediaCategory);
        holder.rcvPictures.setAdapter(mediaAdapter);
        holder.rcvPictures.setItemViewCacheSize(24);
    }

    public class CategoryViewHolder extends RecyclerView.ViewHolder{
        private TextView tvNameCategory;
        private RecyclerView rcvPictures;
        private View horizontalLine;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);

            tvNameCategory = itemView.findViewById(R.id.txtDate);
            rcvPictures = itemView.findViewById(R.id.photos_recview);
            horizontalLine = itemView.findViewById(R.id.category_horizontal_line);
        }
    }
}

