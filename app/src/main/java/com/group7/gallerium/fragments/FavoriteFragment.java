package com.group7.gallerium.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.group7.gallerium.R;
import com.group7.gallerium.adapters.MediaCategoryAdapter;
import com.group7.gallerium.models.Media;
import com.group7.gallerium.models.MediaCategory;
import com.group7.gallerium.utilities.AccessMediaFile;
import com.group7.gallerium.utilities.FileUtils;
import com.group7.gallerium.utilities.SelectMediaInterface;
import com.group7.gallerium.utilities.ToolbarScrollListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("rawtypes")
public class FavoriteFragment extends Fragment  implements SelectMediaInterface {

    public FavoriteFragment() {}

    private View view;
    private Toolbar toolbar;
    Context context;
    private ArrayList<Media> listMedia;
    ArrayList<Media> selectedMedia;
    MediaCategoryAdapter adapter;
    private RecyclerView recyclerView;

    private RecyclerView addAlbumRecyclerView;
    private int spanCount = 3;
    private int firstVisiblePosition;
    private int offset;
    boolean isAllChecked = false;
    ActionMode mode;
    private ActionMode.Callback callback;

    LinearLayout bottom_sheet;
    private BottomSheetBehavior behavior;
    private BottomSheetDialog bottomSheetDialog;

    private TextView btnShare, btnMove, btnDelete, btnCreative, btnCopy;

    private ActivityResultLauncher<IntentSenderRequest> launcher;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        selectedMedia = new ArrayList<>();

        launcher = registerForActivityResult(
                new ActivityResultContracts.StartIntentSenderForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Toast.makeText(context.getApplicationContext(), "deleted", Toast.LENGTH_SHORT).show();

                        for(Media media: selectedMedia) {
                            AccessMediaFile.removeMediaFromAllMedia(media.getPath());
                        }
                    }
                    callback.onDestroyActionMode(mode);
                    adapter.setData(getListCategory());
                    recyclerView.setAdapter(adapter);
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        Toast.makeText(this.getContext(), "Resuming", Toast.LENGTH_SHORT).show();
        if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
            //changeOrientation(6);
            ((LinearLayoutManager) Objects.requireNonNull(recyclerView.getLayoutManager())).scrollToPositionWithOffset(firstVisiblePosition, offset);
        }
        else {
            //changeOrientation(3);
            ((LinearLayoutManager) Objects.requireNonNull(recyclerView.getLayoutManager())).scrollToPositionWithOffset(firstVisiblePosition, offset);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        saveScroll();
    }

    private void saveScroll() {
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
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            changeOrientation(6);
        } else {
            changeOrientation(3);
        }
        refresh();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_favorite, container, false);
        context = getContext();
        toolbarSetting();
        //recyclerViewSetting();
        adapter = new MediaCategoryAdapter(getContext(), spanCount, this);
        recyclerView = view.findViewById(R.id.photo_recyclerview);
        recyclerView.setItemViewCacheSize(4);

        bottomSheetConfig();
        behavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        bottom_sheet.setVisibility(View.GONE);
        bottomSheetButtonConfig();
        recyclerView.addOnScrollListener(new ToolbarScrollListener(toolbar, bottom_sheet));
        callback = new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                mode = actionMode;
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
                // remove previous items
                menu.clear();
                mode = actionMode;
                if (selectedMedia.size() == 0)
                    actionMode.setTitle("Chọn mục");
                else {
                    requireActivity().getMenuInflater().inflate(R.menu.menu_multiple_select, menu);
                    actionMode.setTitle("Đã chọn " + selectedMedia.size() + " mục");
                }
                return true;
            }

            @Override
            public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
                bottom_sheet.setVisibility(View.VISIBLE);
                if (menuItem.getItemId() == R.id.select_all_item) {
                    selectedMedia.clear();
                    adapter.setAllChecked(false);
                    if (isAllChecked) {
                        isAllChecked = false;
                        if (mode != null) mode.setTitle("Chọn mục");
                    } else {
                        selectedMedia.addAll(listMedia);
                        isAllChecked = true;
                        if (mode != null) mode.setTitle("Đã chọn " + selectedMedia.size() + " mục");
                    }
                    adapter.setAllChecked(isAllChecked);
                    return true;
                }
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode actionMode) {
                selectedMedia.clear();
                adapter.setMultipleEnabled(false);
                mode = null;
                requireActivity().findViewById(R.id.bottom_navigation).setVisibility(View.VISIBLE);
                behavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                bottom_sheet.setVisibility(View.GONE);
            }
        };

        return view;
    }

    void bottomSheetConfig(){

        bottom_sheet = view.findViewById(R.id.bottom_sheet);
        behavior = BottomSheetBehavior.from(bottom_sheet);
        behavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback(){

            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_HIDDEN ->
                            Toast.makeText(context, "Hidden sheet", Toast.LENGTH_SHORT).show();
                    case BottomSheetBehavior.STATE_EXPANDED ->
                            Toast.makeText(context, "Expand sheet", Toast.LENGTH_SHORT).show();
                    case BottomSheetBehavior.STATE_COLLAPSED ->
                            Toast.makeText(context, "Collapsed sheet", Toast.LENGTH_SHORT).show();
                    case BottomSheetBehavior.STATE_DRAGGING ->
                            Toast.makeText(context, "Dragging sheet", Toast.LENGTH_SHORT).show();
                    case BottomSheetBehavior.STATE_SETTLING ->
                            Toast.makeText(context, "Settling sheet", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });
    }
    void bottomSheetButtonConfig(){
        btnMove = view.findViewById(R.id.move_album_button);
        btnDelete = view.findViewById(R.id.delete_button);
        btnShare = view.findViewById(R.id.share_button);
        btnCreative = view.findViewById(R.id.create_button);

        btnMove.setOnClickListener((v)-> openAlbumSelectView());
        btnShare.setOnClickListener((v)->{

        });
        btnDelete.setOnClickListener((v)->{

        });
        btnCreative.setOnClickListener((v)->{

        });
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    void openAlbumSelectView(){
        Log.d("Open bottom sheet", "true");
        View viewDialog = LayoutInflater.from(context).inflate(R.layout.add_to_album_bottom_dialog, null);
        addAlbumRecyclerView = viewDialog.findViewById(R.id.rec_add_to_album);
        addAlbumRecyclerView.setLayoutManager(new GridLayoutManager(context, 3));
        bottomSheetDialog = new BottomSheetDialog(context);
        bottomSheetDialog.setContentView(viewDialog);
        bottomSheetDialog.show();
//        AlbumAsyncTask myAsyncTask = new MyAsyncTask();
//        myAsyncTask.execute();
    }

    public void changeOrientation(int spanCount) {
        saveScroll();
        if (spanCount != this.spanCount) {
            this.spanCount = spanCount;
            adapter = new MediaCategoryAdapter(getContext(), spanCount, this);
            refresh();
        }
        recyclerView.setAdapter(adapter);

        if (bottomSheetDialog != null) {
            bottomSheetDialog.cancel();
            selectedMedia.clear();
            mode.finish();
            bottom_sheet.setVisibility(View.GONE);
            requireActivity().findViewById(R.id.bottom_navigation).setVisibility(View.VISIBLE);
        }
        ((LinearLayoutManager) Objects.requireNonNull(recyclerView.getLayoutManager())).scrollToPositionWithOffset(firstVisiblePosition, offset);
    }


    public void refresh() {
        adapter.setData(getListCategory());
    }

    public void refresh(boolean scroll) {
        ((LinearLayoutManager) Objects.requireNonNull(recyclerView.getLayoutManager())).scrollToPosition(0);
        adapter.setData(getListCategory());
    }

    void recyclerViewSetting(){
        adapter = new MediaCategoryAdapter(getContext(), spanCount, this);
        adapter.setData(getListCategory());
        recyclerView = view.findViewById(R.id.photo_recyclerview);
        recyclerView.setAdapter(adapter);
        recyclerView.setItemViewCacheSize(20);
    }

    void toolbarSetting(){
        toolbar = view.findViewById(R.id.toolbar_favorite);
        toolbar.inflateMenu(R.menu.menu_favorite);
        toolbar.setTitle(R.string.fav);
        toolbar.setTitleTextAppearance(context, R.style.ToolbarTitle);
    }

//    ArrayList<String> getListMedia(){
//        AccessMediaFile.refreshAllMedia();
//        var mediaList = AccessMediaFile.getAllMediaFromGallery(getContext());
//        Log.d("List-Media", mediaList.toString());
//        long hash = 0;
//        Map<Long,ArrayList<String>> map = new HashMap<Long,ArrayList<String>>();
//        for (Media img: mediaList.values()) {
//            Bitmap bitmap = BitmapFactory.decodeFile(img.getPath());
//            hash = hashBitmap(bitmap);
//            if(map.containsKey(hash)){
//                map.get(hash).add(img.getPath());
//            }else{
//                ArrayList<String> list = new ArrayList<>();
//                list.add(img.getPath());
//                map.put(hash,list);
//            }
//        }
//        ArrayList<String> result = new ArrayList<>();
//        Set<Long> set = map.keySet();
//        for (Object key: set) {
//            if(map.get(key).size() >=2){
//
//                result.addAll(map.get(key));
//            }
//        }
//        return result;
//    }

    private List<MediaCategory> getListCategory() {
        AccessMediaFile.refreshAllMedia();
        HashMap<String, MediaCategory> categoryList = new LinkedHashMap<>();
        listMedia = new ArrayList<>(AccessMediaFile.getAllFavMedia());

        try {
            categoryList.put(listMedia.get(0).getDateTaken(), new MediaCategory(listMedia.get(0).getDateTaken(), new ArrayList<>()));
            Objects.requireNonNull(categoryList.get(listMedia.get(0).getDateTaken())).addMediaToList(listMedia.get(0));
            for (int i = 1; i < listMedia.size(); i++) {
                if (!categoryList.containsKey(listMedia.get(i).getDateTaken())) {
                    categoryList.put(listMedia.get(i).getDateTaken(), new MediaCategory(listMedia.get(i).getDateTaken(), new ArrayList<>()));
                }

                Objects.requireNonNull(categoryList.get(listMedia.get(i).getDateTaken())).addMediaToList(listMedia.get(i));
            }
//            categoryList.forEach(x -> {
//                Log.d("gallerium", x.getNameCategory() + ": " + x.getList().size());
//            });
            var newCatList = new ArrayList<MediaCategory>();
            int partitionSize = 60;
            for(var cat : categoryList.values()) {
                // cat.getList().sort(Comparator.comparingLong(Media::getRawDate).reversed());
                for (int i = 0; i < cat.getList().size(); i += partitionSize) {

                    newCatList.add(new MediaCategory(cat.getNameCategory(), new ArrayList<>(cat.getList().subList(i,
                            Math.min(i + partitionSize, cat.getList().size())))));

                }
            }
            AccessMediaFile.getAllFavMedia().forEach(x -> Log.d("fav", x.getRawDate() + ": " + x.getPath()));

            return newCatList;
        } catch (Exception e) {
            return null;
        }

    }

    @Override
    public void showAllSelect() {
        adapter.setMultipleEnabled(true);
        toolbar.startActionMode(callback);
        requireActivity().findViewById(R.id.bottom_navigation).setVisibility(View.GONE);
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        bottom_sheet.setVisibility(View.VISIBLE);
    }

    @Override
    public void addToSelectedList(@NonNull Media media) {
        if(!selectedMedia.contains(media)) {
            selectedMedia.add(media);
            for ( int i = 0; i < bottom_sheet.getChildCount();  i++ ){
                View view = bottom_sheet.getChildAt(i);
                view.setEnabled(true);
            }
            if(mode != null) {
                mode.invalidate();
                mode.setTitle("Đã chọn " +  selectedMedia.size() + " mục");
            }
        }
        //Log.d("size outer", "" + selectedMedia.size());
    }

    @NonNull
    @Override
    public ArrayList<Media> getSelectedList() {
        return selectedMedia;
    }

    @Override
    public void deleteFromSelectedList(@NonNull Media media) {
        if(selectedMedia.contains(media)) {
            selectedMedia.remove(media);
            if(mode != null){
                mode.invalidate();
                mode.setTitle("Đã chọn " + selectedMedia.size() + " mục");
                if(selectedMedia.isEmpty()){
                    mode.setTitle("Chọn mục");
                    for ( int i = 0; i < bottom_sheet.getChildCount();  i++ ){
                        View view = bottom_sheet.getChildAt(i);
                        view.setEnabled(false);
                    }
                }
            }
        }
        //Log.d("size outer", "" + selectedMedia.size());
    }

    @Override
    public void moveMedia(@NonNull String albumPath) {
        FileUtils fileUtils = new FileUtils();
        for (Media media : selectedMedia) {
            String[] subDir = media.getPath().split("/");
            String name = subDir[subDir.length - 1];
            fileUtils.moveFile(media.getPath(), launcher, albumPath, context);
        }
        bottomSheetDialog.cancel();
        adapter.setData(getListCategory());
        selectedMedia.clear();
        mode.finish();
        bottom_sheet.setVisibility(View.GONE);
        requireActivity().findViewById(R.id.bottom_navigation).setVisibility(View.VISIBLE);
    }

}
