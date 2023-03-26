package com.group7.gallerium.activities;

import android.Manifest;
import android.app.Activity;
import android.app.UiModeManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceManager;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.group7.gallerium.BuildConfig;
import com.group7.gallerium.R;
import com.group7.gallerium.adapters.ViewPagerAdapter;
import com.group7.gallerium.fragments.AlbumFragment;
import com.group7.gallerium.fragments.FavoriteFragment;
import com.group7.gallerium.fragments.MediaFragment;
import com.group7.gallerium.fragments.SecureFragment;
import com.group7.gallerium.utilities.AccessMediaFile;
import com.group7.gallerium.utilities.BottomNavigationViewBehavior;
import com.group7.gallerium.utilities.FileUtils;
import com.karan.churi.PermissionManager.PermissionManager;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    BottomNavigationView bottom_nav;

    TextView btnShare, btnAddtoAlbum, btnDelete, btnCreative;
    LinearLayout bottom_sheet;
    ViewPager2 view_pager;

    PermissionManager permission;

    SharedPreferences sharedPref;

    String uiPref, locationPref, numGridPref;
    boolean lockPrivatePref, lockTrashPref;

    int uiVal = 0, locationVal = 1, numGridVal = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        bottom_nav = findViewById(R.id.bottom_navigation);
        view_pager = findViewById(R.id.view_pager);
        view_pager.setUserInputEnabled(false);

        bottom_sheet = findViewById(R.id.bottom_sheet);
        btnCreative = findViewById(R.id.create_button);
        btnAddtoAlbum = findViewById(R.id.move_album_button);
        btnDelete = findViewById(R.id.delete_button);
        btnShare = findViewById(R.id.share_button);

        initializeViewPager();

        permission=new PermissionManager() {
            @Override
            public void ifCancelledAndCanRequest(Activity activity) {
                permission.checkAndRequestPermissions(activity);
                super.ifCancelledAndCanRequest(activity);
            }
        };
        permission.checkAndRequestPermissions(this);

        bottom_nav.setOnItemSelectedListener(navItem -> {
            switch (navItem.getItemId()) {
                case R.id.photo_nav_item -> setCurrentPage(0);

                case R.id.album_nav_item -> setCurrentPage(1);

                case R.id.secured_nav_item -> setCurrentPage(2);

                case R.id.fav_nav_item -> setCurrentPage(3);

                default -> throw new IllegalStateException("Unexpected value: " + navItem.getItemId());
            }
            return true;
        });

        SharedPreferences mySharedPref = getSharedPreferences("fav_media", MODE_PRIVATE);
        var favList = mySharedPref.getStringSet("path", null);
        if (favList != null) {
            Log.d("fav", "fav amount = " + favList.size());
            AccessMediaFile.setAllFavMedia(favList);
        }

        CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) bottom_nav.getLayoutParams();
        layoutParams.setBehavior(new BottomNavigationViewBehavior());

        sharedPref =
                PreferenceManager.getDefaultSharedPreferences(this);
        getAllSettingValues();
        setUiMode();
    }

    void getAllSettingValues(){
        try{

            uiPref = sharedPref.getString
                    (SettingsActivity.KEY_PREF_UI, "0");
            if(uiPref.equals("0")){
                uiVal = 0;
            }else if(uiPref.equals("1")){
                uiVal = 1;
            }else{
                uiVal = 2;
            }
            locationPref = sharedPref.getString(SettingsActivity.KEY_PREF_LOCATION, "1");
            numGridPref = sharedPref.getString(SettingsActivity.KEY_PREF_NUM_GRID, "3");

            if(numGridPref.equals("3")){
                numGridVal = 3;
            }else if(numGridPref.equals("4")){
                numGridVal = 4;
            }else{
                numGridVal = 5;
            }
            lockPrivatePref = sharedPref.getBoolean(SettingsActivity.KEY_PREF_LOCK_PRIVATE, false);
            lockTrashPref = sharedPref.getBoolean(SettingsActivity.KEY_PREF_LOCK_TRASH, false);

            Log.d("ui-pref", "" + uiPref);
            Log.d("location-pref", "" + locationPref);
            Log.d("num-grid", ""+numGridPref);

        }catch (Exception e){
            Log.d("tag-e", e.getMessage());
        }
    }

    void setUiMode(){
        Toast.makeText(this, "in mode " + uiVal, Toast.LENGTH_SHORT).show();
        if(Build.VERSION.SDK_INT >= 31){
            UiModeManager uiManager = (UiModeManager) getSystemService(Context.UI_MODE_SERVICE);
            if(uiVal == 0){
                uiManager.setApplicationNightMode(UiModeManager.MODE_NIGHT_AUTO);
            }else if(uiVal == 1){
                uiManager.setApplicationNightMode(UiModeManager.MODE_NIGHT_NO);
            }else{
                uiManager.setApplicationNightMode(UiModeManager.MODE_NIGHT_YES);
            }
        }else{
            if(uiVal == 0){
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
            }else if(uiVal == 1){
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }else{
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStart() {
        super.onStart();
        getAllSettingValues();
        setUiMode();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getAllSettingValues();
        setUiMode();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        changeOrientation(newConfig.orientation, false);
    }

    private void setCurrentPage(int page) {
        changeOrientation(getResources().getConfiguration().orientation, page == view_pager.getCurrentItem());
        view_pager.setCurrentItem(page, false);
    }

    private void changeOrientation(int orientation, boolean refresh) {
        getAllSettingValues();
        setUiMode();
        if(orientation == Configuration.ORIENTATION_LANDSCAPE){
            Toast.makeText(this, "change orientation", Toast.LENGTH_LONG).show();
            Log.d("orientation-changing", "landscape");
            if(view_pager.getCurrentItem() == 0){
                var myFragment = (MediaFragment)this.getSupportFragmentManager().findFragmentByTag("f" + view_pager.getCurrentItem());
                if (myFragment != null)
                {
                    myFragment.changeOrientation(6);
                    if (refresh) myFragment.refresh(true);
                }
            } else if(view_pager.getCurrentItem() == 1){
                var myFragment = (AlbumFragment)this.getSupportFragmentManager().findFragmentByTag("f" + view_pager.getCurrentItem());
                if (myFragment != null)
                {
                    myFragment.changeOrientation(6);
                    if (refresh) myFragment.refresh(true);
                    myFragment.setTrashEnable(lockTrashPref);
                }
            } else if(view_pager.getCurrentItem() == 2){
                var myFragment = (SecureFragment)this.getSupportFragmentManager().findFragmentByTag("f" + view_pager.getCurrentItem());
                if (myFragment != null)
                {
                    myFragment.changeOrientation();
                }
            } else if(view_pager.getCurrentItem() == 3){
                var myFragment = (FavoriteFragment)this.getSupportFragmentManager().findFragmentByTag("f" + view_pager.getCurrentItem());
                if (myFragment != null)
                {
                    myFragment.changeOrientation(6);
                    if (refresh) myFragment.refresh(true);
                }
            }

        }
        else {
            Toast.makeText(this, "change orientation", Toast.LENGTH_LONG).show();
            Log.d("orientation-changing", "portrait");
            if(view_pager.getCurrentItem() == 0){
                var myFragment = (MediaFragment)this.getSupportFragmentManager().findFragmentByTag("f" + view_pager.getCurrentItem());
                if (myFragment != null)
                {
                    myFragment.changeOrientation(numGridVal);
                    if (refresh) myFragment.refresh(true);
                }
            }  else if(view_pager.getCurrentItem() == 1){
                var myFragment = (AlbumFragment)this.getSupportFragmentManager().findFragmentByTag("f" + view_pager.getCurrentItem());
                if (myFragment != null)
                {
                    myFragment.changeOrientation(3);
                    if (refresh) myFragment.refresh(true);
                    myFragment.setTrashEnable(lockTrashPref);
                }
            } else if(view_pager.getCurrentItem() == 2){
                var myFragment = (SecureFragment)this.getSupportFragmentManager().findFragmentByTag("f" + view_pager.getCurrentItem());
                if (myFragment != null)
                {
                    myFragment.changeOrientation();
                }
            } else if(view_pager.getCurrentItem() == 3){
                var myFragment = (FavoriteFragment)this.getSupportFragmentManager().findFragmentByTag("f" + view_pager.getCurrentItem());
                if (myFragment != null)
                {
                    myFragment.changeOrientation(3);
                    if (refresh) myFragment.refresh(true);
                }
            }
        }
    }

    private void initializeViewPager() {
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), getLifecycle());
        viewPagerAdapter.setContext(getApplicationContext());
        view_pager.setAdapter(viewPagerAdapter);

        view_pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
            }

            @Override
            public void onPageSelected(int position) {
                switch (position){
                    case 0->
                        bottom_nav.getMenu().findItem(R.id.photo_nav_item).setChecked(true);
                    case 1->
                        bottom_nav.getMenu().findItem(R.id.album_nav_item).setChecked(true);
                    case 2 ->
                        bottom_nav.getMenu().findItem(R.id.secured_nav_item).setChecked(true);
                    case 3->
                        bottom_nav.getMenu().findItem(R.id.fav_nav_item).setChecked(true);
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (android.os.Build.VERSION.SDK_INT >= 33) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            if (grantResults[0] == PackageManager.PERMISSION_DENIED || grantResults[1] == PackageManager.PERMISSION_DENIED || grantResults[2] == PackageManager.PERMISSION_DENIED) {
                Log.d("permission", "permission denied - requesting it");
                // String[] permissions2 = {android.Manifest.permission.READ_MEDIA_AUDIO, android.Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO};
                // requestPermissions(permissions2, 1);
            }
            for(int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    grantResults[i] = PackageManager.PERMISSION_GRANTED;
                    if (!Environment.isExternalStorageManager() && Objects.equals(permissions[i], Manifest.permission.MANAGE_EXTERNAL_STORAGE)) {
                        try {
                            Uri uri = Uri.parse("package:" + BuildConfig.APPLICATION_ID);
                            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, uri);
                            startActivity(intent);
                        } catch (Exception ex){
                            Intent intent = new Intent();
                            intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                            startActivity(intent);
                        }
                    }
                }

            }
            permission.checkResult(requestCode, permissions, grantResults);

        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);

            //grantResults[0] = grantResults[1] = grantResults[2] = PackageManager.PERMISSION_GRANTED;
            for(int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    if (Objects.equals(permissions[i], Manifest.permission.MANAGE_EXTERNAL_STORAGE) ||
                            Objects.equals(permissions[i], Manifest.permission.READ_MEDIA_IMAGES) ||
                            Objects.equals(permissions[i], Manifest.permission.READ_MEDIA_VIDEO) ||
                            Objects.equals(permissions[i], Manifest.permission.READ_MEDIA_AUDIO)) {
                        grantResults[i] = PackageManager.PERMISSION_GRANTED;
                    }
                }

            }
            permission.checkResult(requestCode, permissions, grantResults);
        }
        setCurrentPage(0);
    }
}

