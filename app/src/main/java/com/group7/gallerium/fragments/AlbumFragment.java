package com.group7.gallerium.fragments;

import static android.content.Context.MODE_PRIVATE;

import android.Manifest;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.SearchRecentSuggestions;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.group7.gallerium.R;
import com.group7.gallerium.activities.SettingsActivity;
import com.group7.gallerium.adapters.AlbumCategoryAdapter;
import com.group7.gallerium.adapters.SuggestionSimpleCursorAdapter;
import com.group7.gallerium.models.Album;
import com.group7.gallerium.models.AlbumCategory;
import com.group7.gallerium.models.AlbumCustomContent;
import com.group7.gallerium.models.Media;
import com.group7.gallerium.utilities.AccessMediaFile;
import com.group7.gallerium.utilities.MySuggestionProvider;
import com.group7.gallerium.utilities.SuggestionsDatabase;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class AlbumFragment extends Fragment{
    private View view;
    private Toolbar toolbar;
    private Context context;

    private int spanCount = 3;

    ArrayList<Album> albumList = new ArrayList<>();

    private ArrayList<AlbumCustomContent> albumCustomContents;

    ArrayList<AlbumCategory> albumCategories;

    private AlbumListTask albumListTask;

    AlbumCategoryAdapter adapter;
    RecyclerView album_rec;
    // long delaySecond = 2000;
    ProgressDialog progressDialog;
    int firstVisiblePosition;
    int offset;

    private boolean isTrashEnable;

    private ActionBottomDialogFragment createAlbumBottomDialogFragment;
    private MenuItem settingButton;

    private boolean isSearching = false;
    private SearchView searchView;
    SuggestionSimpleCursorAdapter cursorAdapter;
    private MenuItem searchButton;

    public AlbumFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
            //changeOrientation(6);
            ((LinearLayoutManager) Objects.requireNonNull(album_rec.getLayoutManager())).scrollToPositionWithOffset(firstVisiblePosition, offset);
        }
        else {
            //changeOrientation(3);
            ((LinearLayoutManager) Objects.requireNonNull(album_rec.getLayoutManager())).scrollToPositionWithOffset(firstVisiblePosition, offset);
        }
      }

    public void onPause() {
        if( ! searchView.isIconified()) {
            searchView.setIconified(true);
        }
        searchButton.collapseActionView();
        super.onPause();
        var albList = AccessMediaFile.getAllYourAlbum();
        //Log.d("fav", "fav amount pause = " + favList.size());
        albList.forEach(x -> Log.d("alb", x));
        SharedPreferences sharedPreferences = context.getSharedPreferences("your_album", MODE_PRIVATE);
        SharedPreferences.Editor myEdit = sharedPreferences.edit();
        myEdit.clear();

        // write all the data entered by the user in SharedPreference and apply
        myEdit.putStringSet("path", albList);
        myEdit.apply();
        saveScroll();
    }

    private void saveScroll() {
        View firstChild = album_rec.getChildAt(0);
        if (firstChild != null) {
            firstVisiblePosition = album_rec.getChildAdapterPosition(firstChild);
            offset = firstChild.getTop();
        }
    }

    public void changeOrientation(int spanCount) {
        saveScroll();
        if (spanCount != this.spanCount)
        {
            this.spanCount = spanCount;
            adapter = new AlbumCategoryAdapter(context, spanCount);
            refresh();
            ((LinearLayoutManager) Objects.requireNonNull(album_rec.getLayoutManager())).scrollToPositionWithOffset(firstVisiblePosition, offset);
        }
    }

    public void refresh(){
//        AccessMediaFile.refreshAllMedia();
//        ArrayList<Media> listMediaTemp = AccessMediaFile.getAllMedia(getContext());
//        if (listMediaTemp.size() != mediaAmount)
//        {
//            albumList = getAllAlbum(listMediaTemp);
//        }
//        categorizeAlbum();
//        adapter.setData(albumCategories);
//        album_rec.setAdapter(adapter);
        albumListTask = new AlbumListTask();
        albumListTask.execute();
    }

    public void refresh(boolean scroll){
//        AccessMediaFile.refreshAllMedia();
//        ArrayList<Media> listMediaTemp = AccessMediaFile.getAllMedia(getContext());
//        if (listMediaTemp.size() != mediaAmount)
//        {
//            albumList = getAllAlbum(listMediaTemp);
//        }
//        categorizeAlbum();
//        adapter.setData(albumCategories);
//        album_rec.setAdapter(adapter);
        albumListTask = new AlbumListTask(scroll);
        albumListTask.execute();
    }

    @Override
    public void onStart() {
        super.onStart();
        GridLayoutManager layoutManager = new GridLayoutManager(context, 1);
        album_rec.setLayoutManager(layoutManager);
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            changeOrientation(6);
        } else {
            changeOrientation(3);
        }
        albumListTask = new AlbumListTask();
        //progressDialog = new ProgressDialog(AlbumFragment.this.getContext());
        //progressDialog.setTitle("Loading (0%)");
        // progressDialog.show();
//        new CountDownTimer(delaySecond, delaySecond / 100) {
//            int counter = 0;
//            @Override
//            public void onTick(long l) {
//                counter += delaySecond / 100;
//                //progressDialog.setTitle("Loading (" + counter + "%)");
//            }
//
//            @Override
//            public void onFinish() {
//                //progressDialog.dismiss();
//            }
//        }.start();

        albumListTask.execute();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_album, container, false);
        context = getContext();
        toolbar = view.findViewById(R.id.toolbar_album);
        album_rec = view.findViewById(R.id.album_recyclerview);
        GridLayoutManager layoutManager = new GridLayoutManager(context, 1);
        album_rec.setLayoutManager(layoutManager);
        album_rec.setItemViewCacheSize(3);
        adapter = new AlbumCategoryAdapter(context, 3);

        albumCustomContents = new ArrayList<>();
        albumCategories = new ArrayList<>();
        toolbarSetting();
        return view;
    }

    void toolbarSetting(){

        toolbar.inflateMenu(R.menu.menu_top_album);
        toolbar.setTitle(R.string.album);
        toolbar.setTitleTextAppearance(context, R.style.ToolbarTitle);

        MenuItem createAlbumButton = toolbar.getMenu().findItem(R.id.create_album_tb_item);

        createAlbumButton.setOnMenuItemClickListener(item -> {
            openBottomDialog();
            return true;
        });

        settingButton = toolbar.getMenu().findItem(R.id.setting_menu_item);

        settingButton.setOnMenuItemClickListener(menuItem -> {
            openSetting();
            return false;
        });

        searchButton = toolbar.getMenu().findItem(R.id.search_tb_item);

        SearchManager searchManager =
                (SearchManager) context.getSystemService(Context.SEARCH_SERVICE);

        searchView =
                (SearchView) searchButton.getActionView();

        searchView.setSuggestionsAdapter(cursorAdapter);
        searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int position) {
                return false;
            }

            @Override
            public boolean onSuggestionClick(int position) {
                SQLiteCursor cursor = (SQLiteCursor) searchView.getSuggestionsAdapter().getItem(position);
                int indexColumnSuggestion = cursor.getColumnIndex( SuggestionsDatabase.FIELD_SUGGESTION);

                searchView.setQuery(cursor.getString(indexColumnSuggestion), false);
                return true;
            }
        });

        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(requireActivity().getComponentName()));

        searchView.setIconifiedByDefault(false);

        searchView.setSubmitButtonEnabled(true);
        SearchView.SearchAutoComplete searchAutoComplete = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        searchAutoComplete.setThreshold(0);
        var database = new SuggestionsDatabase(this.getContext());

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //getSearchResults(query); Also tried
                isSearching = false;
                long result = database.insertSuggestion(query);
                if( ! searchView.isIconified()) {
                    searchView.setIconified(true);
                }
                searchButton.collapseActionView();
                SearchRecentSuggestions suggestions = new SearchRecentSuggestions(context,
                        MySuggestionProvider.AUTHORITY, MySuggestionProvider.MODE);
                suggestions.saveRecentQuery(query, null);
                return result != -1;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isBlank()) {
                    isSearching = false;
                } else {
                    isSearching = true;
                }
                Cursor cursor = database.getSuggestions(newText);
                String[] columns = new String[] {SuggestionsDatabase.FIELD_SUGGESTION };
                int[] columnTextId = new int[] { android.R.id.text1};
                SuggestionSimpleCursorAdapter simple = new SuggestionSimpleCursorAdapter(context.getApplicationContext(),
                        R.layout.search_suggestion_item, cursor,
                        columns , columnTextId
                        , 0);

                searchView.setSuggestionsAdapter(simple);
                refresh();
                if(cursor.getCount() != 0)
                {
                    return true;
                }
                else
                {
                    return false;
                }
            }
        });
    }

    private void openSetting(){
        Intent intent = new Intent(getActivity(), SettingsActivity.class);
        startActivity(intent);
    }

    public void openBottomDialog(){
        createAlbumBottomDialogFragment = ActionBottomDialogFragment.newInstance();
        createAlbumBottomDialogFragment.show(getParentFragmentManager(),
                ActionBottomDialogFragment.TAG);
        createAlbumBottomDialogFragment.setTitle("Nhập tên album");
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
                   // Log.d("path", path);
                    name = cursor.getString(nameColumn);
                    // Log.d("name", name);
                    String[] subDirs = path.split("/");
                    if(!subDirs[subDirs.length-2].equals("owner")) continue;
                    Album album = new Album(null, name);
                    album.setPath(path);
                    album.setType(2);
                    AccessMediaFile.addToYourAlbum(path);
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
            cursor.close();
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

        Album video = new Album("Video");
        Album image = new Album("Ảnh");
        for(var media : listMedia) {
            if(media.getType() == 1){
                image.addMedia(media);
            }else if(media.getType() == 3){
                video.addMedia(media);
            }
        }
        if(image.getListMedia().size()>0) {
            image.setAvatar(image.getListMedia().get(0));
        }
        if(video.getListMedia().size()>0) {
            video.setAvatar(video.getListMedia().get(0));
        }
        image.setType(1);
        video.setType(1);
        image.setPath("/internal/DCIM/Ảnh");
        video.setPath("/internal/DCIM/Video");
        paths.add(image.getPath());
        paths.add(video.getPath());
        albums.add(image);
        albums.add(video);

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
        HashMap<String, Boolean> albumDirs = new HashMap<>();

        for (Album album : albumList) {
            if (!albumDirs.containsKey(album.getPath())) {
                if (!album.getPath().equals("/internal/DCIM/Ảnh")
                        && !album.getName().equals("/internal/DCIM/Video")
                        && !album.getName().equals("/internal/DCIM/Trash")) {
                    albumDirs.put(album.getPath(), true);
                }
            }
        }
        SharedPreferences mySharedPref = context.getSharedPreferences("trash_media", MODE_PRIVATE);
        var tList = mySharedPref.getStringSet("path", null);
        if (tList != null) {
            AccessMediaFile.setAllTrashMedia(tList);
        }


        var sharedPref = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        var trashEnabled =  sharedPref.getBoolean(SettingsActivity.KEY_PREF_LOCK_TRASH, false);
        var canTrash = false;
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager())  canTrash = true;
        }
        else {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                canTrash = true;
            }
        }
        if (trashEnabled && canTrash) {
            Album trashBin = new Album("Thùng rác");
            var trashes = AccessMediaFile.getAllTrashMedia();
            for (var m : trashes) {
                var media = AccessMediaFile.getMediaWithPath(m);
//            media.setPath(m);
//            media.setTitle(m.substring(m.lastIndexOf("/") + 1));
                trashBin.addMedia(media);
            }
            if (trashBin.getListMedia().size() > 0) {
                trashBin.setAvatar(trashBin.getListMedia().get(0));
            }
            trashBin.setPath("/internal/DCIM/Trash");
            trashBin.setType(1);
            paths.add(trashBin.getPath());
            albums.add(trashBin);
        }
        if (isSearching) {
            var query = searchView.getQuery().toString().toLowerCase();
            var s = new ArrayList<Album>();
            for(var a : albums) {
                if (a.getName().toLowerCase().contains(query) || (a.getDateCreated() != null && a.getDateCreated().toLowerCase().contains(query))) {
                    s.add(a);
                }
            }
            albums = s;
        }
        return albums;
    }


    public void categorizeAlbum() {
        HashMap<String, AlbumCategory> categoryList = new LinkedHashMap<>();
        String[] subDir;
        int type;

        categoryList.put("Mặc định", new AlbumCategory("Mặc định", new ArrayList<>()));
        categoryList.put("Của tôi", new AlbumCategory("Của tôi", new ArrayList<>()));
        categoryList.put("Thêm album", new AlbumCategory("Thêm album", new ArrayList<>()));

//        categoryList.get("Mặc định").getList().add(image);
//        categoryList.get("Mặc định").getList().add(video);

        //rescanForUnAddedAlbum();
        for (Album album : albumList) {
            if (album.getListMedia().size() == 0) {
                album.setAvatar(null);
            }
            String path = album.getPath();
            subDir = path.split("/");
            String catName = "";
            String parent = subDir[subDir.length - 1];
            if (subDir.length >= 2) {
                parent = subDir[subDir.length - 2];
                if (parent.equals("DCIM")) {
                    if (subDir[subDir.length - 1].equals("Camera")
                            || subDir[subDir.length - 1].equals("Screenshots")
                            || subDir[subDir.length - 1].equals("Ảnh")
                            || subDir[subDir.length - 1].equals("Video")
                            || subDir[subDir.length - 1].equals("Trash")) {
                        //categoryList.get("Mặc định").addAlbumToList(album);
                        catName = "Mặc định";
                    }
                } else if (parent.equals("owner")) {
                    //categoryList.get("Của tôi").addAlbumToList(album);
                    catName = "Của tôi";
                    AccessMediaFile.addToYourAlbum(path);
                }
            }

            if (catName.length() == 0) {

                catName = "Thêm album";
            }

            boolean needToMerge = false;
            for (Album album1 : categoryList.get(catName).getList()) {
                if (!album.getPath().equals(album1.getPath()) && album.getName().equalsIgnoreCase(album1.getName())) {
                    String path1 = album1.getPath();
                    String[] subDir1 = path1.split("/");
                    String parent1 = subDir1[subDir1.length - 2];
                    if (parent1.equalsIgnoreCase(parent)) {
                        // Log.d("merge", "merging " + album.getPath() + " and " + album1.getPath());
                        album1.getListMedia().addAll(album.getListMedia());
                        album1.setListMedia(
                                new ArrayList<>(album1.getListMedia()
                                        .stream()
                                        .sorted(Comparator.comparingLong(Media::getRawDate).reversed())
                                        .collect(Collectors.toList())));
                        needToMerge = true;
                    } else {
                        album.setName(album.getName() + " (" + parent + ")");
                        album1.setName(album1.getName() + " (" + parent1 + ")");
                    }
                    break;
                }
            }

            if (!needToMerge) {
                categoryList.get(catName).addAlbumToList(album);
            }
        }

        albumList.clear();
        albumCategories.clear();
        for(Map.Entry<String, AlbumCategory> entry: categoryList.entrySet()){
           // Log.d("Key", entry.getKey());
            albumCategories.add(entry.getValue());
            if(entry.getKey().equals("Mặc định")){
                type = 1;
            }else if(entry.getKey().equals("Của tôi")){
                type = 2;
            }else{
                type = 3;
            }
            for(Album album: entry.getValue().getList()){
                album.setType(type);
                for(AlbumCustomContent content: albumCustomContents){
                    if(album.getPath().equals(content.getAlbumPath())){
                        album.setMemoryContent(content.getContent());
                        album.setMemoryTitle(content.getTitle());
                        album.setMemoryDate(content.getDate());
                        break;
                    }
                }
                albumList.add(album);
                //Log.d("value", album.getPath() + " " + album.getName() + " " + album.getListMedia().size());
            }
        }

//        for(AlbumCategory ab: albumCategories){
//            Log.d("alb cat name", ab.getNameCategory());
//            for(Album album: ab.getList()){
//                Log.d("album list", album.getName());
//            }
//        }
    }



    public void setTrashEnable(boolean lockTrashPref) {
        isTrashEnable = lockTrashPref;
    }

    public void getAlbumInfo(){
        File albumInfoFile = new File(context.getFilesDir(), "albumsInfo.txt");
        albumCustomContents.clear();
        String contents;
        if(albumInfoFile.exists()) {
            int length = (int) albumInfoFile.length();

            byte[] bytes = new byte[length];
            try {
                FileInputStream in = new FileInputStream(albumInfoFile);
                in.read(bytes);
                in.close();
            } catch (Exception e) {
                Log.d("tag", e.getMessage());
            }
            contents = new String(bytes);
            Log.d("content-album", contents);
            try {
                JSONArray array = new JSONArray(contents);
                for(int i=0;i<array.length();i++){
                    JSONObject object = array.getJSONObject(i);
                    albumCustomContents.add(new AlbumCustomContent(object));
                }
            } catch (Exception e) {
                Log.d("json error", e.getMessage());
            }
        }
    }


    public class AlbumListTask extends AsyncTask<Void, Integer, Void> {
        boolean scroll = false;
        public AlbumListTask(){}
        public AlbumListTask(boolean s) {scroll=s;}
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            AccessMediaFile.refreshAllMedia();
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            adapter.setData(albumCategories);
            album_rec.setAdapter(adapter);
            Log.d("refresh", "refresh album frag ");
            // album_rec.addOnScrollListener(new ToolbarScrollListener(toolbar));
            if (scroll) {
                Objects.requireNonNull(album_rec.getLayoutManager()).scrollToPosition(0);
            }
            else {
                ((LinearLayoutManager) Objects.requireNonNull(album_rec.getLayoutManager())).scrollToPositionWithOffset(firstVisiblePosition, offset);
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {
            ArrayList<Media> listMediaTemp = AccessMediaFile.getAllMedia(getContext());
//            if (listMediaTemp.size() != mediaAmount)
//            {
                albumList = getAllAlbum(listMediaTemp);
//            }
            getAlbumInfo();
            //setAlbumInfo();
            categorizeAlbum();
            return null;
        }
    }
}
