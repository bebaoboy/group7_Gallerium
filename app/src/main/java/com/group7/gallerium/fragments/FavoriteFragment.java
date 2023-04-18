package com.group7.gallerium.fragments;

import static android.content.Context.MODE_PRIVATE;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.Settings;
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
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.group7.gallerium.BuildConfig;
import com.group7.gallerium.R;
import com.group7.gallerium.activities.SettingsActivity;
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

@SuppressWarnings("rawtypes")
public class FavoriteFragment extends Fragment  implements SelectMediaInterface {

    private MenuItem settingButton;

    public FavoriteFragment() {}

    private View view;
    private Toolbar toolbar;
    Context context;
    private ArrayList<Media> listMedia;
    ArrayList<Media> selectedMedia;
    MediaCategoryAdapter adapter;
    ArrayList<Album> albumList;
    ArrayList<AlbumCategory> albumCategories;

    AlbumCategoryAdapter albumAdapter;
    private RecyclerView recyclerView;

    private RecyclerView addAlbumRecyclerView;
    private int spanCount = 3;
    private int firstVisiblePosition;
    private int offset;
    private boolean changeMode = false;
    boolean isAllChecked = false;
    ActionMode mode;
    private ActionMode.Callback callback;

    LinearLayout bottom_sheet;
    private BottomSheetBehavior behavior;
    private BottomSheetDialog bottomSheetDialog;

    private TextView btnShare, btnMove, btnDelete, btnCreative, btnCopy, btnFav;
    private FavoriteFragment.MediaListTask mediaListTask;
    boolean isPendingForIntent = false;
    static FileUtils fileUtils;
    ArrayList<MediaCategory> mediaCategories;

    private ActivityResultLauncher<IntentSenderRequest> launcher;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        selectedMedia = new ArrayList<>();
        albumList = new ArrayList<>();
        albumCategories = new ArrayList<>();
        mediaCategories = new ArrayList<>();
        fileUtils = new FileUtils();
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
        //Toast.makeText(this.getContext(), "Resuming", Toast.LENGTH_SHORT).show();
        var sharedPref =
                PreferenceManager.getDefaultSharedPreferences(context);
        var numGridPref = sharedPref.getString(SettingsActivity.KEY_PREF_NUM_GRID, "3");
        var numGrid = 0;
        if(numGridPref.equals("5")){
            numGrid = 5;
        }else if(numGridPref.equals("4")){
            numGrid = 4;
        }else{
            numGrid = 3;
        }
        if (numGrid != spanCount) {
            if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                changeOrientation(numGrid * 2);
            } else {
                changeOrientation(numGrid);
            }
        }
        if (selectedMedia.size() == 0) {
            refresh();
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
        //Toast.makeText(this.getContext(), "Start", Toast.LENGTH_SHORT).show();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_favorite, container, false);
        context = requireContext();
        toolbarSetting();
        //recyclerViewSetting();
        adapter = new MediaCategoryAdapter(requireContext(), spanCount, this);
        recyclerView = view.findViewById(R.id.photo_recyclerview);
        recyclerView.setItemViewCacheSize(4);

        bottomSheetConfig();
        behavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        bottom_sheet.setVisibility(View.GONE);
        bottomSheetButtonConfig();
        // recyclerView.addOnScrollListener(new ToolbarScrollListener(toolbar, bottom_sheet));
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

    void bottomSheetConfig(){

        bottom_sheet = view.findViewById(R.id.bottom_sheet);
        behavior = BottomSheetBehavior.from(bottom_sheet);
        behavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback(){

            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    //case BottomSheetBehavior.STATE_HIDDEN ->
   //                         Toast.makeText(context, "Hidden sheet", Toast.LENGTH_SHORT).show();
      //              case BottomSheetBehavior.STATE_EXPANDED ->
         //                   Toast.makeText(context, "Expand sheet", Toast.LENGTH_SHORT).show();
            //        case BottomSheetBehavior.STATE_COLLAPSED ->
               //             Toast.makeText(context, "Collapsed sheet", Toast.LENGTH_SHORT).show();
                  //  case BottomSheetBehavior.STATE_DRAGGING ->
                     //       Toast.makeText(context, "Dragging sheet", Toast.LENGTH_SHORT).show();
                    //case BottomSheetBehavior.STATE_SETTLING ->
                       //     Toast.makeText(context, "Settling sheet", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });
    }
    void bottomSheetButtonConfig(){
        btnMove = view.findViewById(R.id.move_album_button);
        btnCopy = view.findViewById(R.id.copy_album_button);
        btnDelete = view.findViewById(R.id.delete_button);
        btnShare = view.findViewById(R.id.share_button);
        btnCreative = view.findViewById(R.id.create_button);
        var btn = view.findViewById(R.id.add_to_fav_button);
        btn.setVisibility(View.GONE);
        btnFav = view.findViewById(R.id.remove_from_fav_button);
        btnFav.setVisibility(View.VISIBLE);
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
            if (selectedMedia.size() > 500) {
                Toast.makeText(context, "Can only share under 500 files!", Toast.LENGTH_LONG).show();
                return;
            }
            // Create intent to deliver some kind of result data
            if (selectedMedia.size() == 1) {
                Intent result = new Intent(Intent.ACTION_SEND);
                var m = selectedMedia.get(0);
                result.putExtra(Intent.EXTRA_STREAM, new FileUtils().getUri(m.getPath(), m.getType(), requireContext()));
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
                getActivity().startActivity(result);
            } else {
                // Create intent to deliver some kind of result data
                Intent result = new Intent(Intent.ACTION_SEND_MULTIPLE);
                ArrayList<Uri> uris = new ArrayList<>();
                for (var m : selectedMedia) {
                    uris.add(new FileUtils().getUri(m.getPath(), m.getType(), requireContext()));
                }
                result.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
                result.putExtra(Intent.EXTRA_SUBJECT, "Pictures");
                result.putExtra(Intent.EXTRA_TEXT, "Pictures share");
                result.setType("*/*");
                getActivity().startActivity(result);
            }
        });
        btnDelete.setOnClickListener((v)->{

        });
        btnCreative.setOnClickListener((v)->{

        });
        btnFav.setOnClickListener((v) -> {
            removeFromFavorite();
        });
    }

    private void removeFromFavorite() {
        saveScroll();
        for(var m : selectedMedia) {
            AccessMediaFile.removeFromFavMedia(m.getPath());
        }
        var favList = AccessMediaFile.getFavMedia();
        // Log.d("fav", "fav amount pause = " + favList.size());
        favList.forEach(x -> Log.d("fav", x));
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("fav_media", MODE_PRIVATE);
        SharedPreferences.Editor myEdit = sharedPreferences.edit();
        myEdit.clear();

        // write all the data entered by the user in SharedPreference and apply
        myEdit.putStringSet("path", favList);
        myEdit.apply();
        refresh();
        callback.onDestroyActionMode(mode);
    }

    private void deleteMedia() {
        saveScroll();
        var trashEnabled = true;
        if (!trashEnabled) {
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
                    if (fileUtils.deleteMultiple(launcher, selectedMedia, context) > 0) {
                        for(var m : selectedMedia) {
                            AccessMediaFile.removeMediaFromAllMedia(m.getPath());
                        }
                    }
                    else {
                        isPendingForIntent = true;
                    }
                    while (isPendingForIntent) {}
                    return null;
                }
//
//        refresh();
//        callback.onDestroyActionMode(mode);
            }.execute();
            return;
        }
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                var newFileName = fileUtils.trashFileMultiple(selectedMedia);
                SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("trash_media", MODE_PRIVATE);
                SharedPreferences.Editor myEdit = sharedPreferences.edit();
                myEdit.clear();
                // write all the data entered by the user in SharedPreference and apply
                myEdit.putStringSet("path", AccessMediaFile.getAllTrashMedia());
                myEdit.apply();
                callback.onDestroyActionMode(mode);
                refresh();
            } else {
                try {
                    Uri uri = Uri.parse("package:" + BuildConfig.APPLICATION_ID);
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, uri);
                    startActivity(intent);
                } catch (Exception ex) {
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                    startActivity(intent);
                }
                callback.onDestroyActionMode(mode);
            }
        }
        else {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                fileUtils.trashFileMultiple(selectedMedia);
            }
            callback.onDestroyActionMode(mode);
        }
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
        FavoriteFragment.AlbumListTask albumAsyncTask = new FavoriteFragment.AlbumListTask();
        albumAsyncTask.execute();
    }

    public void changeOrientation(int spanCount) {
        saveScroll();
        if (spanCount != this.spanCount) {
            this.spanCount = spanCount;
            adapter = new MediaCategoryAdapter(context, spanCount, this);
            refresh();
            ((LinearLayoutManager) Objects.requireNonNull(recyclerView.getLayoutManager())).scrollToPosition(firstVisiblePosition);
            callback.onDestroyActionMode(mode);
        }
    }

    public void refresh() {
        Log.d("refresh", "");
        mediaListTask = new FavoriteFragment.MediaListTask();
        mediaListTask.execute();
    }

    public void refresh(boolean scroll) {
        Log.d("refresh with result", "");
        mediaListTask = new FavoriteFragment.MediaListTask(scroll);
        mediaListTask.execute();
    }

    void recyclerViewSetting(){
        adapter = new MediaCategoryAdapter(requireContext(), spanCount, this);
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

        settingButton = toolbar.getMenu().findItem(R.id.setting_menu_item);

        settingButton.setOnMenuItemClickListener(menuItem -> {
            openSetting();
            return false;
        });
    }

    private void openSetting(){
        Intent intent = new Intent(getActivity(), SettingsActivity.class);
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

    @NonNull
    ArrayList<MediaCategory> getListCategory() {
        AccessMediaFile.refreshAllMedia();
        AccessMediaFile.getAllMedia(getContext());
        HashMap<String, MediaCategory> categoryList = new LinkedHashMap<>();
        listMedia = new ArrayList<>(AccessMediaFile.getAllFavMedia());

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
                    String name = i == 0 ? cat.getNameCategory() : "";
                    newCatList.add(new MediaCategory(name, new ArrayList<>(cat.getList().subList(i,
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

    @NonNull
    @Override
    public ArrayList<Media> getSelectedList() {
        return selectedMedia;
    }

    @Override
    public void deleteFromSelectedList(@NonNull Media media) {
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
    public void moveMedia(@NonNull String albumPath) {
        FileUtils fileUtils = new FileUtils();
        if(changeMode) {
            fileUtils.moveFileMultiple(selectedMedia, launcher, albumPath, context);
        }else{
            fileUtils.copyFileMultiple(selectedMedia, albumPath, context);
        }
        for(var r : selectedMedia) {
            if (AccessMediaFile.isExistedAnywhere(r.getPath()) && r.getPath().lastIndexOf("/") != -1) {
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
            albumAdapter = new AlbumCategoryAdapter(context, FavoriteFragment.this, 3);
            albumAdapter.setViewType(1);
            albumAdapter.setData(albumCategories);
            addAlbumRecyclerView.setAdapter(albumAdapter);
            bottomSheetDialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            ArrayList<Media> listMediaTemp = AccessMediaFile.getAllMedia(requireContext());
            albumList = getAllAlbum(listMediaTemp);
            categorizeAlbum();

            return null;
        }
    }

    public void rescanForUnAddedAlbum(){
        Cursor cursor =  context.getContentResolver().query(
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


    public void categorizeAlbum() {
        HashMap<String, AlbumCategory> categoryList = new LinkedHashMap<>();
        String[] subDir;
        categoryList.put("Mặc định", new AlbumCategory("Mặc định", new ArrayList<>()));
        categoryList.put("Của tôi", new AlbumCategory("Của tôi", new ArrayList<>()));
        categoryList.put("Thêm album", new AlbumCategory("Thêm album", new ArrayList<>()));

        rescanForUnAddedAlbum();
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
            //Log.d("value", album.getPath() + " " + album.getName() + " " + album.getListMedia().size());
            albumList.addAll(entry.getValue().getList());
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
