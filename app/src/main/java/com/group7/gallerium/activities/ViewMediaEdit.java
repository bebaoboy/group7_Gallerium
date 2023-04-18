package com.group7.gallerium.activities;

import android.app.Activity;
import android.content.ClipData;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.dsphotoeditor.sdk.activity.DsPhotoEditorActivity;
import com.dsphotoeditor.sdk.utils.DsPhotoEditorConstants;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.group7.gallerium.R;
import com.group7.gallerium.adapters.AlbumCategoryAdapter;
import com.group7.gallerium.adapters.SlideAdapter;
import com.group7.gallerium.fragments.ActionBottomDialogFragment;
import com.group7.gallerium.models.Album;
import com.group7.gallerium.models.AlbumCategory;
import com.group7.gallerium.models.Media;
import com.group7.gallerium.utilities.AccessMediaFile;
import com.group7.gallerium.utilities.FileUtils;
import com.group7.gallerium.utilities.MediaItemInterface;
import com.group7.gallerium.utilities.SelectMediaInterface;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

@SuppressWarnings("rawtypes")
public class ViewMediaEdit extends AppCompatActivity implements MediaItemInterface, SelectMediaInterface {

    BottomNavigationView bottom_nav;
    private Toolbar toolbar;
    MenuItem favBtn;
    MediaController videoController;
    private int mediaPos;
    String mediaPath;
    private Intent intent;

    private ActivityResultLauncher<Intent> editResult;
    ArrayList<String> listPath;
    private MediaItemInterface mediaItemInterface;
    private ViewPager viewPager;
    private SlideAdapter slideAdapter;

    VideoView videoView;
    ImageView playButton;
    ImageView img;

    LinearLayout bottomSheet;
    BottomSheetBehavior behavior;

    private BottomSheetDialog bottomSheetDialog;

    TextView btnSetBackGround, btnAddToAlbum;
    private TextView btnShowDetails, btnRename;
    RecyclerView addAlbumRecyclerView;
    ArrayList<Album> albumList;
    ArrayList<AlbumCategory> albumCategories;

    AlbumCategoryAdapter albumAdapter;

    private  ActivityResultLauncher<IntentSenderRequest> launcher;
    private  ActivityResultLauncher<IntentSenderRequest> launcherModified;
    private FileUtils fileUtils;
    ActionBottomDialogFragment renameBottomDialogFragment;
    ArrayList<Media> n = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        albumList = new ArrayList<>();
        albumCategories = new ArrayList<>();
        listPath = new ArrayList<>();
        setContentView(R.layout.activity_view_media);
        toolbar = findViewById(R.id.toolbar_photo_view);
        bottom_nav = findViewById(R.id.view_photo_bottom_navigation);
        bottom_nav.inflateMenu(R.menu.menu_bottom_view_photo);
        viewPager = findViewById(R.id.viewPager_picture);
        videoController = new MediaController(this){
            public boolean dispatchKeyEvent(KeyEvent event)
            {
                if (event.getKeyCode() == KeyEvent.KEYCODE_BACK)
                    ((Activity) getContext()).finish();

                return super.dispatchKeyEvent(event);
            }
        };
        fileUtils = new FileUtils();

        applyData();
        toolbarSetting();
        setUpSlider();
        bottomNavCustom();

        bottomSheetConfig();
        behavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        bottomSheet.setVisibility(View.GONE);

        bottomSheetButtonConfig();

        bottom_nav.setBackgroundTintList(null);

        launcher = registerForActivityResult(
                new ActivityResultContracts.StartIntentSenderForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Toast.makeText(ViewMediaEdit.this, "deleted", Toast.LENGTH_SHORT).show();
                        AccessMediaFile.removeMediaFromAllMedia(mediaPath);
                        slideAdapter.removePath(mediaPath);
                        finish();
                    }
                });

        launcherModified = registerForActivityResult(
                new ActivityResultContracts.StartIntentSenderForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Toast.makeText(ViewMediaEdit.this, "renaming", Toast.LENGTH_SHORT).show();
                        renameBottomDialogFragment.renameAgain();
                    }
                });
    }

    private void bottomSheetConfig() {
        bottomSheet = findViewById(R.id.more_bottom_sheet);

        behavior = BottomSheetBehavior.from(bottomSheet);
        behavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {

            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {

                switch (newState) {
//                    case BottomSheetBehavior.STATE_HIDDEN ->
//                            Toast.makeText(ViewMediaEdit.this, "Hidden sheet", Toast.LENGTH_SHORT).show();
//                    case BottomSheetBehavior.STATE_EXPANDED ->
//                            Toast.makeText(ViewMediaEdit.this, "Expand sheet", Toast.LENGTH_SHORT).show();
//                    case BottomSheetBehavior.STATE_COLLAPSED ->
//                            Toast.makeText(ViewMediaEdit.this, "Collapsed sheet", Toast.LENGTH_SHORT).show();
//                    case BottomSheetBehavior.STATE_DRAGGING ->
//                            Toast.makeText(ViewMediaEdit.this, "Dragging sheet", Toast.LENGTH_SHORT).show();
//                    case BottomSheetBehavior.STATE_SETTLING ->
//                            Toast.makeText(ViewMediaEdit.this, "Settling sheet", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
    });
    }

    void  bottomSheetButtonConfig() {
        btnAddToAlbum = findViewById(R.id.add_to_album_item);
        btnRename = findViewById(R.id.change_name_item);
        btnSetBackGround = findViewById(R.id.set_sys_background_item);
        btnShowDetails = findViewById(R.id.show_details_item);

        btnAddToAlbum.setOnClickListener((v) -> {
            openAlbumSelectView();
        });
        btnRename.setOnClickListener((v) -> {
            behavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            bottomSheet.setVisibility(View.GONE);
            rename();
        });
        btnSetBackGround.setOnClickListener((v) -> {
            behavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            bottomSheet.setVisibility(View.GONE);
            Uri contentURI = fileUtils.getUri(mediaPath, AccessMediaFile.getMediaWithPath(mediaPath).getType(), ViewMediaEdit.this);
//            try {
//                WallpaperManager wallpaperManager = WallpaperManager.getInstance(ViewMediaStandalone.this);
//                intent = wallpaperManager.getCropAndSetWallpaperIntent(contentURI);
//            } catch (Exception e) {
                intent = new Intent(Intent.ACTION_ATTACH_DATA);
                intent.setDataAndType(contentURI, "image/*");
                intent.putExtra("jpg", "image/*");
                startActivity(Intent.createChooser(intent,
                        "Set picture as: "));
//            }
            startActivity(intent);
        });
        btnShowDetails.setOnClickListener((v) -> {
            showDetails();
            behavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            bottomSheet.setVisibility(View.GONE);
        });
    }

    void openAlbumSelectView() {
        Log.d("Open bottom sheet", "true");
        View viewDialog = LayoutInflater.from(ViewMediaEdit.this).inflate(R.layout.add_to_album_bottom_dialog, null);
        addAlbumRecyclerView = viewDialog.findViewById(R.id.rec_add_to_album);
        addAlbumRecyclerView.setLayoutManager(new GridLayoutManager(ViewMediaEdit.this, 1));
        bottomSheetDialog = new BottomSheetDialog(ViewMediaEdit.this);
        bottomSheetDialog.setContentView(viewDialog);
//        AlbumListTask albumAsyncTask = new AlbumListTask();
//        albumAsyncTask.execute();
    }

    void rename(){
        renameBottomDialogFragment =
                ActionBottomDialogFragment.newInstance();
        renameBottomDialogFragment.show(getSupportFragmentManager(),
                ActionBottomDialogFragment.TAG);
        renameBottomDialogFragment.setPath(mediaPath);
        renameBottomDialogFragment.setTitle("Đổi tên");

        renameBottomDialogFragment.setLauncher(launcherModified);

    }

    void showDetails(){
       Intent intent = new Intent(this, MediaDetails.class);
       intent.putExtra("media_path", mediaPath);
       startActivity(intent);
    }


    void applyData() {
        AccessMediaFile.getAllMedia(ViewMediaEdit.this);
        intent = getIntent();
        ArrayList<Uri> uriArrayList = new ArrayList<>();
        Uri uri = intent.getData();
        if (uri != null) {
            uriArrayList.add(uri);
        }
        var uris = intent.getParcelableArrayExtra(Intent.EXTRA_STREAM);
        if (uris != null)
        {
            for(var u : uris) {
                uriArrayList.add((Uri)u);
            }
        }
        ClipData clipData = intent.getClipData();
        String s = "";
        if (clipData != null) {
            for (int i = 0; i < clipData.getItemCount(); i++) {
                ClipData.Item item = clipData.getItemAt(i);
                uriArrayList.add(item.getUri());
            }
        }
        try {
            var dir = Environment.getExternalStorageDirectory();
            String relativePath = "Pictures/t3mp";
            String path = dir.getPath() + File.separator + relativePath;
            fileUtils.createDir(this, path, "t3mp", relativePath);
        }  catch (Exception e) {
            Log.d("tag", e.getMessage());
            // Toast.makeText(context, "Needed permission. \nPress 'Choose this folder' to continue. ", Toast.LENGTH_LONG).show();
        }
        for(var u : uriArrayList) {
            if (AccessMediaFile.getMediaWithPath(u.getPath()) == null) {
                var temp = getFileFromContentUri(this, u, true);
                AccessMediaFile.refreshAllMedia();
                AccessMediaFile.getAllMedia(this);
                listPath.add(temp.getPath());
                n.add(AccessMediaFile.getMediaWithPath(temp.getPath()));
            } else {
                listPath.add(u.getPath());
            }
        }
        //listPath = intent.getStringArrayListExtra("data_list_path");
        mediaPos = intent.getIntExtra("pos", 0);
        mediaItemInterface = this;
    }

    private File getFileFromContentUri(Context context, Uri contentUri,Boolean uniqueName) {
        // Preparing Temp file name
        var fileExtension = getFileExtension(context, contentUri);
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 7;
        Random random = new Random();

        String generatedString = random.ints(leftLimit, rightLimit + 1)
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
        var timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(System.currentTimeMillis()) + generatedString;
        var fileName = ("temp_file_" + timeStamp + "." + fileExtension);
        var dir = Environment.getExternalStorageDirectory();
        String relativePath = "Pictures/t3mp";
        String path = dir.getPath() + File.separator + relativePath;
        var tempFile = new File(Environment.getExternalStorageDirectory() + "/Pictures/t3mp/" + fileName);
        // Initialize streams

        try (InputStream inputStream = getContentResolver().openInputStream(contentUri);
             OutputStream oStream = new FileOutputStream(tempFile)){
            copy(inputStream, oStream);
            oStream.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return tempFile;
    }

    private String getFileExtension(Context context, Uri uri) {
        if (Objects.equals(uri.getScheme(), ContentResolver.SCHEME_CONTENT))
            return MimeTypeMap.getSingleton().getExtensionFromMimeType(context.getContentResolver().getType(uri));
        else {
            return MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(new File(uri.getPath())).toString());
        }
    }

    private void copy(InputStream inputStream, OutputStream out)  throws IOException {
        byte[] buf = new byte[8096];
        int len;
        while ((len = inputStream.read(buf)) > 0) {
            out.write(buf, 0, len); //write input file data to output file
        }
        out.close();
    }


    private void toolbarSetting() {
        // Toolbar events
        toolbar.inflateMenu(R.menu.menu_top_view_photo);
        favBtn = toolbar.getMenu().findItem(R.id.add_fav);
        toolbar.setTitle("hello");
        toolbar.setTitleTextAppearance(ViewMediaEdit.this, R.style.ToolbarTitleMediaView);
        toolbar.setSubtitleTextAppearance(ViewMediaEdit.this, R.style.ToolbarSubtitle);
        toolbar.setNavigationIcon(R.drawable.ic_back_arrow);
        toolbar.setNavigationOnClickListener((view) -> finish());
        favBtn.setOnMenuItemClickListener(menuItem -> {
            if (menuItem.getItemId() == R.id.add_fav) {
                if (AccessMediaFile.isExistedAnywhere(mediaPath)) {
                    AccessMediaFile.removeFromFavMedia(mediaPath);
                    menuItem.setIcon(R.drawable.ic_fav_empty);
                } else {
                    AccessMediaFile.addToFavMedia(mediaPath);
                    menuItem.setIcon(R.drawable.ic_fav_solid);
                }
            }
            return menuItem.isChecked();
        });
    }

    private void showNavigation(boolean flag) {
        if (!flag) {
            bottom_nav.setVisibility(View.INVISIBLE);
            toolbar.setVisibility(View.INVISIBLE);
        } else {
            bottom_nav.setVisibility(View.VISIBLE);
            toolbar.setVisibility(View.VISIBLE);
        }
    }

    private void setUpSlider() {

        slideAdapter = new SlideAdapter(ViewMediaEdit.this);
        slideAdapter.setData(listPath);
        slideAdapter.setInterface(mediaItemInterface);
        viewPager.setAdapter(slideAdapter);
        viewPager.setCurrentItem(mediaPos);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (renameBottomDialogFragment != null) {
                    if (renameBottomDialogFragment.getOldPath().equals(listPath.get(position))) {
                        listPath.set(position, renameBottomDialogFragment.getPath());
                        renameBottomDialogFragment = null;
                    }
                }
                mediaPath = listPath.get(position);
                final Media m = AccessMediaFile.getMediaWithPath(mediaPath);
                if (m!=null) {
                    //assert m != null;
                    setTitleToolbar(m);
                }
                if (videoController != null) {
                    videoController.setVisibility(View.GONE);
                }
                if(videoView != null){
                    videoView.setVisibility(View.GONE);
                    videoView.stopPlayback();
                    img.setVisibility(View.VISIBLE);
                    playButton.setVisibility(View.VISIBLE);
                }
                if (AccessMediaFile.getMediaWithPath(mediaPath).getType() != 1) {
                    btnSetBackGround.setVisibility(View.GONE);
                } else {
                    btnSetBackGround.setVisibility(View.VISIBLE);
                }

                favBtn.setIcon(AccessMediaFile.isExistedAnywhere(mediaPath) ? R.drawable.ic_fav_solid : R.drawable.ic_fav_empty);
                behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                bottomSheet.setVisibility(View.GONE);
            }

            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    public void setTitleToolbar(@NonNull Media m) {
        var name = mediaPath.substring(mediaPath.lastIndexOf('/') + 1);
        toolbar.setTitle(new SimpleDateFormat("EEE, d MMM (HH:mm)").format(m.getRawDate()));
        toolbar.setSubtitle(name);
        if (AccessMediaFile.isExistedAnywhere(mediaPath)) {
            favBtn.setIcon(R.drawable.ic_fav_solid);
        }
        Intent editIntent = new Intent(ViewMediaEdit.this, DsPhotoEditorActivity.class);

        if(mediaPath.contains("gif")){
            Toast.makeText(this,"Cannot edit GIF images",Toast.LENGTH_SHORT).show();
            finish();
        } else{
            // Set data
            editIntent.setData(Uri.fromFile(new File(mediaPath)));
            // Set output directory
            editIntent.putExtra(DsPhotoEditorConstants.DS_PHOTO_EDITOR_OUTPUT_DIRECTORY, "Gallerium");
            // Set toolbar color
            editIntent.putExtra(DsPhotoEditorConstants.DS_TOOL_BAR_BACKGROUND_COLOR, Color.parseColor("#FF000000"));
            // Set background color
            editIntent.putExtra(DsPhotoEditorConstants.DS_MAIN_BACKGROUND_COLOR, Color.parseColor("#FF000000"));
            // Start activity
            startActivity(editIntent);
            finish();
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        var favList = AccessMediaFile.getFavMedia();
        //Log.d("fav", "fav amount pause = " + favList.size());
        favList.forEach(x -> Log.d("fav", x));
        SharedPreferences sharedPreferences = getSharedPreferences("fav_media", MODE_PRIVATE);
        SharedPreferences.Editor myEdit = sharedPreferences.edit();
        myEdit.clear();

        // write all the data entered by the user in SharedPreference and apply
        myEdit.putStringSet("path", favList);
        myEdit.apply();
    }

    public void bottomNavCustom() {
        bottom_nav.setOnItemSelectedListener(item -> {

            switch (item.getItemId()){
                case R.id.edit_nav_item->{
                    Intent editIntent = new Intent(ViewMediaEdit.this, DsPhotoEditorActivity.class);

                    if(mediaPath.contains("gif")){
                        Toast.makeText(this,"Cannot edit GIF images",Toast.LENGTH_SHORT).show();
                    } else{
                        // Set data
                        editIntent.setData(Uri.fromFile(new File(mediaPath)));
                        // Set output directory
                        editIntent.putExtra(DsPhotoEditorConstants.DS_PHOTO_EDITOR_OUTPUT_DIRECTORY, "Gallerium");
                        // Set toolbar color
                        editIntent.putExtra(DsPhotoEditorConstants.DS_TOOL_BAR_BACKGROUND_COLOR, Color.parseColor("#FF000000"));
                        // Set background color
                        editIntent.putExtra(DsPhotoEditorConstants.DS_MAIN_BACKGROUND_COLOR, Color.parseColor("#FF000000"));
                        // Start activity
                        startActivity(editIntent);
                    }
                }

                case R.id.more_nav_item -> {

                    if (bottomSheet.getVisibility() == View.VISIBLE) {
                        behavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                        behavior.setPeekHeight(0);
                        behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        bottomSheet.setVisibility(View.GONE);
                    } else if (bottomSheet.getVisibility() == View.GONE) {
                        bottomSheet.setVisibility(View.VISIBLE);
                        behavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                        behavior.setPeekHeight(820);
                        behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    }
                }

                case R.id.delete_nav_item ->{

                    if (fileUtils.delete(launcher, mediaPath, this) > 0) {
                        Toast.makeText(ViewMediaEdit.this, "deleted", Toast.LENGTH_SHORT).show();
                        AccessMediaFile.removeMediaFromAllMedia(mediaPath);
                        slideAdapter.removePath(mediaPath);
                        finish();
                    }
                }
            }
            return true;
        });
    }

    @Override
    public void showActionBar(boolean trigger) {
        showNavigation(trigger);
    }

    @Override
    public void showVideoPlayer(@NonNull VideoView videoView, @NonNull ImageView img2, @NonNull ImageView btn, @NonNull TextView duration, @NonNull Media m) {
        Uri uri = Uri.parse(mediaPath);

        // sets the resource from the
        // videoUrl to the videoView
        videoView.setVideoURI(uri);

        // creating object of
        // media controller class
        // sets the anchor view
        // anchor view for the videoView
        videoController.setAnchorView(videoView);

        // sets the media player to the videoView
        videoController.setMediaPlayer(videoView);
        videoController.setVisibility(View.VISIBLE);
        duration.setText(m.getDuration());

        videoView.setOnCompletionListener(mediaPlayer -> {
            // showActionBar(true);
            videoController.setVisibility(View.GONE);
            videoView.setMediaController(videoController);
            videoView.setVisibility(View.GONE);
            img.setVisibility(View.VISIBLE);
            playButton.setVisibility(View.VISIBLE);
        });
        setDimension(videoView, m);

        // sets the media controller to the videoView
        videoView.setMediaController(videoController);

        this.videoView = videoView;
        this.videoView.setVisibility(View.VISIBLE);
        img = img2;
        playButton = btn;
        this.img.setVisibility(View.GONE);
        this.playButton.setVisibility(View.GONE);
        // starts the video
        this.videoView.start();
    }

    @Override
    public void setVideoView(@NonNull VideoView videoView) {
        this.videoView = videoView;
    }

    @Override
    public void setImageViewAndButton(@NonNull ImageView img, @NonNull ImageView playButton) {
        this.img = img;
        this.playButton = playButton;
        this.img.setVisibility(View.VISIBLE);
        this.playButton.setVisibility(View.VISIBLE);
    }


    private void setDimension(VideoView videoView, Media m) {
        // Adjust the size of the video
        // so it fits on the screen
        float videoProportion = m.getHeight() / (float)m.getWidth();
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int screenHeight = getResources().getDisplayMetrics().heightPixels;
        float screenProportion = (float) screenHeight / (float) screenWidth;
        android.view.ViewGroup.LayoutParams lp = videoView.getLayoutParams();

        if (videoProportion > screenProportion) {
            if (screenProportion < 1) { // screen width > height (landscape)
                if (m.getHeight() > screenHeight) {
                    float ratio = (float) screenHeight / m.getHeight();
                    lp.height = (int) (m.getHeight() * ratio);
                    lp.width = (int) (m.getWidth() * ratio);
                }
                else {
                    lp.height = screenHeight;
                }
                if (m.getWidth() > screenWidth) {
                    float ratio = (float) screenWidth / lp.width;
                    lp.height = (int) (m.getHeight() * ratio);
                    lp.width = (int) (m.getWidth() * ratio);
                } else {
                    lp.width = screenWidth;
                }
            } else if (screenProportion > 1) { // screen width < height (portrait)
                if (m.getWidth() > screenWidth) {
                    float ratio = (float) screenWidth / m.getWidth();
                    lp.height = (int) (m.getHeight() * ratio);
                    lp.width = (int) (m.getWidth() * ratio);
                }
                else {
                    lp.width = screenWidth;
                }
                if (lp.height > screenHeight) {
                    float ratio = (float) screenHeight / lp.height;
                    lp.height = (int) (m.getHeight() * ratio);
                    lp.width = (int) (m.getWidth() * ratio);
                }
                else {
                    lp.height = screenHeight;
                }
            }
        }
        videoView.setLayoutParams(lp);
    }

    @Override
    public void showAllSelect() {

    }

    @Override
    public void addToSelectedList(@NonNull Media media) {

    }

    @NonNull
    @Override
    public ArrayList<Media> getSelectedList() {
        return null;
    }

    @Override
    public void deleteFromSelectedList(@NonNull Media media) {

    }

    @Override
    public void moveMedia(@NonNull String albumPath) {
        var temp = new ArrayList<Media>();
        temp.add(AccessMediaFile.getMediaWithPath(mediaPath));
        fileUtils.copyFileMultiple(temp, albumPath, ViewMediaEdit.this);
        behavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        bottomSheet.setVisibility(View.GONE);
        bottomSheetDialog.dismiss();
    }


    @Override
    protected void onStop() {
        fileUtils.deleteMultiple(launcher, n, this);
        super.onStop();
    }
}