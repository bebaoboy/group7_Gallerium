package com.group7.gallerium.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;

import com.group7.gallerium.R;
import com.group7.gallerium.adapters.MediaAdapter;
import com.group7.gallerium.adapters.MediaCategoryAdapter;
import com.group7.gallerium.models.Media;
import com.group7.gallerium.models.MediaCategory;
import com.group7.gallerium.utilities.AccessMediaFile;
import com.group7.gallerium.utilities.MySuggestionProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;

public class SearchResultActivity extends AppCompatActivity {

    ArrayList<Media> mediaList;
    MediaAdapter mediaAdapter;

    Toolbar toolbar;
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);

        mediaList = new ArrayList<>();
        mediaAdapter = new MediaAdapter(this, null);
        recyclerView = findViewById(R.id.search_result_rec);
        toolbar = findViewById(R.id.search_tb);

        toolbar.setTitle("Kết quả search");
        toolbar.setTitleTextAppearance(this, R.style.ToolbarTitle);
        toolbar.setNavigationIcon(R.drawable.ic_back_arrow);
        toolbar.setNavigationOnClickListener((view) -> finish());
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            query = query.toLowerCase();
            SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
                    MySuggestionProvider.AUTHORITY, MySuggestionProvider.MODE);
            suggestions.saveRecentQuery(query, null);

            ArrayList<Media> allMedias = AccessMediaFile.getAllMedia(this);
            for(Media media: allMedias){
                if(media.getTitle().toLowerCase().contains(query)){
                    mediaList.add(media);
                }
            }

            if(mediaList.isEmpty()){
                for(Media media: allMedias){
                    if(media.getDateTaken().contains(query)){
                        mediaList.add(media);
                    }
                }
            }

            for(Media media: allMedias){
                if(media.getDateTaken().contains(query) && !mediaList.contains(media)){
                    mediaList.add(media);
                }
            }

            mediaAdapter.setListImages(mediaList);
            mediaAdapter.setUiMode(1);

            MediaCategory category = new MediaCategory("", mediaList);
            int partitionSize = 21;
            ArrayList<MediaCategory> categories = new ArrayList<>(), newCatList = new ArrayList<>();
            categories.add(category);
            for (var cat : categories) {
                if (cat.getList().size() < partitionSize) {
                    newCatList.add(cat);
                    continue;
                }
                for (int i = 0; i < cat.getList().size(); i += partitionSize) {
                    String name = i == 0 ? cat.getNameCategory() : "";
                    var c = new MediaCategory(name, new ArrayList<>(cat.getList().subList(i,
                            Math.min(i + partitionSize, cat.getList().size()))));
                    c.setBackup(cat.getNameCategory());
                    newCatList.add(c);

                }
            }
            MediaCategoryAdapter adapter = new MediaCategoryAdapter(this, 3, null);
            adapter.setData(newCatList);
            recyclerView.setHasFixedSize(true);
            recyclerView.setItemViewCacheSize(10000);
            // recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
            recyclerView.setAdapter(adapter);
        }
    }
}