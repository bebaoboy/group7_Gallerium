package com.group7.gallerium.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.group7.gallerium.R;
import com.group7.gallerium.adapters.AlbumCategoryAdapter;
import com.group7.gallerium.models.Album;
import com.group7.gallerium.models.AlbumCategory;
import com.group7.gallerium.models.Media;
import com.group7.gallerium.utilities.AccessMediaFile;

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

    private ArrayList<Album> albumList = new ArrayList<>();
    private int mediaAmount = 0;

    private ArrayList<AlbumCategory> albumCategories;

    private AlbumListTask albumListTask;

    private AlbumCategoryAdapter adapter;
    private RecyclerView album_rec;
    long delaySecond = 2000;
    ProgressDialog progressDialog;
    private int firstVisiblePosition;
    private int offset;


    private ActionBottomDialogFragment createAlbumBottomDialogFragment;

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
        super.onPause();
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

        albumCategories = new ArrayList<>();
        toolbarSetting();
        return view;
    }

    void toolbarSetting(){

        toolbar.inflateMenu(R.menu.menu_top_album);
        toolbar.setTitle(R.string.album);
        toolbar.setTitleTextAppearance(context, R.style.ToolbarTitle);

        MenuItem createAlbumButton = toolbar.getMenu().findItem(R.id.create_album_tb_item);

        createAlbumButton.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                openBottomDialog();
                return true;
            }
        });
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
        mediaAmount = listMedia.size();

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

//        categoryList.get("Mặc định").getList().add(image);
//        categoryList.get("Mặc định").getList().add(video);

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
                        album.setName(album.getName() + " (" + parent + ")");
                        album1.setName(album1.getName() + " (" + parent1 + ")");
                    }
                    break;
                }
            }

            if (!needToMerge)
            {
                categoryList.get(catName).addAlbumToList(album);
            }

//            if (subDir.length == 6 && (subDir[subDir.length - 1].equals("Camera")
//                    || subDir[subDir.length - 1].equals("Screenshots")
//                    || subDir[subDir.length - 1].equals("Video"))) {
//                categoryList.get("").addAlbumToList(album);
//            }
//            else if (subDir.length == 4 || subDir.length == 5){
//                categoryList.get("Thêm album").addAlbumToList(album);
//            }
//
//            if (subDir.length > 6 || subDir[subDir.length-2] == "owner") {
//                categoryList.get("Của tôi").addAlbumToList(album);
//            }
        }

//       AlbumCategory category = categoryList.get("");
//
//        for(int i=0;i<category.getList().size();i++){
//            String[] path1 = category.getList().get(i).getPath().split("/");
//            for(int j=i+1;j<category.getList().size();j++){
//                String[] path2 = category.getList().get(j).getPath().split("/");
//                if(path1[path1.length-1].equals(path2[path2.length-1])){
//                    category.getList().get(i).getListMedia().addAll(category.getList().get(j).getListMedia());
//                    category.getList().remove(j);
//                    categoryList.replace("", category);
//                    break;
//                }
//            }
//        }
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

//        for(AlbumCategory ab: albumCategories){
//            Log.d("alb cat name", ab.getNameCategory());
//            for(Album album: ab.getList()){
//                Log.d("album list", album.getName());
//            }
//        }
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
                ((LinearLayoutManager) Objects.requireNonNull(album_rec.getLayoutManager())).scrollToPosition(0);
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
            categorizeAlbum();
            return null;
        }
    }
}
