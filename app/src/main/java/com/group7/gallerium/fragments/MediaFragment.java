package com.group7.gallerium.fragments;

import static android.content.Context.MODE_PRIVATE;

import android.Manifest;
import android.animation.Animator;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.group7.gallerium.BuildConfig;
import com.group7.gallerium.R;
import com.group7.gallerium.activities.CameraActivity;
import com.group7.gallerium.activities.SearchResultActivity;
import com.group7.gallerium.activities.SettingsActivity;
import com.group7.gallerium.adapters.AlbumCategoryAdapter;
import com.group7.gallerium.adapters.MediaCategoryAdapter;
import com.group7.gallerium.adapters.SuggestionSimpleCursorAdapter;
import com.group7.gallerium.models.Album;
import com.group7.gallerium.models.AlbumCategory;
import com.group7.gallerium.models.Media;
import com.group7.gallerium.models.MediaCategory;
import com.group7.gallerium.utilities.AccessMediaFile;
import com.group7.gallerium.utilities.FileUtils;
import com.group7.gallerium.utilities.SelectMediaInterface;
import com.group7.gallerium.utilities.SuggestionsDatabase;

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
    MenuItem cameraButton, settingButton, searchButton, sortButton;
    Context context;
    ArrayList<Media> listMedia;
    ArrayList<Media> selectedMedia;
    ArrayList<MediaCategory> mediaCategories;
    ArrayList<Album> albumList;
    ArrayList<AlbumCategory> albumCategories;
    MediaCategoryAdapter adapter;
    AlbumCategoryAdapter albumAdapter;

    SuggestionSimpleCursorAdapter cursorAdapter;
    RecyclerView recyclerView;

    RecyclerView addAlbumRecyclerView;
    private int spanCount = 3;
    int firstVisiblePosition;
    int offset;
    int numGrid;
    boolean isAllChecked = false;

    final int UI_MODE_GRID = 1, UI_MODE_LIST = 2;

    int uiMode = UI_MODE_GRID;
    private boolean changeMode = false;
    ActionMode mode;
    ActionMode.Callback callback;

    static FileUtils fileUtils;

    LinearLayout bottom_sheet;
    BottomSheetBehavior behavior;
    BottomSheetDialog bottomSheetDialog;

    SharedPreferences sharedPreferences;

    TextView txtSize;

    TextView btnShare, btnMove, btnDelete, btnCreative, btnCopy, btnFav, btnHide;
    private MediaFragment.MediaListTask mediaListTask;
    boolean isPendingForIntent = false;
    SwipeRefreshLayout swipeLayout;

    SuggestionsDatabase database;

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
                    }
                    isPendingForIntent = false;
                    refresh();
                });
    }



    @Override
    public void onResume() {
        super.onResume();
        //Toast.makeText(this.getContext(), "Resuming", Toast.LENGTH_SHORT).show();
        var sharedPref =
                PreferenceManager.getDefaultSharedPreferences(context);
        var numGridPref = sharedPref.getString(SettingsActivity.KEY_PREF_NUM_GRID, "3");
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
            // Log.d("pos", firstVisiblePosition + "");
        }
    }

     ActivityResultLauncher<IntentSenderRequest> launcher;

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
        context = requireContext();
        toolbarSetting();
        //recyclerViewSetting();
        adapter = new MediaCategoryAdapter(requireContext(), spanCount, this);
        recyclerView = view.findViewById(R.id.photo_recyclerview);
        recyclerView.setItemViewCacheSize(10000);
        recyclerView.setHasFixedSize(true);
        txtSize = view.findViewById(R.id.txtSizePopUp);
        txtSize.setVisibility(View.GONE);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    txtSize.setText(adapter.getSectionName(recyclerView.getChildAdapterPosition(recyclerView.getChildAt(0))));
                    txtSize.animate().translationY(0).setDuration(1000).setInterpolator(new DecelerateInterpolator())
                            .setListener(new Animator.AnimatorListener() {
                                @Override
                                public void onAnimationStart(@NonNull Animator animator) {
                                    txtSize.setVisibility(View.VISIBLE);
                                }

                                @Override
                                public void onAnimationEnd(@NonNull Animator animator) {
                                }

                                @Override
                                public void onAnimationCancel(@NonNull Animator animator) {
                                }

                                @Override
                                public void onAnimationRepeat(@NonNull Animator animator) {
                                }
                            })
                            .start();
                    super.onScrollStateChanged(recyclerView, newState);
                } else if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    txtSize.animate().translationY(-txtSize.getTop()).setDuration(1000).setInterpolator(new DecelerateInterpolator())
                            .setListener(new Animator.AnimatorListener() {
                                @Override
                                public void onAnimationStart(@NonNull Animator animator) {
                                }

                                @Override
                                public void onAnimationEnd(@NonNull Animator animator) {
                                    //txtSize.setVisibility(View.GONE);
                                }

                                @Override
                                public void onAnimationCancel(@NonNull Animator animator) {
                                }

                                @Override
                                public void onAnimationRepeat(@NonNull Animator animator) {
                                }
                            })
                            .start();
                    super.onScrollStateChanged(recyclerView, newState);

                }

            }
        });

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
        swipeLayout = view.findViewById(R.id.swipe_layout);
        swipeLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        Log.i("SWIPE", "onRefresh called from SwipeRefreshLayout");

                        // This method performs the actual data-refresh operation.
                        // The method calls setRefreshing(false) when it's finished.
                        refresh(true);
                    }
                }
        );
        swipeLayout.setColorSchemeResources(R.color.primary_color_light_20a,
                R.color.primary_color_light,
                R.color.primary_color_dark,
                R.color.primary_color_dark_bg);

        /**
         * Showing Swipe Refresh animation on activity create
         * As animation won't start on onCreate, post runnable is used
         */
        swipeLayout.post(() -> swipeLayout.setRefreshing(true));

        database = new SuggestionsDatabase(this.getContext());

        // Log.d("modeUI","" + uiMode);
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
        btnFav = view.findViewById(R.id.add_to_fav_button);
        btnHide = view.findViewById(R.id.hide_button);

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
        btnDelete.setOnClickListener((v) -> deleteMedia());
        btnCreative.setOnClickListener((v) -> {

        });
        btnFav.setOnClickListener((v) -> addToFavorite());
        btnFav.setOnLongClickListener(view -> {
            btnHide.setVisibility(View.VISIBLE);
            btnFav.setVisibility(View.GONE);
            return true;
        });
        btnHide.setOnClickListener((v) -> hideMedia());
        btnHide.setOnLongClickListener(view -> {
            btnHide.setVisibility(View.GONE);
            btnFav.setVisibility(View.VISIBLE);
            return true;
        });
    }

    private void hideMedia() {

    }

    private void addToFavorite() {
        saveScroll();
        for(var m : selectedMedia) {
            AccessMediaFile.addToFavMedia(m.getPath());
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
        var sharedPref = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        var trashEnabled =  sharedPref.getBoolean(SettingsActivity.KEY_PREF_LOCK_TRASH, false);
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
        AlbumListTask albumAsyncTask = new AlbumListTask();
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
        mediaListTask = new MediaListTask();
        mediaListTask.execute();
    }

    public void refresh(boolean scroll) {
        if (mode == null) {
            Log.d("refresh with result", "");
            mediaListTask = new MediaListTask(scroll);
            mediaListTask.execute();
        }
        else {
            swipeLayout.setRefreshing(false);
        }
    }

//    void recyclerViewSetting() {
//        adapter = new MediaCategoryAdapter(getContext(), spanCount, this);
//        adapter.setData(getListCategory());
//        recyclerView = view.findViewById(R.id.photo_recyclerview);
//        recyclerView.setAdapter(adapter);
//        recyclerView.setItemViewCacheSize(3);
//    }

    void toolbarSetting() {
        toolbar = view.findViewById(R.id.toolbar_photo);
        toolbar.inflateMenu(R.menu.menu_top_photo);
        toolbar.setTitle(R.string.app_name);
        toolbar.setTitleTextAppearance(context, R.style.ToolbarTitle);
        cameraButton = toolbar.getMenu().findItem(R.id.take_photo_tb_item);
        cameraButton.setOnMenuItemClickListener(menuItem -> {
            enableCamera();
            return false;
        });
        settingButton = toolbar.getMenu().findItem(R.id.setting_menu_item);

        settingButton.setOnMenuItemClickListener(menuItem -> {
            openSetting();
            return false;
        });

        sortButton = toolbar.getMenu().findItem(R.id.sort_menu_item);
        sortButton.setTitle("Sắp xếp" + " (" + sortMode + ": " +
                switch(sortMode) {
                    case AccessMediaFile.DATE_DESC -> "Ngày giảm dần";
                    case AccessMediaFile.SIZE_DESC -> "Size giảm dần theo ngày";
                    case AccessMediaFile.SIZE_ASC -> "Size tăng dần theo ngày";
                    case AccessMediaFile.SIZE_DESC_NO_GROUP -> "Size giảm dần";
                    case AccessMediaFile.SIZE_ASC_NO_GROUP -> "Size tăng dần";
                    default -> "Ngày tăng dần";
                }
                + ")");
        sortButton.setOnMenuItemClickListener(menuItem -> {
            switchSortMode();
            return false;
        });

        toolbar.getMenu().findItem(R.id.set_display_menu_item).setOnMenuItemClickListener(menuItem -> {
           if(uiMode == UI_MODE_GRID){
               uiMode = UI_MODE_LIST;
           }else{
               uiMode = UI_MODE_GRID;
           }

           SharedPreferences.Editor editor = sharedPreferences.edit();

           editor.putInt("ui_mode", uiMode);
           editor.apply();
           refresh();

            return false;
        });

        searchButton = toolbar.getMenu().findItem(R.id.search_tb_item);

        SearchManager searchManager =
                (SearchManager) context.getSystemService(Context.SEARCH_SERVICE);

        SearchView searchView =
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
                return false;
            }
        });

        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(requireActivity().getComponentName()));

        searchView.setIconifiedByDefault(false);

        searchView.setSubmitButtonEnabled(true);


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //getSearchResults(query); Also tried

                long result = database.insertSuggestion(query);
                getSearchResults(searchView.getQuery().toString());
                if( ! searchView.isIconified()) {
                    searchView.setIconified(true);
                }
                searchButton.collapseActionView();
                return result != -1;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Cursor cursor = database.getSuggestions(newText);
                if(cursor.getCount() != 0)
                {
                    String[] columns = new String[] {SuggestionsDatabase.FIELD_SUGGESTION };
                    int[] columnTextId = new int[] { android.R.id.text1};

                    SuggestionSimpleCursorAdapter simple = new SuggestionSimpleCursorAdapter(context.getApplicationContext(),
                            R.layout.search_suggestion_item, cursor,
                            columns , columnTextId
                            , 0);

                    searchView.setSuggestionsAdapter(simple);
                    return true;
                }
                else
                {
                    return false;
                }
            }
        });
    }


    void getSearchResults(String toString) {
        Intent intent = new Intent(this.getContext(), SearchResultActivity.class);
        intent.setAction(Intent.ACTION_SEARCH);
        intent.putExtra(SearchManager.QUERY, toString);
        context.startActivity(intent);
    }

    private void enableCamera() {
        Intent intent = new Intent(getActivity(), CameraActivity.class);
        startActivity(intent);
    }

    private void openSetting(){
        Intent intent = new Intent(getActivity(), SettingsActivity.class);
        startActivity(intent);
    }

    int sortMode = AccessMediaFile.DATE_DESC;
    private void switchSortMode() {
        sortMode = (sortMode + 1) % AccessMediaFile.SORT_MODE_COUNT;
        sortButton.setTitle("Sắp xếp" + " (" + sortMode + ": " +
                switch(sortMode) {
                    case AccessMediaFile.DATE_DESC -> "Ngày giảm dần";
                    case AccessMediaFile.SIZE_DESC -> "Size giảm dần theo ngày";
                    case AccessMediaFile.SIZE_ASC -> "Size tăng dần theo ngày";
                    case AccessMediaFile.SIZE_DESC_NO_GROUP -> "Size giảm dần";
                    case AccessMediaFile.SIZE_ASC_NO_GROUP -> "Size tăng dần";
                    default -> "Ngày tăng dần";
                }
                + ")");

        if (sortMode != 1) {
            refresh(true);
        } else {
            refresh();
        }
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

//    public long hashBitmap(Bitmap bmp) {
//        long hash = 31;
//        for (int x = 1; x < bmp.getWidth(); x = x * 2) {
//            for (int y = 1; y < bmp.getHeight(); y = y * 2) {
//                hash *= (bmp.getPixel(x, y) + 31);
//                hash = hash % 1111122233;
//            }
//        }
//        return hash;
//    }

    @NonNull
    ArrayList<MediaCategory> getListCategory() {
        AccessMediaFile.refreshAllMedia();
        HashMap<String, MediaCategory> categoryList = new LinkedHashMap<>();
        listMedia = AccessMediaFile.getAllMedia(context, sortMode);

        try {
//            if (listMedia.get(0).getRawDate() != 0) {
//                categoryList.put(listMedia.get(0).getDateTaken(), new MediaCategory(listMedia.get(0).getDateTaken(), new ArrayList<>()));
//                categoryList.get(listMedia.get(0).getDateTaken()).addMediaToList(listMedia.get(0));
//            } else {
//
//            }

            for (int i = 0; i < listMedia.size(); i++) {
                if (listMedia.get(i).getRawDate() == 0) {
                    continue;
                }
                var catName = listMedia.get(i).getDateTaken();
                if (sortMode >= 4) {
                    // catName = "";
                    String s = listMedia.get(i).getSize();
                    var ext = s.substring(s.length() - 2);
                    s = s.substring(0, s.length() - 2);
                    double size = Double.parseDouble(s);
                    int roundedSize = (int)Math.floor(size);
                    catName = roundedSize < 10 ? roundedSize + ext : (roundedSize / 10) * 10 + ext;
                    if (!categoryList.containsKey(catName)) {
                        categoryList.put(catName, new MediaCategory(catName, new ArrayList<>()));
                    }
                } else {
                    if (!categoryList.containsKey(catName)) {
                        categoryList.put(catName, new MediaCategory(catName, new ArrayList<>()));
                    }
                }

                categoryList.get(catName).addMediaToList(listMedia.get(i));
            }
//            categoryList.forEach(x -> {
//                Log.d("gallerium", x.getNameCategory() + ": " + x.getList().size());
//            });
            var newCatList = new ArrayList<MediaCategory>();
            int partitionSize = numGrid == 3 ? 30 : numGrid == 4 ? 24 : 20;
            for (var cat : categoryList.values()) {
                if (sortMode >= 4) cat.setNameCategory("");
                // cat.getList().sort(Comparator.comparingLong(Media::getRawDate).reversed());
                if (sortMode == AccessMediaFile.SIZE_DESC) {
                    cat.getList().sort(Comparator.comparingLong(Media::getRealSize).reversed());
                } else if (sortMode == AccessMediaFile.SIZE_ASC) {
                    cat.getList().sort(Comparator.comparingLong(Media::getRealSize));
                }
                if (cat.getList().size() < partitionSize) {
                    newCatList.add(cat);
                    continue;
                }
                for (int i = 0; i < cat.getList().size(); i += partitionSize) {
                    String name = i == 0 ? cat.getNameCategory() : "";
                    var c = new MediaCategory(name, new ArrayList<>(cat.getList().subList(i,
                            Math.min(i + partitionSize, cat.getList().size()))));
                    if (c.getNameCategory().isEmpty() && sortMode < 4) {
                        c.setNameCategory("   " + c.getList().get(0).getTimeTaken());
                        c.setBackup(cat.getNameCategory());
                    }
                    newCatList.add(c);

                }
            }
            AccessMediaFile.getAllFavMedia().forEach(x -> Log.d("fav", x.getRawDate() + ": " + x.getPath()));

            return newCatList;
        } catch (Exception e) {
            return new ArrayList<>();
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
            albumAdapter = new AlbumCategoryAdapter(context, MediaFragment.this, 3);
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

//    public void rescanForUnAddedAlbum(){
//        Cursor cursor =  context.getContentResolver().query(
//                MediaStore.Files.getContentUri("external")
//                , new String[]{MediaStore.Files.FileColumns.DISPLAY_NAME, MediaStore.Files.FileColumns.PARENT, MediaStore.Files.FileColumns.DATA}
//                , MediaStore.Files.FileColumns.DATA + " LIKE ?"
//                , new String[]{Environment.getExternalStorageDirectory() + "/Pictures/owner/%"}, null);
//
//        int nameColumn = cursor.getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME);
//        // int bucketNameColumn = cursor.getColumnIndex(MediaStore.Files.FileColumns.PARENT);
//        int pathColumn = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA);
//        String name;
//        try {
//            if (cursor != null) {
//                Log.d("size", "" + cursor.getCount());
//
//                ArrayList<Album> temp = new ArrayList<>();
//                while (cursor.moveToNext()) {
//                    String path = cursor.getString(pathColumn);
//                    Log.d("path", path);
//                    name = cursor.getString(nameColumn);
//                    Log.d("name", name);
//                    String[] subDirs = path.split("/");
//                    if(!subDirs[subDirs.length-2].equals("owner")) continue;
//                    Album album = new Album(null, name);
//                    album.setPath(path);
//                    temp.add(album);
//                }
//                for(Album album: albumList){
//                    for(int i=0;i<temp.size();i++){
//                        if(temp.get(i).getPath().equals(album.getPath())){
//                            temp.remove(i);
//                        }
//                    }
//                }
//                if(temp.size() >0)albumList.addAll(temp);
//            }
//        }catch (Exception e){
//            Log.d("tag", e.getMessage());
//        }
//    }

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

        // rescanForUnAddedAlbum();
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
            sharedPreferences = context.getSharedPreferences("ui_list", MODE_PRIVATE);
            uiMode = sharedPreferences.getInt("ui_mode", UI_MODE_GRID);
            swipeLayout.setRefreshing(true);
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            adapter.setUiMode(uiMode);
            adapter.setData(mediaCategories);
            recyclerView.setAdapter(adapter);
            Log.d("refresh", "refresh media adapter");
            if (scroll) {
                adapter.notifyDataSetChanged();
                ((LinearLayoutManager) Objects.requireNonNull(recyclerView.getLayoutManager())).scrollToPosition(0);
                firstVisiblePosition = 0;
                offset = 0;
            } else {
                ((LinearLayoutManager) Objects.requireNonNull(recyclerView.getLayoutManager())).scrollToPositionWithOffset(firstVisiblePosition, offset);
            }
            swipeLayout.setRefreshing(false);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            mediaCategories = getListCategory();
            return null;
        }
    }
}