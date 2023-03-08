package com.group7.gallerium.fragments;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.group7.gallerium.R;
import com.group7.gallerium.utilities.ToolbarScrollListener;
import com.group7.gallerium.adapters.CategoryAdapter;
import com.group7.gallerium.models.Category;
import com.group7.gallerium.models.Media;
import com.group7.gallerium.utilities.AccessMediaFile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MediaFragment#} factory method to
 * create an instance of this fragment.
 */
public class MediaFragment extends Fragment{
    private View view;
    private Toolbar toolbar;
    private Context context;

    private ArrayList<Media> listMedia;

    private CategoryAdapter adapter;
    private RecyclerView recyclerView;
    private int spanCount = 3;
    private int firstVisiblePosition;
    private int offset;

    public MediaFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        Toast.makeText(this.getContext(), "Resuming", Toast.LENGTH_SHORT).show();
        if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
            changeOrientation(6);
            ((LinearLayoutManager) Objects.requireNonNull(recyclerView.getLayoutManager())).scrollToPositionWithOffset(firstVisiblePosition, offset);
        }
        else {
            adapter.setData(getListCategory());
            recyclerView.setAdapter(adapter);
            ((LinearLayoutManager) Objects.requireNonNull(recyclerView.getLayoutManager())).scrollToPositionWithOffset(firstVisiblePosition, offset);

        }

    }

    @Override
    public void onPause() {
        super.onPause();
        View firstChild = recyclerView.getChildAt(0);
        if (firstChild != null) {
            firstVisiblePosition = recyclerView.getChildAdapterPosition(firstChild);
            offset = firstChild.getTop();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Toast.makeText(this.getContext(), "Start", Toast.LENGTH_SHORT).show();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_media, container, false);
        context = getContext();
        toolbarSetting();
        //recyclerViewSetting();
        adapter = new CategoryAdapter(getContext(), spanCount);
        recyclerView = view.findViewById(R.id.photo_recyclerview);
        recyclerView.addOnScrollListener(new ToolbarScrollListener(toolbar));
        recyclerView.setItemViewCacheSize(4);
        return view;
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    public void changeOrientation(int spanCount) {
        adapter = new CategoryAdapter(getContext(), spanCount);
        adapter.setData(getListCategory());
        recyclerView.setAdapter(adapter);
        ((LinearLayoutManager) Objects.requireNonNull(recyclerView.getLayoutManager())).scrollToPositionWithOffset(firstVisiblePosition, offset);
    }

    void recyclerViewSetting(){
        adapter = new CategoryAdapter(getContext(), spanCount);
        adapter.setData(getListCategory());
        recyclerView = view.findViewById(R.id.photo_recyclerview);
        recyclerView.setAdapter(adapter);
        recyclerView.setItemViewCacheSize(20);
    }

    void toolbarSetting(){
        toolbar = view.findViewById(R.id.toolbar_photo);
        toolbar.inflateMenu(R.menu.menu_photo);
        toolbar.setTitle(R.string.app_name);
        toolbar.setTitleTextAppearance(context, R.style.ToolbarTitle);
    }

    ArrayList<String> getListMedia(){
        AccessMediaFile.refreshAllMedia();
        var mediaList = AccessMediaFile.getAllMediaFromGallery(getContext());
        Log.d("List-Media", mediaList.toString());
        long hash = 0;
        Map<Long,ArrayList<String>> map = new HashMap<Long,ArrayList<String>>();
        for (Media img: mediaList.values()) {
            Bitmap bitmap = BitmapFactory.decodeFile(img.getPath());
            hash = hashBitmap(bitmap);
            if(map.containsKey(hash)){
                map.get(hash).add(img.getPath());
            }else{
                ArrayList<String> list = new ArrayList<>();
                list.add(img.getPath());
                map.put(hash,list);
            }
        }
        ArrayList<String> result = new ArrayList<>();
        Set<Long> set = map.keySet();
        for (Object key: set) {
            if(map.get(key).size() >=2){

                result.addAll(map.get(key));
            }
        }
        return result;
    }

    public long hashBitmap(Bitmap bmp){
        long hash = 31;
        for(int x = 1; x <  bmp.getWidth(); x=x*2){
            for (int y = 1; y < bmp.getHeight(); y=y*2){
                hash *= (bmp.getPixel(x,y) + 31);
                hash = hash%1111122233;
            }
        }
        return hash;
    }

    @NonNull
    private List<Category> getListCategory() {
        AccessMediaFile.refreshAllMedia();
        HashMap<String, Category> categoryList = new LinkedHashMap<>();
        int categoryCount = 0;
        listMedia = new ArrayList<>(AccessMediaFile.getAllMediaFromGallery(getContext()).values());

        try {
            categoryList.put(listMedia.get(0).getDateTaken(), new Category(listMedia.get(0).getDateTaken(), new ArrayList<>()));
            categoryList.get(listMedia.get(0).getDateTaken()).addMediaToList(listMedia.get(0));
            for (int i = 1; i < listMedia.size(); i++) {
                if (!categoryList.containsKey(listMedia.get(i).getDateTaken())) {
                    categoryList.put(listMedia.get(i).getDateTaken(), new Category(listMedia.get(i).getDateTaken(), new ArrayList<>()));
                    categoryCount++;
                }

                categoryList.get(listMedia.get(i).getDateTaken()).addMediaToList(listMedia.get(i));
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