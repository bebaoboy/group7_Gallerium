package com.group7.gallerium.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.loader.app.LoaderManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.group7.gallerium.R;
import com.group7.gallerium.models.Category;
import com.group7.gallerium.models.Media;

import java.util.ArrayList;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>{
    private Context context;
    private List<Category> listCategory;

    public CategoryAdapter(Context context) {
        this.context = context;
    }

    public void setData(List<Category> listCategory){
        this.listCategory = listCategory;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.photo_group, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = listCategory.get(position);
        if (category == null)
            return;

        holder.tvNameCategory.setText(category.getNameCategory());

        GridLayoutManager gridLayoutManager = new GridLayoutManager(context, 3);
        holder.rcvPictures.setLayoutManager(gridLayoutManager);

        MediaAdapter mediaAdapter = new MediaAdapter(context.getApplicationContext());
        mediaAdapter.setListImages((ArrayList<Media>) category.getList());
        mediaAdapter.setListCategory((ArrayList<Category>) listCategory);
        holder.rcvPictures.setAdapter(mediaAdapter);
    }

    @Override
    public int getItemCount() {
        if (listCategory != null){
            return listCategory.size();
        }
        return 0;
    }

    public class CategoryViewHolder extends RecyclerView.ViewHolder{
        private TextView tvNameCategory;
        private RecyclerView rcvPictures;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);

            tvNameCategory = itemView.findViewById(R.id.txtDate);
            rcvPictures = itemView.findViewById(R.id.photos_recview);
        }
    }
}

