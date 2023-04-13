package com.group7.gallerium.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.group7.gallerium.R;
import com.group7.gallerium.activities.ViewMedia;
import com.group7.gallerium.models.Media;
import com.group7.gallerium.models.MediaCategory;
import com.group7.gallerium.utilities.AccessMediaFile;
import com.group7.gallerium.utilities.ViewMediaDataHolder;
import com.smarteist.autoimageslider.SliderView;
import com.smarteist.autoimageslider.SliderViewAdapter;

import java.util.ArrayList;
import java.util.List;

public class ImageSliderAdapter extends
        SliderViewAdapter<ImageSliderAdapter.SliderAdapterVH> {

    private Context context;
    private List<Media> mSliderViews = new ArrayList<>();
    public ImageSliderAdapter(Context context) {
        this.context = context;
    }

    public void renewItems(List<Media> SliderViews) {
        var diff = mSliderViews.size() != SliderViews.size();
        if (!diff) {
            for(int i = 0; i < mSliderViews.size(); i++) {
                if (!mSliderViews.get(i).getPath().equals(SliderViews.get(i).getPath())) {
                    diff = true;
                    break;
                }
            }
        }
        if (diff)
        {
            this.mSliderViews = SliderViews;
            notifyDataSetChanged();
        }
    }

    public void deleteItem(int position) {
        this.mSliderViews.remove(position);
        notifyDataSetChanged();
    }

    public void addItem(Media SliderView) {
        this.mSliderViews.add(SliderView);
        notifyDataSetChanged();
    }

    Intent intent;

    @Override
    public SliderAdapterVH onCreateViewHolder(ViewGroup parent) {
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_slider_layout_item, null);
        return new SliderAdapterVH(inflate);
    }

    @Override
    public void onBindViewHolder(SliderAdapterVH viewHolder, final int position) {

        Media SliderView = mSliderViews.get(position);

        viewHolder.textViewDescription.setText(SliderView.getDateTimeTaken());
        viewHolder.textViewDescription.setTextSize(16);
        viewHolder.textViewDescription.setTextColor(Color.WHITE);
        Glide.with(viewHolder.itemView)
                .load(SliderView.getThumbnail())
                .fitCenter()
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .into(viewHolder.imageViewBackground);

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "This is item in position " + position, Toast.LENGTH_SHORT).show();
                intent = new Intent(context, ViewMedia.class);
                var navAsyncTask = new navAsyncTask();
                navAsyncTask.setPos(position);
                navAsyncTask.execute();
            }
        });
    }

    class navAsyncTask extends AsyncTask<Void, Integer, Void> {
        public int pos;
        ArrayList<String> listPath = new ArrayList<>();

        public void setPos(int pos) {
            this.pos = pos;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if(mSliderViews != null) {
                for(var m : mSliderViews) {
                    listPath.add(m.getPath());
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            if (listPath.size() > 1000) {
                ViewMediaDataHolder.setList(listPath);
            } else {
                intent.putStringArrayListExtra("data_list_path", listPath);
            }
            intent.putExtra("pos", pos);
            intent.putExtra("view-type", 2);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }

    @Override
    public int getCount() {
        //slider view count could be dynamic size
        return mSliderViews.size();
    }

    class SliderAdapterVH extends SliderViewAdapter.ViewHolder {

        View itemView;
        ImageView imageViewBackground;
        ImageView imageGifContainer;
        TextView textViewDescription;

        public SliderAdapterVH(View itemView) {
            super(itemView);
            imageViewBackground = itemView.findViewById(R.id.iv_auto_image_slider);
            imageGifContainer = itemView.findViewById(R.id.iv_gif_container);
            textViewDescription = itemView.findViewById(R.id.tv_auto_image_slider);
            this.itemView = itemView;
        }
    }

}