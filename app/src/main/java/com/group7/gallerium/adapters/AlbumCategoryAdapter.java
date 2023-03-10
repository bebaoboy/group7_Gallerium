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
import com.group7.gallerium.models.Album;
import com.group7.gallerium.models.AlbumCategory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AlbumCategoryAdapter extends ListAdapter<AlbumCategory, AlbumCategoryAdapter.CategoryViewHolder> {

    public static final DiffUtil.ItemCallback<AlbumCategory> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<>() {
                @Override
                public boolean areItemsTheSame(@NonNull AlbumCategory oldItem, @NonNull AlbumCategory newItem) {
                    return Objects.equals(oldItem.getNameCategory(), newItem.getNameCategory());
                }

                @Override
                public boolean areContentsTheSame(@NonNull AlbumCategory oldItem, @NonNull AlbumCategory newItem) {
                    return oldItem.getList().size() == newItem.getList().size();
                }
            };
    private int spanCount = 3;
    private Context context;
    private List<AlbumCategory> listAlbumCategory;

    public AlbumCategoryAdapter(Context context, int count) {
        super(DIFF_CALLBACK);
        this.context = context;
        this.spanCount = count;
    }

    public void setData(List<AlbumCategory> listAlbumCategory) {
        this.listAlbumCategory = listAlbumCategory;
        submitList(listAlbumCategory);
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.album_group, parent, false);
        return new CategoryViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        AlbumCategory albumCategory = getItem(position);
        if (albumCategory == null)
            return;

        if (albumCategory.getNameCategory().isEmpty()) {
            holder.tvNameCategory.setVisibility(View.GONE);
        } else {
            holder.tvNameCategory.setVisibility(View.VISIBLE);
//            holder.horizontalLine.setVisibility(View.VISIBLE);
            holder.tvNameCategory.setText(albumCategory.getNameCategory());
        }
//        if (position == getCurrentList().size() - 1) holder.horizontalLine.setVisibility(View.GONE);
//

        GridLayoutManager gridLayoutManager = new GridLayoutManager(context, spanCount);
        holder.rcvAlbum.setLayoutManager(gridLayoutManager);

        AlbumAdapter albumAdapter = new AlbumAdapter(context.getApplicationContext());
        albumAdapter.setData((ArrayList<Album>) albumCategory.getList());
        holder.rcvAlbum.setAdapter(albumAdapter);
        holder.rcvAlbum.setItemViewCacheSize(24);
    }

    public class CategoryViewHolder extends RecyclerView.ViewHolder {
        private TextView tvNameCategory;
        private RecyclerView rcvAlbum;
        private View horizontalLine;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNameCategory = itemView.findViewById(R.id.txtAlbumName);
            rcvAlbum = itemView.findViewById(R.id.albums_recview);

        }
    }
}

