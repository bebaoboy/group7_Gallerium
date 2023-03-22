package com.group7.gallerium.activities;

import static android.app.WallpaperManager.ACTION_CROP_AND_SET_WALLPAPER;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.media.ExifInterface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.dsphotoeditor.sdk.activity.DsPhotoEditorActivity;
import com.dsphotoeditor.sdk.utils.DsPhotoEditorConstants;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.navigation.NavigationBarView;
import com.group7.gallerium.R;
import com.group7.gallerium.adapters.SlideAdapter;
import com.group7.gallerium.models.Media;
import com.group7.gallerium.utilities.AccessMediaFile;
import com.group7.gallerium.utilities.FileUtils;
import com.group7.gallerium.utilities.MediaItemInterface;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class ViewMedia extends AppCompatActivity implements MediaItemInterface{

    BottomNavigationView bottom_nav;
    private Toolbar toolbar;
    private MenuItem favBtn;
    private MediaController videoController;
    private int mediaPos;
    private String mediaPath;
    private String mediaName;
    private Intent intent;

    private  ActivityResultLauncher<Intent> editResult;
    private ArrayList<String> listPath;
    private MediaItemInterface mediaItemInterface;
    private ViewPager viewPager;
    private SlideAdapter slideAdapter;

    private VideoView videoView;
    private ImageView playButton;
    private ImageView img;

    private LinearLayout bottomSheet;
    private BottomSheetBehavior behavior;

    private BottomSheetDialog bottomSheetDialog;

    private TextView btnSetBackGround, btnAddToAlbum;
    private TextView btnShowDetails, btnRename;

    private  ActivityResultLauncher<IntentSenderRequest> launcher;
    private FileUtils fileUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_view_media);
        toolbar = findViewById(R.id.toolbar_photo_view);
        bottom_nav = findViewById(R.id.view_photo_bottom_navigation);
        viewPager = findViewById(R.id.viewPager_picture);
        videoController = new MediaController(this){
            public boolean dispatchKeyEvent(KeyEvent event)
            {
                if (event.getKeyCode() == KeyEvent.KEYCODE_BACK)
                    ((Activity) getContext()).finish();

                return super.dispatchKeyEvent(event);
            }
        };
        applyData();
        toolbarSetting();
        setUpSlider();
        bottomNavCustom();

        bottomSheetConfig();
        behavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        bottomSheet.setVisibility(View.GONE);

        bottomSheetButtonConfig();

        bottom_nav.setBackgroundTintList(null);

        fileUtils = new FileUtils();

        launcher = registerForActivityResult(
                new ActivityResultContracts.StartIntentSenderForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Toast.makeText(getApplicationContext(), "deleted", Toast.LENGTH_SHORT).show();
                        AccessMediaFile.removeMediaFromAllMedia(mediaPath);
                        slideAdapter.removePath(mediaPath);
                        finish();
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

    void  bottomSheetButtonConfig() {
        btnAddToAlbum = findViewById(R.id.add_to_album_item);
        btnRename = findViewById(R.id.change_name_item);
        btnSetBackGround = findViewById(R.id.set_sys_background_item);
        btnShowDetails = findViewById(R.id.show_details_item);

        btnAddToAlbum.setOnClickListener((v) -> {
            behavior.setState(BottomSheetBehavior.STATE_HIDDEN);
           bottomSheet.setVisibility(View.GONE);
        });
        btnRename.setOnClickListener((v) -> {
            behavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            bottomSheet.setVisibility(View.GONE);
        });
        btnSetBackGround.setOnClickListener((v) -> {
            behavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            bottomSheet.setVisibility(View.GONE);
            File wallpaperFile = new File(mediaPath);
            Uri contentURI = getImageContentUri(this, wallpaperFile.getAbsolutePath());
            WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);
            intent = new Intent(wallpaperManager.getCropAndSetWallpaperIntent(contentURI));
            startActivity(intent);
        });
        btnShowDetails.setOnClickListener((v) -> {
            Uri mediaUri = Uri.parse("file://" + mediaPath);
            showDetails(mediaUri);
            behavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            bottomSheet.setVisibility(View.GONE);
        });
    }

    public static Uri getImageContentUri(Context context, String absPath) {
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                , new String[] { MediaStore.Images.Media._ID }
                , MediaStore.Images.Media.DATA + "=? "
                , new String[] { absPath }, null);

        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
            return Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI , Integer.toString(id));

        } else if (!absPath.isEmpty()) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DATA, absPath);
            return context.getContentResolver().insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        } else {
            return null;
        }
    }

    void bottomSheetDialogConfig() {

        View moreBottomView = LayoutInflater.from(this).inflate(R.layout.more_bottom_sheet,
                (LinearLayout)findViewById(R.id.more_bottom_sheet),  false);
        bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(moreBottomView);

        btnAddToAlbum = moreBottomView.findViewById(R.id.add_to_album_item);
        btnRename = moreBottomView.findViewById(R.id.change_name_item);
        btnSetBackGround = moreBottomView.findViewById(R.id.set_sys_background_item);
        btnShowDetails = moreBottomView.findViewById(R.id.show_details_item);

        btnAddToAlbum.setOnClickListener((v) -> {
            bottomSheetDialog.cancel();
        });
        btnRename.setOnClickListener((v) -> {
            bottomSheetDialog.cancel();
        });
        btnSetBackGround.setOnClickListener((v) -> {
            bottomSheetDialog.cancel();
        });
        btnShowDetails.setOnClickListener((v) -> {
            Uri mediaUri = Uri.parse("file://" + mediaPath);
            showDetails(mediaUri);
            bottomSheetDialog.cancel();
        });
    }

    void showDetails(Uri uri){
       Intent intent = new Intent(this, MediaDetails.class);
       intent.putExtra("media_path", mediaPath);
       startActivity(intent);
    }


    void applyData() {
        intent = getIntent();
        listPath = intent.getStringArrayListExtra("data_list_path");
        mediaPos = intent.getIntExtra("pos", 0);
        mediaItemInterface = this;
    }


    private void toolbarSetting() {
        // Toolbar events
        toolbar.inflateMenu(R.menu.menu_view_photo);
        favBtn = toolbar.getMenu().findItem(R.id.add_fav);
        toolbar.setTitle("hello");
        toolbar.setTitleTextAppearance(getApplicationContext(), R.style.ToolbarTitleMediaView);
        toolbar.setSubtitleTextAppearance(getApplicationContext(), R.style.ToolbarSubtitle);
        toolbar.setNavigationIcon(R.drawable.ic_back_arrow);
        toolbar.setNavigationOnClickListener((view) -> finish());
        favBtn.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.add_fav) {
                    if (AccessMediaFile.isFavMediaContains(mediaPath)) {
                        AccessMediaFile.removeFromFavMedia(mediaPath);
                        menuItem.setIcon(R.drawable.ic_fav_empty);
                    } else {
                        AccessMediaFile.addToFavMedia(mediaPath);
                        menuItem.setIcon(R.drawable.ic_fav_solid);
                    }
                }
                return menuItem.isChecked();
            }
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

        slideAdapter = new SlideAdapter(getApplicationContext());
        slideAdapter.setData(listPath);
        slideAdapter.setInterface(mediaItemInterface);
        viewPager.setAdapter(slideAdapter);
        viewPager.setCurrentItem(mediaPos);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                mediaPath = listPath.get(position);
                final Media m = AccessMediaFile.getMediaWithPath(mediaPath);
                assert m != null;
                setTitleToolbar(m);
                if (videoController != null) {
                    videoController.setVisibility(View.GONE);
                }
                if(videoView != null){
                    videoView.setVisibility(View.GONE);
                    videoView.stopPlayback();
                    img.setVisibility(View.VISIBLE);
                    playButton.setVisibility(View.VISIBLE);
                }
                favBtn.setIcon(AccessMediaFile.isFavMediaContains(mediaPath) ? R.drawable.ic_fav_solid : R.drawable.ic_fav_empty);
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

    public void setTitleToolbar(Media m) {
        var name = mediaPath.substring(mediaPath.lastIndexOf('/') + 1);
        this.mediaName = name;
        toolbar.setTitle(new SimpleDateFormat("EEE, d MMM (HH:mm)").format(m.getRawDate()));
        toolbar.setSubtitle(name);
        if (AccessMediaFile.isFavMediaContains(mediaPath)) {
            favBtn.setIcon(R.drawable.ic_fav_solid);
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        var favList = AccessMediaFile.getFavMedia();
        Log.d("fav", "fav amount pause = " + favList.size());
        favList.forEach(x -> Log.d("fav", x));
        SharedPreferences sharedPreferences = getSharedPreferences("fav_media", MODE_PRIVATE);
        SharedPreferences.Editor myEdit = sharedPreferences.edit();
        myEdit.clear();

        // write all the data entered by the user in SharedPreference and apply
        myEdit.putStringSet("path", favList);
        myEdit.apply();
    }

    public void bottomNavCustom() {
        bottom_nav.setOnItemSelectedListener((NavigationBarView.OnItemSelectedListener) item -> {
            Uri targetUri = Uri.parse("file://" + mediaPath);

            switch (item.getItemId()){
                case R.id.edit_nav_item->{
                    Intent editIntent = new Intent(ViewMedia.this, DsPhotoEditorActivity.class);

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
                    String type = AccessMediaFile.getMediaWithPath(mediaPath).getType() == 1 ? "Image" : "Video";
//                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
//
//                    builder.setTitle("Confirm");
//                    builder.setMessage("Do you want to delete this " + type + "?");
//                    builder.setPositiveButton("YES", (dialog, which) -> {
//                        FileUtils fileUtils = new FileUtils();
//                        fileUtils.deleteFile(mediaPath);
//                        dialog.dismiss();
//                        finish();
//                    });
//
//                    builder.setNegativeButton("NO", (dialog, which) -> {
//                        dialog.dismiss();
//                    });
//
//                    AlertDialog alert = builder.create();
//                    alert.show();

                    if (fileUtils.delete(launcher, mediaPath, this) > 0) {
                        Toast.makeText(getApplicationContext(), "deleted", Toast.LENGTH_SHORT).show();
                        AccessMediaFile.removeMediaFromAllMedia(mediaPath);
                        slideAdapter.removePath(mediaPath);
                        finish();
                    }
                }

//                case R.id.view_photo_secured_nav_item->{
//                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
//
//                    builder.setTitle("Confirm");
//                    builder.setMessage("Do you want to hide/show this image?");
//
//                    builder.setPositiveButton("YES", (dialog, which) -> {
//                        String scrPath = Environment.getExternalStorageDirectory() + File.separator+".secret";
//                        File scrDir = new File(scrPath);
//                        if(!scrDir.exists()){
//                            Toast.makeText(this, "You haven't created secret album", Toast.LENGTH_SHORT).show();
//                        }
//                        else{
//                            FileUtility fu = new FileUtility();
//                            File mediaFile = new File(mediaPath);
//                            if(!(scrPath+File.separator+mediaFile.getName()).equals(mediaPath)){
//                                fu.moveFile(mediaPath, mediaFile.getName(),scrPath);
//                                Toast.makeText(this, "Your image is secured", Toast.LENGTH_SHORT).show();
//                            }
//                            else{
//                                String outputPath = Environment.getExternalStorageDirectory()+File.separator+"DCIM" + File.separator + "Restore";
//                                File folder = new File(outputPath);
//                                File file = new File(mediaFile.getPath());
//                                File desImgFile = new File(outputPath,mediaFile.getName());
//                                if(!folder.exists()) {
//                                    folder.mkdir();
//                                }
//                                mediaFile.renameTo(desImgFile);
//                                mediaFile.deleteOnExit();
//                                desImgFile.getPath();
//                                MediaScannerConnection.scanFile(getApplicationContext(), new String[]{outputPath+File.separator+desImgFile.getName()}, null, null);
//                            }
//                        }
//                        Intent intentResult = new Intent();
//                        intentResult.putExtra("path_media", mediaPath);
//                        setResult(RESULT_OK, intentResult);
//                        finish();
//                        dialog.dismiss();
//                    });
//                    builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
//
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//
//                            // Do nothing
//                            dialog.dismiss();
//                        }
//                    });
//
//                    AlertDialog alert = builder.create();
//                    alert.show();
//
//                    break;
//                }
            }
            return true;
        });
    }

    @Override
    public void showActionBar(boolean trigger) {
        showNavigation(trigger);
    }

    @Override
    public void showVideoPlayer(VideoView videoView, ImageView img2, ImageView btn, TextView duration, Media m) {
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

        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                // showActionBar(true);
                videoController.setVisibility(View.GONE);
                videoView.setMediaController(videoController);
                videoView.setVisibility(View.GONE);
                img.setVisibility(View.VISIBLE);
                playButton.setVisibility(View.VISIBLE);
            }
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
    public void setVideoView(VideoView videoView) {
        this.videoView = videoView;
    }

    @Override
    public void setImageViewAndButton(ImageView img, ImageView playButton) {
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

//        if (videoProportion < screenProportion) {
//            lp.height= screenHeight;
//            lp.width = (int) ((float) screenHeight / videoProportion);
//        } else {
//            lp.width = screenWidth;
//            lp.height = (int) ((float) screenWidth * videoProportion);
//        }
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

}