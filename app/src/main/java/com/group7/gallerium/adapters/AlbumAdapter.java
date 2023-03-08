package com.group7.gallerium.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.group7.gallerium.R;
import com.group7.gallerium.activities.ViewAlbum;
import com.group7.gallerium.models.Album;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Objects;

public class AlbumAdapter extends ListAdapter<Album, AlbumAdapter.AlbumViewHolder> {

    Context context;
    Intent intent;

    public static final DiffUtil.ItemCallback<Album> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<>() {
                @Override
                public boolean areItemsTheSame(@NonNull Album oldItem, @NonNull Album newItem) {
                    return Objects.equals(oldItem.getPath(), newItem.getPath());
                }

                @Override
                public boolean areContentsTheSame(@NonNull Album oldItem, @NonNull Album newItem) {
                    return oldItem.getListMedia().size() == newItem.getListMedia().size();
                }
            };

    public ArrayList<Album> albumList;
    public AlbumAdapter(@NonNull Context context) {
        super(DIFF_CALLBACK);
        this.context = context;
        intent = new Intent(context, ViewAlbum.class);
    }

    public void setData(ArrayList<Album> albumList){
        this.albumList = albumList;
        submitList(this.albumList);
    }

    @NonNull
    @Override
    public AlbumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        return new AlbumViewHolder(layoutInflater.inflate(R.layout.album_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumViewHolder holder, int position) {
        Album album = getItem(position);
        if(album == null) return;

        holder.amountPic.setText(String.valueOf(album.getListMedia().size()));
        holder.albumName.setText(album.getName());

        Glide.with(context).load(album.getAvatar().getPath()).into(holder.albumAvatar);

        holder.albumAvatar.setOnClickListener((view -> {
            ArrayList<String> listPath = new ArrayList<>();
            for(int i=0;i< album.getListMedia().size();i++) {
                listPath.add(album.getListMedia().get(i).getPath());
            }

            intent.putStringArrayListExtra("media_paths", listPath);
            intent.putExtra("path_folder", album.getPath());
            intent.putExtra("name", album.getName());
            intent.putExtra("ok", 1);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }));
    }

    static class AlbumViewHolder extends RecyclerView.ViewHolder {

        ImageView albumAvatar;
        TextView albumName;
        TextView amountPic;
        public AlbumViewHolder(@NonNull View itemView) {
            super(itemView);

            albumAvatar = itemView.findViewById(R.id.album_avatar);
            albumName = itemView.findViewById(R.id.album_name);
            amountPic = itemView.findViewById(R.id.amount_pics);
        }
    }
}
