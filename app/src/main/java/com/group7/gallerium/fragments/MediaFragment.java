package com.group7.gallerium.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.group7.gallerium.R;
import com.group7.gallerium.activities.CameraActivity;
import com.group7.gallerium.adapters.AlbumCategoryAdapter;
import com.group7.gallerium.adapters.MediaCategoryAdapter;
import com.group7.gallerium.models.Album;
import com.group7.gallerium.models.AlbumCategory;
import com.group7.gallerium.models.Media;
import com.group7.gallerium.models.MediaCategory;
import com.group7.gallerium.utilities.AccessMediaFile;
import com.group7.gallerium.utilities.FileUtils;
import com.group7.gallerium.utilities.SelectMediaInterface;
import com.group7.gallerium.utilities.ToolbarScrollListener;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


public class MediaFragment extends Fragment  implements SelectMediaInterface {
    private View view;
    private Toolbar toolbar;
    private MenuItem cameraButton;
    private Context context;
    private ArrayList<Media> listMedia;
    private ArrayList<Media> selectedMedia;
    private ArrayList<MediaCategory> mediaCategories;
    private ArrayList<Album> albumList;
    private ArrayList<AlbumCategory> albumCategories;
    private MediaCategoryAdapter adapter;

    private AlbumCategoryAdapter albumAdapter;
    private RecyclerView recyclerView;

    private RecyclerView addAlbumRecyclerView;
    private int spanCount = 3;
    private int firstVisiblePosition;
    private int offset;
    private boolean isAllChecked = false;

    private boolean changeMode = false;
    private ActionMode mode;
    private ActionMode.Callback callback;

    private static FileUtils fileUtils;

    private LinearLayout bottom_sheet;
    private BottomSheetBehavior behavior;
    private BottomSheetDialog bottomSheetDialog;

    private TextView btnShare, btnMove, btnDelete, btnCreative, btnCopy;
    private MediaFragment.MediaListTask mediaListTask;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        selectedMedia = new ArrayList<>();
        albumList = new ArrayList<>();
        albumCategories = new ArrayList<>();
        mediaCategories = new ArrayList<>();
        fileUtils = new FileUtils();
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
                    refresh();
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        //Toast.makeText(this.getContext(), "Resuming", Toast.LENGTH_SHORT).show();
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            //changeOrientation(6);
            ((LinearLayoutManager) Objects.requireNonNull(recyclerView.getLayoutManager())).scrollToPositionWithOffset(firstVisiblePosition, offset);
        } else {
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
            // Log.d("pos", firstVisiblePosition + "");
        }
    }

    private  ActivityResultLauncher<IntentSenderRequest> launcher;

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
        view = inflater.inflate(R.layout.fragment_media, container, false);
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
                if (actionMode != null) {
                    actionMode.finish();
                }
                mode = null;
                requireActivity().findViewById(R.id.bottom_navigation).setVisibility(View.VISIBLE);
                behavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                bottom_sheet.setVisibility(View.GONE);
                if (bottomSheetDialog != null) {
                    bottomSheetDialog.cancel();
                }
            }
        };

        return view;
    }

    void bottomSheetConfig() {

        bottom_sheet = view.findViewById(R.id.bottom_sheet);
        behavior = BottomSheetBehavior.from(bottom_sheet);
        behavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {

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

    void bottomSheetButtonConfig() {
        btnMove = view.findViewById(R.id.move_album_button);
        btnCopy = view.findViewById(R.id.copy_album_button);
        btnDelete = view.findViewById(R.id.delete_button);
        btnShare = view.findViewById(R.id.share_button);
        btnCreative = view.findViewById(R.id.create_button);

        btnCopy.setOnClickListener((v)->{
            changeMode = false;
            openAlbumSelectView();
        });
        btnMove.setOnClickListener((v) -> {
            changeMode = true;
            openAlbumSelectView();
        });
        btnShare.setOnClickListener((v) -> {

        });
        btnDelete.setOnClickListener((v) -> {
            deleteMedia();
        });
        btnCreative.setOnClickListener((v) -> {

        });
    }

    private void deleteMedia() {
        for(Media media: selectedMedia) {
            fileUtils.delete(launcher, media.getPath(), context);
        }
        refresh();
        callback.onDestroyActionMode(mode);

    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    void openAlbumSelectView() {
        Log.d("Open bottom sheet", "true");
        View viewDialog = LayoutInflater.from(context).inflate(R.layout.add_to_album_bottom_dialog, null);
        addAlbumRecyclerView = viewDialog.findViewById(R.id.rec_add_to_album);
        addAlbumRecyclerView.setLayoutManager(new GridLayoutManager(context, 1));
        bottomSheetDialog = new BottomSheetDialog(context);
        bottomSheetDialog.setContentView(viewDialog);
        AlbumListTask albumAsyncTask = new AlbumListTask();
        albumAsyncTask.execute();
    }

    public void changeOrientation(int spanCount) {
        saveScroll();
        if (spanCount != this.spanCount) {
            this.spanCount = spanCount;
            adapter = new MediaCategoryAdapter(getContext(), spanCount, this);
            refresh();
        }
        callback.onDestroyActionMode(mode);
        ((LinearLayoutManager) Objects.requireNonNull(recyclerView.getLayoutManager())).scrollToPosition(firstVisiblePosition);
    }

    public void refresh() {
        Log.d("refresh", "");
        mediaListTask = new MediaListTask();
        mediaListTask.execute();
    }

    public void refresh(boolean scroll) {
        Log.d("refresh with result", "");
        mediaListTask = new MediaListTask(scroll);
        mediaListTask.execute();
    }

    void recyclerViewSetting() {
        adapter = new MediaCategoryAdapter(getContext(), spanCount, this);
        adapter.setData(getListCategory());
        recyclerView = view.findViewById(R.id.photo_recyclerview);
        recyclerView.setAdapter(adapter);
        recyclerView.setItemViewCacheSize(3);
    }

    void toolbarSetting() {
        toolbar = view.findViewById(R.id.toolbar_photo);
        toolbar.inflateMenu(R.menu.menu_photo);
        toolbar.setTitle(R.string.app_name);
        toolbar.setTitleTextAppearance(context, R.style.ToolbarTitle);
        cameraButton = toolbar.getMenu().findItem(R.id.take_photo_tb_item);
        cameraButton.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(@NonNull MenuItem menuItem) {
                enableCamera();
                return false;
            }
        });
    }

    private void enableCamera() {
        Intent intent = new Intent(getActivity(), CameraActivity.class);
        startActivity(intent);
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

    public long hashBitmap(Bitmap bmp) {
        long hash = 31;
        for (int x = 1; x < bmp.getWidth(); x = x * 2) {
            for (int y = 1; y < bmp.getHeight(); y = y * 2) {
                hash *= (bmp.getPixel(x, y) + 31);
                hash = hash % 1111122233;
            }
        }
        return hash;
    }

    @NonNull
    private ArrayList<MediaCategory> getListCategory() {
        AccessMediaFile.refreshAllMedia();
        HashMap<String, MediaCategory> categoryList = new LinkedHashMap<>();
        listMedia = AccessMediaFile.getAllMedia(getContext());

        try {
            categoryList.put(listMedia.get(0).getDateTaken(), new MediaCategory(listMedia.get(0).getDateTaken(), new ArrayList<>()));
            categoryList.get(listMedia.get(0).getDateTaken()).addMediaToList(listMedia.get(0));
            for (int i = 1; i < listMedia.size(); i++) {
                if (!categoryList.containsKey(listMedia.get(i).getDateTaken())) {
                    categoryList.put(listMedia.get(i).getDateTaken(), new MediaCategory(listMedia.get(i).getDateTaken(), new ArrayList<>()));
                }

                categoryList.get(listMedia.get(i).getDateTaken()).addMediaToList(listMedia.get(i));
            }
//            categoryList.forEach(x -> {
//                Log.d("gallerium", x.getNameCategory() + ": " + x.getList().size());
//            });
            var newCatList = new ArrayList<MediaCategory>();
            int partitionSize = 60;
            for (var cat : categoryList.values()) {
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
    public void addToSelectedList(Media media) {
        if (!selectedMedia.contains(media)) {
            selectedMedia.add(media);
            for (int i = 0; i < bottom_sheet.getChildCount(); i++) {
                View view = bottom_sheet.getChildAt(i);
                view.setEnabled(true);
            }
            if (mode != null) {
                mode.invalidate();
                mode.setTitle("Đã chọn " + selectedMedia.size() + " mục");
            }
        }
        //Log.d("size outer", "" + selectedMedia.size());
    }

    @Override
    public ArrayList<Media> getSelectedList() {
        return selectedMedia;
    }

    @Override
    public void deleteFromSelectedList(Media media) {
        if (selectedMedia.contains(media)) {
            selectedMedia.remove(media);
            if (mode != null) {
                mode.invalidate();
                mode.setTitle("Đã chọn " + selectedMedia.size() + " mục");
                if (selectedMedia.isEmpty()) {
                    mode.setTitle("Chọn mục");
                    for (int i = 0; i < bottom_sheet.getChildCount(); i++) {
                        View view = bottom_sheet.getChildAt(i);
                        view.setEnabled(false);
                    }
                }
            }
        }
        //Log.d("size outer", "" + selectedMedia.size());
    }

    @Override
    public void moveMedia(String albumPath) {
        FileUtils fileUtils = new FileUtils();
        if(changeMode) {
            for (Media media : selectedMedia) {
                fileUtils.moveFile(media.getPath(), launcher, albumPath, context);
            }
        }else{
            for (Media media : selectedMedia) {
                fileUtils.copyFile(media.getPath(), albumPath, context);
            }
        }
        refresh();
    }


    public class AlbumListTask extends AsyncTask<Void, Integer, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            albumAdapter = new AlbumCategoryAdapter(context, MediaFragment.this, 3);
            albumAdapter.setViewType(1);
            albumAdapter.setData(albumCategories);
            addAlbumRecyclerView.setAdapter(albumAdapter);
            bottomSheetDialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            ArrayList<Media> listMediaTemp = AccessMediaFile.getAllMedia(getContext());
            albumList = getAllAlbum(listMediaTemp);
            categorizeAlbum();

            return null;
        }
    }

    public ArrayList<Album> getAllAlbum(ArrayList<Media> listMedia) {
        List<String> paths = new ArrayList<>();
        ArrayList<Album> albums = new ArrayList<>();

        for (int i = 0; i < listMedia.size(); i++) {
            String[] subDirectories = listMedia.get(i).getPath().split("/");
            String folderPath = listMedia.get(i).getPath().substring(0, listMedia.get(i).getPath().lastIndexOf("/"));
            String name = subDirectories[subDirectories.length - 2];
            if (!paths.contains(folderPath)) {
                paths.add(folderPath);
                Album album = new Album(listMedia.get(i), name);
                album.setPath(folderPath);
                album.addMedia(listMedia.get(i));
                albums.add(album);
            } else {
                albums.get(paths.indexOf(folderPath)).addMedia(listMedia.get(i));
            }
        }

        return albums;
    }


    public void categorizeAlbum() {
        HashMap<String, AlbumCategory> categoryList = new LinkedHashMap<>();
        String[] subDir = albumList.get(0).getPath().split("/");


        categoryList.put("Mặc định", new AlbumCategory("Mặc định", new ArrayList<>()));
        categoryList.put("Thêm album", new AlbumCategory("Thêm album", new ArrayList<>()));
        categoryList.put("Của tôi", new AlbumCategory("Của tôi", new ArrayList<>()));

        for (Album album : albumList) {
            String path = album.getPath();
            subDir = path.split("/");
            String catName = "";
            String parent = subDir[subDir.length - 1];
            if(subDir.length>=2) {
                parent = subDir[subDir.length - 2];
                if (parent.equals("DCIM")) {
                    if (subDir[subDir.length - 1].equals("Camera")
                            || subDir[subDir.length - 1].equals("Screenshots")
                            || subDir[subDir.length - 1].equals("Ảnh")
                            || subDir[subDir.length - 1].equals("Video") ) {
                        //categoryList.get("Mặc định").addAlbumToList(album);
                        catName = "Mặc định";
                    }
                } else if (parent.equals("owner")) {
                    //categoryList.get("Của tôi").addAlbumToList(album);
                    catName = "Của tôi";
                }
            }

            if (catName.length() == 0) {

                catName = "Thêm album";
            }

            boolean needToMerge = false;
            for(Album album1: categoryList.get(catName).getList()){
                if (!album.getPath().equals(album1.getPath()) && album.getName().equalsIgnoreCase(album1.getName()))
                {
                    String path1 = album1.getPath();
                    String[] subDir1 = path1.split("/");
                    String parent1 = subDir1[subDir1.length - 2];
                    if (parent1.equalsIgnoreCase(parent))
                    {
                        // Log.d("merge", "merging " + album.getPath() + " and " + album1.getPath());
                        album1.getListMedia().addAll(album.getListMedia());
                        album1.setListMedia(
                                new ArrayList<>(album1.getListMedia()
                                        .stream()
                                        .sorted(Comparator.comparingLong(Media::getRawDate).reversed())
                                        .collect(Collectors.toList())));
                        needToMerge = true;
                    }
                    else
                    {
                        album.setName(album1.getName() + " (" + parent + ")");
                    }
                    break;
                }
            }

            if (!needToMerge)
            {
                categoryList.get(catName).addAlbumToList(album);
            }

        }

        albumList.clear();
        albumCategories.clear();
        for(Map.Entry<String, AlbumCategory> entry: categoryList.entrySet()){
            // Log.d("Key", entry.getKey());
            albumCategories.add(entry.getValue());
            for(Album album: entry.getValue().getList()){
                albumList.add(album);
                //Log.d("value", album.getPath() + " " + album.getName() + " " + album.getListMedia().size());
            }
        }
    }
    public class MediaListTask extends AsyncTask<Void, Integer, Void> {
        boolean scroll = false;
        public MediaListTask(boolean scroll) {
            this.scroll = scroll;
        }

        public MediaListTask() {}
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            AccessMediaFile.refreshAllMedia();
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            adapter.setData(mediaCategories);
            recyclerView.setAdapter(adapter);
            Log.d("refresh", "refresh media adapter");
            if (scroll) {
                ((LinearLayoutManager) Objects.requireNonNull(recyclerView.getLayoutManager())).scrollToPosition(0);
            } else {
                ((LinearLayoutManager) Objects.requireNonNull(recyclerView.getLayoutManager())).scrollToPositionWithOffset(firstVisiblePosition, offset);
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {
            mediaCategories = getListCategory();
            return null;
        }
    }
}