package com.group7.gallerium.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.icu.util.Calendar;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.GpsDirectory;
import com.drew.metadata.mp4.Mp4Directory;
import com.drew.metadata.mp4.media.Mp4MediaDirectory;
import com.google.android.material.button.MaterialButton;
import com.group7.gallerium.R;
import com.group7.gallerium.activities.MainActivity;
import com.group7.gallerium.activities.PhotoMapActivity;
import com.group7.gallerium.activities.ViewAlbum;
import com.group7.gallerium.models.Media;
import com.group7.gallerium.models.MediaCategory;
import com.group7.gallerium.utilities.AccessMediaFile;
import com.group7.gallerium.utilities.SelectMediaInterface;
import com.group7.gallerium.utilities.ViewAnimationUtils;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;
import com.smarteist.autoimageslider.IndicatorView.animation.type.IndicatorAnimationType;
import com.smarteist.autoimageslider.SliderAnimations;
import com.smarteist.autoimageslider.SliderView;
import com.whiteelephant.monthpicker.MonthPickerDialog;

import java.time.Instant;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class MediaCategoryAdapter extends ListAdapter<MediaCategory, RecyclerView.ViewHolder> implements FastScrollRecyclerView.SectionedAdapter{

    RecyclerView.RecycledViewPool viewPool = new RecyclerView.RecycledViewPool();
    private MediaAdapter mediaAdapter;
    private SelectMediaInterface selectMediaInterface;
    private final int lastBound = -1;
    private int lastDetach = -1;

    private RecyclerView rec;
    public static final DiffUtil.ItemCallback<MediaCategory> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<MediaCategory>() {
                @Override
                public boolean areItemsTheSame(@NonNull MediaCategory oldItem, @NonNull MediaCategory newItem) {
                    return oldItem.getNameCategory().equals(newItem.getNameCategory())
                            && oldItem.getBackup().equals(newItem.getBackup())
                            && oldItem.willCollapse == newItem.willCollapse && oldItem.willExpand == newItem.willExpand;
                }

                @Override
                public boolean areContentsTheSame(@NonNull MediaCategory oldItem, @NonNull MediaCategory newItem) {
                    return oldItem.getList().size() == newItem.getList().size()
                            && oldItem.willCollapse == newItem.willCollapse && oldItem.willExpand == newItem.willExpand;
                }
            };
    private int spanCount = 3;
    private Context context;
    private List<MediaCategory> listMediaCategory;

    private boolean isMultipleEnabled = false;
    final int UI_MODE_GRID = 1, UI_MODE_LIST = 2;

    private int uiMode = UI_MODE_GRID;

    public int getLastBound(){
        return lastBound;
    }
    public int getLastDetach(){
        return  lastDetach;
    }

    public void setUiMode(int uiMode){
        this.uiMode = uiMode;
        notifyDataSetChanged();
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

    ArrayList<Media> imageList = new ArrayList<>();
    MonthPickerDialog.Builder builder;
    boolean isHomepage = false;
    public void setSliderImageList(ArrayList<Media> m) {
        if (m.size() != 0)
        {
            imageList = m;
            notifyItemChanged(0);
        }
    }

    public void setHomepage(boolean b) {
        isHomepage = b;
    }

    public void setDateBuilder(MonthPickerDialog.Builder b) {
        builder = b;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 1) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.media_group, parent, false);
            return new CategoryViewHolder(view);
        }
        else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_slider, parent, false);
            return new ImageSlideViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder.getItemViewType() == 2) {
            var holder = (ImageSlideViewHolder) viewHolder;
            if (imageList.size() != 0) {
                ImageSliderAdapter adapter = new ImageSliderAdapter(context);
                adapter.renewItems(imageList);
                holder.slider.setSliderAdapter(adapter);
                holder.slider.startAutoCycle();
            }
            var today = Calendar.getInstance();
            holder.datePicker.setOnClickListener(view -> builder.build().show());
            holder.mapPicker.setOnClickListener(view -> {
                var intent = new Intent(context, PhotoMapActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            });

        } else {
            var holder = (CategoryViewHolder) viewHolder;
            MediaCategory mediaCategory = getItem(position);
            if (mediaCategory == null || mediaCategory.getNameCategory().equals("null"))
                return;
            if (mediaCategory.getNameCategory().isEmpty()) {
                holder.tvNameCategory.setVisibility(View.GONE);
                holder.horizontalLine.setVisibility(View.GONE);
            } else {
                holder.tvNameCategory.setVisibility(View.VISIBLE);
                holder.horizontalLine.setVisibility(View.VISIBLE);
                holder.tvNameCategory.setText(mediaCategory.getNameCategory());
            }

            mediaAdapter = new MediaAdapter(context.getApplicationContext(), this.selectMediaInterface, spanCount);
            mediaAdapter.setImageSize(calculateImageSize());
            mediaAdapter.setListImages((ArrayList<Media>) mediaCategory.getList());
            mediaAdapter.setListCategory((ArrayList<MediaCategory>) listMediaCategory);

            mediaAdapter.setUiMode(uiMode);

            if (uiMode == UI_MODE_GRID) {
                GridLayoutManager gridLayoutManager = new GridLayoutManager(context, spanCount);
                holder.rcvPictures.setLayoutManager(gridLayoutManager);
            } else {
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
                holder.rcvPictures.setLayoutManager(linearLayoutManager);
            }

            holder.rcvPictures.setAdapter(mediaAdapter);
            holder.rcvPictures.setItemViewCacheSize(24);
            holder.rcvPictures.setRecycledViewPool(viewPool);

            if (!mediaCategory.getNameCategory().isEmpty()) {
                holder.tvNameCategory.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mediaCategory.height == 1) {
                            boolean animNow = uiMode == UI_MODE_LIST;
                            ViewAnimationUtils.collapse(holder.rcvPictures, true);
                            mediaCategory.height = 0;
                        } else {
                            ViewAnimationUtils.expand(holder.rcvPictures);
                            mediaCategory.height = 1;
                        }
                    }
                });

                holder.tvNameCategory.setOnLongClickListener(new View.OnLongClickListener() {
                    final MediaAdapter med = mediaAdapter;

                    @Override
                    public boolean onLongClick(View view) {
                        if (isMultipleEnabled) {
                            if (med != null) {
//                            ArrayList<Media> selectedMedia = selectMediaInterface.getSelectedList();
//                            Log.d("parent size", " " + selectedMedia.size());
//                            med.setSelectedList(selectedMedia);
                                med.setAllSelect(true);
                            }
                        }
                        return true;
                    }
                });
            }
        }
    }

    @Override
    public void onViewAttachedToWindow(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        if (holder.getItemViewType() == 2) return;
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

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        rec = recyclerView;
    }

    @NonNull
    @Override
    public String getSectionName(int position) {
        if(position == -1) return "";
        if (getItemViewType(position) == 2) return "";
        if (getItem(position).getNameCategory().isEmpty()) {
            if (getItem(position).getBackup().isEmpty()) {
                if (getItem(position).getList().size() > 0)
                {
                    String s = getItem(position).getList().get(0).getSize();
                    var ext = s.substring(s.length() - 2);
                    s = s.substring(0, s.length() - 2);
                    double size = Double.parseDouble(s);
                    int roundedSize = (int)Math.floor(size);
                    return roundedSize < 10 ? roundedSize + ext : (roundedSize / 10) * 10 + ext;
                }
                else {
                    return "";
                }
            }
            return getItem(position).getBackup();
        } else {
            return getItem(position).getBackup() + getItem(position).getNameCategory();
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

    public class ImageSlideViewHolder extends RecyclerView.ViewHolder {
        SliderView slider;
        MaterialButton datePicker;
        MaterialButton mapPicker;

        public ImageSlideViewHolder(@NonNull View itemView) {
            super(itemView);
            mapPicker = itemView.findViewById(R.id.map_picker);
            datePicker = itemView.findViewById(R.id.date_picker);
            slider = itemView.findViewById(R.id.imageSlider);
            slider.setIndicatorAnimation(IndicatorAnimationType.FILL); //set indicator animation by using IndicatorAnimationType. :WORM or THIN_WORM or COLOR or DROP or FILL or NONE or SCALE or SCALE_DOWN or SLIDE and SWAP!!
            slider.setSliderTransformAnimation(SliderAnimations.CUBEINDEPTHTRANSFORMATION);
            slider.setAutoCycleDirection(SliderView.AUTO_CYCLE_DIRECTION_BACK_AND_FORTH);
            slider.setIndicatorSelectedColor(Color.WHITE);
            slider.setIndicatorUnselectedColor(Color.GRAY);
            slider.setScrollTimeInSec(4); //set scroll delay in seconds :
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

    private int calculateImageAmount() {
        int screenHeight = context.getResources().getDisplayMetrics().heightPixels;
        int spacing = dpToPx(5);
        var imageSize = calculateImageSize();
        return (int)Math.floor((screenHeight - spacing) / (double)imageSize) * 3;
    }

    @Override
    public int getItemViewType(int position) {
        if (isHomepage)
        {
            return position == 0 ? 2 : 1;
        }
        else {
            return 1;
        }
    }
}

