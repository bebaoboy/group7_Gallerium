package com.group7.gallerium.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.group7.gallerium.R;
import com.group7.gallerium.models.Album;
import com.group7.gallerium.utilities.SelectMediaInterface;

import java.util.ArrayList;
import java.util.Objects;

public class AddToAlbumAdapter extends ListAdapter<Album, AddToAlbumAdapter.AlbumViewHolder> {

    SelectMediaInterface selectMediaInterface;
    Context context;
    public static final DiffUtil.ItemCallback<Album> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<>() {
                @Override
                public boolean areItemsTheSame(@NonNull Album oldItem, @NonNull Album newItem) {
                    return Objects.equals(oldItem.getPath(), newItem.getPath()) &&
                            oldItem.getListMedia().size() == newItem.getListMedia().size();
                }

                @Override
                public boolean areContentsTheSame(@NonNull Album oldItem, @NonNull Album newItem) {
                    return oldItem.getListMedia().size() == newItem.getListMedia().size();
                }
            };

    public AddToAlbumAdapter(@NonNull Context context, @NonNull SelectMediaInterface selectMediaInterface) {
        super(DIFF_CALLBACK);
        this.context = context;
        this.selectMediaInterface = selectMediaInterface;
    }

    public void setData(@NonNull ArrayList<Album> albumList){
        submitList(albumList);
    }

    @NonNull
    @Override
    public AddToAlbumAdapter.AlbumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        return new AddToAlbumAdapter.AlbumViewHolder(layoutInflater.inflate(R.layout.album_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull AddToAlbumAdapter.AlbumViewHolder holder, int position) {
        Album album = getItem(position);
        if(Objects.equals(album,null)) return;
        holder.amountPic.setText(String.valueOf(album.getListMedia().size()));
        holder.albumName.setText(album.getName());

        if(album.getAvatar() !=null)Glide.with(context).load(album.getAvatar().getPath()).into(holder.albumAvatar);

        holder.albumAvatar.setOnClickListener((view -> selectMediaInterface.moveMedia(album.getPath())));
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
