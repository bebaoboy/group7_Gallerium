package com.group7.gallerium.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.group7.gallerium.R;
import com.group7.gallerium.adapters.CategoryAdapter;
import com.group7.gallerium.models.Category;
import com.group7.gallerium.models.Media;
import com.group7.gallerium.utilities.AccessMediaFile;
import com.group7.gallerium.utilities.ToolbarScrollListener;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

public class ViewAlbum extends AppCompatActivity {

    private String albumPath;
    private String albumName;
    private ArrayList<String> mediaPaths;
    private Intent intent;
    private CategoryAdapter adapter;
    private int spanCount = 3;

    private Toolbar toolbar;
    private RecyclerView album_rec_item;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_album);
        intent = getIntent();
        mediaPaths = intent.getStringArrayListExtra("media_paths");
        albumPath = intent.getStringExtra("folder_path");
        albumName = intent.getStringExtra("name");

        toolbarSetting();

        album_rec_item = findViewById(R.id.rec_menu_item);

        adapter = new CategoryAdapter(this, spanCount);

        // album_rec_item.addOnScrollListener(new ToolbarScrollListener(toolbar));
        album_rec_item.setItemViewCacheSize(4);
    }

    @Override
    public void onResume() {
        super.onResume();
        Toast.makeText(this, "Resuming", Toast.LENGTH_SHORT).show();
        adapter.setData(getListCategory());
        album_rec_item.setAdapter(adapter);
//        if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
//            changeOrientation(6);
//            //((LinearLayoutManager) Objects.requireNonNull(recyclerView.getLayoutManager())).scrollToPositionWithOffset(firstVisiblePosition, offset);
//        }
//        else {
//            adapter.setData(getListCategory());
//            album_rec_item.setAdapter(adapter);
//            //((LinearLayoutManager) Objects.requireNonNull(recyclerView.getLayoutManager())).scrollToPositionWithOffset(firstVisiblePosition, offset);
//        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    public void changeOrientation(int spanCount) {
        adapter = new CategoryAdapter(this, spanCount);
        adapter.setData(getListCategory());
        album_rec_item.setAdapter(adapter);
        //((LinearLayoutManager) Objects.requireNonNull(album_rec_item.getLayoutManager())).scrollToPositionWithOffset(firstVisiblePosition, offset);
    }

    void toolbarSetting(){
        toolbar = findViewById(R.id.toolbar_view_album);
        toolbar.inflateMenu(R.menu.menu_view_album);
        toolbar.setTitle(R.string.app_name);
        toolbar.setTitleTextAppearance(this, R.style.ToolbarTitle);
        toolbar.setNavigationIcon(R.drawable.ic_back_arrow);
        toolbar.setNavigationOnClickListener((view) -> finish());
        toolbar.setTitle(albumName);
    }

    @NonNull
    private List<Category> getListCategory() {

        AccessMediaFile.refreshAllMedia();
        HashMap<String, Category> categoryList = new LinkedHashMap<>();
        int categoryCount = 0;
        Media media;
        List<Media> mediaList = new ArrayList<>();
        for(String path: mediaPaths){
            media = AccessMediaFile.getMediaWithPath(path);
            if(media != null)
                mediaList.add(media);
        }

        try {
            categoryList.put(mediaList.get(0).getDateTaken(), new Category(mediaList.get(0).getDateTaken(), new ArrayList<>()));
            categoryList.get(mediaList.get(0).getDateTaken()).addMediaToList(mediaList.get(0));
            for (int i = 1; i < mediaList.size(); i++) {
                if (!categoryList.containsKey(mediaList.get(i).getDateTaken())) {
                    categoryList.put(mediaList.get(i).getDateTaken(), new Category(mediaList.get(i).getDateTaken(), new ArrayList<>()));
                    categoryCount++;
                }

                categoryList.get(mediaList.get(i).getDateTaken()).addMediaToList(mediaList.get(i));
            }
//            categoryList.forEach(x -> {
//                Log.d("gallerium", x.getNameCategory() + ": " + x.getList().size());
//            });
            var newCatList = new ArrayList<Category>();
            int partitionSize = 60;
            for(var cat : categoryList.values()) {
                cat.getList().sort(Comparator.comparingLong(Media::getRawDate).reversed());
                for (int i = 0; i < cat.getList().size(); i += partitionSize) {

                    newCatList.add(new Category(cat.getNameCategory(), new ArrayList<>(cat.getList().subList(i,
                            Math.min(i + partitionSize, cat.getList().size())))));

                }
            }

            return newCatList;
        } catch (Exception e) {
            return null;
        }


    }
}