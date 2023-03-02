package com.group7.gallerium.fragments;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.group7.gallerium.R;
import com.group7.gallerium.adapters.CategoryAdapter;
import com.group7.gallerium.models.Category;
import com.group7.gallerium.models.Media;
import com.group7.gallerium.utilities.AccessMediaFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    public MediaFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        //Toast.makeText(this.getContext(), "Resuming", Toast.LENGTH_SHORT).show();
        recyclerViewSetting();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_photo, container, false);
        context = getContext();
        toolbarSetting();
        recyclerViewSetting();
        return view;
    }

    public void changeOrientation(int spanCount) {
        adapter = new CategoryAdapter(getContext(), spanCount);
        adapter.setData(getListCategory());
        recyclerView.setAdapter(adapter);
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
        List<Media> mediaList = AccessMediaFile.getAllMediaFromGallery(getContext());
        Log.d("List-Media", mediaList.toString());
        long hash = 0;
        Map<Long,ArrayList<String>> map = new HashMap<Long,ArrayList<String>>();
        for (Media img: mediaList) {
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
        List<Category> categoryList = new ArrayList<>();
        int categoryCount = 0;
        listMedia = (ArrayList<Media>) AccessMediaFile.getAllMediaFromGallery(getContext());

        try {
            categoryList.add(new Category(listMedia.get(0).getDateTaken(), new ArrayList<>()));
            categoryList.get(categoryCount).addMediaToList(listMedia.get(0));
            for (int i = 1; i < listMedia.size(); i++) {
                if (!listMedia.get(i).getDateTaken().equals(listMedia.get(i - 1).getDateTaken())) {
                    categoryList.add(new Category(listMedia.get(i).getDateTaken(), new ArrayList<>()));
                    categoryCount++;
                }
                categoryList.get(categoryCount).addMediaToList(listMedia.get(i));
            }
            return categoryList;
        } catch (Exception e) {
            return null;
        }

    }
}