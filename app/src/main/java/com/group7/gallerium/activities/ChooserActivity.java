package com.group7.gallerium.activities;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.group7.gallerium.R;
import com.group7.gallerium.adapters.ViewPagerAdapterChooser;
import com.group7.gallerium.fragments.MediaFragmentChooser;
import com.group7.gallerium.utilities.BottomNavigationViewBehavior;
import com.karan.churi.PermissionManager.PermissionManager;

import java.util.Objects;

public class ChooserActivity extends AppCompatActivity {
    BottomNavigationView bottom_nav;

    TextView btnShare, btnAddtoAlbum, btnDelete, btnCreative;
    LinearLayout bottom_sheet;
    ViewPager2 view_pager;

    PermissionManager permission;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_chooser);

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

//        SharedPreferences mySharedPref = getSharedPreferences("fav_media", MODE_PRIVATE);
//        var favList = mySharedPref.getStringSet("path", null);
//        if (favList != null) {
//            Log.d("fav", "fav amount = " + favList.size());
//            AccessMediaFile.setAllFavMedia(favList);
//        }

        CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) bottom_nav.getLayoutParams();
        layoutParams.setBehavior(new BottomNavigationViewBehavior());

    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        changeOrientation(newConfig.orientation, false);
    }

    private void setCurrentPage(int page) {
        changeOrientation(getResources().getConfiguration().orientation, true);
        view_pager.setCurrentItem(0, false);
    }

    private void changeOrientation(int orientation, boolean refresh) {
        if(orientation == Configuration.ORIENTATION_LANDSCAPE){
            Toast.makeText(this, "change orientation", Toast.LENGTH_LONG).show();
            Log.d("orientation-changing", "landscape");
            if(view_pager.getCurrentItem() == 0) {
                var myFragment = (MediaFragmentChooser) this.getSupportFragmentManager().findFragmentByTag("f" + view_pager.getCurrentItem());
                if (myFragment != null) {
                    myFragment.changeOrientation(6);
                    if (refresh) myFragment.refresh(true);
                }
            }

        }
        else {
            Toast.makeText(this, "change orientation", Toast.LENGTH_LONG).show();
            Log.d("orientation-changing", "portrait");
            if(view_pager.getCurrentItem() == 0){
                var myFragment = (MediaFragmentChooser)this.getSupportFragmentManager().findFragmentByTag("f" + view_pager.getCurrentItem());
                if (myFragment != null)
                {
                    myFragment.changeOrientation(3);
                    if (refresh) myFragment.refresh(true);
                }
            }
        }
    }

    public void setVisibleForBottomNav(boolean v){
        if(v){
            bottom_nav.setVisibility(View.GONE);
        }else{
            bottom_nav.setVisibility(View.VISIBLE);
        }
    }

    private void initializeViewPager() {
        ViewPagerAdapterChooser viewPagerAdapter = new ViewPagerAdapterChooser(getSupportFragmentManager(), getLifecycle());
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
                    case 0, 5 ->
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
//                    if (!Environment.isExternalStorageManager() && Objects.equals(permissions[i], Manifest.permission.MANAGE_EXTERNAL_STORAGE)) {
//                        try {
//                            Uri uri = Uri.parse("package:" + BuildConfig.APPLICATION_ID);
//                            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, uri);
//                            startActivity(intent);
//                        } catch (Exception ex){
//                            Intent intent = new Intent();
//                            intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
//                            startActivity(intent);
//                        }
//                    }
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

