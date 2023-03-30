package com.group7.gallerium.activities;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.group7.gallerium.R;
import com.group7.gallerium.adapters.AlbumCategoryAdapter;
import com.group7.gallerium.adapters.MediaCategoryAdapter;
import com.group7.gallerium.models.Album;
import com.group7.gallerium.models.AlbumCategory;
import com.group7.gallerium.models.Media;
import com.group7.gallerium.models.MediaCategory;
import com.group7.gallerium.utilities.AccessMediaFile;
import com.group7.gallerium.utilities.FileUtils;
import com.group7.gallerium.utilities.SelectMediaInterface;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class ViewAlbum extends AppCompatActivity implements SelectMediaInterface {

    private String albumName;
    private ArrayList<String> mediaPaths;
    ArrayList<Album> albumList;
    ArrayList<Media> selectedMedia;
    ArrayList<AlbumCategory> albumCategories;
    ArrayList<Media> listMedia;
    private Intent intent;
    MediaCategoryAdapter adapter;
    private int spanCount = 3;

    private Toolbar toolbar;
    private RecyclerView album_rec_item;

    RecyclerView addAlbumRecyclerView;

    private int firstVisiblePosition;
    private int offset;

    boolean isAllChecked = false;
    ActionMode mode;

    private ActionMode.Callback callback;

    private ActivityResultLauncher<IntentSenderRequest> launcher;

    LinearLayout bottom_sheet;
    BottomSheetBehavior behavior;

    BottomSheetDialog bottomSheetDialog;
    private TextView btnShare, btnMove, btnDelete, btnCreative, btnCopy, btnFav;

    private boolean changeMode = false;

    boolean isPendingForIntent = false;

    FileUtils fileUtils;
    AlbumCategoryAdapter albumAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_album);
        intent = getIntent();
        mediaPaths = intent.getStringArrayListExtra("media_paths");
        albumName = intent.getStringExtra("name");

        selectedMedia = new ArrayList<>();
        listMedia = new ArrayList<>();
        albumList = new ArrayList<>();
        albumCategories = new ArrayList<>();

        fileUtils = new FileUtils();
        for(String path: mediaPaths){
            Media media = AccessMediaFile.getMediaWithPath(path);
            listMedia.add(media);
        }
        toolbarSetting();

        album_rec_item = findViewById(R.id.rec_menu_item);

        adapter = new MediaCategoryAdapter(this, spanCount, this);

        // album_rec_item.addOnScrollListener(new ToolbarScrollListener(toolbar));
        album_rec_item.setItemViewCacheSize(4);

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
                if(selectedMedia.size() == 0)
                    actionMode.setTitle("Chọn mục");
                else{
                    getMenuInflater().inflate(R.menu.menu_multiple_select, menu);
                    actionMode.setTitle("Đã chọn " + selectedMedia.size() + " mục");
                }
                return true;
            }

            @Override
            public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
                bottom_sheet.setVisibility(View.VISIBLE);
                if(menuItem.getItemId() == R.id.select_all_item) {
                    selectedMedia.clear();
                    adapter.setAllChecked(false);
                    if(isAllChecked){
                        isAllChecked = false;
                        if(mode != null) mode.setTitle("Chọn mục");
                    }else{
                        selectedMedia.addAll(listMedia);
                        isAllChecked = true;
                        if(mode != null) mode.setTitle("Đã chọn " + selectedMedia.size() + " mục");
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
                bottom_sheet.setVisibility(View.GONE);
                bottom_sheet.setVisibility(View.GONE);
                if (bottomSheetDialog != null) {
                    bottomSheetDialog.cancel();
                }
                // refresh();
            }
        };

        launcher = registerForActivityResult(
                new ActivityResultContracts.StartIntentSenderForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Toast.makeText(getApplicationContext(), "deleted", Toast.LENGTH_SHORT).show();
                        for(var m : selectedMedia) {
                            AccessMediaFile.removeMediaFromAllMedia(m.getPath());
                            listMedia.remove(m);
                            mediaPaths.remove(m.getPath());
                        }
                    }
                    isPendingForIntent = false;
                    refresh();
                });

        bottomSheetConfig();
        behavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        bottom_sheet.setVisibility(View.GONE);
        bottomSheetButtonConfig();
    }

    void bottomSheetConfig() {

        bottom_sheet = findViewById(R.id.bottom_sheet);
        behavior = BottomSheetBehavior.from(bottom_sheet);
        behavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {

            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_HIDDEN ->
                            Toast.makeText(getApplicationContext(), "Hidden sheet", Toast.LENGTH_SHORT).show();
                    case BottomSheetBehavior.STATE_EXPANDED ->
                            Toast.makeText(getApplicationContext(), "Expand sheet", Toast.LENGTH_SHORT).show();
                    case BottomSheetBehavior.STATE_COLLAPSED ->
                            Toast.makeText(getApplicationContext(), "Collapsed sheet", Toast.LENGTH_SHORT).show();
                    case BottomSheetBehavior.STATE_DRAGGING ->
                            Toast.makeText(getApplicationContext(), "Dragging sheet", Toast.LENGTH_SHORT).show();
                    case BottomSheetBehavior.STATE_SETTLING ->
                            Toast.makeText(getApplicationContext(), "Settling sheet", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });
    }

    void bottomSheetButtonConfig() {
        btnMove = findViewById(R.id.move_album_button);
        btnCopy = findViewById(R.id.copy_album_button);
        btnDelete = findViewById(R.id.delete_button);
        btnShare = findViewById(R.id.share_button);
        btnCreative = findViewById(R.id.create_button);
        btnFav = findViewById(R.id.add_to_fav_button);

        btnCopy.setOnClickListener((v)->{
            changeMode = false;
            openAlbumSelectView();
        });
        btnMove.setOnClickListener((v) -> {
            changeMode = true;
            openAlbumSelectView();
        });
        btnShare.setOnClickListener((v) -> {
            if (selectedMedia.size() <= 0) return;
            // Create intent to deliver some kind of result data
            if (selectedMedia.size() == 1) {
                Intent result = new Intent(Intent.ACTION_SEND);
                var m = selectedMedia.get(0);
                result.putExtra(Intent.EXTRA_STREAM, new FileUtils().getUri(m.getPath(), m.getType(), this));
//                    ArrayList<Uri> uris = new ArrayList<>();
//                    uris.add(new FileUtils().getUri(m.getPath(), m.getType(), this));
//
//                    result.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
                result.putExtra(Intent.EXTRA_SUBJECT, "Pictures");
                result.putExtra(Intent.EXTRA_TEXT, "Pictures share");
                if (m.getType() == 1) {
                    result.setType("image/*");
                } else {
                    result.setType("video/*");
                }
                startActivity(result);
            } else {
                // Create intent to deliver some kind of result data
                Intent result = new Intent(Intent.ACTION_SEND_MULTIPLE);
                ArrayList<Uri> uris = new ArrayList<>();
                for (var m : selectedMedia) {
                    uris.add(new FileUtils().getUri(m.getPath(), m.getType(), this));
                }
                result.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
                result.putExtra(Intent.EXTRA_SUBJECT, "Pictures");
                result.putExtra(Intent.EXTRA_TEXT, "Pictures share");
                result.setType("*/*");
                startActivity(result);
            }
        });
        btnDelete.setOnClickListener((v) -> deleteMedia());
        btnCreative.setOnClickListener((v) -> {

        });
        btnFav.setOnClickListener((v) -> {
            addToFavorite();
        });
    }

    private void addToFavorite() {
        for(var m : selectedMedia) {
            AccessMediaFile.addToFavMedia(m.getPath());
        }
        refresh();
        callback.onDestroyActionMode(mode);
    }

    private void deleteMedia() {
        //saveScroll();
        new AsyncTask<Void, Integer, Void>() {
            @Override
            protected void onPostExecute(Void unused) {
                super.onPostExecute(unused);
                callback.onDestroyActionMode(mode);
                isPendingForIntent = false;
                refresh();
            }

            @Override
            protected Void doInBackground(Void... voids) {
                if (fileUtils.deleteMultiple(launcher, selectedMedia, getApplicationContext()) > 0) {
                    for(var m : selectedMedia) {
                        AccessMediaFile.removeMediaFromAllMedia(m.getPath());
                        listMedia.remove(m);
                        mediaPaths.remove(m.getPath());
                    }
                }
                else {
                    isPendingForIntent = true;
                }
                while (isPendingForIntent) {}
                return null;
            }

//        refresh();
//        callback.onDestroyActionMode(mode);
        }.execute();
    }

    void refresh(){
        adapter.setData(getListCategory());
        callback.onDestroyActionMode(mode);
        album_rec_item.setAdapter(adapter);
    }


    void openAlbumSelectView() {
        Log.d("Open bottom sheet", "true");
        View viewDialog = LayoutInflater.from(this).inflate(R.layout.add_to_album_bottom_dialog, null);
        addAlbumRecyclerView = viewDialog.findViewById(R.id.rec_add_to_album);
        addAlbumRecyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(viewDialog);
        AlbumListTask albumAsyncTask = new AlbumListTask();
        albumAsyncTask.execute();
    }



    @Override
    public void onResume() {
        super.onResume();
        Toast.makeText(this, "Resuming", Toast.LENGTH_SHORT).show();
        var sharedPref =
                PreferenceManager.getDefaultSharedPreferences(this);
        var numGridPref = sharedPref.getString(SettingsActivity.KEY_PREF_NUM_GRID, "3");
        if(numGridPref.equals("5")){
            spanCount = 5;
        }else if(numGridPref.equals("4")){
            spanCount = 4;
        }else{
            spanCount = 3;
        }
        adapter.setData(getListCategory());
        album_rec_item.setAdapter(adapter);
        if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
            changeOrientation(spanCount * 2);
        }
        else {
            changeOrientation(spanCount);
            adapter.setData(getListCategory());
            album_rec_item.setAdapter(adapter);
        }
    }

    public void onPause() {
        super.onPause();
        View firstChild = album_rec_item.getChildAt(0);
        if (firstChild != null) {
            firstVisiblePosition = album_rec_item.getChildAdapterPosition(firstChild);
            offset = firstChild.getTop();
        }
        var favList = AccessMediaFile.getFavMedia();
        // Log.d("fav", "fav amount pause = " + favList.size());
        favList.forEach(x -> Log.d("fav", x));
        SharedPreferences sharedPreferences = getSharedPreferences("fav_media", MODE_PRIVATE);
        SharedPreferences.Editor myEdit = sharedPreferences.edit();
        myEdit.clear();

        // write all the data entered by the user in SharedPreference and apply
        myEdit.putStringSet("path", favList);
        myEdit.apply();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    public void changeOrientation(int spanCount) {
        View firstChild = album_rec_item.getChildAt(0);
        if (firstChild != null) {
            firstVisiblePosition = album_rec_item.getChildAdapterPosition(firstChild);
            offset = firstChild.getTop();
        }
        adapter = new MediaCategoryAdapter(this, spanCount, this);
        adapter.setData(getListCategory());
        album_rec_item.setAdapter(adapter);
        ((LinearLayoutManager) Objects.requireNonNull(album_rec_item.getLayoutManager())).scrollToPositionWithOffset(firstVisiblePosition, offset);
    }

    void toolbarSetting(){
        toolbar = findViewById(R.id.toolbar_view_album);
        toolbar.inflateMenu(R.menu.menu_top_view_album);
        toolbar.setTitle(R.string.app_name);
        toolbar.setTitleTextAppearance(this, R.style.ToolbarTitle);
        toolbar.setNavigationIcon(R.drawable.ic_back_arrow);
        toolbar.setNavigationOnClickListener((view) -> finish());
        toolbar.setTitle(albumName);
    }

    private List<MediaCategory> getListCategory() {
        HashMap<String, MediaCategory> categoryList = new LinkedHashMap<>();
        Media media;
        List<Media> mediaList = new ArrayList<>();
        for(String path: mediaPaths){
            media = AccessMediaFile.getMediaWithPath(path);
            if(media != null)
                mediaList.add(media);
        }

        try {
            categoryList.put(mediaList.get(0).getDateTaken(), new MediaCategory(mediaList.get(0).getDateTaken(), new ArrayList<>()));
            categoryList.get(mediaList.get(0).getDateTaken()).addMediaToList(mediaList.get(0));
            for (int i = 1; i < mediaList.size(); i++) {
                if (!categoryList.containsKey(mediaList.get(i).getDateTaken())) {
                    categoryList.put(mediaList.get(i).getDateTaken(), new MediaCategory(mediaList.get(i).getDateTaken(), new ArrayList<>()));
                }

                categoryList.get(mediaList.get(i).getDateTaken()).addMediaToList(mediaList.get(i));
            }
            var newCatList = new ArrayList<MediaCategory>();
            int partitionSize = 60;
            for(var cat : categoryList.values()) {
                //cat.getList().sort(Comparator.comparingLong(Media::getRawDate).reversed());
                for (int i = 0; i < cat.getList().size(); i += partitionSize) {

                    newCatList.add(new MediaCategory(cat.getNameCategory(), new ArrayList<>(cat.getList().subList(i,
                            Math.min(i + partitionSize, cat.getList().size())))));

                }
            }

            return newCatList;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void showAllSelect() {
        adapter.setMultipleEnabled(true);
        toolbar.startActionMode(callback);
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        bottom_sheet.setVisibility(View.VISIBLE);
    }

    @Override
    public void addToSelectedList(@NonNull Media media) {
        if(!selectedMedia.contains(media)) {
            selectedMedia.add(media);
            if(mode != null) {
                mode.invalidate();
                mode.setTitle("Đã chọn " +  selectedMedia.size() + " mục");
            }
        }
       // Log.d("size outer", "" + selectedMedia.size());
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
                }
            }
        }
        // Log.d("size outer", "" + selectedMedia.size());
    }

    @Override
    public void moveMedia(@NonNull String albumPath) {
        FileUtils fileUtils = new FileUtils();
        if(changeMode) {
            fileUtils.moveFileMultiple(selectedMedia, launcher, albumPath, getApplicationContext());
        }else{
            fileUtils.copyFileMultiple(selectedMedia, albumPath, getApplicationContext());
        }
        for(var r : selectedMedia) {
            if (r.getPath().lastIndexOf("/") != -1) {
                AccessMediaFile.addToFavMedia(albumPath + r.getPath().substring(r.getPath().lastIndexOf("/")));
            }
        }
        refresh();
        callback.onDestroyActionMode(mode);
    }

    public class AlbumListTask extends AsyncTask<Void, Integer, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            albumAdapter = new AlbumCategoryAdapter(getApplicationContext(), ViewAlbum.this, 3);
            albumAdapter.setViewType(1);
            albumAdapter.setData(albumCategories);
            addAlbumRecyclerView.setAdapter(albumAdapter);
            bottomSheetDialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            ArrayList<Media> listMediaTemp = AccessMediaFile.getAllMedia(getApplicationContext());
            albumList = getAllAlbum(listMediaTemp);
            categorizeWithExcludeAlbum();

            return null;
        }
    }

    @NonNull
    public void rescanForUnAddedAlbum(){
        Cursor cursor =  getContentResolver().query(
                MediaStore.Files.getContentUri("external")
                , new String[]{MediaStore.Files.FileColumns.DISPLAY_NAME, MediaStore.Files.FileColumns.PARENT, MediaStore.Files.FileColumns.DATA}
                , MediaStore.Files.FileColumns.DATA + " LIKE ?"
                , new String[]{Environment.getExternalStorageDirectory() + "/Pictures/owner/%"}, null);

        int nameColumn = cursor.getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME);
        // int bucketNameColumn = cursor.getColumnIndex(MediaStore.Files.FileColumns.PARENT);
        int pathColumn = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA);
        String name;
        try {
            if (cursor != null) {
                Log.d("size", "" + cursor.getCount());

                ArrayList<Album> temp = new ArrayList<>();
                while (cursor.moveToNext()) {
                    String path = cursor.getString(pathColumn);
                    Log.d("path", path);
                    name = cursor.getString(nameColumn);
                    Log.d("name", name);
                    String[] subDirs = path.split("/");
                    if(!subDirs[subDirs.length-2].equals("owner")) continue;
                    Album album = new Album(null, name);
                    album.setPath(path);
                    temp.add(album);
                }
                for(Album album: albumList){
                    for(int i=0;i<temp.size();i++){
                        if(temp.get(i).getPath().equals(album.getPath())){
                            temp.remove(i);
                        }
                    }
                }
                if(temp.size() >0)albumList.addAll(temp);
            }
        }catch (Exception e){
            Log.d("tag", e.getMessage());
        }
    }

    public ArrayList<Album> getAllAlbum(ArrayList<Media> listMedia){
        List<String> paths = new ArrayList<>();
        ArrayList<Album> albums = new ArrayList<>();
        for(var a : AccessMediaFile.getAllYourAlbum()) {
            String[] subDirs = a.split("/");
            String name = subDirs[subDirs.length - 1];
            Album album = new Album(null, name);
            album.setPath(a);
            paths.add(a);
            albums.add(album);
        }

        for (int i = 0; i < listMedia.size(); i++) {
            String[] subDirectories = listMedia.get(i).getPath().split("/");
            String folderPath = listMedia.get(i).getPath().substring(0, listMedia.get(i).getPath().lastIndexOf("/"));
            String name = subDirectories[subDirectories.length - 2];
            if (name.equals("t3mp")) continue;
            if (!paths.contains(folderPath)) {
                paths.add(folderPath);
                Album album = new Album(listMedia.get(i), name);
                album.setPath(folderPath);
                if (listMedia.get(i).getHeight() != 0) {
                    album.addMedia(listMedia.get(i));
                }
                albums.add(album);
            } else {
                if (listMedia.get(i).getHeight() != 0) {
                    albums.get(paths.indexOf(folderPath)).addMedia(listMedia.get(i));
                    if (albums.get(paths.indexOf(folderPath)).getAvatar() == null) {
                        albums.get(paths.indexOf(folderPath)).setAvatar(listMedia.get(i));
                    }
                }
            }
        }
//        for(Album album: albums){
//            Log.d("album", album.toString());
//        }
        return albums;
    }


    public void categorizeWithExcludeAlbum() {
        HashMap<String, AlbumCategory> categoryList = new LinkedHashMap<>();
        String[] subDir;
        categoryList.put("Mặc định", new AlbumCategory("Mặc định", new ArrayList<>()));
        categoryList.put("Của tôi", new AlbumCategory("Của tôi", new ArrayList<>()));
        categoryList.put("Thêm album", new AlbumCategory("Thêm album", new ArrayList<>()));

        rescanForUnAddedAlbum();
        for (Album album : albumList) {
            if(Objects.equals(album.getName(), albumName)) continue;
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
            //Log.d("value", album.getPath() + " " + album.getName() + " " + album.getListMedia().size());
            albumList.addAll(entry.getValue().getList());
        }
    }
}