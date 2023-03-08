package com.group7.gallerium.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.group7.gallerium.R;
import com.group7.gallerium.adapters.AlbumAdapter;
import com.group7.gallerium.adapters.CategoryAdapter;
import com.group7.gallerium.models.Album;
import com.group7.gallerium.models.Media;
import com.group7.gallerium.utilities.AccessMediaFile;
import com.group7.gallerium.utilities.ProgressDialogHelper;
import com.group7.gallerium.utilities.ToolbarScrollListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AlbumFragment extends Fragment {
    private View view;
    private Toolbar toolbar;
    private Context context;

    private ArrayList<Album> albumList;

    private AlbumListTask albumListTask;

    private AlbumAdapter adapter;
    private RecyclerView album_rec;
    long delaySecond = 2000;
    ProgressDialog progressDialog;
    private int firstVisiblePosition;
    private int offset;


    public AlbumFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void onPause() {
        super.onPause();
        View firstChild = album_rec.getChildAt(0);
        if (firstChild != null) {
            firstVisiblePosition = album_rec.getChildAdapterPosition(firstChild);
            offset = firstChild.getTop();
        }
    }

    public void changeOrientation(int spanCount) {
        GridLayoutManager layoutManager = new GridLayoutManager(context, spanCount);
        album_rec.setLayoutManager(layoutManager);
        adapter = new AlbumAdapter(context);
        ArrayList<Media> listMediaTemp = new ArrayList<>(AccessMediaFile.getAllMedia().values());
        albumList = getAlbum(listMediaTemp);
        adapter.setData(albumList);
    }


    @Override
    public void onStart() {
        super.onStart();
        albumListTask = new AlbumListTask();
        progressDialog = new ProgressDialog(AlbumFragment.this.getContext());
        progressDialog.setTitle("Loading (0%)");
        new CountDownTimer(delaySecond, delaySecond / 100) {
            int counter = 0;
            @Override
            public void onTick(long l) {
                counter += delaySecond / 100;
                progressDialog.setTitle("Loading (" + counter + "%)");
            }

            @Override
            public void onFinish() {
                progressDialog.dismiss();
            }
        }.start();

        albumListTask.execute();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_album, container, false);
        context = getContext();
        toolbar = view.findViewById(R.id.toolbar_album);
        album_rec = view.findViewById(R.id.album_recyclerview);
        GridLayoutManager layoutManager = new GridLayoutManager(context, 3);
        album_rec.setLayoutManager(layoutManager);
        album_rec.setItemViewCacheSize(3);
        adapter = new AlbumAdapter(context);
        toolbarSetting();
        return view;
    }

    void toolbarSetting(){

        toolbar.inflateMenu(R.menu.menu_album);
        toolbar.setTitle(R.string.album);
        toolbar.setTitleTextAppearance(context, R.style.ToolbarTitle);
    }

    public ArrayList<Album> getAlbum(ArrayList<Media> listMedia){
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
        for(Album album: albums){
            Log.d("album", album.toString());
        }
        return albums;
    }



    public class AlbumListTask extends AsyncTask<Void, Integer, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            album_rec.setAdapter(adapter);
            ((LinearLayoutManager) Objects.requireNonNull(album_rec.getLayoutManager())).scrollToPositionWithOffset(firstVisiblePosition, offset);
            // album_rec.addOnScrollListener(new ToolbarScrollListener(toolbar));
        }

        @Override
        protected Void doInBackground(Void... voids) {
            ArrayList<Media> listMediaTemp = new ArrayList<>(AccessMediaFile.getAllMedia().values());
            albumList = getAlbum(listMediaTemp);
            adapter.setData(albumList);
            return null;
        }
    }
}
